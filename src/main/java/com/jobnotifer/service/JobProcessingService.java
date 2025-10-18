package com.jobnotifer.service;

import com.jobnotifer.entity.Job;
import com.jobnotifer.entity.Notification;
import com.jobnotifer.entity.Notifier;
import com.jobnotifer.entity.SchedulerState;
import com.jobnotifer.repository.JobRepository;
import com.jobnotifer.repository.NotificationRepository;
import com.jobnotifer.repository.NotifierRepository;
import com.jobnotifer.repository.SchedulerStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobProcessingService {
    
    private final JobRepository jobRepository;
    private final NotifierRepository notifierRepository;
    private final NotificationRepository notificationRepository;
    private final SchedulerStateRepository schedulerStateRepository;
    private final GeminiService geminiService;
    private final LatexCompilerService latexCompilerService;
    private final CloudinaryService cloudinaryService;
    
    @Value("${scheduler.enabled}")
    private boolean schedulerEnabled;
    
    @Value("${ai.relevance.threshold}")
    private double relevanceThreshold;
    
    @Scheduled(fixedRateString = "${scheduler.fixed-rate}")
    @Transactional
    public void processJobs() {
        if (!schedulerEnabled) {
            log.debug("Scheduler is disabled");
            return;
        }
        
        SchedulerState schedulerState = getOrCreateSchedulerState();
        
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime startWindow = schedulerState.getLastRunTimestamp();
        LocalDateTime endWindow = currentTime;
        
        log.info("Starting scheduler run {}", schedulerState.getCurrentRun() + 1);
        log.info("Processing jobs from {} to {}", startWindow, endWindow);
        log.info("DEBUG: Start window = {}, End window = {}", startWindow, endWindow);
        
        List<Job> jobs = jobRepository.findUnprocessedJobsInTimeWindow(startWindow, endWindow);
        log.info("Found {} unprocessed jobs", jobs.size());
        
        List<Job> allUnprocessed = jobRepository.findAll().stream()
            .filter(j -> !j.getProcessed())
            .toList();
        log.info("DEBUG: Total unprocessed jobs in DB: {}", allUnprocessed.size());
        for (Job j : allUnprocessed) {
            log.info("DEBUG: Job {} - timestamp: {}, processed: {}", j.getId(), j.getTimestamp(), j.getProcessed());
        }
        
        List<Notifier> notifiers = notifierRepository.findAll();
        log.info("Processing jobs for {} notifiers", notifiers.size());
        
        int processedJobsCount = 0;
        int totalAiCalls = 0;
        
        for (Job job : jobs) {
            for (Notifier notifier : notifiers) {
                try {
                    processJobForNotifier(job, notifier, schedulerState.getCurrentRun() + 1);
                    totalAiCalls++;
                } catch (Exception e) {
                    log.error("Error processing job {} for notifier {}", job.getId(), notifier.getId(), e);
                }
            }
            
            job.setProcessed(true);
            jobRepository.save(job);
            processedJobsCount++;
        }
        
        schedulerState.setCurrentRun(schedulerState.getCurrentRun() + 1);
        schedulerState.setLastRunTimestamp(currentTime);
        schedulerStateRepository.save(schedulerState);
        
        log.info("Scheduler run completed. Processed {} jobs, found {} ai calls", 
                processedJobsCount, totalAiCalls);
    }
    
    private void processJobForNotifier(Job job, Notifier notifier, int schedulerRun) {
        Map<String, Object> analysisResult = geminiService.analyzeJobRelevance(job.getJob(), notifier);
        
        double relevanceScore = (double) analysisResult.get("score");
        String relevanceReason = (String) analysisResult.get("reason");
        
        log.debug("Job {} relevance for notifier {}: {}", job.getId(), notifier.getId(), relevanceScore);
        
        if (relevanceScore >= relevanceThreshold) {
            log.info("Job {} is relevant for notifier {} (score: {})", job.getId(), notifier.getId(), relevanceScore);
            
            String resumeLink = null;
            if (notifier.getResumeLatex() != null && !notifier.getResumeLatex().isEmpty()) {
                resumeLink = generateAndUploadResume(notifier, job);
            }
            
            String company = (String) analysisResult.get("company");
            String experience = (String) analysisResult.get("experience");
            String location = (String) analysisResult.get("location");
            String salary = (String) analysisResult.get("salary");
            String description = (String) analysisResult.get("description");
            String jobLink = (String) analysisResult.get("jobLink");
            
            Notification notification = new Notification();
            notification.setNotifier(notifier);
            notification.setTimestamp(job.getTimestamp());
            notification.setSchedulerRun(schedulerRun);
            notification.setResumeLink(resumeLink);
            notification.setJobLink(jobLink);
            notification.setCompanyName(company);
            notification.setExperience(experience);
            notification.setLocation(location);
            notification.setSalary(salary);
            notification.setJobDescription(description);
            notification.setRelevanceScore(relevanceScore);
            notification.setRelevanceReason(relevanceReason);
            notification.setOriginalJobPosting(job.getJob());
            notification.setViewed(false);
            
            notificationRepository.save(notification);
            log.info("Notification created for notifier {} - Company: {}", notifier.getId(), company);
        }
    }
    
    private String generateAndUploadResume(Notifier notifier, Job job) {
        try {
            byte[] pdfBytes = latexCompilerService.compileToPdf(notifier.getResumeLatex());
            
            if (pdfBytes == null) {
                log.error("Failed to compile LaTeX for notifier {}", notifier.getId());
                return null;
            }
            
            String fileName = String.format("resume_%s_%s_%s", 
                    notifier.getId(), 
                    job.getId(), 
                    UUID.randomUUID().toString().substring(0, 8));
            
            String url = cloudinaryService.uploadPdfFromBytes(pdfBytes, fileName);
            
            log.info("Resume compiled and uploaded successfully for notifier {} ({} bytes)", 
                    notifier.getId(), pdfBytes.length);
            
            return url;
            
        } catch (Exception e) {
            log.error("Error generating and uploading resume", e);
            return null;
        }
    }
    
    private SchedulerState getOrCreateSchedulerState() {
        return schedulerStateRepository.findBySchedulerName("job-processor")
                .orElseGet(() -> {
                    SchedulerState state = new SchedulerState();
                    state.setSchedulerName("job-processor");
                    state.setCurrentRun(0);
                    state.setLastRunTimestamp(LocalDateTime.now());
                    state.setEnabled(true);
                    return schedulerStateRepository.save(state);
                });
    }
    
    public void resetScheduler() {
        SchedulerState state = getOrCreateSchedulerState();
        state.setCurrentRun(0);
        state.setLastRunTimestamp(LocalDateTime.now());
        schedulerStateRepository.save(state);
        log.info("Scheduler state reset");
    }
}

