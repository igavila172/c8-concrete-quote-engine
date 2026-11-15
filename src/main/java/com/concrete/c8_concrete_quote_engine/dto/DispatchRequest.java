package com.concrete.c8_concrete_quote_engine.dto;

import jakarta.validation.constraints.NotBlank;

public record DispatchRequest(
    @NotBlank String goal
) {}
