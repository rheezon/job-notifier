package com.jobnotifer.controller;

import com.jobnotifer.dto.NotificationResponse;
import com.jobnotifer.security.UserPrincipal;
import com.jobnotifer.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @GetMapping("/notifier/{notifierId}")
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long notifierId) {
        List<NotificationResponse> notifications = notificationService.getNotifications(
                currentUser.getId(), notifierId);
        return ResponseEntity.ok(notifications);
    }
    
    @PutMapping("/{id}/applied")
    public ResponseEntity<NotificationResponse> markAsApplied(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id) {
        NotificationResponse response = notificationService.markAsApplied(currentUser.getId(), id);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id) {
        notificationService.deleteNotification(currentUser.getId(), id);
        return ResponseEntity.noContent().build();
    }
}

