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
        $scope.mayEditOrganization = $.grep(response.root.links, function (link) {
            return link.rel === 'updateOrganization';
        }) !== [];
        $scope.root = response.root;
    });
});

styxControllers.controller('UsersController', function ($scope, $location, notificationChannel, $routeParams) {
    $scope.changeOrganization = function(){
        if($scope.selectedOrgId !== $scope.root.selectedOrganization.id){
            $location.path("/org/" + $scope.selectedOrgId + "/users")
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
        if ($routeParams.organizationId !== $scope.root.selectedOrganization.id) {
            notificationChannel.updateRoot($routeParams.organizationId);
        }else{
            $scope.selectedOrgId = $scope.root.selectedOrganization.id;
            $scope.loading = false;
        }
    }
    notificationChannel.onRootUpdated($scope, function (response) {
        $scope.selectedOrgId = $scope.root.selectedOrganization.id;
        $scope.loading = false;
    });
});

styxControllers.controller('OrganizationController', function ($scope, $rootScope, $location, $routeParams, notificationChannel) {
    $scope.changeOrganization = function(){
        if($scope.selectedOrgId !== $scope.root.selectedOrganization.id){
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
        if ($routeParams.organizationId !== $scope.root.selectedOrganization.id) {
            notificationChannel.updateRoot($routeParams.organizationId);
        }else{
            $scope.selectedOrgId = $scope.root.selectedOrganization.id;
            $scope.loading = false;
        }
    }
    notificationChannel.onRootUpdated($scope, function (response) {
        $scope.selectedOrgId = $scope.root.selectedOrganization.id;
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

    var appendPlanNames = function (admin) {
        angular.forEach(admin.organizations, function (organization, organizationIndex) {
            angular.forEach(admin.plans, function (plan, planIndex) {
                if (plan.id === organization.quotaId) {
                    organization.quotaName = plan.name;
                }
            });
        });
    }

    $scope.createPlan = function (plan) {
        $scope.loading = true;
        if (plan.name.length > 0) {
            plan.id = "";
            var config = {
                method: 'POST',
                url: "api/plans",
                headers: {
                    'Accept': 'application/json',
                    'Authorization': authToken.getToken(),
                    'Content-Type': 'application/json'},
                data: JSON.stringify(plan)
            }
            var promise = $http(config);
            promise.success(function (response, status, headers) {
                notificationChannel.changeStatus(200, "New plan " + plan.name + " has been added.")
                $route.reload();
            });
            promise.error(function (response, status, headers) {
                notificationChannel.changeStatus(500, "New plan " + plan.name + " could not be added.");
                $route.reload();
            });
        }
    }

    $scope.deletePlan = function (id, name) {
        $scope.loading = true;
        var config = {
            method: 'DELETE',
            url: "api/plans/" + id,
            headers: {
                'Accept': 'application/json',
                'Authorization': authToken.getToken(),
                'Content-Type': 'application/json'}
        }
        var promise = $http(config);
        promise.success(function (response, status, headers) {
            notificationChannel.changeStatus(200, "Plan " + name + " has been successfully deleted.");
            $route.reload();
        });
        promise.error(function (response, status, headers) {
            notificationChannel.changeStatus(500, "Unable to delete plan " + name + ".");
            $route.reload();
        });
    }

    $scope.editPlan = function (id) {
        $scope.editingPlan = id;
    }

    $scope.savePlan = function (id) {
        $scope.loading = true;
        angular.forEach($scope.admin.plans, function (plan, planIndex) {
            if (plan.id === id) {
                var config = {
                    method: 'PUT',
                    url: "api/plans/" + id,
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json',
                        'Authorization': authToken.getToken()},
                    data: JSON.stringify(plan)
                }
                var promise = $http(config);
                promise.success(function (response, status, headers) {
                    notificationChannel.changeStatus(200, "Plan " + plan.name + " has been successfully updated.")
                    $route.reload();
                });
                promise.error(function (response, status, headers) {
                    notificationChannel.changeStatus(500, "Unable to update plan " + plan.name + ".")
                    $route.reload();
                });
            }
        });
    }

    $scope.cancelEdit = function () {
        $route.reload();
    }

    $scope.editOrg = function (id) {
        $scope.editingOrg = id;
    }

    $scope.createOrg = function (org) {
        if (org.name.length > 0) {
            $scope.loading = true;
            org.id = "";
            org.quotaId = org.plan;
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
            data: JSON.stringify({id: id, name: name, quotaId: quotaId})
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

    $scope.newPlan = {name: "", services: 1, memoryLimit: 1024, nonBasicServicesAllowed: false, trialDbAllowed: false};
    $scope.newOrganization = {name: "", plan: ""};


    $scope.admin = {plans: [], organizations: []};
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
        appendPlanNames(admin);
        $scope.admin = admin;
        $scope.loading = false;
    });
    promise.error(function (response, status, headers) {

    });
});