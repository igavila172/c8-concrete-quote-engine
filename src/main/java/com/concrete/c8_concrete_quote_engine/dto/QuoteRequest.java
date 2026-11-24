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
    String surfaceFinish,
    String reinforcementType,
    Boolean vaporBarrier,
    String pumpingMethod,
    Integer haulDistanceFeet
) {
    public QuoteRequest(BigDecimal lengthFeet, BigDecimal widthFeet, BigDecimal thicknessInches, BigDecimal customWasteFactor) {
        this(lengthFeet, widthFeet, thicknessInches, customWasteFactor, null, null, null, null, null, null);
    }

    public QuoteRequest(BigDecimal lengthFeet, BigDecimal widthFeet, BigDecimal thicknessInches, BigDecimal customWasteFactor, Integer mixGradePsi, String surfaceFinish) {
        this(lengthFeet, widthFeet, thicknessInches, customWasteFactor, mixGradePsi, surfaceFinish, null, null, null, null);
    }

    public QuoteRequest(BigDecimal lengthFeet, BigDecimal widthFeet, BigDecimal thicknessInches, BigDecimal customWasteFactor, Integer mixGradePsi, String surfaceFinish, String reinforcementType, Boolean vaporBarrier) {
        this(lengthFeet, widthFeet, thicknessInches, customWasteFactor, mixGradePsi, surfaceFinish, reinforcementType, vaporBarrier, null, null);
    }
}
