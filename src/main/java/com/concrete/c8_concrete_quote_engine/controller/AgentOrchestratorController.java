package com.concrete.c8_concrete_quote_engine.controller;

import com.concrete.c8_concrete_quote_engine.dto.DispatchRequest;
import com.concrete.c8_concrete_quote_engine.service.MultiAgentService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/agents")
public class AgentOrchestratorController {

    private final MultiAgentService multiAgentService;

    public AgentOrchestratorController(MultiAgentService multiAgentService) {
        this.multiAgentService = multiAgentService;
    }

    @PostMapping("/dispatch")
    public void dispatch(@Valid @RequestBody DispatchRequest request) {
        multiAgentService.dispatchGoal(request.goal());
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(0L); // Infinite timeout
        multiAgentService.addEmitter(emitter);
        return emitter;
    }
}
