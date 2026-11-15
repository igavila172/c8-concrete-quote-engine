package com.concrete.c8_concrete_quote_engine.dto;

import java.math.BigDecimal;
import java.util.List;

public record QuoteResponse(
    BigDecimal rawCubicYards,
    BigDecimal yardsWithWaste,
    BigDecimal billedYards,
    BigDecimal concreteSubtotal,
    BigDecimal shortLoadFee,
    BigDecimal totalCost,
    List<LoanPaymentOption> financingOptions
) {}
