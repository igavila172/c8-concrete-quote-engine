package com.concrete.c8_concrete_quote_engine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import java.math.BigDecimal;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "pricing")
@PropertySource(value = "classpath:pricing-rules.yml", factory = YamlPropertySourceFactory.class)
public class PricingProperties {
    private BigDecimal concretePricePerYard;
    private BigDecimal shortLoadFee;
    private BigDecimal shortLoadThreshold;
    private BigDecimal defaultWasteFactor;
    private List<LoanOption> loanOptions;

    public static record LoanOption(int termMonths, BigDecimal annualRate) {}

    // Getters and Setters
    public BigDecimal getConcretePricePerYard() { return concretePricePerYard; }
    public void setConcretePricePerYard(BigDecimal concretePricePerYard) { this.concretePricePerYard = concretePricePerYard; }
    public BigDecimal getShortLoadFee() { return shortLoadFee; }
    public void setShortLoadFee(BigDecimal shortLoadFee) { this.shortLoadFee = shortLoadFee; }
    public BigDecimal getShortLoadThreshold() { return shortLoadThreshold; }
    public void setShortLoadThreshold(BigDecimal shortLoadThreshold) { this.shortLoadThreshold = shortLoadThreshold; }
    public BigDecimal getDefaultWasteFactor() { return defaultWasteFactor; }
    public void setDefaultWasteFactor(BigDecimal defaultWasteFactor) { this.defaultWasteFactor = defaultWasteFactor; }
    public List<LoanOption> getLoanOptions() { return loanOptions; }
    public void setLoanOptions(List<LoanOption> loanOptions) { this.loanOptions = loanOptions; }
}
