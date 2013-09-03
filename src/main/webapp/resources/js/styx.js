'use strict';

var styx = angular.module('styx', ['styx.services', 'styx.controllers']);

styx.config(['$routeProvider', '$httpProvider', function($routeProvider, $httpProvider) {
    $routeProvider.when("/org/:organizationId", {templateUrl: 'partials/organization.html',   controller: 'OrganizationController'});
    $routeProvider.when("/org", {templateUrl: 'partials/organization.html',   controller: 'OrganizationController'});
    $routeProvider.when("/login", {templateUrl: 'partials/login.html',   controller: 'LoginController'});
    $routeProvider.otherwise({redirectTo: "/org"});
    function requestInterceptor($q,$log,$location) {
        function success(response) {
            return response;
        }
        function error(response) {
            var status = response.status;
            if (status === 401) {
                $location.path('/login')
            }
            else {
                return $q.reject(response);
            }
        }
        return function(promise) {
            return promise.then(success, error);
        }
    }
    $httpProvider.responseInterceptors.push(requestInterceptor);
}]);

styx.run(function($rootScope, $location, authToken){
    $rootScope.$on( "$routeChangeStart", function(event, next, current) {
        if(!authToken.getToken() && next.templateUrl !== "/login") {
            $location.path("/login");
        }
    });
});