'use strict';

var styxControllers = angular.module('styx.controllers', ['styx.services']);

styxControllers.controller('StyxController', function ($scope, $route, notificationChannel, authToken) {
    $scope.logout = function(){
        authToken.clear();
        $scope.root = null;
        $route.reload();
    }
    notificationChannel.onRootUpdated($scope, function(response){
        $scope.root = response.root;
    });
});

styxControllers.controller('OrganizationController', function ($scope, notificationChannel) {
    $scope.availablePlans = ["paid", "free"];
    $scope.changingOrganization = false;
    $scope.editingApplication = 0;
    $scope.editApplication = function(appId){
        $scope.editingApplication = appId;
    }
    $scope.updateApplication = function(appId){
        $scope.editingApplication = appId;
    }
    $scope.cancelEditApplication = function(){
        $scope.editingApplication = 0;
    }
    $scope.changeOrganization = function(){
        if(!$scope.editingOrganization && $scope.editingApplication === 0){
            $scope.changingOrganization = true;
        }
    }
    $scope.setOrganization = function(){
        $scope.changingOrganization = false;
    }
    $scope.mayEditOrganization = false;
    $scope.editingOrganization = false;
    $scope.cancelEditOrganization = function(){
        $scope.editingOrganization = false;
    };
    $scope.editOrganization = function(){
        $scope.editingOrganization = true;
    };
    $scope.updateOrganization = function(){
        $scope.editingOrganization = false;
    };
    notificationChannel.updateRoot();
    $scope.selectSpace = function(spaceName){
        $scope.selectedSpaceName = spaceName;
    }
    notificationChannel.onRootUpdated($scope, function(response){
        $scope.mayEditOrganization = $.grep(response.root.links, function(link){ return link.rel === 'updateOrganization'; }) !== [];
        if(response.root.selectedOrganization.spaces.length > 0){
            $scope.selectedSpaceName = response.root.selectedOrganization.spaces[0].name;
        }
        $scope.root = response.root;
    });
});

styxControllers.controller('LoginController', function ($scope, $location, notificationChannel) {
    $scope.login = function (userForm) {
        notificationChannel.login(userForm.email, userForm.password);
    }
    notificationChannel.onLoginFailure($scope, function(response){

    });
    notificationChannel.onSuccessfulLogin($scope, function(response){
        $location.path("/org");
    });
});