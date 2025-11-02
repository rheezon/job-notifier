package com.jobnotifer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NotifierRequest {
    
    @NotBlank(message = "Notifier name is required")
    private String name;
    
    private String role;
    private String city;
    private String salaryExpectation;
    private String companiesPreference;
    private String experience;
    private String noticePeriod;
    private String skills;
    private String resumeLatex;
    private String additionalPreferences;
    private Boolean isDraft;
    private Boolean isActive;
}

