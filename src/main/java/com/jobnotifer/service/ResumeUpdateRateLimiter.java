package com.jobnotifer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ResumeUpdateRateLimiter {
    
    @Value("${rate-limit.resume-update.cooldown-minutes:60}")
    private int cooldownMinutes;
    
    // Map of userId -> last update timestamp
    private final Map<Long, LocalDateTime> lastUpdateTime = new ConcurrentHashMap<>();
    
    /**
     * Check if user is allowed to update resume
     * @param userId The user ID
     * @return true if allowed, false if in cooldown period
     */
    public boolean isAllowed(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastUpdate = lastUpdateTime.get(userId);
        
        if (lastUpdate == null) {
            lastUpdateTime.put(userId, now);
            log.info("Resume update allowed for user {} (first time)", userId);
            return true;
        }
        
        LocalDateTime nextAllowedTime = lastUpdate.plusMinutes(cooldownMinutes);
        
        if (now.isBefore(nextAllowedTime)) {
            long minutesRemaining = java.time.Duration.between(now, nextAllowedTime).toMinutes();
            log.warn("Resume update rate limit exceeded for user {}. Minutes until next update: {}", 
                    userId, minutesRemaining);
            return false;
        }
        
        lastUpdateTime.put(userId, now);
        log.info("Resume update allowed for user {}", userId);
        return true;
    }
    
    /**
     * Get minutes until next update is allowed
     * @param userId The user ID
     * @return Minutes until next update, or 0 if allowed now
     */
    public long getMinutesUntilNextUpdate(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastUpdate = lastUpdateTime.get(userId);
        
        if (lastUpdate == null) {
            return 0;
        }
        
        LocalDateTime nextAllowedTime = lastUpdate.plusMinutes(cooldownMinutes);
        
        if (now.isBefore(nextAllowedTime)) {
            return java.time.Duration.between(now, nextAllowedTime).toMinutes();
        }
        
        return 0;
    }
    
    /**
     * Clear rate limit for a user (admin action)
     * @param userId The user ID
     */
    public void clearUserLimit(Long userId) {
        lastUpdateTime.remove(userId);
        log.info("Resume update rate limit cleared for user {}", userId);
    }
}

