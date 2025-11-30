# Quick Start Guide - Crypto Trading Portal

## 🚀 5-Minute Setup

### Step 1: Navigate to Project
```powershell
cd c:\Users\Pooh.PoohWorld\TradingApp
```

### Step 2: Build & Run
```powershell
mvn spring-boot:run
```

### Step 3: Open in Browser
```
http://localhost:8080
```

### Step 4: Login
- **Username**: `testuser`
- **Password**: `password`

---

## 📱 Features Overview

### Dashboard
- View wallet balance and holdings
- See real-time cryptocurrency prices
- Auto-updates every 10 seconds

### Trading
- Buy/Sell cryptocurrencies (ETH/USDT, BTC/USDT)
- Real-time price display
- Total amount calculation
- Available balance verification

### History
- View all past trades
- Filter by symbol
- Sort by any column
- View trading statistics

### Account
- Detailed wallet breakdown
- Portfolio distribution visualization
- Trading activity summary
- Total asset value calculation

---

## 🔧 Project Contents

### ✅ Completed Components

#### Backend (Java Spring Boot)
- ✓ 4 JPA Entities (User, Wallet, Trade, CryptoPrice)
- ✓ 4 Spring Data Repositories
- ✓ 3 Service Classes with business logic
- ✓ REST Controller with 6 API endpoints
- ✓ H2 In-Memory Database
- ✓ Price aggregation scheduler (10-second intervals)
- ✓ Configuration classes
- ✓ Data initializer with demo data

#### Frontend (AngularJS)
- ✓ Single-page application (SPA) with routing
- ✓ 5 HTML views (login, dashboard, trade, history, account)
- ✓ 6 AngularJS controllers with full business logic
- ✓ 2 Service layers (API + Authentication)
- ✓ Bootstrap 3 responsive design
- ✓ Font Awesome icons
- ✓ Custom CSS styling
- ✓ Session/localStorage persistence

---

## 🎯 API Endpoints

### Health Check
```
GET /api/health
```

### Prices
```
GET /api/price/{symbol}     # Get current price (ETHUSDT, BTCUSDT)
```

### Trading
```
POST /api/trade             # Execute buy/sell order
GET /api/trades             # Get all trades
GET /api/trades/{symbol}    # Get trades by symbol
```

### Wallet
```
GET /api/wallet             # Get wallet balance
```

---

## 💾 Initial Data

**Demo Account Created on Startup:**
```
Username: testuser
Password: password
```

**Initial Wallet:**
- 50,000 USDT
- 0 ETH
- 0 BTC

---

## 📊 Database

- **Type**: H2 In-Memory Database
- **URL**: `jdbc:h2:mem:tradingdb`
- **Console**: `http://localhost:8080/h2-console`
- **Data Persistence**: Lost on application restart (in-memory)

---

## 🔌 External API Integration

**Price Sources:**
- Binance API
- Huobi API

**Update Frequency**: Every 10 seconds

**Symbols Supported:**
- ETHUSDT (Ethereum)
- BTCUSDT (Bitcoin)

---

## 📝 File Structure

```
src/main/resources/
├── static/
│   ├── index.html                    # Main entry point
│   ├── css/
│   │   └── style.css                 # All styling
│   ├── js/
│   │   ├── app.js                    # AngularJS config & routing
│   │   ├── controllers/
│   │   │   ├── mainController.js     # Navbar logic
│   │   │   ├── loginController.js    # Login form
│   │   │   ├── dashboardController.js # Wallet & prices
│   │   │   ├── tradeController.js    # Buy/sell trades
│   │   │   ├── historyController.js  # Trade history
│   │   │   └── accountController.js  # Account details
│   │   └── services/
│   │       ├── apiService.js         # Backend API calls
│   │       └── authService.js        # Authentication
│   └── views/
│       ├── login.html
│       ├── dashboard.html
│       ├── trade.html
│       ├── history.html
│       └── account.html
└── application.properties
```

---

## 🧪 Test Scenarios

### Test 1: Login
1. Navigate to `http://localhost:8080`
2. Enter: `testuser` / `password`
3. Should redirect to dashboard

### Test 2: Execute Trade
1. Go to Trading page
2. Select BUY and ETHUSDT
3. Enter quantity (e.g., 1.0)
4. Should show calculated amount
5. Click BUY button
6. Should see success message

### Test 3: View History
1. Go to History page
2. Should see all trades
3. Click column header to sort
4. Select symbol filter
5. Verify statistics update

### Test 4: Check Account
1. Go to Account page
2. Should show wallet breakdown
3. Should show portfolio distribution
4. Should show trading statistics

---

## ⚙️ Configuration Files

### `application.properties`
```properties
spring.application.name=trading-app
spring.jpa.hibernate.ddl-auto=create-drop
spring.datasource.url=jdbc:h2:mem:tradingdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;INIT=CREATE SCHEMA IF NOT EXISTS public
spring.h2.console.enabled=true
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

---

## 🆘 Troubleshooting

### Port 8080 Already in Use
```powershell
# Find process using port 8080
Get-Process | Where-Object {$_.ProcessName -eq "java"}

# Kill Java process
taskkill /PID <PID> /F
```

### Maven Build Issues
```powershell
# Clean and rebuild
mvn clean install -U
```

### Frontend Not Loading
1. Check console for JavaScript errors (F12)
2. Verify all resources load (Network tab)
3. Clear browser cache (Ctrl+Shift+Del)
4. Check backend is running on port 8080

### Can't Login
1. Backend must be running on `localhost:8080`
2. Use credentials: `testuser` / `password`
3. Check browser console for API errors
4. Verify session storage is enabled

---

## 📚 Technology Stack Summary

| Component | Technology | Version |
|-----------|-----------|---------|
| Backend | Spring Boot | 3.2.2 |
| Language | Java | 17 |
| Build | Maven | 3.8+ |
| Database | H2 | In-Memory |
| ORM | Hibernate | 6.4.4.Final |
| Frontend | AngularJS | 1.6.9 |
| CSS | Bootstrap | 3.3.7 |
| Icons | Font Awesome | 4.7.0 |

---

## ✨ Key Features

- ✅ Real-time cryptocurrency prices
- ✅ Buy/Sell trading interface
- ✅ Trade history with filtering/sorting
- ✅ Portfolio management
- ✅ Responsive design
- ✅ Auto-refresh every 10 seconds
- ✅ Session persistence
- ✅ Form validation
- ✅ Error handling
- ✅ Statistics dashboard

---

## 📞 Support

For detailed documentation, see `FRONTEND_README.md`

For API documentation, see `API_TESTING_GUIDE.md`

---

**Status**: ✅ Ready to Use
**Last Updated**: 2025-11-30
