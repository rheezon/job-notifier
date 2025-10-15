package com.jobnotifer.dto;

import com.jobnotifer.entity.Notifier;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotifierResponse {
    private Long id;
    private String name;
    private String city;
    private String salaryExpectation;
    private String companiesPreference;
    private String experience;
    private String noticePeriod;
    private String college;
    private String resumeLatex;
    private String additionalPreferences;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long unreadNotificationsCount;
    
    public static NotifierResponse fromEntity(Notifier notifier) {
        NotifierResponse response = new NotifierResponse();
        response.setId(notifier.getId());
        response.setName(notifier.getName());
        response.setCity(notifier.getCity());
        response.setSalaryExpectation(notifier.getSalaryExpectation());
        response.setCompaniesPreference(notifier.getCompaniesPreference());
        response.setExperience(notifier.getExperience());
        response.setNoticePeriod(notifier.getNoticePeriod());
        response.setCollege(notifier.getCollege());
        response.setResumeLatex(notifier.getResumeLatex());
        response.setAdditionalPreferences(notifier.getAdditionalPreferences());
        response.setCreatedAt(notifier.getCreatedAt());
        response.setUpdatedAt(notifier.getUpdatedAt());
        return response;
    }
}

