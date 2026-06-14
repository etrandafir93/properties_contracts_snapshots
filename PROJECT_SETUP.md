# Trade Clearing System - Multi-Module Project Setup

## Prerequisites

- Java 25
- Maven 3.9+ (or use `./mvnw` / `mvnw.cmd` provided)
- Docker & Docker Compose (for Kafka in clearing-house)

## Project Structure

```
properties_contracts_snapshots/
├── pom.xml                           # Parent multi-module POM
├── mvnw / mvnw.cmd                   # Maven wrapper scripts
├── .mvn/wrapper/                     # Maven wrapper configuration
│
├── clearing-house/                   # Submodule: Trade clearing service
│   ├── pom.xml
│   ├── docker-compose.yml            # Kafka & Zookeeper setup
│   ├── src/main/java/com/clearinghouse/
│   │   ├── TradeClearingApplication.java
│   │   ├── api/
│   │   │   └── TradeController.java
│   │   ├── domain/
│   │   │   ├── IncomingTrade.java
│   │   │   ├── ValidatedTrade.java
│   │   │   ├── NovatedTrade.java
│   │   │   ├── EnrichedConfirmation.java
│   │   │   ├── TradeEntity.java
│   │   │   └── TradeRepository.java
│   │   └── filters/
│   │       ├── RiskValidator.java
│   │       ├── TradeNovation.java
│   │       ├── NovatedTradeRepository.java
│   │       ├── TradeConfirmationEnricher.java
│   │       └── TradeConfirmationPublisher.java
│   └── src/main/resources/
│       └── application.yml
│
└── currency-api/                     # Submodule: Currency reference data service
    ├── pom.xml
    ├── src/main/java/com/clearinghouse/
    │   ├── CurrencyApiApplication.java
    │   ├── api/
    │   │   └── CurrencyController.java
    │   ├── domain/
    │   │   └── Currency.java
    │   └── service/
    │       └── CurrencyService.java
    └── src/main/resources/
        ├── application.yml
        └── currencies.json              # Static currency data
```

## Building the Project

### Build All Modules

```bash
./mvnw clean package
# or on Windows:
mvnw.cmd clean package
```

### Build Individual Module

```bash
./mvnw clean package -pl clearing-house
./mvnw clean package -pl currency-api
```

## Running the Services

### 1. Start Kafka & Zookeeper (for clearing-house)

```bash
cd clearing-house
docker-compose up -d
cd ..
```

### 2. Start Currency API Service

```bash
./mvnw spring-boot:run -pl currency-api
```

Currency API will start on `http://localhost:8081`

### 3. Start Clearing House Service

```bash
./mvnw spring-boot:run -pl clearing-house
```

Clearing House will start on `http://localhost:8080`

## Testing the Services

### Get Available Currencies

```bash
curl http://localhost:8081/api/currencies
```

### Get Specific Currency

```bash
curl http://localhost:8081/api/currencies/USD
```

### Submit a Trade

```bash
curl -X POST http://localhost:8080/api/trades \
  -H "Content-Type: application/json" \
  -d '{
    "tradeId": "TRADE-001",
    "counterpartyA": "Bank A",
    "counterpartyB": "Bank B",
    "amount": 1000000.00,
    "currency": "USD",
    "settlementDate": "2026-06-20"
  }'
```

### View H2 Database Console

Navigate to: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- User: `sa`
- Password: (leave empty)

Query trades:
```sql
SELECT * FROM trades;
```

## Service Communication

**Clearing House Data Flow:**
1. REST API receives `IncomingTrade`
2. **RiskValidator** → `ValidatedTrade`
3. **TradeNovation** → Splits to 2 `NovatedTrade` (Alice → CH, CH → Bob)
4. **NovatedTradeRepository** → Persists to H2
5. **TradeConfirmationEnricher** → `EnrichedConfirmation` (fetches currency names from static data)
6. **TradeConfirmationPublisher** → Logs confirmations

All inter-filter communication flows through Kafka topics.

**Currency Service:**
- Serves currency reference data via REST API
- Loads currencies from `currencies.json` at startup
- No database, fully static data in memory

## Next Steps

- Add property-based tests (jqwik) for domain logic
- Add contract tests (Spring Cloud Contract) for REST/Kafka boundaries
- Add snapshot tests for confirmation emails/outputs
