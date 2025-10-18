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
    
    /**
     * Upload PDF from byte array directly without creating temporary file
     * @param pdfBytes PDF content as byte array
     * @param fileName Desired file name
     * @return Cloudinary URL or null if upload fails
     */
    public String uploadPdfFromBytes(byte[] pdfBytes, String fileName) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(pdfBytes, ObjectUtils.asMap(
                    "resource_type", "raw",
                    "public_id", "resumes/" + fileName,
                    "format", "pdf"
            ));
            
            String url = (String) uploadResult.get("secure_url");
            log.info("PDF uploaded to Cloudinary from bytes: {} ({} bytes)", url, pdfBytes.length);
            return url;
            
        } catch (IOException e) {
            log.error("Error uploading PDF bytes to Cloudinary", e);
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

