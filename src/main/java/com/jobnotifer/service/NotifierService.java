package com.jobnotifer.service;

import com.jobnotifer.dto.NotifierRequest;
import com.jobnotifer.dto.NotifierResponse;
import com.jobnotifer.entity.Notifier;
import com.jobnotifer.entity.User;
import com.jobnotifer.repository.NotificationRepository;
import com.jobnotifer.repository.NotifierRepository;
import com.jobnotifer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotifierService {
    
    private final NotifierRepository notifierRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    
    @Value("${notifier.max-per-user}")
    private int maxNotifiersPerUser;
    
    @Transactional
    public NotifierResponse createNotifier(Long userId, NotifierRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        long currentNotifierCount = notifierRepository.countByUserId(userId);
        if (currentNotifierCount >= maxNotifiersPerUser) {
            log.warn("User {} attempted to create notifier but has reached the limit of {}", 
                    userId, maxNotifiersPerUser);
            throw new RuntimeException(String.format(
                    "Maximum notifier limit reached. You can only create up to %d notifiers.", 
                    maxNotifiersPerUser));
        }
        
        Notifier notifier = new Notifier();
        notifier.setUser(user);
        notifier.setName(request.getName());
        notifier.setCity(request.getCity());
        notifier.setSalaryExpectation(request.getSalaryExpectation());
        notifier.setCompaniesPreference(request.getCompaniesPreference());
        notifier.setExperience(request.getExperience());
        notifier.setNoticePeriod(request.getNoticePeriod());
        notifier.setCollege(request.getCollege());
        notifier.setResumeLatex(request.getResumeLatex());
        notifier.setAdditionalPreferences(request.getAdditionalPreferences());
        
        Notifier savedNotifier = notifierRepository.save(notifier);
        
        log.info("Notifier created successfully: {} for user: {}", savedNotifier.getId(), userId);
        
        NotifierResponse response = NotifierResponse.fromEntity(savedNotifier);
        response.setUnreadNotificationsCount(0L);
        return response;
    }
    
    @Transactional
    public NotifierResponse updateNotifier(Long userId, Long notifierId, NotifierRequest request) {
        Notifier notifier = notifierRepository.findById(notifierId)
                .orElseThrow(() -> new RuntimeException("Notifier not found"));
        
        if (!notifier.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to notifier");
        }
        
        notifier.setName(request.getName());
        notifier.setCity(request.getCity());
        notifier.setSalaryExpectation(request.getSalaryExpectation());
        notifier.setCompaniesPreference(request.getCompaniesPreference());
        notifier.setExperience(request.getExperience());
        notifier.setNoticePeriod(request.getNoticePeriod());
        notifier.setCollege(request.getCollege());
        notifier.setResumeLatex(request.getResumeLatex());
        notifier.setAdditionalPreferences(request.getAdditionalPreferences());
        
        Notifier updatedNotifier = notifierRepository.save(notifier);
        
        log.info("Notifier updated successfully: {}", notifierId);
        
        NotifierResponse response = NotifierResponse.fromEntity(updatedNotifier);
        response.setUnreadNotificationsCount(
                notificationRepository.countByNotifierIdAndViewedFalse(notifierId)
        );
        return response;
    }
    
    @Transactional(readOnly = true)
    public List<NotifierResponse> getUserNotifiers(Long userId) {
        List<Notifier> notifiers = notifierRepository.findByUserId(userId);
        
        return notifiers.stream()
                .map(notifier -> {
                    NotifierResponse response = NotifierResponse.fromEntity(notifier);
                    response.setUnreadNotificationsCount(
                            notificationRepository.countByNotifierIdAndViewedFalse(notifier.getId())
                    );
                    return response;
                })
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public NotifierResponse getNotifier(Long userId, Long notifierId) {
        Notifier notifier = notifierRepository.findById(notifierId)
                .orElseThrow(() -> new RuntimeException("Notifier not found"));
        
        if (!notifier.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to notifier");
        }
        
        NotifierResponse response = NotifierResponse.fromEntity(notifier);
        response.setUnreadNotificationsCount(
                notificationRepository.countByNotifierIdAndViewedFalse(notifierId)
        );
        return response;
    }
    
    @Transactional
    public void deleteNotifier(Long userId, Long notifierId) {
        Notifier notifier = notifierRepository.findById(notifierId)
                .orElseThrow(() -> new RuntimeException("Notifier not found"));
        
        if (!notifier.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to notifier");
        }
        
        notifierRepository.delete(notifier);
        log.info("Notifier deleted successfully: {}", notifierId);
    }
    
    /**
     * Get notifier limit information for a user
     * @param userId User ID
     * @return Map with current count, max limit, and remaining slots
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getNotifierLimitInfo(Long userId) {
        long currentCount = notifierRepository.countByUserId(userId);
        long remaining = Math.max(0, maxNotifiersPerUser - currentCount);
        
        return java.util.Map.of(
                "current", currentCount,
                "max", maxNotifiersPerUser,
                "remaining", remaining,
                "canCreateMore", remaining > 0
        );
    }
}

