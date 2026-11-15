package com.concrete.c8_concrete_quote_engine.service;

import com.concrete.c8_concrete_quote_engine.config.PricingProperties;
import com.concrete.c8_concrete_quote_engine.dto.QuoteRequest;
import com.concrete.c8_concrete_quote_engine.dto.QuoteResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConcreteCalculatorServiceTest {

    private ConcreteCalculatorService service;
    private PricingProperties pricing;

    @BeforeEach
    void setUp() {
        pricing = new PricingProperties();
        pricing.setConcretePricePerYard(new BigDecimal("175.00"));
        pricing.setShortLoadFee(new BigDecimal("150.00"));
        pricing.setShortLoadThreshold(new BigDecimal("4.00"));
        pricing.setDefaultWasteFactor(new BigDecimal("0.10"));
        pricing.setLoanOptions(List.of(
            new PricingProperties.LoanOption(12, BigDecimal.ZERO),
            new PricingProperties.LoanOption(36, new BigDecimal("0.059"))
        ));
        service = new ConcreteCalculatorService(pricing);
    }

    @Test
    void calculateQuote_SmallJob_IncursShortLoadFee() {
        // 10ft x 10ft x 4in = 33.33 cu ft = 1.2346 cu yd
        // With 10% waste: 1.358 cu yd
        // Rounded UP to 0.25: 1.50 cu yd
        // 1.50 yd < 4.00 yd -> Short Load Fee applies
        QuoteRequest request = new QuoteRequest(
            new BigDecimal("10.0"),
            new BigDecimal("10.0"),
            new BigDecimal("4.0"),
            null
        );

        QuoteResponse response = service.calculateQuote(request);

        assertThat(response.billedYards()).isEqualByComparingTo("1.50");
        assertThat(response.shortLoadFee()).isEqualByComparingTo("150.00");
        assertThat(response.concreteSubtotal()).isEqualByComparingTo("262.50"); // 1.5 * 175
        assertThat(response.totalCost()).isEqualByComparingTo("412.50"); // 262.5 + 150
    }

    @Test
    void calculateQuote_LargeJob_NoShortLoadFee() {
        // 20ft x 40ft x 4in = 266.67 cu ft = 9.876 cu yd
        // With 10% waste: 10.86 cu yd
        // Rounded UP to 0.25: 11.00 cu yd
        // 11.00 yd >= 4.00 yd -> No Short Load Fee
        QuoteRequest request = new QuoteRequest(
            new BigDecimal("20.0"),
            new BigDecimal("40.0"),
            new BigDecimal("4.0"),
            null
        );

        QuoteResponse response = service.calculateQuote(request);

        assertThat(response.billedYards()).isEqualByComparingTo("11.00");
        assertThat(response.shortLoadFee()).isEqualByComparingTo("0.00");
        assertThat(response.totalCost()).isEqualByComparingTo("1925.00"); // 11 * 175
    }

    @Test
    void calculateQuote_ExactQuarterYard_NoExtraRounding() {
        // 27ft x 1ft x 3in = 6.75 cu ft = 0.25 cu yd
        // With 0% waste: 0.25 cu yd
        // Rounded UP to 0.25: 0.25 cu yd
        QuoteRequest request = new QuoteRequest(
            new BigDecimal("27.0"),
            new BigDecimal("1.0"),
            new BigDecimal("3.0"),
            BigDecimal.ZERO
        );

        QuoteResponse response = service.calculateQuote(request);

        assertThat(response.billedYards()).isEqualByComparingTo("0.25");
    }

    @Test
    void calculateQuote_Amortization_CorrectMonthlyPayment() {
        // Simple case: $1200 over 12 months at 0%
        QuoteRequest request = new QuoteRequest(
            new BigDecimal("10.0"),
            new BigDecimal("10.0"),
            new BigDecimal("4.0"),
            null
        );
        // Force total cost to $1200 for easier math (via mocked pricing if needed, but we'll just check the response)
        QuoteResponse response = service.calculateQuote(request);
        
        // Check 12 month 0% option
        var zeroPercentOption = response.financingOptions().get(0);
        assertThat(zeroPercentOption.termMonths()).isEqualTo(12);
        assertThat(zeroPercentOption.annualRate()).isEqualByComparingTo(BigDecimal.ZERO);
        
        BigDecimal expectedMonthly = response.totalCost().divide(new BigDecimal("12"), 2, java.math.RoundingMode.HALF_UP);
        assertThat(zeroPercentOption.monthlyPayment()).isEqualByComparingTo(expectedMonthly);
        assertThat(zeroPercentOption.totalInterest()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
