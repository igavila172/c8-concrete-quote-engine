package com.concrete.c8_concrete_quote_engine.controller;

import com.concrete.c8_concrete_quote_engine.dto.QuoteRequest;
import com.concrete.c8_concrete_quote_engine.dto.QuoteResponse;
import com.concrete.c8_concrete_quote_engine.service.ConcreteCalculatorService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    private final ConcreteCalculatorService calculatorService;

    public QuoteController(ConcreteCalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @PostMapping
    public QuoteResponse createQuote(@Valid @RequestBody QuoteRequest request) {
        return calculatorService.calculateQuote(request);
    }
}
