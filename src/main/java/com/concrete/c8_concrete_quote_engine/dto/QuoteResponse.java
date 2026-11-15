package com.concrete.c8_concrete_quote_engine.dto;

import java.math.BigDecimal;
import java.util.List;

public record QuoteResponse(
    BigDecimal rawCubicYards,
    BigDecimal yardsWithWaste,
    BigDecimal billedYards,
    Integer mixGradePsi,
    BigDecimal mixGradeSurcharge,
    String surfaceFinish,
    BigDecimal surfaceFinishSurcharge,
    String reinforcementType,
    BigDecimal reinforcementSurcharge,
    Boolean vaporBarrier,
    BigDecimal vaporBarrierSurcharge,
    BigDecimal concreteSubtotal,
    BigDecimal shortLoadFee,
    BigDecimal totalCost,
    List<LoanPaymentOption> financingOptions
) {
    public QuoteResponse(
        BigDecimal rawCubicYards,
        BigDecimal yardsWithWaste,
        BigDecimal billedYards,
        BigDecimal concreteSubtotal,
        BigDecimal shortLoadFee,
        BigDecimal totalCost,
        List<LoanPaymentOption> financingOptions
    ) {
        this(rawCubicYards, yardsWithWaste, billedYards, 2500, BigDecimal.ZERO, "BROOM", BigDecimal.ZERO, "NONE", BigDecimal.ZERO, false, BigDecimal.ZERO, concreteSubtotal, shortLoadFee, totalCost, financingOptions);
    }
}
