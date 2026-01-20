package pt.ipleiria.dei.ei.estg.researchcenter.ejbs;

import jakarta.ejb.Stateless;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Logger;

@Stateless
public class LLMClientBean {
    
    private static final Logger logger = Logger.getLogger(LLMClientBean.class.getName());
    private static final String OLLAMA_BASE_URL = System.getenv().getOrDefault("OLLAMA_URL", "http://localhost:11434");
    private static final String OLLAMA_GENERATE_URL = OLLAMA_BASE_URL + "/api/generate";
    private static final String OLLAMA_TAGS_URL = OLLAMA_BASE_URL + "/api/tags";
    private static final String DEFAULT_MODEL = "llama3";
    private static final int TIMEOUT_SECONDS = 60;
    
    private final HttpClient httpClient;
    
    public LLMClientBean() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }
    
    public String generateSummary(String title, String abstractContent) throws Exception {
        String prompt = buildPrompt(title, abstractContent);
        
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", DEFAULT_MODEL);
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);
        requestBody.put("options", new JSONObject()
                .put("temperature", 0.7)
                .put("num_predict", 300));
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OLLAMA_GENERATE_URL))
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();
        
        logger.info("Sending request to Ollama API for summary generation");
        
        try {
            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JSONObject responseJson = new JSONObject(response.body());
                String generatedText = responseJson.optString("response", "");
                
                // Remove common preamble phrases
                generatedText = generatedText.replaceFirst("^Here is (a |an )?concise summary (of|for) .+?:\\s*", "");
                generatedText = generatedText.replaceFirst("^This (publication|paper|review|study) .+?:\\s*", "");
                
                logger.info("Successfully generated summary");
                return generatedText.trim();
            } else {
                String errorMsg = "Ollama API returned status code: " + response.statusCode();
                logger.severe(errorMsg);
                throw new Exception(errorMsg);
            }
        } catch (Exception e) {
            logger.severe("Error calling Ollama API: " + e.getMessage());
            throw new Exception("Failed to generate summary: " + e.getMessage(), e);
        }
    }
    
    private String buildPrompt(String title, String abstractContent) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an AI assistant helping to summarize academic publications.\n\n");
        prompt.append("Publication Title: ").append(title).append("\n\n");
        
        if (abstractContent != null && !abstractContent.isBlank()) {
            prompt.append("Abstract: ").append(abstractContent).append("\n\n");
        }
        
        prompt.append("Task: Generate a concise summary (2-3 sentences, maximum 150 words) ");
        prompt.append("that captures the key points and contributions of this publication. ");
        prompt.append("Focus on the main findings, methodology, and significance.\n\n");
        prompt.append("Summary:");
        
        return prompt.toString();
    }
    
    public boolean isServiceAvailable() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_TAGS_URL))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            return response.statusCode() == 200;
        } catch (Exception e) {
            logger.warning("Ollama service not available: " + e.getMessage());
            return false;
        }
    }
}
