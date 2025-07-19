package point.ttodoApi.common.validation.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class DisposableEmailService {
    
    private final Set<String> disposableDomains = new HashSet<>();
    
    @PostConstruct
    public void loadDisposableDomains() {
        try {
            ClassPathResource resource = new ClassPathResource("disposable-email-domains.txt");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim().toLowerCase();
                    // Skip empty lines and comments
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        disposableDomains.add(line);
                    }
                }
                
                log.info("Loaded {} disposable email domains", disposableDomains.size());
            }
        } catch (IOException e) {
            log.error("Failed to load disposable email domains file", e);
            // Add default disposable domains if file loading fails
            addDefaultDisposableDomains();
        }
    }
    
    private void addDefaultDisposableDomains() {
        disposableDomains.add("10minutemail.com");
        disposableDomains.add("tempmail.com");
        disposableDomains.add("guerrillamail.com");
        disposableDomains.add("mailinator.com");
        disposableDomains.add("trashmail.com");
        disposableDomains.add("yopmail.com");
        log.info("Using default disposable email domains list");
    }
    
    public boolean isDisposableEmail(String email) {
        if (email == null || !email.contains("@")) {
            return false;
        }
        
        String domain = extractDomain(email);
        return disposableDomains.contains(domain.toLowerCase());
    }
    
    private String extractDomain(String email) {
        int atIndex = email.lastIndexOf('@');
        if (atIndex == -1 || atIndex == email.length() - 1) {
            return "";
        }
        return email.substring(atIndex + 1);
    }
    
    public Set<String> getDisposableDomains() {
        return new HashSet<>(disposableDomains);
    }
}