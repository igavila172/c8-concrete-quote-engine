# C-8 Concrete Quote Engine & Autonomous AI Swarm

An enterprise-grade, Spring Boot-powered estimation engine and multi-agent AI ecosystem built for California **C-8 Concrete Contractors**.

---

## 🏛️ Architecture Overview

The system bridges customer-facing contractor tools with an autonomous multi-agent AI command center.

```
┌────────────────────────────────────────────────────────┐
│                      Client Layer                      │
│   ┌────────────────────────┐  ┌─────────────────────┐  │
│   │ Customer Quote Engine  │  │ AI Mission Control  │  │
│   │      (/index.html)     │  │    (/agents.html)   │  │
│   └───────────┬────────────  └──────────┬──────────┘  │
│                 │                         │             │
└─────────────────┼─────────────────────────┼─────────────┘
                  ▼                         ▼
┌────────────────────────────────────────────────────────┐
│                   Spring Boot Engine                   │
│   ┌────────────────────┐     ┌──────────────────────┐  │
│   │  QuoteController   │     │AgentOrchestratorCtrl │  │
│   └─────────┬──────────┘     └──────────┬───────────┘  │
│             ▼                           ▼              │
│   ┌────────────────────┐     ┌──────────────────────┐  │
│   │ConcreteCalculatorSvc│    │  MultiAgentService   │  │
│   └────────────────────┘     └──────────┬───────────┘  │
└─────────────────────────────────────────┼──────────────┘
                                          ▼
                               ┌──────────────────────┐
                               │     Gemini API       │
                               │  (2.5-Flash / Lite)  │
                               └──────────────────────┘
```

---

## 🚀 Key Features

1. **C-8 Concrete Calculation Engine**:
   - **Volume Formula**: $\text{Cubic Yards} = \frac{\text{Length (ft)} \times \text{Width (ft)} \times (\text{Thickness (in)} / 12)}{27}$
   - **Waste Margin**: Standard 10% allowance for subgrade variance and form deflection.
   - **Delivery Rounding**: Billed volume strictly **rounded UP** to the nearest **0.25 cubic yard**.
   - **Short-Load Fee**: Automatically applies a \$150.00 fee for orders under 4.00 cubic yards.
2. **Financial Precision (`BigDecimal`)**:
   - Zero floating-point math; all monetary and volume calculations use `BigDecimal` with explicit rounding modes (`HALF_UP` for currency, `CEILING` for yardage).
   - **Loan Amortization**: Instant monthly payment estimates for 12, 36, and 60-month financing terms.
3. **Live AI Mission Control (`/agents.html`)**:
   - Cyberpunk neural feed with Server-Sent Events (SSE).
   - Coordinates four autonomous agents (R&D Strategist, Backend Architect, Frontend Designer, QA Auditor) in real-time.
4. **Autonomous Cloud CI/CD (`GitHub Actions`)**:
   - Daily automated R&D and feature building.
   - Automated AI PR code reviews enforcing math accuracy and test coverage.

---

## 🛠️ Local Setup Instructions

1. **Prerequisites**: Java 21, Maven.
2. **Clone & Configure**:
   ```bash
   git clone https://github.com/your-username/c8-concrete-quote-engine.git
   cd c8-concrete-quote-engine
   ```
3. **API Key Setup**:
   Ensure `src/main/resources/application-local.properties` contains your Gemini API key:
   ```properties
   gemini.api.key=YOUR_GEMINI_API_KEY
   ```
4. **Run the Application**:
   ```bash
   ./mvnw spring-boot:run
   ```
5. **Access the Interfaces**:
   - Customer Quote Engine: `http://localhost:8080/index.html`
   - AI Mission Control: `http://localhost:8080/agents.html`
6. **Run Tests**:
   ```bash
   ./mvnw test
   ```

---

## 🤖 Autonomous Multi-Agent GitHub Pipeline

Our GitHub Actions workflows automate continuous evolution:
- **`autonomous-rd-and-build.yml`**: Runs daily at 8:00 AM PT. The R&D Strategist proposes features, and the Code Builder implements them, runs tests, and opens PRs automatically.
- **`ai-pr-reviewer.yml`**: Acts as QA Auditor on every Pull Request, verifying `BigDecimal` math rules and security before merging.
