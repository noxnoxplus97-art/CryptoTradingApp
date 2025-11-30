// API Service for backend communication
tradingApp.service('APIService', ['$http', '$q', function($http, $q) {
    var apiUrl = '/api';

    return {
        // Authentication
        login: function(username, password) {
            return $http.post(apiUrl + '/login', {
                username: username,
                password: password
            });
        },

        // Health check
        checkHealth: function() {
            return $http.get(apiUrl + '/health');
        },

        // Price endpoints
        getLatestPrice: function(symbol) {
            return $http.get(apiUrl + '/price/' + symbol);
        },

        // Wallet endpoints
        getWalletBalance: function() {
            return $http.get(apiUrl + '/wallet');
        },

        // Trade endpoints
        executeTrade: function(tradeRequest) {
            return $http.post(apiUrl + '/trade', tradeRequest);
        },

        getTradeHistory: function() {
            return $http.get(apiUrl + '/trades');
        },

        getTradeHistoryBySymbol: function(symbol) {
            return $http.get(apiUrl + '/trades/' + symbol);
        }
    };
}]);
