package com.concrete.c8_concrete_quote_engine.service;

import com.concrete.c8_concrete_quote_engine.dto.AgentStatusUpdate;
import com.concrete.c8_concrete_quote_engine.model.AgentState;
import com.concrete.c8_concrete_quote_engine.model.AgentType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class MultiAgentService {

    private static final Logger log = LoggerFactory.getLogger(MultiAgentService.class);
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key:REPLACE_ME}")
    private String apiKey;

    public void addEmitter(SseEmitter emitter) {
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
    }

    public void dispatchGoal(String goal) {
        executor.submit(() -> {
            try {
                broadcast(new AgentStatusUpdate(null, AgentState.THINKING, "Orchestrating mission: " + goal, System.currentTimeMillis()));
                
                // Sequential execution for demonstration of handoffs
                runAgent(AgentType.RD_STRATEGIST, goal, null);
                runAgent(AgentType.BACKEND_ARCHITECT, goal, "R&D Research data");
                runAgent(AgentType.FRONTEND_DESIGNER, goal, "Architectural specs");
                runAgent(AgentType.QA_AUDITOR, goal, "Implementation plan");

                broadcast(new AgentStatusUpdate(null, AgentState.DONE, "Mission Accomplished.", System.currentTimeMillis()));
            } catch (Exception e) {
                log.error("Dispatch failed", e);
                broadcast(new AgentStatusUpdate(null, AgentState.ERROR, "System Failure: " + e.getMessage(), System.currentTimeMillis()));
            }
        });
    }

    private void runAgent(AgentType type, String goal, String previousContext) throws Exception {
        broadcast(new AgentStatusUpdate(type, AgentState.THINKING, "Initializing thinking process...", System.currentTimeMillis()));
        
        String prompt = String.format("You are the %s. Goal: %s. Context: %s. Provide a short, strategic response.", 
                type.getDisplayName(), goal, previousContext);
        
        String response = callGemini(prompt);
        
        broadcast(new AgentStatusUpdate(type, AgentState.HANDOFF, response, System.currentTimeMillis()));
        Thread.sleep(1000); // Simulate processing time for visualization
        broadcast(new AgentStatusUpdate(type, AgentState.DONE, "Task completed.", System.currentTimeMillis()));
    }

    private String callGemini(String prompt) throws Exception {
        if ("REPLACE_ME".equals(apiKey) || apiKey == null || apiKey.isEmpty()) {
            return "[MOCK] Gemini API Key not set. Simulated response for: " + prompt;
        }

        String[] models = {"gemini-2.5-flash", "gemini-3.5-flash-lite"};
        for (int i = 0; i < models.length; i++) {
            String model = models[i];
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;

            Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                    "parts", List.of(Map.of("text", prompt))
                ))
            );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseGeminiResponse(response.body());
            } else {
                log.error("Gemini API call failed with model {} (Status {}). Response body: {}", model, response.statusCode(), response.body());
                if ((response.statusCode() == 404 || response.statusCode() == 429) && i < models.length - 1) {
                    log.warn("Encountered status {} for model {}. Falling back to model: {}", response.statusCode(), model, models[i + 1]);
                    continue;
                }
                return "[ERROR] Gemini API returned status " + response.statusCode() + ": " + response.body();
            }
        }
        return "[ERROR] All Gemini models failed.";
    }

    private String parseGeminiResponse(String jsonBody) {
        try {
            Map map = objectMapper.readValue(jsonBody, Map.class);
            List<Map> candidates = (List<Map>) map.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map content = (Map) candidates.get(0).get("content");
                List<Map> parts = (List<Map>) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    return (String) parts.get(0).get("text");
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Gemini response", e);
        }
        return jsonBody;
    }

    private void broadcast(AgentStatusUpdate update) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(update);
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }
}
