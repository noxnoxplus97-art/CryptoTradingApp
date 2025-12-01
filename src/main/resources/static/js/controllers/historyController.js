// History Controller
tradingApp.controller('HistoryController', ['$scope', '$interval', 'APIService', function($scope, $interval, APIService) {
    $scope.trades = [];
    $scope.filteredTrades = [];
    $scope.symbols = ['All', 'ETHUSDT', 'BTCUSDT'];
    $scope.selectedSymbol = 'All';
    $scope.loading = true;
    $scope.error = null;

    // Use names expected by the view
    $scope.sortColumn = 'timestamp';
    $scope.reverseSort = true;
    $scope.stats = {
        buyCount: 0,
        sellCount: 0,
        buyVolume: 0.0,
        sellVolume: 0.0,
        totalVolume: 0.0
    };

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
        // Compute stats
        var buyCount = 0, sellCount = 0, buyVolume = 0.0, sellVolume = 0.0, totalVolume = 0.0;
        $scope.filteredTrades.forEach(function(trade) {
            var amt = parseFloat(trade.totalAmount || 0);
            totalVolume += amt;
            if (trade.type === 'BUY') {
                buyCount++; buyVolume += amt;
            } else if (trade.type === 'SELL') {
                sellCount++; sellVolume += amt;
            }
        });
        $scope.stats.buyCount = buyCount;
        $scope.stats.sellCount = sellCount;
        $scope.stats.buyVolume = buyVolume;
        $scope.stats.sellVolume = sellVolume;
        $scope.stats.totalVolume = totalVolume;

        $scope.lastUpdate = new Date();

        $scope.sortTrades();
    };

    $scope.sortTrades = function() {
        $scope.filteredTrades.sort(function(a, b) {
            var aVal = a[$scope.sortColumn];
            var bVal = b[$scope.sortColumn];

            if ($scope.sortColumn === 'timestamp') {
                aVal = new Date(aVal);
                bVal = new Date(bVal);
                return $scope.reverseSort ? bVal - aVal : aVal - bVal;
            }

            aVal = parseFloat(aVal || 0);
            bVal = parseFloat(bVal || 0);

            return $scope.reverseSort ? bVal - aVal : aVal - bVal;
        });
    };

    $scope.setSortColumn = function(field) {
        if ($scope.sortColumn === field) {
            $scope.reverseSort = !$scope.reverseSort;
        } else {
            $scope.sortColumn = field;
            $scope.reverseSort = true;
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
        return $scope.stats.totalVolume.toFixed(2);
    };

    $scope.getTotalBuyVolume = function() {
        return $scope.stats.buyVolume.toFixed(2);
    };

    $scope.getTotalSellVolume = function() {
        return $scope.stats.sellVolume.toFixed(2);
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
