package com.concrete.c8_concrete_quote_engine.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record QuoteRequest(
    @NotNull @DecimalMin("0.1") BigDecimal lengthFeet,
    @NotNull @DecimalMin("0.1") BigDecimal widthFeet,
    @NotNull @DecimalMin("0.1") BigDecimal thicknessInches,
    BigDecimal customWasteFactor,
    Integer mixGradePsi,
    String surfaceFinish
) {
    public QuoteRequest(BigDecimal lengthFeet, BigDecimal widthFeet, BigDecimal thicknessInches, BigDecimal customWasteFactor) {
        this(lengthFeet, widthFeet, thicknessInches, customWasteFactor, null, null);
    }
}
