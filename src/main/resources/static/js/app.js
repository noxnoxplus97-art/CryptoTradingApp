// Main AngularJS Application
var tradingApp = angular.module('tradingApp', ['ngRoute']);

// Configure routes
tradingApp.config(['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {
    // Use empty hashPrefix so routes use "#/path" instead of "#!/path" (avoid double-hash redirects)
    $locationProvider.hashPrefix('');

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
            // Use AuthService.requireLogin() to centralize auth check and avoid
            // relying on direct localStorage reads here (prevents stale logic).
            AuthService.requireLogin().then(function(user) {
                // Auth resolved; allow route
                console.debug('[RouteGuard] ALLOWING route - authenticated', user);
            }).catch(function() {
                // Not authenticated: prevent navigation (AuthService will redirect)
                console.warn('[RouteGuard] BLOCKING route - not authenticated');
                event.preventDefault();
            });
        }
    });
}]);
