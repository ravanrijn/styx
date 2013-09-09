'use strict';

var styxControllers = angular.module('styx.controllers', ['styx.services']);

styxControllers.controller('StyxController', function ($scope, $route, notificationChannel, authToken) {
    $scope.logout = function () {
        authToken.clear();
        $scope.root = null;
        $route.reload();
    }
    $scope.routeToAdministration = function(){
        $location.path("/admin");
    }
    $scope.editingOrganization = false;
    $scope.cancelEditOrganization = function () {
        $scope.editingOrganization = false;
    };
    $scope.editOrganization = function () {
        $scope.editingOrganization = true;
    };
    $scope.updateOrganization = function () {
        $scope.editingOrganization = false;
    };
    $scope.isAdmin = false;
    notificationChannel.onRootUpdated($scope, function (response) {
        $scope.mayEditOrganization = $.grep(response.root.links, function (link) {return link.rel === 'updateOrganization';}) !== [];
        $scope.root = response.root;
    });
});

styxControllers.controller('UsersController', function ($scope, $location, notificationChannel) {
    $scope.routeToSpaces = function (organizationId) {
        $location.path("/org/" + organizationId);
    }
    if (!$scope.root) {
        notificationChannel.updateRoot();
    }
});

styxControllers.controller('OrganizationController', function ($scope, $location, $routeParams, notificationChannel) {
    $scope.routeToUsers = function (organizationId) {
        $location.path("/org/" + organizationId + "/users");
    }
    $scope.availablePlans = ["paid", "free"];
    $scope.editingApplication = 0;
    $scope.editApplication = function (appId) {
        $scope.editingApplication = appId;
    }
    $scope.updateApplication = function (appId) {
        $scope.editingApplication = appId;
    }
    $scope.cancelEditApplication = function () {
        $scope.editingApplication = 0;
    }
    if (!$scope.root) {
        if (!$routeParams.organizationId) {
            notificationChannel.updateRoot();
        } else {
            notificationChannel.updateRoot($routeParams.organizationId);
        }
    } else {
        if ($routeParams.organizationId !== $scope.root.selectedOrganization.id) {
            notificationChannel.updateRoot($routeParams.organizationId);
        }
    }
});

styxControllers.controller('LoginController', function ($scope, $location, notificationChannel) {
    $scope.login = function (userForm) {
        notificationChannel.login(userForm.email, userForm.password);
    }
    notificationChannel.onLoginFailure($scope, function (response) {

    });
    notificationChannel.onSuccessfulLogin($scope, function (response) {
        $location.path("/org");
    });
});

styxControllers.controller('AdminController', function ($scope, $http, authToken) {
    $scope.newPlan = {name:"", services:1, memoryLimit:1024, nonBasicServicesAllowed:false, trialDbAllowed:false};
    $scope.newOrganization = {name:"", plan:"6cfd32e0-2439-45b8-900f-8146a7e3daf5"};
    $scope.admin = {plans:[], organizations:[]};
    var config = {
        method: 'GET',
        url: "api/administration",
        headers: {
            'Accept': 'application/json',
            'Authorization': authToken.getToken(),
            'Content-Type': 'application/json'}
    }
    var promise = $http(config);
    promise.success(function (response, status, headers) {
        var admin = response;
        angular.forEach(admin.organizations, function (organization, organizationIndex) {
            angular.forEach(admin.plans, function (plan, planIndex) {
                if(plan.id === organization.quotaId){
                    organization.quotaName = plan.name;
                }
            });
        });
        $scope.admin = admin;
    });
    promise.error(function (response, status, headers) {

    });
});