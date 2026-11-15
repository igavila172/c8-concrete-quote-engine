package com.concrete.c8_concrete_quote_engine.dto;

import com.concrete.c8_concrete_quote_engine.model.AgentState;
import com.concrete.c8_concrete_quote_engine.model.AgentType;

public record AgentStatusUpdate(
    AgentType agentType,
    AgentState state,
    String message,
    long timestamp
) {}
