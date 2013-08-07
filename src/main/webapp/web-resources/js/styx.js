'use strict';

var styx = angular.module('styx', ['angular-spin', 'ui.bootstrap', 'ui.state', 'styx.controllers', 'styx.services', 'styx.user.services']);

styx.config(function($stateProvider) {
    var navigation = {
        templateUrl: 'partials/${project.version}/navigation.html',
        controller: 'NavigationController'
    }
    var footer = {
        templateUrl: 'partials/${project.version}/footer.html',
        controller: 'FooterController'
    }
    $stateProvider.state('app-log', {
        url: '/app-log/:organizationId/:applicationId/:instanceId/:fileName',
        views: {
            'navigation': navigation,
            'footer': footer,
            'body': {
                templateUrl: 'partials/${project.version}/app-log.html',
                controller: 'AppLogController'
            }
        }
    });
    $stateProvider.state('app-settings', {
        url: '/app-settings/:organizationId/:applicationId',
        views: {
            'navigation': navigation,
            'footer': footer,
            'body': {
                templateUrl: 'partials/${project.version}/app-settings.html',
                controller: 'AppSettingsController'
            }
        }
    });
    $stateProvider.state('app-spaces', {
        url: '/app-spaces/:organizationId',
        views: {
            'navigation': navigation,
            'footer': footer,
            'body': {
                templateUrl: 'partials/${project.version}/app-spaces.html',
                controller: 'AppSpacesController'
            }
        }
    });
    $stateProvider.state('create-org', {
        url: '/create-org/:organizationId',
        views: {
            'navigation': navigation,
            'footer': footer,
            'body': {
                templateUrl: 'partials/${project.version}/create-org.html',
                controller: 'CreateOrganizationController'
            }
        }
    });
    $stateProvider.state('create-space', {
        url: '/create-space/:organizationId',
        views: {
            'navigation': navigation,
            'footer': footer,
            'body': {
                templateUrl: 'partials/${project.version}/create-space.html',
                controller: 'CreateSpaceController'
            }
        }
    });
    $stateProvider.state('marketplace', {
        url: '/marketplace/:organizationId',
        views: {
            'navigation': navigation,
            'footer': footer,
            'body': {
                templateUrl: 'partials/${project.version}/marketplace.html',
                controller: 'MarketplaceController'
            }
        }
    });
    $stateProvider.state('org-settings', {
        url: '/org-settings/:organizationId',
        views: {
            'navigation': navigation,
            'footer': footer,
            'body': {
                templateUrl: 'partials/${project.version}/org-settings.html',
                controller: 'OrganizationSettingsController'
            }
        }
    });
    $stateProvider.state('space-settings', {
        url: '/space-settings/:organizationId/:spaceId',
        views: {
            'navigation': navigation,
            'footer': footer,
            'body': {
                templateUrl: 'partials/${project.version}/space-settings.html',
                controller: 'SpaceSettingsController'
            }
        }
    });
    $stateProvider.state('newUsers', {
        url: '/organization/:organizationId/users',
        views: {
            'navigation': navigation,
            'footer': footer,
            'body': {
                templateUrl: 'partials/${project.version}/org-users.html',
                controller: 'OrgUsersController'
            }
        }
    });
    $stateProvider.state('users', {
        url: '/users/:organizationId',
        views: {
            'navigation': navigation,
            'footer': footer,
            'body': {
                templateUrl: 'partials/${project.version}/users.html',
                controller: 'UsersController'
            }
        }
    });
    $stateProvider.state('login', {
        url: '/login',
        views: {
            'navigation': navigation,
            'footer': footer,
            'body': {
                templateUrl: 'partials/${project.version}/login.html',
                controller: 'LoginController'
            }
        }
    });
});

styx.run(function($rootScope, $location, cloudfoundry){
    $rootScope.forceLogin = function(status){
        if(status === 401){
            cloudfoundry.logout();
            $location.path('/login')
        }
    }
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
        top: '50px' // Top position relative to parent in px
//        left: '110px' // Left position relative to parent in px
    };
    $rootScope.$on("$stateChangeStart", function(event, toState, toParams, fromState, fromParams) {
        if(!cloudfoundry.isAuthenticated()){
            if(toState.name != '/login'){
                $location.path('/login');
            }
        }
    });
});