// Main Controller
tradingApp.controller('MainController', ['$scope', '$location', 'AuthService', function($scope, $location, AuthService) {
    $scope.isLoggedIn = AuthService.isLoggedIn();
    $scope.currentUser = AuthService.getCurrentUser() ? AuthService.getCurrentUser().username : '';

    $scope.$watch(function() {
        return AuthService.isLoggedIn();
    }, function(newVal) {
        $scope.isLoggedIn = newVal;
        if (newVal) {
            $scope.currentUser = AuthService.getCurrentUser().username;
        }
    });

    $scope.logout = function() {
        AuthService.logout();
    };
}]);
