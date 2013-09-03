'use strict';

var styxServices = angular.module('styx.services', ['LocalStorageModule']);

styxServices.factory('authToken', function ($http, localStorageService) {

    var authToken = {};

    authToken.setToken = function(token){
        localStorageService.add("authToken", token);
    }

    authToken.getToken = function(){
        return localStorageService.get("authToken");
    }

    authToken.clear = function(){
        localStorageService.clearAll();
    }

    return authToken;

});

styxServices.factory('notificationChannel', function ($rootScope, apiServices) {

    var ROOT_UPDATED = "_ROOT_UPDATED_";
    var USER_MANAGEMENT_UPDATED = "_USER_MANAGEMENT_UPDATED_";
    var LOADING = "_LOADING_";
    var LOADED = "_LOADED_";
    var ERROR = "_ERROR_";
    var LOGIN_SUCCESS = "_LOGIN_";
    var LOGIN_FAILURE

    var notificationChannel = {};

    notificationChannel.updateRoot = function(organizationId){
        apiServices.getRoot(organizationId).then(
            function (response, status, headers) {
                $rootScope.$broadcast(ROOT_UPDATED, {root: response.data, status: status, headers: headers});
            },
            function (response, status, headers) {
                $rootScope.$broadcast(ERROR, {response: response.data, status: status, headers: headers});
            }
        );
    }

    notificationChannel.onRootUpdated = function($scope, handler){
        $scope.$on(ROOT_UPDATED, function(event, args) {
            handler(args);
        });
    }

    notificationChannel.login = function(username, password){
        var tokenPromise = apiServices.getAuthToken(username, password);
        tokenPromise.success(function (response, status, headers) {
            $rootScope.$broadcast(LOGIN_SUCCESS, {root: response.data, status: status, headers: headers});
        });
        tokenPromise.error(function (response, status, headers) {
            $rootScope.$broadcast(ERROR, {root: response.data, status: status, headers: headers});
        });
    }

    notificationChannel.onSuccessfulLogin = function($scope, handler){
        $scope.$on(LOGIN_SUCCESS, function(event, args) {
            handler(args);
        });
    }

    notificationChannel.onLoginFailure = function($scope, handler){
        $scope.$on(LOGIN_FAILURE, function(event, args) {
            handler(args);
        });
    }

    notificationChannel.updateUserManagement = function(){

    }

    notificationChannel.onUserManagementUpdated = function($scope, handler){
        $scope.$on(USER_MANAGEMENT_UPDATED, function(event, args) {
            handler(args.item);
        });
    }

    return notificationChannel;

});

styxServices.factory('apiServices', function ($http, authToken) {

    var apiServices = {};

    apiServices.getAuthToken = function(username, password) {
        return $http({
            method: 'POST',
            url: 'api/access_token',
            headers: {'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8',
                'Accept': 'application/json;charset=utf-8'},
            data: $.param({'grant_type': 'password', 'username': username, 'password': password})
        });
    }

    apiServices.getRoot = function(organizationId){
        console.log(authToken.getToken());
        var url = "api/";
        if(organizationId){
            url = url + "/" + organizationId;
        }
        var config = {
            method: 'GET',
            url: url,
            headers: {
                'Accept': 'application/json',
                'Authorization': authToken.getToken(),
                'Content-Type': 'application/json'}
        }
        return $http(config);
    }

    apiServices.getUserManagement = function(){
        return null;
    }

    return apiServices;

});