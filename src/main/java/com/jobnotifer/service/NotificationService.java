package com.jobnotifer.service;

import com.jobnotifer.dto.NotificationResponse;
import com.jobnotifer.entity.Notification;
import com.jobnotifer.entity.Notifier;
import com.jobnotifer.repository.NotificationRepository;
import com.jobnotifer.repository.NotifierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final NotifierRepository notifierRepository;
    
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long userId, Long notifierId) {
        Notifier notifier = notifierRepository.findById(notifierId)
                .orElseThrow(() -> new RuntimeException("Notifier not found"));
        
        if (!notifier.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to notifications");
        }
        
        List<Notification> notifications = notificationRepository
                .findByNotifierIdOrderByTimestampDesc(notifierId);
        
        return notifications.stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public NotificationResponse markAsViewed(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getNotifier().getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to notification");
        }
        
        notification.setViewed(true);
        Notification updated = notificationRepository.save(notification);
        
        log.info("Notification marked as viewed: {}", notificationId);
        
        return NotificationResponse.fromEntity(updated);
    }
}

