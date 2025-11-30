// Login Controller
tradingApp.controller('LoginController', ['$scope', '$location', 'AuthService', 'APIService', function($scope, $location, AuthService, APIService) {
    $scope.loginForm = {};
    $scope.error = null;
    $scope.loading = false;

    // If already logged in, redirect to dashboard
    if (AuthService.isLoggedIn()) {
        console.debug('[LoginController] Already logged in, redirecting to dashboard');
        $location.path('/dashboard');
    }

    $scope.login = function() {
        if (!$scope.loginForm.username || !$scope.loginForm.password) {
            $scope.error = 'Please enter username and password';
            return;
        }

        $scope.loading = true;
        $scope.error = null;

        // For demo purposes, we're using a simple authentication
        // The backend currently assumes user ID 1 (testuser)
        if ($scope.loginForm.username === 'testuser' && $scope.loginForm.password === 'password') {
            AuthService.login({
                username: $scope.loginForm.username,
                id: 1
            });
            $location.path('/dashboard');
        } else {
            $scope.error = 'Invalid username or password. Use testuser/password';
            $scope.loading = false;
        }
    };

    // Auto-fill for demo
    $scope.autoFill = function() {
        $scope.loginForm.username = 'testuser';
        $scope.loginForm.password = 'password';
    };
}]);
