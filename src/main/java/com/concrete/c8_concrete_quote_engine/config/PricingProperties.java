package com.concrete.c8_concrete_quote_engine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "pricing")
@PropertySource(value = "classpath:pricing-rules.yml", factory = YamlPropertySourceFactory.class)
public class PricingProperties {
    private BigDecimal concretePricePerYard;
    private BigDecimal shortLoadFee;
    private BigDecimal shortLoadThreshold;
    private BigDecimal defaultWasteFactor;
    private Map<Integer, BigDecimal> mixGrades;
    private Map<String, BigDecimal> surfaceFinishes;
    private Map<String, BigDecimal> reinforcements;
    private BigDecimal vaporBarrierPricePerSqFt;
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
    
    public Map<Integer, BigDecimal> getMixGrades() {
        if (mixGrades == null) {
            return Map.of(
                2500, BigDecimal.ZERO,
                3000, new BigDecimal("15.00"),
                4000, new BigDecimal("30.00"),
                5000, new BigDecimal("50.00")
            );
        }
        return mixGrades;
    }
    public void setMixGrades(Map<Integer, BigDecimal> mixGrades) { this.mixGrades = mixGrades; }

    public Map<String, BigDecimal> getSurfaceFinishes() {
        if (surfaceFinishes == null) {
            return Map.of(
                "BROOM", BigDecimal.ZERO,
                "SALT_SWIRL", new BigDecimal("0.50"),
                "STAMPED", new BigDecimal("3.50"),
                "EXPOSED_AGGREGATE", new BigDecimal("2.00")
            );
        }
        return surfaceFinishes;
    }
    public void setSurfaceFinishes(Map<String, BigDecimal> surfaceFinishes) { this.surfaceFinishes = surfaceFinishes; }

    public Map<String, BigDecimal> getReinforcements() {
        if (reinforcements == null) {
            return Map.of(
                "NONE", BigDecimal.ZERO,
                "REBAR_3", new BigDecimal("1.25"),
                "REBAR_4", new BigDecimal("1.75"),
                "WWF", new BigDecimal("0.85")
            );
        }
        return reinforcements;
    }
    public void setReinforcements(Map<String, BigDecimal> reinforcements) { this.reinforcements = reinforcements; }

    public BigDecimal getVaporBarrierPricePerSqFt() {
        return vaporBarrierPricePerSqFt != null ? vaporBarrierPricePerSqFt : new BigDecimal("0.45");
    }
    public void setVaporBarrierPricePerSqFt(BigDecimal vaporBarrierPricePerSqFt) { this.vaporBarrierPricePerSqFt = vaporBarrierPricePerSqFt; }

    public List<LoanOption> getLoanOptions() { return loanOptions; }
    public void setLoanOptions(List<LoanOption> loanOptions) { this.loanOptions = loanOptions; }
}
