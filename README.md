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
│   │       ├── Trade.java (DTO)
│   │       ├── Wallet.java (DTO)
│   │       ├── TradeRequest.java
│   │       ├── PriceResponse.java
│   │       └── ApiResponse.java
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

### CryptoPrices Table
```sql
CREATE TABLE crypto_prices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    symbol VARCHAR(20) NOT NULL,
    bid_price DECIMAL(18, 8) NOT NULL,
    ask_price DECIMAL(18, 8) NOT NULL,
    timestamp DATETIME NOT NULL,
    source VARCHAR(20) NOT NULL,
    bid_qty DECIMAL(18, 8),
    ask_qty DECIMAL(18, 8),
    INDEX idx_symbol_timestamp (symbol, timestamp DESC)
);
```

## REST API Endpoints

### 1. Get Latest Aggregated Price
```
GET /api/price/{symbol}

Parameters:
- symbol: ETHUSDT or BTCUSDT

Response:
{
    "success": true,
    "message": "Success",
    "data": {
        "symbol": "ETHUSDT",
        "bidPrice": 2000.50,
        "askPrice": 2001.50,
        "timestamp": "2024-01-15 10:30:45"
    }
}
```

### 2. Execute Trade
```
POST /api/trade

Request Body:
{
    "symbol": "ETHUSDT",
    "type": "BUY",
    "quantity": 1.5
}

Response:
{
    "success": true,
    "message": "Trade executed successfully",
    "data": {
        "id": 1,
        "symbol": "ETHUSDT",
        "type": "BUY",
        "quantity": 1.5,
        "price": 2001.50,
        "totalAmount": 3002.25,
        "timestamp": "2024-01-15 10:30:45",
        "status": "COMPLETED"
    }
}
```

### 3. Get User's Wallet Balance
```
GET /api/wallet

Response:
{
    "success": true,
    "message": "Success",
    "data": [
        {
            "id": 1,
            "currency": "USDT",
            "balance": 46997.75,
            "availableBalance": 46997.75
        },
        {
            "id": 2,
            "currency": "ETH",
            "balance": 1.5,
            "availableBalance": 1.5
        },
        {
            "id": 3,
            "currency": "BTC",
            "balance": 0,
            "availableBalance": 0
        }
    ]
}
```

### 4. Get User's Trading History
```
GET /api/trades

Response:
{
    "success": true,
    "message": "Success",
    "data": [
        {
            "id": 1,
            "symbol": "ETHUSDT",
            "type": "BUY",
            "quantity": 1.5,
            "price": 2001.50,
            "totalAmount": 3002.25,
            "timestamp": "2024-01-15 10:30:45",
            "status": "COMPLETED"
        }
    ]
}
```

### 5. Get Trading History by Symbol
```
GET /api/trades/{symbol}

Parameters:
- symbol: ETHUSDT or BTCUSDT

Response: (Same format as above, filtered by symbol)
```

### 6. Health Check
```
GET /api/health

Response:
{
    "success": true,
    "message": "Crypto Trading App is running",
    "data": null
}
```

## Requirements

- Java 17+
- Maven 3.6+

## Build

Using a local Maven installation:

```powershell
mvn -DskipTests package
```

Run

```powershell
mvn spring-boot:run
```

Try the sample endpoint:

```powershell
curl http://localhost:8080/api/hello
```
