// Account Controller
tradingApp.controller('AccountController', ['$scope', '$interval', '$rootScope', 'APIService', function($scope, $interval, $rootScope, APIService) {
    $scope.wallet = {holdings: []};
    $scope.tradeStats = {total: 0, buy: 0, sell: 0};
    $scope.error = null;
    $scope.lastUpdate = new Date();
    $scope.currentUser = $rootScope.currentUser;

    // Get price for a symbol
    $scope.getPrice = function(symbol) {
        return $rootScope.priceMap && $rootScope.priceMap[symbol] ? $rootScope.priceMap[symbol].bid : 0;
    };

    // Get color for symbol
    $scope.getColorForSymbol = function(symbol) {
        if (symbol === 'ETHUSDT') return '#627ee7';
        if (symbol === 'BTCUSDT') return '#f7931a';
        return '#5cb85c';
    };

    // Get symbol name
    $scope.getSymbolName = function(symbol) {
        var names = {
            'ETHUSDT': 'Ethereum',
            'BTCUSDT': 'Bitcoin',
            'USDT': 'US Dollar Tether'
        };
        return names[symbol] || symbol;
    };

    // Load account details
    $scope.loadAccount = function() {
        APIService.getWalletBalance().then(function(response) {
            if (response.data.success) {
                var wallets = response.data.data || [];
                // Transform wallet data
                $scope.wallet.holdings = wallets.map(function(w) {
                    return {
                        symbol: w.currency,
                        totalBalance: parseFloat(w.balance),
                        availableBalance: parseFloat(w.availableBalance)
                    };
                });
                calculateTotalValues();
            }
        }, function(error) {
            $scope.error = error.data?.message || 'Failed to load wallet';
        });
    };

    // Load trade statistics
    $scope.loadTradeStats = function() {
        APIService.getTradeHistory().then(function(response) {
            if (response.data.success) {
                var trades = response.data.data || [];
                $scope.tradeStats.total = trades.length;
                $scope.tradeStats.buy = trades.filter(function(t) { return t.tradeType === 'BUY'; }).length;
                $scope.tradeStats.sell = trades.filter(function(t) { return t.tradeType === 'SELL'; }).length;
            }
        }, function(error) {
            console.error('Failed to load trade stats:', error);
        });
    };

    // Calculate total values
    function calculateTotalValues() {
        $scope.totalWalletValue = 0;
        $scope.totalAvailable = 0;

        if ($scope.wallet.holdings && $scope.wallet.holdings.length > 0) {
            $scope.wallet.holdings.forEach(function(holding) {
                var price = $scope.getPrice(holding.symbol) || 0;
                var usdValue = holding.totalBalance * price;
                $scope.totalWalletValue += usdValue;
                
                var availValue = holding.availableBalance * price;
                $scope.totalAvailable += availValue;
            });

            // Find largest position
            var largest = null;
            $scope.wallet.holdings.forEach(function(holding) {
                var usdValue = holding.totalBalance * ($scope.getPrice(holding.symbol) || 0);
                if (!largest || usdValue > largest.value) {
                    largest = {symbol: holding.symbol, value: usdValue};
                }
            });
            if (largest) {
                largest.percentage = (largest.value / $scope.totalWalletValue * 100);
            }
            $scope.largestPosition = largest || {};
        }
    }

    // Refresh account
    $scope.refreshAccount = function() {
        $scope.refreshing = true;
        $scope.loadAccount();
        $scope.loadTradeStats();
        setTimeout(function() {
            $scope.$apply(function() {
                $scope.refreshing = false;
            });
        }, 500);
    };

    // Auto-refresh every 10 seconds
    var autoRefresh = $interval(function() {
        $scope.loadAccount();
        $scope.lastUpdate = new Date();
    }, 10000);

    // Cleanup on destroy
    $scope.$on('$destroy', function() {
        if (autoRefresh) $interval.cancel(autoRefresh);
    });

    // Initial load
    $scope.loadAccount();
    $scope.loadTradeStats();
}]);
