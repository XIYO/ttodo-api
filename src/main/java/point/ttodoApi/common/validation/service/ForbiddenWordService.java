package point.ttodoApi.common.validation.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class ForbiddenWordService {
    
    private final Set<String> forbiddenWords = new HashSet<>();
    
    @PostConstruct
    public void loadForbiddenWords() {
        try {
            ClassPathResource resource = new ClassPathResource("forbidden-words.txt");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    // Skip empty lines and comments
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        forbiddenWords.add(line.toLowerCase());
                    }
                }
                
                log.info("Loaded {} forbidden words", forbiddenWords.size());
            }
        } catch (IOException e) {
            log.error("Failed to load forbidden words file", e);
            // Add default forbidden words if file loading fails
            addDefaultForbiddenWords();
        }
    }
    
    private void addDefaultForbiddenWords() {
        forbiddenWords.add("admin");
        forbiddenWords.add("administrator");
        forbiddenWords.add("root");
        forbiddenWords.add("system");
        forbiddenWords.add("moderator");
        forbiddenWords.add("test");
        forbiddenWords.add("demo");
        log.info("Using default forbidden words list");
    }
    
    public boolean containsForbiddenWord(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        String lowerText = text.toLowerCase();
        
        // Check exact match
        if (forbiddenWords.contains(lowerText)) {
            return true;
        }
        
        // Check if any forbidden word is contained in the text
        for (String forbiddenWord : forbiddenWords) {
            if (lowerText.contains(forbiddenWord)) {
                return true;
            }
        }
        
        return false;
    }
    
    public Set<String> getForbiddenWords() {
        return new HashSet<>(forbiddenWords);
    }
}