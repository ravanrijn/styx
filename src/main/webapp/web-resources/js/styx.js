'use strict';

var styx = angular.module('styx', ['angular-spin', 'ui.bootstrap', 'ui.state', 'styx.controllers', 'styx.services', 'styx.user.services']);

styx.config(function($stateProvider) {
    var navigation = {
        templateUrl: 'partials/navigation.html',
        controller: 'NavigationController'
    }
    var footer = {
        templateUrl: 'partials/footer.html',
        controller: 'FooterController'
    }
    $stateProvider.state('app-log', {
        url: '/app-log/:organizationId/:applicationId/:instanceId/:fileName',
        views: {
            'navigation': navigation,
            'footer': footer,
            'body': {
                templateUrl: 'partials/app-log.html',
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
                templateUrl: 'partials/app-settings.html',
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
                templateUrl: 'partials/app-spaces.html',
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
                templateUrl: 'partials/create-org.html',
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
                templateUrl: 'partials/create-space.html',
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
                templateUrl: 'partials/marketplace.html',
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
                templateUrl: 'partials/org-settings.html',
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
                templateUrl: 'partials/space-settings.html',
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
                templateUrl: 'partials/org-users.html',
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
                templateUrl: 'partials/users.html',
                controller: 'UsersController'
            }
        }
    });
    $stateProvider.state('userinfo', {
        url: '/userinfo/:organizationId',
        views: {
            'navigation': navigation,
            'footer': footer,
            'body': {
                templateUrl: 'partials/user-info.html',
                controller: 'UserInfoController'
            }
        }
    });
    $stateProvider.state('login', {
        url: '/login',
        views: {
            'navigation': navigation,
            'footer': footer,
            'body': {
                templateUrl: 'partials/login.html',
                controller: 'LoginController'
            }
        }
    });
    $stateProvider.state('register', {
        url: '/register',
        views: {
            'navigation': navigation,
            'footer': footer,
            'body': {
                templateUrl: 'partials/register.html',
                controller: 'RegisterController'
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
});