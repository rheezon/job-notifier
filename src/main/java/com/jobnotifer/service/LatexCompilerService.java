package com.jobnotifer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@Slf4j
public class LatexCompilerService {
    
    @Value("${latex.compiler.url}")
    private String latexCompilerUrl;
    
    @Value("${latex.timeout}")
    private long timeout;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    public File compileToPdf(String latexCode) {
        try {
            // Create a temporary directory for LaTeX files
            Path tempDir = Files.createTempDirectory("latex_");
            File texFile = new File(tempDir.toFile(), "resume.tex");
            
            // Write LaTeX code to file
            Files.writeString(texFile.toPath(), latexCode);
            
            // Compile using online service or local compiler
            return compileUsingOnlineService(latexCode);
            
        } catch (IOException e) {
            log.error("Error compiling LaTeX to PDF", e);
            return null;
        }
    }
    
    private File compileUsingOnlineService(String latexCode) {
        try {
            // Using LaTeX.Online service
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            
            HttpEntity<String> request = new HttpEntity<>(latexCode, headers);
            
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    latexCompilerUrl + "?command=pdflatex",
                    HttpMethod.POST,
                    request,
                    byte[].class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // Create temporary file for PDF
                File pdfFile = File.createTempFile("resume_" + UUID.randomUUID(), ".pdf");
                
                try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                    fos.write(response.getBody());
                }
                
                log.info("LaTeX compiled successfully to PDF: {}", pdfFile.getAbsolutePath());
                return pdfFile;
            }
            
            log.error("LaTeX compilation failed with status: {}", response.getStatusCode());
            return null;
            
        } catch (Exception e) {
            log.error("Error using online LaTeX compiler", e);
            return null;
        }
    }
}

