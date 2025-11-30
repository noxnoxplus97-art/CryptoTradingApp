// Dashboard Controller
tradingApp.controller('DashboardController', ['$scope', '$interval', '$rootScope', 'APIService', function($scope, $interval, $rootScope, APIService) {
    $scope.wallet = { holdings: [] };
    $scope.latestPrices = {};
    $scope.error = null;
    $scope.lastUpdate = new Date();
    $scope.refreshing = false;
    $scope.totalWalletValue = 0;

    // Get price for a symbol
    $scope.getPrice = function(symbol) {
        // USDT is a stablecoin, always 1:1 with USD
        if (symbol === 'USDT') return 1;
        
        // Try to find price by symbol + USDT suffix (e.g., ETH -> ETHUSDT)
        var priceKey = symbol + 'USDT';
        if ($scope.latestPrices[priceKey]) {
            return $scope.latestPrices[priceKey].askPrice || 0;
        }
        
        // Fallback to direct symbol lookup
        return $scope.latestPrices[symbol] ? $scope.latestPrices[symbol].askPrice : 0;
    };

    // Load wallet data
    $scope.loadWallet = function() {
        APIService.getWalletBalance().then(function(response) {
            if (response.data.success) {
                var wallets = response.data.data || [];
                // Transform backend wallet format to frontend format
                $scope.wallet.holdings = wallets.map(function(w) {
                    return {
                        symbol: w.currency,
                        totalBalance: parseFloat(w.balance) || 0,
                        availableBalance: parseFloat(w.availableBalance) || 0
                    };
                });
                calculateTotalValue();
            }
        }, function(error) {
            $scope.error = error.data?.message || 'Failed to load wallet';
        });
    };

    // Load prices
    $scope.loadPrices = function() {
        var symbols = ['ETHUSDT', 'BTCUSDT'];
        symbols.forEach(function(symbol) {
            APIService.getLatestPrice(symbol).then(function(response) {
                if (response.data.success) {
                    var priceData = response.data.data;
                    // Store with both field name variants for compatibility
                    $scope.latestPrices[symbol] = {
                        symbol: priceData.symbol,
                        bid: parseFloat(priceData.bidPrice) || 0,
                        ask: parseFloat(priceData.askPrice) || 0,
                        bidPrice: parseFloat(priceData.bidPrice) || 0,
                        askPrice: parseFloat(priceData.askPrice) || 0
                    };
                    $rootScope.priceMap = $scope.latestPrices;
                    calculateTotalValue();
                }
            }, function(error) {
                console.error('Error loading price for ' + symbol + ':', error);
            });
        });
    };

    // Calculate total wallet value
    function calculateTotalValue() {
        $scope.totalWalletValue = 0;
        if ($scope.wallet.holdings && $scope.wallet.holdings.length > 0) {
            $scope.wallet.holdings.forEach(function(holding) {
                // For USDT, use available balance (since it's the stablecoin cash)
                var balance = (holding.symbol === 'USDT') ? holding.availableBalance : holding.totalBalance;
                var price = $scope.getPrice(holding.symbol) || 0;
                $scope.totalWalletValue += (balance * price);
            });
        }
    }

    // Refresh wallet
    $scope.refreshWallet = function() {
        $scope.refreshing = true;
        $scope.loadWallet();
        $scope.loadPrices();
        setTimeout(function() {
            $scope.$apply(function() {
                $scope.refreshing = false;
            });
        }, 500);
    };

    // Auto-refresh every 10 seconds
    var autoRefresh = $interval(function() {
        $scope.loadWallet();
        $scope.loadPrices();
        $scope.lastUpdate = new Date();
    }, 10000);

    // Cleanup on destroy
    $scope.$on('$destroy', function() {
        if (autoRefresh) $interval.cancel(autoRefresh);
    });

    // Initial load
    $scope.loadWallet();
    $scope.loadPrices();
}]);
