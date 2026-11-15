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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class MultiAgentService {

    private static final Logger log = LoggerFactory.getLogger(MultiAgentService.class);
    private static final List<String> FALLBACK_MODELS = List.of(
            "gemini-3.5-flash-lite",
            "gemini-3.8-flash",
            "gemini-2.5-flash-lite",
            "gemini-2.0-flash"
    );

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

                String rdSpec = runChatAgent(AgentType.RD_STRATEGIST, goal, "Analyze the C-8 Concrete Quote Engine codebase and outline exact requirements.");
                String backendNotes = runCodeAgent(AgentType.BACKEND_ARCHITECT, goal, "Inspect or update backend Java classes and pricing-rules.yml if needed for: " + rdSpec);
                String frontendNotes = runCodeAgent(AgentType.FRONTEND_DESIGNER, goal, "Implement any required UI changes in src/main/resources/static/index.html matching backend DTOs. Prior notes: " + backendNotes);
                runChatAgent(AgentType.QA_AUDITOR, goal, "Audit completed changes for BigDecimal accuracy, security, and test coverage. Summary: " + frontendNotes);

                broadcast(new AgentStatusUpdate(null, AgentState.DONE, "Mission Accomplished. Refresh index.html or check git diff!", System.currentTimeMillis()));
            } catch (Exception e) {
                log.error("Dispatch failed", e);
                broadcast(new AgentStatusUpdate(null, AgentState.ERROR, "System Failure: " + e.getMessage(), System.currentTimeMillis()));
            }
        });
    }

    private String runChatAgent(AgentType type, String goal, String context) throws Exception {
        broadcast(new AgentStatusUpdate(type, AgentState.THINKING, "Analyzing strategy and constraints...", System.currentTimeMillis()));

        String prompt = String.format(
                "You are the %s for a C-8 Concrete Quote Engine (Spring Boot + Vanilla HTML/JS). Goal: %s. Context: %s. Provide a concise, actionable technical summary.",
                type.getDisplayName(), goal, context
        );

        String response = callGeminiRestWithFallback(prompt);
        broadcast(new AgentStatusUpdate(type, AgentState.HANDOFF, response, System.currentTimeMillis()));
        broadcast(new AgentStatusUpdate(type, AgentState.DONE, "Task completed.", System.currentTimeMillis()));
        return response;
    }

    private String runCodeAgent(AgentType type, String goal, String instructions) throws Exception {
        if ("REPLACE_ME".equals(apiKey) || apiKey == null || apiKey.isEmpty()) {
            String mock = "[MOCK] Gemini API Key not set. Skipping local file edits.";
            broadcast(new AgentStatusUpdate(type, AgentState.DONE, mock, System.currentTimeMillis()));
            return mock;
        }

        broadcast(new AgentStatusUpdate(type, AgentState.THINKING, "Launching local Gemini CLI workspace agent...", System.currentTimeMillis()));

        String fullPrompt = String.format(
                "You are the %s working directly inside this C-8 Concrete Quote Engine repository. " +
                "Follow GEMINI.md strictly (use BigDecimal for all money/volume math, zero external JS build tools). " +
                "Mission Goal: %s. Specific Role Task: %s. " +
                "Make the required file edits directly in the workspace, verify compilation/tests if Java files changed, and output a brief summary of files modified.",
                type.getDisplayName(), goal, instructions
        );

        for (String model : FALLBACK_MODELS) {
            broadcast(new AgentStatusUpdate(type, AgentState.THINKING, "Executing CLI agent with model: " + model, System.currentTimeMillis()));

            ProcessBuilder pb = new ProcessBuilder("gemini", "--yolo", "--model", model, "-p", fullPrompt);
            pb.directory(new File(System.getProperty("user.dir")));
            pb.redirectErrorStream(true);
            pb.environment().put("GEMINI_API_KEY", apiKey);

            Process process = pb.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    if (!line.isBlank() && !line.startsWith("Warning:") && !line.startsWith("YOLO mode")) {
                        broadcast(new AgentStatusUpdate(type, AgentState.THINKING, line, System.currentTimeMillis()));
                    }
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                String summary = output.toString().trim();
                if (summary.length() > 1200) {
                    summary = summary.substring(summary.length() - 1200);
                }
                broadcast(new AgentStatusUpdate(type, AgentState.HANDOFF, summary, System.currentTimeMillis()));
                broadcast(new AgentStatusUpdate(type, AgentState.DONE, "Workspace edits completed with " + model, System.currentTimeMillis()));
                return summary;
            }

            log.warn("Gemini CLI failed with model {} (exit {}). Cycling to next model...", model, exitCode);
        }

        String err = "All Gemini CLI models failed or hit quota limits.";
        broadcast(new AgentStatusUpdate(type, AgentState.ERROR, err, System.currentTimeMillis()));
        return err;
    }

    private String callGeminiRestWithFallback(String prompt) throws Exception {
        if ("REPLACE_ME".equals(apiKey) || apiKey == null || apiKey.isEmpty()) {
            return "[MOCK] Gemini API Key not set. Simulated response for: " + prompt;
        }

        for (int i = 0; i < FALLBACK_MODELS.size(); i++) {
            String model = FALLBACK_MODELS.get(i);
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
                int status = response.statusCode();
                if ((status == 404 || status == 429 || status == 503) && i < FALLBACK_MODELS.size() - 1) {
                    log.warn("Status {} on {}. Falling back to {}...", status, model, FALLBACK_MODELS.get(i + 1));
                    continue;
                }
                return "[ERROR] Gemini API returned status " + status + ": " + response.body();
            }
        }
        return "[ERROR] All Gemini models failed.";
    }

    @SuppressWarnings("unchecked")
    private String parseGeminiResponse(String jsonBody) {
        try {
            Map<String, Object> map = objectMapper.readValue(jsonBody, Map.class);
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) map.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    return (String) parts.get(0).get("text");
                }
            }
        } catch (Exception e) {log.error("Failed to parse Gemini response", e);
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
