// Authentication Service
tradingApp.service('AuthService', ['$q', '$location', function($q, $location) {
    var currentUser = null;
    var isAuthenticated = false;

    function loadFromStorage() {
        var authFlag = localStorage.getItem('isAuthenticated');
        if (authFlag === 'true') {
            isAuthenticated = true;
            try {
                var s = localStorage.getItem('currentUser');
                currentUser = s ? JSON.parse(s) : null;
            } catch (e) {
                currentUser = null;
            }
        } else {
            isAuthenticated = false;
            currentUser = null;
        }
    }

    return {
        login: function(user) {
            currentUser = user;
            isAuthenticated = true;
            localStorage.setItem('currentUser', JSON.stringify(user));
            localStorage.setItem('isAuthenticated', 'true');
        },

        logout: function() {
            currentUser = null;
            isAuthenticated = false;
            localStorage.removeItem('currentUser');
            localStorage.removeItem('isAuthenticated');
            $location.path('/login');
        },

        isLoggedIn: function() {
            loadFromStorage();
            return !!isAuthenticated;
        },

        getCurrentUser: function() {
            if (!currentUser) loadFromStorage();
            return currentUser;
        },

        requireLogin: function() {
            // Read directly from storage to avoid stale in-memory state
            var authFlag = localStorage.getItem('isAuthenticated');
            if (authFlag === 'true') {
                try { currentUser = JSON.parse(localStorage.getItem('currentUser')); } catch (e) { currentUser = null; }
                isAuthenticated = true;
                console.debug('[AuthService] requireLogin: authenticated, user=', currentUser);
                return $q.when(currentUser);
            } else {
                // Not authenticated: redirect to login and reject the resolve
                console.debug('[AuthService] requireLogin: not authenticated, redirecting to /login');
                $location.path('/login');
                return $q.reject('Not logged in');
            }
        }
    };
}]);
