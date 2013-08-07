'use strict';

var styxUserServices = angular.module('styx.user.services', ['styx.services']);

styxUserServices.factory('userManager', function (cloudfoundry, $q) {

    var userManager = {};

    userManager.getUsers = function (organizationId) {
        var deferred = $q.defer();
        var organizationDetailsPromise = cloudfoundry.getOrganizationDetails(organizationId, 2);
        organizationDetailsPromise.success(
            function (result, status, headers) {
                var organization = {id: result.metadata.guid, name: result.entity.name, users: [], spaces: []};
                angular.forEach(result.entity.users, function (cfUser, cfUserIndex) {
                    var orgUser = {id: cfUser.metadata.guid, admin: cfUser.entity.admin, active: cfUser.entity.active};
                    angular.forEach(result.entity.managers, function (orgManager, orgManagerIndex) {
                        if (orgManager.metadata.guid === orgUser.id) {
                            orgUser.isManager = true;
                        }
                    });
                    angular.forEach(result.entity.billing_managers, function (billingManager, billingManagerIndex) {
                        if (billingManager.metadata.guid == orgUser.id) {
                            orgUser.isBillingManager = true;
                        }
                    });
                    angular.forEach(result.entity.auditors, function (auditor, auditorIndex) {
                        if (auditor.metadata.guid == orgUser.id) {
                            orgUser.isAuditor = true;
                        }
                    });
                    organization.users.push(orgUser);
                });
                angular.forEach(result.entity.spaces, function (cfSpace, cfSpaceIndex) {
                    var space = {id: cfSpace.metadata.guid, name: cfSpace.entity.name, users: []};

                    angular.forEach(organization.users, function (user, developerIndex) {
                        var spaceUser = {id: user.id};
                        angular.forEach(cfSpace.entity.developers, function (developer, developerIndex) {
                            if (developer.metadata.guid === spaceUser.id) {
                                spaceUser.isDeveloper = true;
                            }
                        });
                        angular.forEach(cfSpace.entity.managers, function (manager, managerIndex) {
                            if (manager.metadata.guid === spaceUser.id) {
                                spaceUser.isManager = true;
                            }
                        });
                        angular.forEach(cfSpace.entity.auditors, function (auditor, auditorIndex) {
                            if (auditor.metadata.guid === spaceUser.id) {
                                spaceUser.isAuditor = true;
                            }
                        });
                        space.users.push(spaceUser);
                    });

                    organization.spaces.push(space);
                });
                var userNameFilter = '';
                angular.forEach(organization.users, function (user, userIndex) {
                    userNameFilter = userNameFilter + 'id eq \'' + user.id + "'";
                    if (userIndex < organization.users.length - 1) {
                        userNameFilter = userNameFilter + ' or ';
                    }
                });
                cloudfoundry.getUserNames(userNameFilter).then(
                    function (result, status, headers) {
                        angular.forEach(result.data.resources, function(user, userIndex){
                            angular.forEach(organization.users, function(orgUser, orgUserIndex){
                                if(user.id === orgUser.id){
                                    orgUser.userName = user.userName;
                                }
                            });
                            angular.forEach(organization.spaces, function(space, spaceIndex){
                                angular.forEach(space.users, function(spaceUser, spaceUserIndex){
                                    if(user.id === spaceUser.id){
                                        spaceUser.userName = user.userName;
                                    }
                                });
                            });
                        });
                        deferred.resolve(organization);
                    }, function (result) {
                        deferred.reject({reason:result.data, status:result.status, headers:result.headers});
                    }
                );
            });
        organizationDetailsPromise.error(function (reason, status, headers) {
            deferred.reject({reason:reason, status:status, headers:headers});
        });
        return deferred.promise;
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
            if(user.isManager){
                ids.push(user.id);
            }
        });
        return cloudfoundry.updateOrganization(organization.id, {manager_guids: ids});
    }

    userManager.setOrgBillingManagers = function (organization) {
        var ids = [];
        angular.forEach(organization.users, function(user, userIndex){
            if(user.isBillingManager){
                ids.push(user.id);
            }
        });
        return cloudfoundry.updateOrganization(organization.id, {billing_manager_guids: ids});
    }

    userManager.setOrgAuditors = function (organization) {
        var ids = [];
        angular.forEach(organization.users, function(user, userIndex){
            if(user.isAuditor){
                ids.push(user.id);
            }
        });
        return cloudfoundry.updateOrganization(organization.id, {auditor_guids: ids});
    }

    userManager.setSpaceManagers = function (space) {
        var ids = [];
        angular.forEach(space.users, function(user, userIndex){
            if(user.isManager){
                ids.push(user.id);
            }
        });
        return cloudfoundry.updateSpace(space.id, {manager_guids: ids});
    }

    userManager.setSpaceDevelopers = function (space) {
        var ids = [];
        angular.forEach(space.users, function(user, userIndex){
            if(user.isDeveloper){
                ids.push(user.id);
            }
        });
        return cloudfoundry.updateSpace(space.id, {developer_guids: ids});
    }

    userManager.setSpaceAuditors = function (space) {
        var ids = [];
        angular.forEach(space.users, function(user, userIndex){
            if(user.isAuditor){
                ids.push(user.id);
            }
        });
        return cloudfoundry.updateSpace(space.id, {auditor_guids: ids});
    }

    return userManager;

});