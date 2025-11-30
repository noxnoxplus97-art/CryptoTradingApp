# Crypto Trading System - API Testing Guide

## Quick Start

### 1. Build and Run
```bash
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run

# Application will start on http://localhost:8080
```

### 2. Check Health
```bash
curl -X GET http://localhost:8080/api/health
```

## API Testing Examples

### Testing with cURL

#### 1. Health Check
```bash
curl -X GET http://localhost:8080/api/health
```

**Expected Response** (after ~10 seconds for price aggregation):
```json
{
  "success": true,
  "message": "Crypto Trading App is running",
  "data": null
}
```

#### 2. Get Latest Price for ETHUSDT
```bash
curl -X GET http://localhost:8080/api/price/ETHUSDT
```

**Expected Response** (after price aggregation runs):
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "symbol": "ETHUSDT",
    "bidPrice": 2345.67,
    "askPrice": 2346.78,
    "timestamp": "2024-01-15 10:30:45"
  }
}
```

#### 3. Get User's Wallet Balance
```bash
curl -X GET http://localhost:8080/api/wallet
```

**Expected Response**:
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "currency": "USDT",
      "balance": 50000.00000000,
      "availableBalance": 50000.00000000
    },
    {
      "id": 2,
      "currency": "ETH",
      "balance": 0.00000000,
      "availableBalance": 0.00000000
    },
    {
      "id": 3,
      "currency": "BTC",
      "balance": 0.00000000,
      "availableBalance": 0.00000000
    }
  ]
}
```

#### 4. Execute BUY Trade
```bash
curl -X POST http://localhost:8080/api/trade \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "ETHUSDT",
    "type": "BUY",
    "quantity": 1.5
  }'
```

**Expected Response** (successful trade):
```json
{
  "success": true,
  "message": "Trade executed successfully",
  "data": {
    "id": 1,
    "symbol": "ETHUSDT",
    "type": "BUY",
    "quantity": 1.50000000,
    "price": 2346.78,
    "totalAmount": 3520.17,
    "timestamp": "2024-01-15 10:30:45",
    "status": "COMPLETED"
  }
}
```

#### 5. Get Updated Wallet After Trade
```bash
curl -X GET http://localhost:8080/api/wallet
```

**Expected Response** (USDT decreased, ETH increased):
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "currency": "USDT",
      "balance": 46479.83000000,
      "availableBalance": 46479.83000000
    },
    {
      "id": 2,
      "currency": "ETH",
      "balance": 1.50000000,
      "availableBalance": 1.50000000
    },
    {
      "id": 3,
      "currency": "BTC",
      "balance": 0.00000000,
      "availableBalance": 0.00000000
    }
  ]
}
```

#### 6. Execute SELL Trade
```bash
curl -X POST http://localhost:8080/api/trade \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "ETHUSDT",
    "type": "SELL",
    "quantity": 0.5
  }'
```

**Expected Response**:
```json
{
  "success": true,
  "message": "Trade executed successfully",
  "data": {
    "id": 2,
    "symbol": "ETHUSDT",
    "type": "SELL",
    "quantity": 0.50000000,
    "price": 2345.67,
    "totalAmount": 1172.84,
    "timestamp": "2024-01-15 10:31:45",
    "status": "COMPLETED"
  }
}
```

#### 7. Get All Trading History
```bash
curl -X GET http://localhost:8080/api/trades
```

**Expected Response** (all trades):
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": 2,
      "symbol": "ETHUSDT",
      "type": "SELL",
      "quantity": 0.50000000,
      "price": 2345.67,
      "totalAmount": 1172.84,
      "timestamp": "2024-01-15 10:31:45",
      "status": "COMPLETED"
    },
    {
      "id": 1,
      "symbol": "ETHUSDT",
      "type": "BUY",
      "quantity": 1.50000000,
      "price": 2346.78,
      "totalAmount": 3520.17,
      "timestamp": "2024-01-15 10:30:45",
      "status": "COMPLETED"
    }
  ]
}
```

#### 8. Get Trading History for Specific Symbol
```bash
curl -X GET http://localhost:8080/api/trades/ETHUSDT
```

**Expected Response** (ETHUSDT trades only):
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": 2,
      "symbol": "ETHUSDT",
      "type": "SELL",
      "quantity": 0.50000000,
      "price": 2345.67,
      "totalAmount": 1172.84,
      "timestamp": "2024-01-15 10:31:45",
      "status": "COMPLETED"
    },
    {
      "id": 1,
      "symbol": "ETHUSDT",
      "type": "BUY",
      "quantity": 1.50000000,
      "price": 2346.78,
      "totalAmount": 3520.17,
      "timestamp": "2024-01-15 10:30:45",
      "status": "COMPLETED"
    }
  ]
}
```

## Testing with Postman

### Import Collection
You can create requests manually or use the following templates:

#### Collection Name: Crypto Trading System

**Request 1: Health Check**
- Method: GET
- URL: `http://localhost:8080/api/health`

**Request 2: Get Price**
- Method: GET
- URL: `http://localhost:8080/api/price/ETHUSDT`

**Request 3: Get Wallet**
- Method: GET
- URL: `http://localhost:8080/api/wallet`

**Request 4: Buy Trade**
- Method: POST
- URL: `http://localhost:8080/api/trade`
- Body (JSON):
```json
{
  "symbol": "ETHUSDT",
  "type": "BUY",
  "quantity": 1.5
}
```

**Request 5: Sell Trade**
- Method: POST
- URL: `http://localhost:8080/api/trade`
- Body (JSON):
```json
{
  "symbol": "ETHUSDT",
  "type": "SELL",
  "quantity": 0.5
}
```

**Request 6: Get Trade History**
- Method: GET
- URL: `http://localhost:8080/api/trades`

**Request 7: Get Trade History by Symbol**
- Method: GET
- URL: `http://localhost:8080/api/trades/ETHUSDT`

## Error Scenarios

### 1. Insufficient Balance
**Request**:
```bash
curl -X POST http://localhost:8080/api/trade \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "ETHUSDT",
    "type": "BUY",
    "quantity": 100000
  }'
```

**Response**:
```json
{
  "success": false,
  "message": "Insufficient USDT balance",
  "data": null
}
```

### 2. Invalid Symbol
**Request**:
```bash
curl -X POST http://localhost:8080/api/trade \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "INVALID",
    "type": "BUY",
    "quantity": 1
  }'
```

**Response**:
```json
{
  "success": false,
  "message": "Invalid trading symbol: INVALID",
  "data": null
}
```

### 3. Invalid Trade Type
**Request**:
```bash
curl -X POST http://localhost:8080/api/trade \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "ETHUSDT",
    "type": "INVALID",
    "quantity": 1
  }'
```

**Response**:
```json
{
  "success": false,
  "message": "Invalid trade type: INVALID",
  "data": null
}
```

### 4. Insufficient Crypto Balance for SELL
**Request** (sell when you have no ETH):
```bash
curl -X POST http://localhost:8080/api/trade \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "ETHUSDT",
    "type": "SELL",
    "quantity": 1
  }'
```

**Response**:
```json
{
  "success": false,
  "message": "Insufficient ETH balance",
  "data": null
}
```

### 5. No Price Data Available
**Request** (BTCUSDT before 10 seconds elapsed):
```bash
curl -X GET http://localhost:8080/api/price/BTCUSDT
```

**Response**:
```json
{
  "success": false,
  "message": "No price data available for symbol: BTCUSDT",
  "data": null
}
```

## Testing Workflow

### Complete Workflow Test Sequence

1. **Wait for price aggregation** (10 seconds after start)
2. **Check health**: Verify app is running
3. **Get initial wallet**: Confirm 50,000 USDT
4. **Get price**: Check latest ETHUSDT price
5. **Buy ETH**: Execute BUY trade with valid amount
6. **Check wallet**: Verify USDT decreased, ETH increased
7. **Get trade history**: Verify trade recorded
8. **Sell partial ETH**: Execute SELL trade
9. **Check wallet**: Verify balances updated correctly
10. **Get filtered history**: Verify both trades appear

## Database Console

Access H2 database console:
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:tradingdb`
- User: `sa`
- Password: (leave empty)

### Useful SQL Queries

```sql
-- View all users
SELECT * FROM USERS;

-- View all wallets
SELECT u.USERNAME, w.CURRENCY, w.BALANCE, w.AVAILABLE_BALANCE 
FROM WALLETS w 
JOIN USERS u ON w.USER_ID = u.ID;

-- View all trades
SELECT u.USERNAME, t.SYMBOL, t.TYPE, t.QUANTITY, t.PRICE, t.TOTAL_AMOUNT, t.TIMESTAMP 
FROM TRADES t 
JOIN USERS u ON t.USER_ID = u.ID 
ORDER BY t.TIMESTAMP DESC;

-- View recent prices
SELECT SYMBOL, BID_PRICE, ASK_PRICE, SOURCE, TIMESTAMP 
FROM CRYPTO_PRICES 
ORDER BY TIMESTAMP DESC 
LIMIT 10;

-- View price statistics
SELECT SYMBOL, SOURCE, COUNT(*) AS PRICE_UPDATES, 
       AVG(BID_PRICE) AS AVG_BID, AVG(ASK_PRICE) AS AVG_ASK
FROM CRYPTO_PRICES
GROUP BY SYMBOL, SOURCE;
```

## Performance Notes

- Price aggregation: Every 10 seconds
- Database: In-memory (fast queries)
- HTTP timeouts: 5s connect, 10s read
- Transaction isolation: Default (READ_COMMITTED)

## Support for Multiple Trades

Test multiple buy/sell cycles:

```bash
# Buy 1 ETH
curl -X POST http://localhost:8080/api/trade \
  -H "Content-Type: application/json" \
  -d '{"symbol": "ETHUSDT", "type": "BUY", "quantity": 1}'

# Buy 0.5 BTC
curl -X POST http://localhost:8080/api/trade \
  -H "Content-Type: application/json" \
  -d '{"symbol": "BTCUSDT", "type": "BUY", "quantity": 0.5}'

# Sell 0.25 BTC
curl -X POST http://localhost:8080/api/trade \
  -H "Content-Type: application/json" \
  -d '{"symbol": "BTCUSDT", "type": "SELL", "quantity": 0.25}'

# View complete history
curl -X GET http://localhost:8080/api/trades
```

All trades are recorded with full details for audit trail and analysis.
