package com.jobnotifer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobnotifer.entity.Notifier;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OpenAIService {
    
    @Value("${openai.api.key}")
    private String apiKey;
    
    @Value("${openai.model}")
    private String model;
    
    @Value("${ai.prompt.template}")
    private String promptTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public Map<String, Object> analyzeJobRelevance(String jobPosting, Notifier notifier) {
        try {
            OpenAiService service = new OpenAiService(apiKey, Duration.ofSeconds(60));
            
            String prompt = buildPrompt(jobPosting, notifier);
            
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("system", "You are a job matching assistant. Analyze job postings and provide relevance scores."));
            messages.add(new ChatMessage("user", prompt));
            
            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(messages)
                    .temperature(0.3)
                    .maxTokens(500)
                    .build();
            
            String response = service.createChatCompletion(completionRequest)
                    .getChoices().get(0).getMessage().getContent();
            
            // Parse JSON response
            JsonNode jsonNode = objectMapper.readTree(response);
            Map<String, Object> result = new HashMap<>();
            result.put("score", jsonNode.get("score").asDouble());
            result.put("reason", jsonNode.get("reason").asText());
            
            log.info("AI Analysis completed. Score: {}", result.get("score"));
            return result;
            
        } catch (Exception e) {
            log.error("Error analyzing job relevance with AI", e);
            // Return default values on error
            Map<String, Object> result = new HashMap<>();
            result.put("score", 0.0);
            result.put("reason", "Error processing with AI: " + e.getMessage());
            return result;
        }
    }
    
    private String buildPrompt(String jobPosting, Notifier notifier) {
        return promptTemplate
                .replace("{job}", jobPosting)
                .replace("{city}", notifier.getCity() != null ? notifier.getCity() : "Any")
                .replace("{salary}", notifier.getSalaryExpectation() != null ? notifier.getSalaryExpectation() : "Any")
                .replace("{companies}", notifier.getCompaniesPreference() != null ? notifier.getCompaniesPreference() : "Any")
                .replace("{experience}", notifier.getExperience() != null ? notifier.getExperience() : "Any")
                .replace("{noticePeriod}", notifier.getNoticePeriod() != null ? notifier.getNoticePeriod() : "Any")
                .replace("{college}", notifier.getCollege() != null ? notifier.getCollege() : "Any");
    }
}

