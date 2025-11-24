package com.jobnotifer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobnotifer.entity.Notifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiService {
    
    @Value("${gemini.api.key}")
    private String apiKey;
    
    @Value("${gemini.model}")
    private String model;
    
    @Value("${ai.prompt.template}")
    private String promptTemplate;
    
    @Value("${ai.resume.modification.prompt}")
    private String resumeModificationPrompt;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient;
    
    public GeminiService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
    
    public Map<String, Object> analyzeJobRelevance(String jobPosting, Notifier notifier, String educationInfo) {
        try {
            String prompt = buildPrompt(jobPosting, notifier, educationInfo);
            
            Map<String, Object> requestBody = new HashMap<>();
            
            Map<String, Object> content = new HashMap<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", prompt);
            content.put("parts", List.of(part));
            
            requestBody.put("contents", List.of(content));
            
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.3);
            generationConfig.put("maxOutputTokens", 800);
            requestBody.put("generationConfig", generationConfig);
            
            log.debug("Calling Gemini API with model: {}", model);
            
            String response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1beta/models/" + model + ":generateContent")
                            .queryParam("key", apiKey)
                            .build())
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            log.debug("Gemini API response: {}", response);
            
            JsonNode responseNode = objectMapper.readTree(response);
            String generatedText = responseNode
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text").asText();
            
            log.debug("Generated text: {}", generatedText);
            
            String jsonText = extractJsonFromResponse(generatedText);
            
            JsonNode jsonNode = objectMapper.readTree(jsonText);
            Map<String, Object> result = new HashMap<>();
            
            result.put("score", jsonNode.has("score") ? jsonNode.get("score").asDouble() : 0.0);
            result.put("reason", jsonNode.has("reason") ? jsonNode.get("reason").asText() : "No reason provided");
            
            result.put("company", jsonNode.has("company") ? jsonNode.get("company").asText() : "Unknown");
            result.put("role", jsonNode.has("role") ? (jsonNode.get("role").isNull() ? null : jsonNode.get("role").asText()) : "Not specified");
            result.put("experience", jsonNode.has("experience") ? jsonNode.get("experience").asText() : "Not specified");
            result.put("location", jsonNode.has("location") ? jsonNode.get("location").asText() : "Not specified");
            result.put("salary", jsonNode.has("salary") ? jsonNode.get("salary").asText() : "Not specified");
            result.put("batch", jsonNode.has("batch") ? (jsonNode.get("batch").isNull() ? null : jsonNode.get("batch").asText()) : null);
            result.put("jobType", jsonNode.has("jobType") ? jsonNode.get("jobType").asText() : "Full-Time");
            result.put("deadline", jsonNode.has("deadline") ? (jsonNode.get("deadline").isNull() ? null : jsonNode.get("deadline").asText()) : null);
            result.put("duration", jsonNode.has("duration") ? (jsonNode.get("duration").isNull() ? null : jsonNode.get("duration").asText()) : null);
            result.put("description", jsonNode.has("description") ? jsonNode.get("description").asText() : jobPosting);
            result.put("jobLink", jsonNode.has("jobLink") ? (jsonNode.get("jobLink").isNull() ? null : jsonNode.get("jobLink").asText()) : null);
            
            log.info("AI Analysis completed. Score: {}, Company: {}, Role: {}, JobType: {}", 
                    result.get("score"), result.get("company"), result.get("role"), result.get("jobType"));
            return result;
            
        } catch (Exception e) {
            log.error("Error analyzing job relevance with Gemini AI", e);
            Map<String, Object> result = new HashMap<>();
            result.put("score", 0.0);
            result.put("reason", "Error processing with AI: " + e.getMessage());
            result.put("company", "Unknown");
            result.put("role", "Not specified");
            result.put("experience", "Not specified");
            result.put("location", "Not specified");
            result.put("salary", "Not specified");
            result.put("batch", null);
            result.put("jobType", "Full-Time");
            result.put("deadline", null);
            result.put("duration", null);
            result.put("description", jobPosting);
            result.put("jobLink", null);
            return result;
        }
    }
    
    private String extractJsonFromResponse(String text) {
        text = text.trim();
        
        if (text.startsWith("```json")) {
            text = text.substring(7);
        } else if (text.startsWith("```")) {
            text = text.substring(3);
        }
        
        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - 3);
        }
        
        return text.trim();
    }
    
    private String buildPrompt(String jobPosting, Notifier notifier, String educationInfo) {
        return promptTemplate
                .replace("{job}", jobPosting)
                .replace("{role}", notifier.getRole() != null ? notifier.getRole() : "Any")
                .replace("{skills}", notifier.getSkills() != null ? notifier.getSkills() : "Any")
                .replace("{city}", notifier.getCity() != null ? notifier.getCity() : "Any")
                .replace("{salary}", notifier.getSalaryExpectation() != null ? notifier.getSalaryExpectation() : "Any")
                .replace("{companies}", notifier.getCompaniesPreference() != null ? notifier.getCompaniesPreference() : "Any")
                .replace("{experience}", notifier.getExperience() != null ? notifier.getExperience() : "Any")
                .replace("{noticePeriod}", notifier.getNoticePeriod() != null ? notifier.getNoticePeriod() : "Any")
                .replace("{education}", educationInfo != null && !educationInfo.isEmpty() ? educationInfo : "Not specified");
    }
    
    /**
     * Modify resume LaTeX to better align with job posting
     * Only makes minimal changes - adds relevant missing skills, preserves structure
     * @param resumeLatex Original resume LaTeX
     * @param jobPosting Job posting text
     * @return Modified resume LaTeX
     */
    public String modifyResumeForJob(String resumeLatex, String jobPosting) {
        try {
            String prompt = resumeModificationPrompt
                    .replace("{job}", jobPosting)
                    .replace("{resumeLatex}", resumeLatex);
            
            Map<String, Object> requestBody = new HashMap<>();
            
            Map<String, Object> content = new HashMap<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", prompt);
            content.put("parts", List.of(part));
            
            requestBody.put("contents", List.of(content));
            
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.2);
            generationConfig.put("maxOutputTokens", 8000);
            requestBody.put("generationConfig", generationConfig);
            
            log.debug("Calling Gemini API to modify resume LaTeX");
            
            String response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1beta/models/" + model + ":generateContent")
                            .queryParam("key", apiKey)
                            .build())
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            log.debug("Gemini API response for resume modification");
            
            JsonNode responseNode = objectMapper.readTree(response);
            String modifiedLatex = responseNode
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text").asText();
            
            // Clean up the response - remove markdown code blocks if present
            modifiedLatex = modifiedLatex.trim();
            if (modifiedLatex.startsWith("```latex")) {
                modifiedLatex = modifiedLatex.substring(8);
            } else if (modifiedLatex.startsWith("```")) {
                modifiedLatex = modifiedLatex.substring(3);
            }
            if (modifiedLatex.endsWith("```")) {
                modifiedLatex = modifiedLatex.substring(0, modifiedLatex.length() - 3);
            }
            modifiedLatex = modifiedLatex.trim();
            
            log.info("Resume LaTeX modified successfully by AI");
            return modifiedLatex;
            
        } catch (Exception e) {
            log.error("Error modifying resume LaTeX with Gemini AI. Returning original LaTeX.", e);
            // Return original LaTeX if AI modification fails
            return resumeLatex;
        }
    }
}

