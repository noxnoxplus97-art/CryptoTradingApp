# Crypto Trading System - Spring Boot Application

## Overview

A Spring Boot-based cryptocurrency trading application that aggregates real-time prices from multiple sources (Binance and Huobi) and allows users to execute buy/sell trades with wallet balance management.

## Features

1. **Price Aggregation (10-second scheduler)**
   - Fetches prices from Binance: `https://api.binance.com/api/v3/ticker/bookTicker`
   - Fetches prices from Huobi: `https://api.huobi.pro/market/tickers`
   - Stores best bid/ask prices in the H2 database
   - Bid price used for SELL orders, Ask price used for BUY orders

2. **Trading Operations**
   - BUY: Deduct USDT, add cryptocurrency at the current ask price
   - SELL: Deduct cryptocurrency, add USDT at the current bid price
   - Real-time balance management and transaction recording

3. **Wallet Management**
   - Each user has three wallets: USDT, ETH, BTC
   - Initial USDT balance: 50,000 (as per assumption)
   - Track available and total balance

4. **Trading History**
   - View all trades for a user
   - View trades filtered by trading pair
   - Track trade timestamps, prices, and amounts

## Technology Stack

- **Framework**: Spring Boot 3.2.2
- **Java Version**: 17
- **Database**: H2 (in-memory)
- **ORM**: Spring Data JPA / Hibernate
- **Build Tool**: Maven
- **HTTP Client**: RestTemplate
- **Logging**: SLF4J
- **Additional**: Lombok (annotations for getters/setters)

## Project Structure

```
src/
├── main/
│   ├── java/com/example/tradingapp/
│   │   ├── TradingApplication.java (Main entry point)
│   │   ├── config/
│   │   │   ├── RestTemplateConfig.java (RestTemplate bean)
│   │   │   └── DataInitializer.java (Initialize default user and wallets)
│   │   ├── controller/
│   │   │   └── MainController.java (REST APIs)
│   │   ├── service/
│   │   │   ├── PriceAggregationService.java (Price fetching and aggregation)
│   │   │   ├── TradeService.java (Trade execution logic)
│   │   │   └── WalletService.java (Wallet management)
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   ├── CryptoPrice.java
│   │   │   ├── Trade.java
│   │   │   └── Wallet.java
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   ├── CryptoPriceRepository.java
│   │   │   ├── TradeRepository.java
│   │   │   └── WalletRepository.java
│   │   └── dto/
│   │       ├── TradeDTO.java
│   │       ├── WalletDTO.java
│   │       ├── TradeRequestDTO.java
│   │       ├── PriceResponseDTO.java
│   │       └── ApiResponseDTO.java
│   └── resources/
│       └── application.properties
```

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) NOT NULL
);
```

### Wallets Table
```sql
CREATE TABLE wallets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    currency VARCHAR(10) NOT NULL,
    balance DECIMAL(18, 8) NOT NULL,
    available_balance DECIMAL(18, 8) NOT NULL,
    UNIQUE(user_id, currency),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Trades Table
```sql
CREATE TABLE trades (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    type VARCHAR(10) NOT NULL,
    quantity DECIMAL(18, 8) NOT NULL,
    price DECIMAL(18, 8) NOT NULL,
    total_amount DECIMAL(18, 2) NOT NULL,
    timestamp DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user_timestamp (user_id, timestamp DESC)
);
```

"""
# Crypto Trading System

This repository contains a simple crypto trading backend (Spring Boot) and a lightweight AngularJS (1.x) frontend used for demos and learning.

This README focuses on quick start steps and developer guidance. Full frontend documentation is in `FRONTEND_DOCUMENTATION.md`.

## Quickstart (developer)

Prerequisites:
- Java 17
- Maven 3.6+

Build and run (from project root):

```powershell
mvn -DskipTests package
mvn spring-boot:run
```

Or run the packaged JAR:

```powershell
mvn -DskipTests package
java -jar target\*.jar
```

Open the application in a browser: http://localhost:8080

H2 Console: http://localhost:8080/h2-console
 - JDBC URL (default in-memory): `jdbc:h2:mem:tradingdb`
 - User: `sa` (no password)

Notes about the database:
- By default the app uses an in-memory H2 database (fast for local development). Data is lost when the process stops.

Quick API examples:

Get latest price for ETH:

```powershell
curl http://localhost:8080/api/price/ETHUSDT
```

Execute a trade (BUY 1.5 ETH):

```powershell
curl -H "Content-Type: application/json" -X POST -d "{\"symbol\":\"ETHUSDT\",\"type\":\"BUY\",\"quantity\":1.5}" http://localhost:8080/api/trade
```

View wallets:

```powershell
curl http://localhost:8080/api/wallet
```

## Project layout (short)

Key folders:

- `src/main/java` – Java backend (controllers, services, repositories, entities)
- `src/main/resources/application.properties` – Spring Boot properties (H2 config)
- `src/main/resources/static` – frontend static files (AngularJS app, views, CSS)

Frontend quick notes:
- AngularJS app script: `src/main/resources/static/js/app.js`
- Controllers: `src/main/resources/static/js/controllers/`
- Services: `src/main/resources/static/js/services/`
- Views/Templates: `src/main/resources/static/views/`

For a full frontend reference, read `FRONTEND_DOCUMENTATION.md`.

## Development tips

- If you enable file-based H2 and want to migrate data from a running in-memory instance, use the H2 console's `SCRIPT TO 'C:/path/dump.sql'` on the running JVM, then `RUNSCRIPT FROM 'C:/path/dump.sql'` after switching to file-based.
- `spring.jpa.hibernate.ddl-auto` is set to `create-drop` for in-memory default; if you persist data, consider `update` or `validate` depending on your needs.

## Next steps (optional tasks you can ask me to do)

- Add a property `app.data.initializer.enabled` and make `DataInitializer` respect it (disable reseeding after first run).
- Enable file-based H2 by default and create migration helpers.
- Add a small README for frontend dev workflow and testing.

---
Updated documentation and a dedicated frontend doc are included in the repository.
"""
            "id": 3,
