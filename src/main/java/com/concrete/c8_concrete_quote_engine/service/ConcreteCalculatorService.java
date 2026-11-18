package com.concrete.c8_concrete_quote_engine.service;

import com.concrete.c8_concrete_quote_engine.config.PricingProperties;
import com.concrete.c8_concrete_quote_engine.dto.LoanPaymentOption;
import com.concrete.c8_concrete_quote_engine.dto.QuoteRequest;
import com.concrete.c8_concrete_quote_engine.dto.QuoteResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConcreteCalculatorService {

    private final PricingProperties pricing;
    private static final BigDecimal TWENTY_SEVEN = new BigDecimal("27");
    private static final BigDecimal TWELVE = new BigDecimal("12");
    private static final BigDecimal QUARTER_YARD = new BigDecimal("0.25");

    public ConcreteCalculatorService(PricingProperties pricing) {
        this.pricing = pricing;
    }

    public QuoteResponse calculateQuote(QuoteRequest request) {
        // 1. Raw Cubic Yards: (L * W * (T/12)) / 27
        BigDecimal thicknessFeet = request.thicknessInches().divide(TWELVE, MathContext.DECIMAL128);
        BigDecimal volumeCubicFeet = request.lengthFeet()
                .multiply(request.widthFeet())
                .multiply(thicknessFeet);
        BigDecimal rawCubicYards = volumeCubicFeet.divide(TWENTY_SEVEN, MathContext.DECIMAL128);

        // 2. Apply Waste
        BigDecimal wasteFactor = request.customWasteFactor() != null 
                ? request.customWasteFactor() 
                : pricing.getDefaultWasteFactor();
        BigDecimal yardsWithWaste = rawCubicYards.multiply(BigDecimal.ONE.add(wasteFactor));

        // 3. Round UP to nearest 0.25 yard
        BigDecimal billedYards = yardsWithWaste.divide(QUARTER_YARD, 0, RoundingMode.CEILING)
                .multiply(QUARTER_YARD);

        // 4. Mix Grade & Price Calculation
        int mixGradePsi = request.mixGradePsi() != null ? request.mixGradePsi() : 2500;
        BigDecimal gradeSurchargePerYard = pricing.getMixGrades().getOrDefault(mixGradePsi, BigDecimal.ZERO);
        BigDecimal effectivePricePerYard = pricing.getConcretePricePerYard().add(gradeSurchargePerYard);
        
        BigDecimal concreteSubtotal = billedYards.multiply(effectivePricePerYard)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal mixGradeSurcharge = billedYards.multiply(gradeSurchargePerYard)
                .setScale(2, RoundingMode.HALF_UP);

        // 5. Surface Finish Surcharge Calculation
        BigDecimal areaSqFt = request.lengthFeet().multiply(request.widthFeet());
        String surfaceFinish = request.surfaceFinish() != null && !request.surfaceFinish().isBlank() 
                ? request.surfaceFinish().toUpperCase() 
                : "BROOM";
        BigDecimal finishPricePerSqFt = pricing.getSurfaceFinishes().getOrDefault(surfaceFinish, BigDecimal.ZERO);
        BigDecimal surfaceFinishSurcharge = areaSqFt.multiply(finishPricePerSqFt)
                .setScale(2, RoundingMode.HALF_UP);

        // 6. Reinforcement Surcharge Calculation
        String reinforcementType = request.reinforcementType() != null && !request.reinforcementType().isBlank()
                ? request.reinforcementType().toUpperCase()
                : "NONE";
        BigDecimal reinforcementPricePerSqFt = pricing.getReinforcements().getOrDefault(reinforcementType, BigDecimal.ZERO);
        BigDecimal reinforcementSurcharge = areaSqFt.multiply(reinforcementPricePerSqFt)
                .setScale(2, RoundingMode.HALF_UP);

        // 7. Vapor Barrier Surcharge Calculation
        boolean vaporBarrier = Boolean.TRUE.equals(request.vaporBarrier());
        BigDecimal vaporBarrierSurcharge = vaporBarrier
                ? areaSqFt.multiply(pricing.getVaporBarrierPricePerSqFt()).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        // 8. Pumping Surcharge Calculation
        String pumpingMethod = request.pumpingMethod() != null && !request.pumpingMethod().isBlank()
                ? request.pumpingMethod().toUpperCase()
                : "NONE";
        PricingProperties.PumpingMethodConfig pumpConfig = pricing.getPumpingMethods().getOrDefault(
                pumpingMethod,
                new PricingProperties.PumpingMethodConfig(BigDecimal.ZERO, BigDecimal.ZERO)
        );
        BigDecimal pumpingSurcharge = pumpConfig.getBaseFee()
                .add(billedYards.multiply(pumpConfig.getPerYardFee()))
                .setScale(2, RoundingMode.HALF_UP);

        // 9. Long-Distance Haul Surcharge Calculation
        Integer haulDistanceFeet = request.haulDistanceFeet();
        BigDecimal haulSurcharge;
        if (haulDistanceFeet != null && haulDistanceFeet > pricing.getLongDistanceHaulThresholdFeet()) {
            haulSurcharge = areaSqFt.multiply(pricing.getLongDistanceHaulFeePerSqFt())
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            haulSurcharge = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        // 10. Short-Load Fee
        BigDecimal shortLoadFee = billedYards.compareTo(pricing.getShortLoadThreshold()) < 0
                ? pricing.getShortLoadFee()
                : BigDecimal.ZERO;
        shortLoadFee = shortLoadFee.setScale(2, RoundingMode.HALF_UP);

        // 11. Total Cost
        BigDecimal totalCost = concreteSubtotal
                .add(surfaceFinishSurcharge)
                .add(reinforcementSurcharge)
                .add(vaporBarrierSurcharge)
                .add(pumpingSurcharge)
                .add(haulSurcharge)
                .add(shortLoadFee);

        // 12. Financing Options
        List<LoanPaymentOption> financingOptions = pricing.getLoanOptions().stream()
                .map(option -> calculateLoanOption(totalCost, option.termMonths(), option.annualRate()))
                .collect(Collectors.toList());

        return new QuoteResponse(
            rawCubicYards.setScale(4, RoundingMode.HALF_UP),
            yardsWithWaste.setScale(4, RoundingMode.HALF_UP),
            billedYards.setScale(2, RoundingMode.HALF_UP),
            mixGradePsi,
            mixGradeSurcharge,
            surfaceFinish,
            surfaceFinishSurcharge,
            reinforcementType,
            reinforcementSurcharge,
            vaporBarrier,
            vaporBarrierSurcharge,
            pumpingMethod,
            pumpingSurcharge,
            haulDistanceFeet,
            haulSurcharge,
            concreteSubtotal,
            shortLoadFee,
            totalCost,
            financingOptions
        );
    }

    private LoanPaymentOption calculateLoanOption(BigDecimal principal, int termMonths, BigDecimal annualRate) {
        BigDecimal monthlyRate = annualRate.divide(new BigDecimal("12"), MathContext.DECIMAL128);
        BigDecimal monthlyPayment;
        BigDecimal totalInterest;

        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            monthlyPayment = principal.divide(new BigDecimal(termMonths), 2, RoundingMode.HALF_UP);
            totalInterest = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else {
            // M = P [ r(1 + r)^n ] / [ (1 + r)^n – 1 ]
            BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
            BigDecimal onePlusRPowerN = onePlusR.pow(termMonths, MathContext.DECIMAL128);
            
            BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRPowerN);
            BigDecimal denominator = onePlusRPowerN.subtract(BigDecimal.ONE);
            
            monthlyPayment = numerator.divide(denominator, 2, RoundingMode.HALF_UP);
            BigDecimal totalPayment = monthlyPayment.multiply(new BigDecimal(termMonths));
            totalInterest = totalPayment.subtract(principal).setScale(2, RoundingMode.HALF_UP);
        }

        return new LoanPaymentOption(termMonths, annualRate, monthlyPayment, totalInterest);
    }
}
