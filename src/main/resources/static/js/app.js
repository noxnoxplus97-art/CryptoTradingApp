// Main AngularJS Application
var tradingApp = angular.module('tradingApp', ['ngRoute']);

// Configure routes
tradingApp.config(['$routeProvider', function($routeProvider) {
    $routeProvider
        .when('/login', {
            templateUrl: 'views/login.html',
            controller: 'LoginController'
        })
        .when('/dashboard', {
            templateUrl: 'views/dashboard.html',
            controller: 'DashboardController'
        })
        .when('/trade', {
            templateUrl: 'views/trade.html',
            controller: 'TradeController'
        })
        .when('/history', {
            templateUrl: 'views/history.html',
            controller: 'HistoryController'
        })
        .when('/account', {
            templateUrl: 'views/account.html',
            controller: 'AccountController'
        })
        .otherwise({
            redirectTo: '/login'
        });
}]);

// Interceptor for API requests
tradingApp.factory('APIInterceptor', ['$q', '$location', function($q, $location) {
    return {
        request: function(config) {
            config.headers = config.headers || {};
            return config;
        },
        response: function(response) {
            return response;
        },
        responseError: function(response) {
            if (response.status === 401) {
                $location.path('/login');
            }
            return $q.reject(response);
        }
    };
}]);

tradingApp.config(['$httpProvider', function($httpProvider) {
    $httpProvider.interceptors.push('APIInterceptor');
}]);

// Route change handler for auth
tradingApp.run(['$rootScope', '$location', 'AuthService', function($rootScope, $location, AuthService) {
    var protectedRoutes = ['/dashboard', '/trade', '/history', '/account'];
    
    $rootScope.$on('$routeChangeStart', function(event, next, current) {
        if (!next.$$route) return; // Skip if no route
        
        var routePath = next.$$route.originalPath;
        var isProtected = protectedRoutes.indexOf(routePath) !== -1;
        
        if (isProtected) {
            // Check auth directly from localStorage to avoid stale state
            var authFlag = localStorage.getItem('isAuthenticated');
            var isAuth = authFlag === 'true';
            
            console.debug('[RouteGuard] Route:', routePath, 'Protected:', isProtected, 'Auth:', isAuth, 'LocalStorage:', authFlag);
            
            if (!isAuth) {
                console.warn('[RouteGuard] BLOCKING route - not authenticated');
                event.preventDefault();
                $location.path('/login');
            } else {
                console.debug('[RouteGuard] ALLOWING route - authenticated');
            }
        }
    });
}]);
