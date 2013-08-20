'use strict';

var styxUserServices = angular.module('styx.user.services', ['styx.services']);

styxUserServices.factory('userManager', function (cloudfoundry, cache, $q, $http) {

    var userManager = {};

    var resourcePromise = function (endpoint, method, body) {
        var config = {
            method: method,
            url: endpoint,
            headers: {
                'Accept': 'application/json',
                'Authorization': 'bearer ' + cache.getUser().accessToken,
                'Content-Type': 'application/json'}
        }
        if (body !== null) {
            config.data = JSON.stringify(body);
        }
        return $http(config);
    }

    userManager.getUsers = function (organizationId) {
        return resourcePromise('api/organizations/' + organizationId, 'GET');
    }

    userManager.getAllUsers = function() {
        var deferred = $q.defer();
        cloudfoundry.getUsers().then(
            function(users, status, headers){
                var userNameFilter = '';
                angular.forEach(users.data.resources, function (user, userIndex) {
                    userNameFilter = userNameFilter + 'id eq \'' + user.metadata.guid + "'";
                    if (userIndex < users.data.resources.length - 1) {
                        userNameFilter = userNameFilter + ' or ';
                    }
                });
                cloudfoundry.getUserNames(userNameFilter).then(
                    function(userDetails, status, headers){
                        var filteredUsers = [];
                        angular.forEach(users.data.resources, function(cfUser, cfUserIndex){
                            angular.forEach(userDetails.data.resources, function(userDetail, userDetailIndex){
                                if(userDetail.id === cfUser.metadata.guid){
                                    filteredUsers.push({id:userDetail.id, userName:userDetail.userName})
                                }
                            });
                        });
                        deferred.resolve(filteredUsers);
                    },
                    function(reason, status, headers){
                        deferred.reject({reason:reason, status:status, headers:headers});
                    }
                );
            },
            function(reason, status, headers){
                deferred.reject({reason:reason, status:status, headers:headers});
            }
        );
        return deferred.promise;
    }

    userManager.setOrgUsers = function (organization) {
        var ids = [];
        angular.forEach(organization.users, function(user, userIndex){
            ids.push(user.id);
        });
        return cloudfoundry.updateOrganization(organization.id, {user_guids: ids});
    }

    userManager.setOrgManagers = function (organization) {
        var ids = [];
        angular.forEach(organization.users, function(user, userIndex){
            if(user.manager){
                ids.push(user.id);
            }
        });
        return cloudfoundry.updateOrganization(organization.id, {manager_guids: ids});
    }

    userManager.setOrgBillingManagers = function (organization) {
        var ids = [];
        angular.forEach(organization.users, function(user, userIndex){
            if(user.billingManager){
                ids.push(user.id);
            }
        });
        return cloudfoundry.updateOrganization(organization.id, {billing_manager_guids: ids});
    }

    userManager.setOrgAuditors = function (organization) {
        var ids = [];
        angular.forEach(organization.users, function(user, userIndex){
            if(user.auditor){
                ids.push(user.id);
            }
        });
        return cloudfoundry.updateOrganization(organization.id, {auditor_guids: ids});
    }

    userManager.setSpaceManagers = function (space) {
        var ids = [];
        angular.forEach(space.users, function(user, userIndex){
            if(user.manager){
                ids.push(user.id);
            }
        });
        return cloudfoundry.updateSpace(space.id, {manager_guids: ids});
    }

    userManager.setSpaceDevelopers = function (space) {
        var ids = [];
        angular.forEach(space.users, function(user, userIndex){
            if(user.developer){
                ids.push(user.id);
            }
        });
        return cloudfoundry.updateSpace(space.id, {developer_guids: ids});
    }

    userManager.setSpaceAuditors = function (space) {
        var ids = [];
        angular.forEach(space.users, function(user, userIndex){
            if(user.auditor){
                ids.push(user.id);
            }
        });
        return cloudfoundry.updateSpace(space.id, {auditor_guids: ids});
    }

    return userManager;

});