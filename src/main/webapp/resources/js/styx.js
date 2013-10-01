'use strict';

var styx = angular.module('styx', ['ui.bootstrap', 'angular-spin', 'styx.services', 'styx.controllers']);

styx.config(['$routeProvider', '$httpProvider', function($routeProvider, $httpProvider) {
    $routeProvider.when("/org/:organizationId", {templateUrl: 'partials/organization.html',   controller: 'OrganizationController'});
    $routeProvider.when("/org", {templateUrl: 'partials/organization.html',   controller: 'OrganizationController'});
    $routeProvider.when("/org/:organizationId/users", {templateUrl: 'partials/org_users.html',   controller: 'OrganizationUsersController'});
    $routeProvider.when("/org/:organizationId/:spaceId/users", {templateUrl: 'partials/space_users.html',   controller: 'SpaceUsersController'});
    $routeProvider.when("/login", {templateUrl: 'partials/login.html',   controller: 'LoginController'});
    $routeProvider.when("/admin", {templateUrl: 'partials/admin.html',   controller: 'AdminController'});
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
    $rootScope.defaultSpinnerConfig = {
        lines: 17, // The number of lines to draw
        length: 14, // The length of each line
        width: 2, // The line thickness
        radius: 24, // The radius of the inner circle
        corners: 1, // Corner roundness (0..1)
        rotate: 0, // The rotation offset
        direction: 1, // 1: clockwise, -1: counterclockwise
        color: '#000', // #rgb or #rrggbb
        speed: 1.8, // Rounds per second
        trail: 58, // Afterglow percentage
        shadow: false, // Whether to render a shadow
        hwaccel: false, // Whether to use hardware acceleration
        className: 'spinner', // The CSS class to assign to the spinner
        zIndex: 2e9, // The z-index (defaults to 2000000000)
        top: '20px' // Top position relative to parent in px
//        left: '110px' // Left position relative to parent in px
    };
});