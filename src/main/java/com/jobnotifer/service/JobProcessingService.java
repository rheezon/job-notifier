package com.jobnotifer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.File;
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
    private final OpenAIService openAIService;
    private final LatexCompilerService latexCompilerService;
    private final CloudinaryService cloudinaryService;
    
    @Value("${scheduler.enabled}")
    private boolean schedulerEnabled;
    
    @Value("${scheduler.max-runs}")
    private int maxRuns;
    
    @Value("${scheduler.fixed-rate}")
    private long fixedRate;
    
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
        
        if (schedulerState.getCurrentRun() >= schedulerState.getMaxRuns()) {
            log.info("Scheduler has reached max runs: {}", schedulerState.getMaxRuns());
            return;
        }
        
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime startWindow = schedulerState.getLastRunTimestamp();
        LocalDateTime endWindow = currentTime;
        
        log.info("Starting scheduler run {} of {}", schedulerState.getCurrentRun() + 1, schedulerState.getMaxRuns());
        log.info("Processing jobs from {} to {}", startWindow, endWindow);
        
        // Fetch unprocessed jobs in the time window
        List<Job> jobs = jobRepository.findUnprocessedJobsInTimeWindow(startWindow, endWindow);
        log.info("Found {} unprocessed jobs", jobs.size());
        
        // Get all active notifiers
        List<Notifier> notifiers = notifierRepository.findAll();
        log.info("Processing jobs for {} notifiers", notifiers.size());
        
        int processedJobsCount = 0;
        int relevantJobsCount = 0;
        
        // Process each job
        for (Job job : jobs) {
            for (Notifier notifier : notifiers) {
                try {
                    processJobForNotifier(job, notifier, schedulerState.getCurrentRun() + 1);
                    relevantJobsCount++;
                } catch (Exception e) {
                    log.error("Error processing job {} for notifier {}", job.getId(), notifier.getId(), e);
                }
            }
            
            // Mark job as processed
            job.setProcessed(true);
            jobRepository.save(job);
            processedJobsCount++;
        }
        
        // Update scheduler state
        schedulerState.setCurrentRun(schedulerState.getCurrentRun() + 1);
        schedulerState.setLastRunTimestamp(currentTime);
        schedulerStateRepository.save(schedulerState);
        
        log.info("Scheduler run completed. Processed {} jobs, found {} relevant matches", 
                processedJobsCount, relevantJobsCount);
    }
    
    private void processJobForNotifier(Job job, Notifier notifier, int schedulerRun) {
        // Use AI to analyze job relevance
        Map<String, Object> analysisResult = openAIService.analyzeJobRelevance(job.getJob(), notifier);
        
        double relevanceScore = (double) analysisResult.get("score");
        String relevanceReason = (String) analysisResult.get("reason");
        
        log.debug("Job {} relevance for notifier {}: {}", job.getId(), notifier.getId(), relevanceScore);
        
        if (relevanceScore >= relevanceThreshold) {
            log.info("Job {} is relevant for notifier {} (score: {})", job.getId(), notifier.getId(), relevanceScore);
            
            // Generate resume if latex is provided
            String resumeLink = null;
            if (notifier.getResumeLatex() != null && !notifier.getResumeLatex().isEmpty()) {
                resumeLink = generateAndUploadResume(notifier, job);
            }
            
            // Parse job details
            Map<String, String> jobDetails = parseJobDetails(job.getJob());
            
            // Create notification
            Notification notification = new Notification();
            notification.setNotifier(notifier);
            notification.setTimestamp(job.getTimestamp());
            notification.setSchedulerRun(schedulerRun);
            notification.setResumeLink(resumeLink);
            notification.setCompanyName(jobDetails.getOrDefault("company", "Unknown"));
            notification.setExperience(jobDetails.getOrDefault("experience", "Not specified"));
            notification.setLocation(jobDetails.getOrDefault("location", "Not specified"));
            notification.setSalary(jobDetails.getOrDefault("salary", "Not specified"));
            notification.setJobDescription(jobDetails.getOrDefault("description", job.getJob()));
            notification.setRelevanceScore(relevanceScore);
            notification.setRelevanceReason(relevanceReason);
            notification.setOriginalJobPosting(job.getJob());
            notification.setViewed(false);
            
            notificationRepository.save(notification);
            log.info("Notification created for notifier {}", notifier.getId());
        }
    }
    
    private String generateAndUploadResume(Notifier notifier, Job job) {
        try {
            // Compile LaTeX to PDF
            File pdfFile = latexCompilerService.compileToPdf(notifier.getResumeLatex());
            
            if (pdfFile == null) {
                log.error("Failed to compile LaTeX for notifier {}", notifier.getId());
                return null;
            }
            
            // Upload to Cloudinary
            String fileName = String.format("resume_%s_%s_%s", 
                    notifier.getId(), 
                    job.getId(), 
                    UUID.randomUUID().toString().substring(0, 8));
            
            String url = cloudinaryService.uploadPdf(pdfFile, fileName);
            
            // Clean up temporary file
            pdfFile.delete();
            
            return url;
            
        } catch (Exception e) {
            log.error("Error generating and uploading resume", e);
            return null;
        }
    }
    
    private Map<String, String> parseJobDetails(String jobPosting) {
        // Simple parsing - in production, you might want to use AI for this too
        Map<String, String> details = new java.util.HashMap<>();
        
        try {
            // Try to parse as JSON first
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(jobPosting);
            
            if (jsonNode.has("company")) details.put("company", jsonNode.get("company").asText());
            if (jsonNode.has("experience")) details.put("experience", jsonNode.get("experience").asText());
            if (jsonNode.has("location")) details.put("location", jsonNode.get("location").asText());
            if (jsonNode.has("salary")) details.put("salary", jsonNode.get("salary").asText());
            if (jsonNode.has("description")) details.put("description", jsonNode.get("description").asText());
            
        } catch (Exception e) {
            // If not JSON, extract from text
            String[] lines = jobPosting.split("\n");
            for (String line : lines) {
                String lowerLine = line.toLowerCase();
                if (lowerLine.contains("company:")) {
                    details.put("company", line.substring(line.indexOf(":") + 1).trim());
                } else if (lowerLine.contains("experience:")) {
                    details.put("experience", line.substring(line.indexOf(":") + 1).trim());
                } else if (lowerLine.contains("location:")) {
                    details.put("location", line.substring(line.indexOf(":") + 1).trim());
                } else if (lowerLine.contains("salary:")) {
                    details.put("salary", line.substring(line.indexOf(":") + 1).trim());
                }
            }
        }
        
        return details;
    }
    
    private SchedulerState getOrCreateSchedulerState() {
        return schedulerStateRepository.findBySchedulerName("job-processor")
                .orElseGet(() -> {
                    SchedulerState state = new SchedulerState();
                    state.setSchedulerName("job-processor");
                    state.setCurrentRun(0);
                    state.setMaxRuns(maxRuns);
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

