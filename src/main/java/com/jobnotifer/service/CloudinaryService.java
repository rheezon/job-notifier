package com.jobnotifer.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
public class CloudinaryService {
    
    @Value("${cloudinary.cloud-name}")
    private String cloudName;
    
    @Value("${cloudinary.api-key}")
    private String apiKey;
    
    @Value("${cloudinary.api-secret}")
    private String apiSecret;
    
    private Cloudinary cloudinary;
    
    @PostConstruct
    public void init() {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }
    
    public String uploadPdf(File pdfFile, String fileName) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(pdfFile, ObjectUtils.asMap(
                    "resource_type", "raw",
                    "public_id", "resumes/" + fileName,
                    "format", "pdf"
            ));
            
            String url = (String) uploadResult.get("secure_url");
            log.info("PDF uploaded to Cloudinary: {}", url);
            return url;
            
        } catch (IOException e) {
            log.error("Error uploading PDF to Cloudinary", e);
            return null;
        }
    }
    
    public boolean deletePdf(String publicId) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "raw"));
            log.info("PDF deleted from Cloudinary: {}", publicId);
            return "ok".equals(result.get("result"));
        } catch (IOException e) {
            log.error("Error deleting PDF from Cloudinary", e);
            return false;
        }
    }
}

