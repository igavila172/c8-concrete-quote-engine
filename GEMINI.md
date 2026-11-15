# C-8 Concrete Quote Engine - Project & Engineering Guidelines

This document serves as the architectural and domain reference for the **C-8 Concrete Quote Engine**.

---

## 1. Industry Context: California C-8 Concrete Contractor License

In California, a **C-8 Concrete Contractor** specializes in preparing and placing concrete, including formwork, reinforcement installation, pouring, finishing, and architectural flatwork (driveways, slabs, patios, walkways, foundations, and footings). Accurate estimation requires strict adherence to trade volume formulas, plant delivery limitations, material waste allowances, and exact financial precision.

---

## 2. Core Concrete Calculation Rules

### 2.1 Cubic Yards Volume Formula
Ready-mix concrete is ordered and sold by the **cubic yard** ($1 \text{ yd}^3 = 27 \text{ ft}^3$). Job measurements are typically taken in **feet** for length and width, and **inches** for slab thickness.

$$\text{Thickness in Feet} = \frac{\text{Thickness (inches)}}{12}$$

$$\text{Volume (Cubic Feet)} = \text{Length (feet)} \times \text{Width (feet)} \times \left(\frac{\text{Thickness (inches)}}{12}\right)$$

$$\text{Raw Cubic Yards} = \frac{\text{Volume (Cubic Feet)}}{27} = \frac{\text{Length (ft)} \times \text{Width (ft)} \times \text{Thickness (in)}}{324}$$

### 2.2 Waste Allowance
- Concrete formwork deflects, subgrades have ground elevation variance, and minor spillage occurs during discharge.
- **Rule:** A minimum **10% waste factor** (`+10%`, or $\times 1.10$) must be applied to the raw volume before scheduling concrete delivery.
- Custom waste percentages may be configured for complex pours or irregular grade preparations.

### 2.3 Ready-Mix Batch plant Delivery Rounding (0.25 yd Increment)
- Concrete batch plants and transit mixers batch concrete in **0.25 cubic yard increments** (quarter-yard).
- **Rule:** After applying waste, the billed volume must be **rounded UP (CEILING)** to the nearest **0.25 cubic yard**.
- Formula using `BigDecimal`:
  $$\text{Billed Yards} = \left\lceil \frac{\text{Volume With Waste}}{0.25} \right\rceil \times 0.25$$

### 2.4 Short-Load Fee Under 4 Cubic Yards
- Ready-mix transit concrete trucks typically have a capacity of 9 to 10 cubic yards. Delivering small batches is economically inefficient for concrete suppliers.
- **Rule:** Any order where the billed volume is strictly **less than 4.00 cubic yards** incurs a **Short-Load Fee** (e.g., \$150.00 flat fee).
- If billed volume $\ge 4.00\text{ yd}^3$, the short-load fee is \$0.00.

---

## 3. Financial & Precision Standards (`BigDecimal` Math)

1. **Strict Prohibition of Floating-Point Math:**
   - Never use `double` or `float` for prices, dimensions involved in financial calculations, totals, tax amounts, or loan payments.
   - All arithmetic must use `java.math.BigDecimal` with appropriate `RoundingMode`.

2. **Rounding Rules:**
   - **Currency Amounts:** Rounded to **2 decimal places** using `RoundingMode.HALF_UP`.
   - **Volume Rounding:** Rounded to nearest quarter-yard using `RoundingMode.CEILING`.
   - **Intermediate Calculations:** Preserve high precision using `MathContext.DECIMAL128`.

3. **Estimated Monthly Loan Payments (Amortization):**
   - For home-improvement financing, monthly payments are computed using standard amortization:
     $$M = P \frac{r(1 + r)^n}{(1 + r)^n - 1}$$
     - $P$ = Total project cost (principal)
     - $r$ = Monthly interest rate ($\text{Annual Interest Rate} / 12$)
     - $n$ = Number of monthly payments (term in months, e.g., 12, 24, 36, 60)
     - If $r = 0$, $M = P / n$.
   - Monthly payments and total interest must be calculated in `BigDecimal` and rounded to 2 decimal places (`HALF_UP`).

---

## 4. Architecture & Technical Conventions

- **Spring Boot 4.x / Java 21:**
  - Modern Java records / immutable DTOs and POJOs.
  - Bean Validation (`jakarta.validation.constraints`) on API boundaries.
  - Configuration decoupled via `pricing-rules.yml` and bound with `@ConfigurationProperties`.
- **Package Layout:**
  - `com.concrete.c8_concrete_quote_engine.config`: Configuration properties (`PricingProperties`).
  - `com.concrete.c8_concrete_quote_engine.dto`: Request, response, and breakdown transfer objects.
  - `com.concrete.c8_concrete_quote_engine.model`: Domain enums (e.g., `FinishType`, `ReinforcementType`).
  - `com.concrete.c8_concrete_quote_engine.service`: Core business logic (`ConcreteCalculatorService`).
  - `com.concrete.c8_concrete_quote_engine.controller`: REST APIs (`QuoteController`).
- **Testing:**
  - JUnit 5 (`org.junit.jupiter.api.*`) and AssertJ for fluent assertions.
  - Thorough testing of volume math, 10% waste, 0.25 yd rounding boundaries, short-load cutoff at 4.00 yds, financial math, and MockMvc controller testing.
