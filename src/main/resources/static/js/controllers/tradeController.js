// Trade Controller
tradingApp.controller('TradeController', ['$scope', '$rootScope', 'APIService', function($scope, $rootScope, APIService) {
    $scope.tradeForm = {
        type: 'BUY',
        symbol: 'ETHUSDT',
        quantity: ''
    };

    $scope.currentPrice = null;
    $scope.availableBalance = null;
    $scope.totalAmount = 0;
    $scope.validationError = null;
    $scope.error = null;
    $scope.success = null;
    $scope.loading = false;
    $scope.recentTrades = [];

    // Load price for selected symbol
    $scope.loadPrice = function() {
        if (!$scope.tradeForm.symbol) return;

        APIService.getLatestPrice($scope.tradeForm.symbol).then(function(response) {
            if (response.data.success) {
                var priceData = response.data.data;
                $scope.currentPrice = {
                    symbol: priceData.symbol,
                    bid: parseFloat(priceData.bidPrice),
                    ask: parseFloat(priceData.askPrice),
                    bidPrice: parseFloat(priceData.bidPrice),
                    askPrice: parseFloat(priceData.askPrice)
                };
                $scope.error = null;
            } else {
                $scope.error = 'Price not available';
            }
        }, function(error) {
            $scope.error = 'Failed to load price';
        });

        // Load available balance
        loadAvailableBalance();
    };

    // Load available USDT balance
    function loadAvailableBalance() {
        APIService.getWalletBalance().then(function(response) {
            if (response.data.success) {
                var wallets = response.data.data || [];
                var usdtWallet = wallets.find(function(w) { return w.currency === 'USDT'; });
                $scope.availableBalance = usdtWallet ? parseFloat(usdtWallet.availableBalance) : 0;
            }
        });
    }

    // Calculate total amount
    $scope.calculateTotal = function() {
        if (!$scope.tradeForm.quantity || !$scope.currentPrice) {
            $scope.totalAmount = 0;
            $scope.validationError = null;
            return;
        }

        var quantity = parseFloat($scope.tradeForm.quantity);
        if (quantity <= 0) {
            $scope.validationError = 'Quantity must be greater than 0';
            $scope.totalAmount = 0;
            return;
        }

        var price = $scope.tradeForm.type === 'BUY' ? $scope.currentPrice.askPrice : $scope.currentPrice.bidPrice;
        $scope.totalAmount = quantity * price;

        // Validate balance for BUY orders
        if ($scope.tradeForm.type === 'BUY' && $scope.totalAmount > $scope.availableBalance) {
            $scope.validationError = 'Insufficient USDT balance';
        } else {
            $scope.validationError = null;
        }
    };

    // Execute trade
    $scope.executeTrade = function() {
        if ($scope.validationError) {
            $scope.error = $scope.validationError;
            return;
        }

        if (!$scope.tradeForm.symbol || !$scope.tradeForm.type || !$scope.tradeForm.quantity) {
            $scope.error = 'Please fill all fields';
            return;
        }

        $scope.loading = true;
        $scope.error = null;
        $scope.success = null;

        var tradeRequest = {
            symbol: $scope.tradeForm.symbol,
            type: $scope.tradeForm.type,
            quantity: parseFloat($scope.tradeForm.quantity)
        };

        APIService.executeTrade(tradeRequest).then(function(response) {
            if (response.data.success) {
                $scope.success = 'Trade executed successfully!';
                $scope.tradeForm.quantity = '';
                $scope.totalAmount = 0;
                // Reload wallets and history
                loadAvailableBalance();
                loadRecentTrades();
                // Reset price
                setTimeout(function() {
                    $scope.$apply(function() {
                        $scope.loadPrice();
                    });
                }, 500);
            } else {
                $scope.error = response.data.message || 'Trade execution failed';
            }
            $scope.loading = false;
        }, function(error) {
            $scope.error = error.data?.message || 'Failed to execute trade';
            $scope.loading = false;
        });
    };

    // Load recent trades
    function loadRecentTrades() {
        APIService.getTradeHistory().then(function(response) {
            if (response.data.success) {
                var trades = response.data.data || [];
                $scope.recentTrades = trades.slice(0, 5);
            }
        });
    }

    // Initialize
    $scope.loadPrice();
    loadAvailableBalance();
    loadRecentTrades();

    // Watch for symbol changes
    $scope.$watch('tradeForm.symbol', function() {
        $scope.loadPrice();
    });

    // Watch for quantity changes
    $scope.$watch('tradeForm.quantity', function() {
        $scope.calculateTotal();
    });

    // Watch for type changes
    $scope.$watch('tradeForm.type', function() {
        if ($scope.currentPrice) {
            $scope.calculateTotal();
        }
    });
}]);
