package com.jobnotifer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NotifierRequest {
    
    @NotBlank(message = "Notifier name is required")
    private String name;
    
    private String city;
    private String salaryExpectation;
    private String companiesPreference;
    private String experience;
    private String noticePeriod;
    private String college;
    private String resumeLatex;
    private String additionalPreferences;
}

