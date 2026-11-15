package com.concrete.c8_concrete_quote_engine.dto;

import java.math.BigDecimal;

public record LoanPaymentOption(
    int termMonths,
    BigDecimal annualRate,
    BigDecimal monthlyPayment,
    BigDecimal totalInterest
) {}
