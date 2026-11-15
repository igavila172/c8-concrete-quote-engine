package com.concrete.c8_concrete_quote_engine.model;

public enum AgentType {
    RD_STRATEGIST("R&D Strategist"),
    BACKEND_ARCHITECT("Backend Architect"),
    FRONTEND_DESIGNER("Frontend Designer"),
    QA_AUDITOR("QA Auditor");

    private final String displayName;

    AgentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
