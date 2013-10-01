'use strict';

var styxControllers = angular.module('styx.controllers', ['styx.services']);

styxControllers.controller('StyxController', function ($scope, $route, notificationChannel, authToken) {

    $scope.isInRole = function (user, expectedRole) {
        if(!$scope.root){
            return false;
        }
        var isInRole = false;
        angular.forEach(user.roles, function (role, roleIndex) {
            if (!isInRole) {
                if (role === expectedRole) {
                    isInRole = true;
                }
            }
        });
        return isInRole;
    }

    $scope.clearStatus = function () {
        $scope.status = null;
    }

    notificationChannel.onStatusChange($scope, function (status) {
        $scope.status = status;
    });

    $scope.cancelEdit = function () {
        $route.reload();
    }

    $scope.logout = function () {
        authToken.clear();
        $scope.root = null;
        $route.reload();
    }
    $scope.routeToAdministration = function () {
        $location.path("/admin");
    }
    $scope.isAdmin = false;
    notificationChannel.onRootUpdated($scope, function (response) {
        $scope.root = response.root;
    });
});

styxControllers.controller('SpaceUsersController', function ($scope, $location, notificationChannel, $routeParams) {
    $scope.selectedSpaceId = $routeParams.spaceId;
    $scope.editUser = function(user){
        var editingUser = user;
        if($scope.isInRole(user, 'SPACE_MANAGER')){
            editingUser.isManager = true;
        }
        if($scope.isInRole(user, 'DEVELOPER')){
            editingUser.isDeveloper = true;
        }
        if($scope.isInRole(user, 'SPACE_AUDITOR')){
            editingUser.isAuditor = true;
        }
        $scope.editingUser = editingUser;
    }
    $scope.changeSpace = function(newSpaceId, currentSpaceId){
        if(newSpaceId !== currentSpaceId){
            $location.path("/org/" + $scope.selectedOrgId + "/" + newSpaceId + "/users")
        }
    }
    if (!$scope.root) {
        $scope.loading = true;
        if (!$routeParams.organizationId) {
            notificationChannel.updateRoot();
        } else {
            notificationChannel.updateRoot($routeParams.organizationId);
        }
    }
    notificationChannel.onRootUpdated($scope, function (response) {
        $scope.selectedSpaceId = $scope.root.organization.spaces[0].id;
        $scope.loading = false;
    });
});

styxControllers.controller('OrganizationUsersController', function ($scope, $route, $location, notificationChannel, apiServices, $routeParams) {
    $scope.loading = false;
    $scope.editUser = function(user){
        var editingUser = user;
        if($scope.isInRole(user, 'MANAGER')){
            editingUser.isManager = true;
        }
        if($scope.isInRole(user, 'BILLING_MANAGER')){
            editingUser.isBillingManager = true;
        }
        if($scope.isInRole(user, 'AUDITOR')){
            editingUser.isAuditor = true;
        }
        $scope.editingUser = editingUser;
    }
    $scope.changeOrganization = function(){
        if($scope.selectedOrgId !== $scope.root.organization.id){
            $location.path("/org/" + $scope.selectedOrgId + "/users")
        }
    }
    $scope.updateOrganization = function(orgId, user){
        $scope.loading = true;
        user.roles = []
        if(user.isManager){
            user.roles.push("MANAGER")
        }
        if(user.isAuditor){
            user.roles.push("AUDITOR")
        }
        if(user.isBillingManager){
            user.roles.push("BILLING_MANAGER")
        }
        apiServices.updateOrganizationUser(orgId, user).
        success(function(data, status, headers, config) {
            notificationChannel.changeStatus(200, "Successfully updated " + user.username + ".")
            $route.reload()
        }).
        error(function(data, status, headers, config) {
            notificationChannel.changeStatus(500, "Unable to successfully update " + user.username + ".")
            $route.reload()
        });
    }
    if (!$scope.root) {
        $scope.loading = true;
        if (!$routeParams.organizationId) {
            notificationChannel.updateRoot();
        } else {
            notificationChannel.updateRoot($routeParams.organizationId);
        }
    } else {
        if ($routeParams.organizationId !== $scope.root.organization.id) {
            notificationChannel.updateRoot($routeParams.organizationId);
        }else{
            $scope.selectedOrgId = $scope.root.organization.id;
        }
    }
    notificationChannel.onRootUpdated($scope, function (response) {
        $scope.selectedOrgId = $scope.root.organization.id;
        $scope.loading = false;
    });
});

styxControllers.controller('OrganizationController', function ($scope, $location, $routeParams, notificationChannel) {
    $scope.changeOrganization = function(){
        if($scope.selectedOrgId !== $scope.root.organization.id){
            $location.path("/org/" + $scope.selectedOrgId);
        }
    }
    $scope.loading = true;
    if (!$scope.root) {
        if (!$routeParams.organizationId) {
            notificationChannel.updateRoot();
        } else {
            notificationChannel.updateRoot($routeParams.organizationId);
        }
    } else {
        if ($routeParams.organizationId !== $scope.root.organization.id) {
            notificationChannel.updateRoot($routeParams.organizationId);
        }else{
            $scope.selectedOrgId = $scope.root.organization.id;
            $scope.loading = false;
        }
    }
    notificationChannel.onRootUpdated($scope, function (response) {
        $scope.selectedOrgId = response.root.organization.id;
        $scope.loading = false;
    });
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

styxControllers.controller('AdminController', function ($scope, $http, $route, $location, notificationChannel, authToken) {
    if(!$scope.root){
        notificationChannel.updateRoot();
    }
    $scope.loading = true;

    var appendQuotaNames = function (admin) {
        angular.forEach(admin.organizations, function (organization, organizationIndex) {
            angular.forEach(admin.quotas, function (quota, quotaIndex) {
                if (quota.id === organization.quotaId) {
                    organization.quotaName = quota.name;
                }
            });
        });
    }

    $scope.createQuota = function (quota) {
        $scope.loading = true;
        if (quota.name.length > 0) {
            var config = {
                method: 'POST',
                url: "api/quotas",
                headers: {
                    'Accept': 'application/json',
                    'Authorization': authToken.getToken(),
                    'Content-Type': 'application/json'},
                data: JSON.stringify(quota)
            }
            var promise = $http(config);
            promise.success(function (response, status, headers) {
                notificationChannel.changeStatus(200, "New quota " + quota.name + " has been added.")
                $route.reload();
            });
            promise.error(function (response, status, headers) {
                notificationChannel.changeStatus(500, "New quota " + quota.name + " could not be added.");
                $route.reload();
            });
        }
    }

    $scope.deleteQuota = function (id, name) {
        $scope.loading = true;
        var config = {
            method: 'DELETE',
            url: "api/quotas/" + id,
            headers: {
                'Accept': 'application/json',
                'Authorization': authToken.getToken(),
                'Content-Type': 'application/json'}
        }
        var promise = $http(config);
        promise.success(function (response, status, headers) {
            notificationChannel.changeStatus(200, "Quota " + name + " has been successfully deleted.");
            $route.reload();
        });
        promise.error(function (response, status, headers) {
            notificationChannel.changeStatus(500, "Unable to delete quota " + name + ".");
            $route.reload();
        });
    }

    $scope.editQuota = function (id) {
        $scope.editingQuota = id;
    }

    $scope.saveQuota = function (id) {
        $scope.loading = true;
        angular.forEach($scope.admin.quotas, function (quota, quotaIndex) {
            if (quota.id === id) {
                var config = {
                    method: 'PUT',
                    url: "api/quotas/" + id,
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json',
                        'Authorization': authToken.getToken()},
                    data: JSON.stringify(quota)
                }
                var promise = $http(config);
                promise.success(function (response, status, headers) {
                    notificationChannel.changeStatus(200, "Quota " + quota.name + " has been successfully updated.")
                    $route.reload();
                });
                promise.error(function (response, status, headers) {
                    notificationChannel.changeStatus(500, "Unable to update quota " + quota.name + ".")
                    $route.reload();
                });
            }
        });
    }

    $scope.editOrg = function (id) {
        $scope.editingOrg = id;
    }

    $scope.createOrg = function (org) {
        if (org.name.length > 0) {
            $scope.loading = true;
            var config = {
                method: 'POST',
                url: "api/organizations",
                headers: {
                    'Accept': 'application/json',
                    'Authorization': authToken.getToken(),
                    'Content-Type': 'application/json'},
                data: JSON.stringify(org)
            }
            var promise = $http(config);
            promise.success(function (response, status, headers) {
                notificationChannel.changeStatus(200, "New organization " + org.name + " has been added.");
                $route.reload();
            });
            promise.error(function (response, status, headers) {
                notificationChannel.changeStatus(500, "New organization " + org.name + " could not be added.");
                $route.reload();
            });
        }
    }

    $scope.deleteOrg = function (id, name) {
        $scope.loading = true;
        var config = {
            method: 'DELETE',
            url: "api/organizations/" + id,
            headers: {
                'Accept': 'application/json',
                'Authorization': authToken.getToken(),
                'Content-Type': 'application/json'}
        }
        var promise = $http(config);
        promise.success(function (response, status, headers) {
            notificationChannel.changeStatus(200, "Organization " + name + " has been successfully deleted.");
            $route.reload();
        });
        promise.error(function (response, status, headers) {
            notificationChannel.changeStatus(500, "Unable to delete organization " + name + ".");
            $route.reload();
        });
    }

    $scope.saveOrg = function (id, name, quotaId) {
        $scope.loading = true;
        var config = {
            method: 'PUT',
            url: "api/organizations/" + id,
            headers: {
                'Accept': 'application/json',
                'Authorization': authToken.getToken(),
                'Content-Type': 'application/json'},
            data: JSON.stringify({name: name, quotaId: quotaId})
        }
        var promise = $http(config);
        promise.success(function (response, status, headers) {
            notificationChannel.changeStatus(200, "Organization " + name + " has been successfully updated.");
            $route.reload();
        });
        promise.error(function (response, status, headers) {
            notificationChannel.changeStatus(500, "Unable to update organization " + name + ".");
            $route.reload();
        });
    }

    $scope.routeToUsers = function (id) {
        $location.path("/org/" + id + "/users")
    }

    $scope.newQuota = {name: "", services: 1, memoryLimit: 1024, nonBasicServicesAllowed: false, trialDbAllowed: false};
    $scope.newOrganization = {name: "", quota: ""};


    $scope.admin = {quotas: [], organizations: []};
    var config = {
        method: 'GET',
        url: "api/admin",
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            'Authorization': authToken.getToken()
        }
    }
    var promise = $http(config);
    promise.success(function (response, status, headers) {
        var admin = response;
        appendQuotaNames(admin);
        $scope.admin = admin;
        $scope.loading = false;
    });
    promise.error(function (response, status, headers) {

    });
});