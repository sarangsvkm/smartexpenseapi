# 💰 SmartExpense Manager – Enterprise Financial Backend

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![JWT](https://img.shields.io/badge/Security-Sliding_JWT-gold.svg)](https://jwt.io/)

SmartExpense Manager is a production-grade financial tracking backend built with **Spring Boot 3**. It offers advanced features like **Indian ITR (1-4) tax calculation**, **Debt Repayment Optimization**, and **Sliding Session Security**, making it a perfect project for demonstrating high-level backend engineering skills.

---

## 🌟 Interview Highlights (Enterprise Features)

-   **Indian Tax Compliance Engine**: Supports **ITR-1 to ITR-4** models with specialized logic for Section 87A rebates and **Presumptive Taxation (8%)**.
-   **Sliding Session Security**: Implemented a **Rolling JWT refresh mechanism** that resets the inactivity timer on every request—balancing security with user experience.
-   **Multi-Regime Tax Comparator**: Real-time comparison between the **Old vs. New Tax Regimes (FY 2025-26)** to recommend the most tax-efficient path.
-   **Intelligent Debt Strategy**: Custom implementations of **Avalanche** (Interest-focused) and **Snowball** (Balance-focused) repayment algorithms.
-   **Automated Data Seeding**: Robust initialization logic that ensures mission-critical database roles exist on startup—zero manual DB setup required for auth.

---

## 🚀 Key Features

### 1. 🛡️ Advanced Security
- **Sliding JWT Sessions**: Tokens are refreshed automatically in the `New-Token` response header.
- **BCrypt Encryption**: Industry-standard hashing for all user credentials.
- **Role-Based Access**: Granular protection (USER/ADMIN) using Spring Security.

### 2. 📊 Indian Tax & ITR Module
- **ITR Forms**: Automated data gathering for ITR-1 (Salary), ITR-2 (Capital Gains), ITR-3 (Professional), and ITR-4 (Presumptive).
- **Asset Classes**: Detailed tracking for **Stocks, Mutual Funds, and Gold** with buy/sell price analysis.
- **FY 2025-26 Ready**: Includes the ₹75,000 standard deduction and ₹12 Lakh rebate rules.

### 3. 📉 Debt & EMI Management
- **Automated EMI Logic**: Paying an EMI automatically reduces loan principal and updates financial snapshots.
- **Strategy Recommendation**: Suggests the best repayment method based on the user's current debt profile.

---

## 📊 System Flow

### Sliding Session Authentication Flow
```mermaid
sequenceDiagram
    participant User
    participant AuthTokenFilter
    participant SecurityContext
    participant JWTProvider
    participant API

    User->>API: Request (Header: Authorization: Bearer <OldToken>)
    API->>AuthTokenFilter: Intercept Request
    AuthTokenFilter->>JWTProvider: Validate OldToken
    JWTProvider-->>AuthTokenFilter: Token Valid
    AuthTokenFilter->>SecurityContext: Set Authentication
    
    Note right of AuthTokenFilter: SLIDING SESSION LOGIC
    AuthTokenFilter->>JWTProvider: Generate Fresh Token
    JWTProvider-->>AuthTokenFilter: NewToken (30 min expiry)
    
    AuthTokenFilter->>API: Continue Filter Chain
    API-->>User: Response (Header: New-Token: <NewToken>)
    
    Note over User, API: Client updates local storage with NewToken
```

---

## 💾 Database Schema (ERD)

```mermaid
erDiagram
    USER ||--o{ EXPENSE : logs
    USER ||--o{ INCOME : earns
    USER ||--o{ LOAN : owes
    USER ||--o{ BUDGET : sets
    USER ||--o{ ROLE : "has roles (M:M)"
    USER ||--o{ FINANCIAL_SNAPSHOT : "tracks history"
    USER }o--|| DEBT_STRATEGY : prefers

    EXPENSE }o--|| CATEGORY : categorized_as
    BUDGET }o--|o CATEGORY : limits_on
    LOAN ||--o{ EMI_PAYMENT : consists_of

    INCOME {
        double amount
        string category "SALARY, BUSINESS, CAPITAL_GAINS..."
        string assetType "GOLD, STOCKS, MUTUAL_FUND..."
        double purchasePrice
        boolean isTaxable
    }

    EXPENSE {
        double amount
        boolean isTaxDeductible
    }
```

---

## 🏛 Architecture Diagram

```mermaid
graph TD
    User([User]) --> AuthController
    User --> TaxController
    User --> AnalyticsController
    
    subgraph "Core Business Services"
        TaxCalcService[TaxCalculationService]
        TaxRecService[TaxRecommendationService]
        DebtService[DebtStrategyService]
        AnalyticsService[AnalyticsService]
    end
    
    subgraph "Entities"
        IncomeE[Income]
        ExpenseE[Expense]
        UserE[User]
        LoanE[Loan]
    end
    
    TaxCalcService --> IncomeE
    TaxCalcService --> ExpenseE
    TaxRecService --> IncomeE
    AnalyticsService --> UserE
    
    PostgreSQL[(PostgreSQL DB)]
    IncomeE --> PostgreSQL
    UserE --> PostgreSQL
```

---

## 🚦 New API Endpoints (Tax Module)

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| GET | `/api/tax/recommendation` | Suggests ITR-1, 2, 3, or 4 based on user data |
| GET | `/api/tax/report` | Detailed comparison of Old vs New Tax Regimes |
| POST | `/api/analytics/snapshot` | Save current financial state for historical tracking |

---

## 🛣 Future Roadmap

- [ ] **PDF Report Generator**: Generate professional ITR summary PDFs using iText.
- [ ] **Email Notifications**: Weekly financial health summaries and EMI reminders.
- [ ] **Third-Party Integration**: Real-time stock/gold price syncing for accurate Capital Gains.
- [ ] **Mutual Fund Portfolio Sync**: Integration with CAS (Consolidated Account Statement) files.

---

## ⚙️ Setup & Installation

1.  **Clone & Run**:
    ```bash
    git clone https://github.com/sarangsvkm/smartexpenseapi.git
    ./mvnw spring-boot:run
    ```
2.  **Docker Setup**:
    ```bash
    docker-compose up --build
    ```

---

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
