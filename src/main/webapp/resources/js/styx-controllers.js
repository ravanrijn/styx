'use strict';

var styxControllers = angular.module('styx.controllers', ['styx.services']);

styxControllers.controller('StyxController', function ($scope, notificationChannel) {
    notificationChannel.onRootUpdated($scope, function(response){
        $scope.root = response.root;
    });
});

styxControllers.controller('OrganizationController', function ($scope, notificationChannel) {
    $scope.availablePlans = ["paid", "free"];
    $scope.editingOrganization = false;
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