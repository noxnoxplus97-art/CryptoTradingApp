// History Controller
tradingApp.controller('HistoryController', ['$scope', '$interval', 'APIService', function($scope, $interval, APIService) {
    $scope.trades = [];
    $scope.filteredTrades = [];
    $scope.symbols = ['All', 'ETHUSDT', 'BTCUSDT'];
    $scope.selectedSymbol = 'All';
    $scope.loading = true;
    $scope.error = null;

    $scope.sortBy = 'timestamp';
    $scope.sortReverse = true;

    $scope.loadTradeHistory = function() {
        $scope.loading = true;
        $scope.error = null;

        APIService.getTradeHistory().then(
            function(response) {
                if (response.data.success) {
                    $scope.trades = response.data.data;
                    $scope.filterTrades();
                } else {
                    $scope.error = response.data.message;
                }
                $scope.loading = false;
            },
            function(error) {
                $scope.error = 'Failed to load trade history: ' + (error.data ? error.data.message : error.statusText);
                $scope.loading = false;
            }
        );
    };

    $scope.filterTrades = function() {
        if ($scope.selectedSymbol === 'All') {
            $scope.filteredTrades = $scope.trades;
        } else {
            $scope.filteredTrades = $scope.trades.filter(function(trade) {
                return trade.symbol === $scope.selectedSymbol;
            });
        }
        $scope.sortTrades();
    };

    $scope.sortTrades = function() {
        $scope.filteredTrades.sort(function(a, b) {
            var aVal = a[$scope.sortBy];
            var bVal = b[$scope.sortBy];

            if ($scope.sortBy === 'timestamp') {
                aVal = new Date(aVal);
                bVal = new Date(bVal);
            } else {
                aVal = parseFloat(aVal);
                bVal = parseFloat(bVal);
            }

            if ($scope.sortReverse) {
                return bVal - aVal;
            } else {
                return aVal - bVal;
            }
        });
    };

    $scope.setSortBy = function(field) {
        if ($scope.sortBy === field) {
            $scope.sortReverse = !$scope.sortReverse;
        } else {
            $scope.sortBy = field;
            $scope.sortReverse = true;
        }
        $scope.sortTrades();
    };

    $scope.getTradeTypeClass = function(type) {
        return type === 'BUY' ? 'text-success' : 'text-danger';
    };

    $scope.getTradeTypeIcon = function(type) {
        return type === 'BUY' ? 'fa-arrow-up' : 'fa-arrow-down';
    };

    $scope.getTotalTradeVolume = function() {
        var total = 0;
        $scope.filteredTrades.forEach(function(trade) {
            total += parseFloat(trade.totalAmount);
        });
        return total.toFixed(2);
    };

    $scope.getTotalBuyVolume = function() {
        var total = 0;
        $scope.filteredTrades.forEach(function(trade) {
            if (trade.tradeType === 'BUY') {
                total += parseFloat(trade.totalAmount);
            }
        });
        return total.toFixed(2);
    };

    $scope.getTotalSellVolume = function() {
        var total = 0;
        $scope.filteredTrades.forEach(function(trade) {
            if (trade.tradeType === 'SELL') {
                total += parseFloat(trade.totalAmount);
            }
        });
        return total.toFixed(2);
    };

    // Load data on controller init
    $scope.loadTradeHistory();

    // Refresh every 10 seconds
    var refreshInterval = $interval(function() {
        $scope.loadTradeHistory();
    }, 10000);

    $scope.$watch('selectedSymbol', function() {
        $scope.filterTrades();
    });

    $scope.$on('$destroy', function() {
        if (refreshInterval) {
            $interval.cancel(refreshInterval);
        }
    });
}]);
