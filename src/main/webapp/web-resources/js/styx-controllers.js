'use strict';

var styxControllers = angular.module('styx.controllers', []);

styxControllers.controller('LoginController', function ($scope, cloudfoundry, $location) {
    $scope.authenticating = false;
    $scope.login = function (userForm) {
        $scope.authenticating = true;
        var authenticationPromise = cloudfoundry.authenticate(userForm);
        authenticationPromise.success(function (data, status, headers) {
            var organizationPromise = cloudfoundry.getOrganizations();
            organizationPromise.success(function (data, status, headers) {
                $location.path('/app-spaces/' + data[0].id);
            });
            organizationPromise.error(function (data, status, headers) {
                $scope.error = 'Invalid user credentials';
                $scope.authenticating = false;
            });
        });
        authenticationPromise.error(function (data, status, headers) {
            $scope.error = 'Invalid user credentials';
            $scope.authenticating = false;
        });
    }
});

styxControllers.controller('RegisterController', function($scope, cloudfoundry, $location) {
    $scope.register = function(userForm) {
        $scope.success = '';
        $scope.error = '';

        var registrationPromise = cloudfoundry.register(userForm);
        registrationPromise.success(function (data, status, headers) {
            $scope.success = 'Registration successful! Your user is active, ask an organization manager to add you to an organization.';
        });
        registrationPromise.error(function (data, status, headers) {
            $scope.error = 'Registration failed. Reason: ' + data.message;
        });
    }
});

styxControllers.controller('AppSettingsController', function ($scope, $stateParams, $location, cloudfoundry) {
    $scope.loading = true;
    $scope.organizationId = $stateParams.organizationId;

    var applicationPromise = cloudfoundry.getApplication($stateParams.applicationId);
    applicationPromise.success(function (application, status, headers) {
        $scope.application = application;
        $scope.loading = false;
    });
    applicationPromise.error(function (data, status, headers) {
        $scope.forceLogin(status);
        $scope.error = 'Failed to load application. Reason: ' + data.code + ' - ' + data.description;
        $scope.loading = false;
    });

    $scope.deleteApplication = function() {
        var applicationPromise = cloudfoundry.deleteApplication($scope.application.metadata.guid);
        applicationPromise.success(function (services, status, headers) {
            $location.path('/app-spaces/' + $scope.organizationId);
        });
        applicationPromise.error(function (data, status, headers) {
            $scope.error = 'Failed to delete application. Reason: ' + data.code + ' - ' + data.description;
        });
    }
});

styxControllers.controller('AppSpacesController', function ($scope, $stateParams, $location, cloudfoundry, userManager) {
    $scope.loading = true;
    $scope.organizationId = $stateParams.organizationId;

    var spacesPromise = cloudfoundry.getSpaces($stateParams.organizationId);
    spacesPromise.success(function (data, status, headers) {
        $scope.space = {selected: data[0].name};
        data[0].selected = true;
        $scope.spaces = data;
        $scope.loading = false;
    });
    spacesPromise.error(function (data, status, headers) {
        $scope.forceLogin(status);
        $scope.error = 'Failed to load spaces. Reason: ' + data.code + ' - ' + data.description;
        $scope.loading = false;
    });

    $scope.startApplication = function (applicationId) {
        var applicationPromise = cloudfoundry.updateApplication(applicationId, {'state': 'STARTED'});
        applicationPromise.success(function (data, status, headers) {
            angular.forEach($scope.spaces.resources, function (space, spaceIndex) {
                if (space.selected) {
                    var index = -1;
                    angular.forEach(space.entity.apps, function (app, appIndex) {
                        if (app.metadata.guid == applicationId) {
                            index = appIndex;
                        }
                    });
                    if (index > -1) {
                        space.entity.apps[index] = data;
                    }
                }
            });
        });
        applicationPromise.error(function (data, status, headers) {
            $scope.error = 'Failed to start application. Reason: ' + data.code + ' - ' + data.description;
        });
    }

    $scope.stopApplication = function (applicationId) {
        var applicationPromise = cloudfoundry.updateApplication(applicationId, {'state': 'STOPPED'});
        applicationPromise.success(function (data, status, headers) {
            angular.forEach($scope.spaces.resources, function (space, spaceIndex) {
                if (space.selected) {
                    var index = -1;
                    angular.forEach(space.entity.apps, function (app, appIndex) {
                        if (app.metadata.guid == applicationId) {
                            index = appIndex;
                        }
                    });
                    if (index > -1) {
                        space.entity.apps[index] = data;
                    }
                }
            });
        });
        applicationPromise.error(function (data, status, headers) {
            $scope.error = 'Failed to start application. Reason: ' + data.code + ' - ' + data.description;
        });
    }

    $scope.selectSpace = function (spaceName) {
        $scope.space.selected = spaceName;
    }
});

styxControllers.controller('AppLogController', function ($scope, $stateParams, cloudfoundry) {
    $scope.loading = true;

    var applicationId = $stateParams.applicationId;
    var instanceId = $stateParams.instanceId;
    var fileName = $stateParams.fileName;

    var logPromise = cloudfoundry.getApplicationLog(applicationId, instanceId, fileName);
    logPromise.success(function (data, status, headers) {
        $scope.log = data;
        $scope.loading = false;
    });
    logPromise.error(function (data, status, headers) {
        $scope.forceLogin(status);
        $scope.error = 'Failed to retrieve log file. Reason: ' + data.code + ' - ' + data.description;
        $scope.loading = false;
    });
});

styxControllers.controller('CreateOrganizationController', function ($scope, $stateParams, $location, cloudfoundry) {
    $scope.createOrganization = function (organizationForm) {
        var organizationPromise = cloudfoundry.createOrganization(organizationForm);
        organizationPromise.success(function (organization, status, headers) {
            var spacesPromise = cloudfoundry.createSpace(organization.metadata.guid, 'development');
            spacesPromise.success(function (space, status, headers) {
                $location.path('/app-spaces/' + organization.metadata.guid);
            });
            spacesPromise.error(function (data, status, headers) {
                $scope.error = 'Failed to create organization. Reason: ' + data.code + ' - ' + data.description;
            });
        });
        organizationPromise.error(function (data, status, headers) {
            $scope.error = 'Failed to create organization. Reason: ' + data.code + ' - ' + data.description;
        });
    }
});

styxControllers.controller('CreateSpaceController', function ($scope, $stateParams, $location, cloudfoundry) {
    $scope.organizationId = $stateParams.organizationId;

    $scope.createSpace = function (spaceForm) {
        var spacesPromise = cloudfoundry.createSpace($scope.organizationId, spaceForm.name);
        spacesPromise.success(function (space, status, headers) {
            $location.path('/app-spaces/' + $scope.organizationId);
        });
        spacesPromise.error(function (data, status, headers) {
            $scope.error = 'Failed to create space. Reason: ' + data.code + ' - ' + data.description;
        });
    }
});

styxControllers.controller('MarketplaceController', function ($scope, $stateParams, cloudfoundry) {
    var servicesPromise = cloudfoundry.getServices();
    servicesPromise.success(function (data, status, headers) {
        $scope.services = data;
    });
    servicesPromise.error(function (data, status, headers) {
        $scope.forceLogin(status);
        $scope.error = 'Failed to load services. Reason: ' + data.code + ' - ' + data.description;
    });
});

styxControllers.controller('NavigationController', function ($scope, $stateParams, cloudfoundry, $location, cache) {
    $scope.logout = function () {
        cloudfoundry.logout();
        $location.path('/login');
    }
    $scope.loadingData = true;
    $scope.isAuthenticated = cloudfoundry.isAuthenticated();
    if (cloudfoundry.isAuthenticated()) {
        var organizationsPromise = cloudfoundry.getOrganizations();
        organizationsPromise.success(function (data, status, headers) {
            angular.forEach(data, function (organization, organizationIndex) {
                if (organization.id == $stateParams.organizationId) {
                    organization.selected = true;
                    $scope.organization = organization;
                }
            });
            $scope.user = cache.getUser();
            $scope.organizations = data;
            $scope.loadingData = false;
        });
        organizationsPromise.error(function (data, status, headers) {
            $scope.forceLogin(status);
        });
    }
});

styxControllers.controller('FooterController', function ($scope, chucknorris) {
    chucknorris.fact().then(function (fact) {
        $scope.fact = fact.joke;
    });
});

styxControllers.controller('OrganizationSettingsController', function ($scope, $stateParams, $location, cloudfoundry) {
    var organizationPromise = cloudfoundry.getOrganization($stateParams.organizationId);
    organizationPromise.success(function (data, status, headers) {
        $scope.organization = data;
    });
    organizationPromise.error(function (data, status, headers) {
        $scope.forceLogin(status);
        $scope.error = 'Failed to load organizations. Reason: ' + data.code + ' - ' + data.description;
    });

    $scope.deleteOrganization = function () {
        $scope.loading = true;

        var organizationDeletePromise = cloudfoundry.deleteOrganization($scope.organization.id);
        organizationDeletePromise.success(function (data, status, headers) {
            var organizationsPromise = cloudfoundry.getOrganizations();
            organizationsPromise.success(function (data, status, headers) {
                $location.path('/app-spaces/' + data[0].id);
            });
            organizationsPromise.error(function (data, status, headers) {
                $scope.error = 'Failed to load organization. Reason: ' + data.code + ' - ' + data.description;
            });
        });
        organizationDeletePromise.error(function (data, status, headers) {
            $scope.error = 'Failed to delete organization. Reason: ' + data.code + ' - ' + data.description;
        });
    }
});

styxControllers.controller('SpaceSettingsController', function ($scope, $stateParams, $location, cloudfoundry) {
    var spacePromise = cloudfoundry.getSpace($stateParams.spaceId);
    spacePromise.success(function (data, status, headers) {
        $scope.space = data;
    });
    spacePromise.error(function (data, status, headers) {
        $scope.forceLogin(status);
        $scope.error = 'Failed to get space. Reason: ' + data.code + ' - ' + data.description;
    });

    $scope.deleteSpace = function () {
        var spacePromise = cloudfoundry.deleteSpace($scope.space.id);
        spacePromise.success(function (data, status, headers) {
            $location.path('/app-spaces/' + $stateParams.organizationId);
        });
        spacePromise.error(function (data, status, headers) {
            $scope.error = 'Failed to delete space. Reason: ' + data.code + ' - ' + data.description;
        });
    }
});

styxControllers.controller('OrgUsersController', function ($scope, $stateParams, userManager, $location) {
    $scope.loading = true;
    var containsUser = function (orgUsers, userId) {
        for (var i = 0; i < orgUsers.length; i++) {
            if (orgUsers[i].id === userId) {
                return true;
            }
        }
        return false;
    }
    $scope.addUser = function (organization, user) {
        if (user) {
            organization.users.push(user);
            userManager.setOrgUsers(organization).then(
                function (result, status, headers) {
                    $location.path('/users/' + organization.id);
                },
                function (reason, status, headers) {
                    $scope.forceLogin(status);
                    $scope.error = 'Failed to set user role for ' + user.userName + ' user details. Reason: ' + JSON.stringify(reason.data);
                }
            );
        }
    }
    userManager.getAllUsers().then(
        function (users, status, headers) {
            userManager.getUsers($stateParams.organizationId).then(
                function (organization, status, headers) {
                    var filteredUsers = [];
                    angular.forEach(users, function (user, userIndex) {
                        if (!containsUser(organization.users, user.id)) {
                            filteredUsers.push(user);
                        }
                    });
                    $scope.organization = organization;
                    $scope.users = filteredUsers;
                    $scope.loading = false;
                },
                function (reason, status, headers) {
                    $scope.loading = false;
                    $scope.error = 'Failed to retrieve users details. Reason: ' + JSON.stringify(reason.data);
                }
            );
        },
        function (response) {
            $scope.forceLogin(response.status);
            $scope.loading = false;
            $scope.error = 'Failed to retrieve users. Reason: ' + JSON.stringify(response.reason);
        }
    );
});

styxControllers.controller('UsersController', function ($scope, $stateParams, userManager, $location, cloudfoundry) {
    $scope.loading = true;
    $scope.blockInput = true;
    userManager.getUsers($stateParams.organizationId).then(function (organization) {
        $scope.loggedInUser = cloudfoundry.getUser();
        var mayManipulate = false;
        angular.forEach(organization.users, function(orgUser, orgUserIndex){
            if($scope.loggedInUser.id === orgUser.id && orgUser.isManager){
                mayManipulate = true;
            }
        });
        $scope.selectedGroup = organization.name;
        $scope.showOrganizationUsers = true;
        $scope.organization = organization;
        $scope.loading = false;
        if(mayManipulate === true){
            $scope.blockInput = false;
        }
    }, function (response) {
        $scope.forceLogin(response.status);
        $scope.loading = false;
        $scope.error = 'Failed to retrieve organization users. Reason: ' + JSON.stringify(response);
    });
    $scope.openConfirmation = function(user){
        $scope.selectedUser = user;
        $scope.confirmationRequested = true;
    }
    $scope.closeConfirmation = function(){
        $scope.selectedUser = 'undefined';
        $scope.confirmationRequested = false;
    }
    $scope.confirmationOpts = {
        backdropFade: true,
        dialogFade:true
    };
    $scope.addNewUser = function (organization) {
        $location.path("/organization/" + organization.id + "/users");
    }
    $scope.removeUser = function (organization, user) {
        $scope.blockInput = true;
        $scope.confirmationRequested = false;
        var index = organization.users.indexOf(user);
        organization.users.splice(index, 1);
        userManager.setOrgUsers(organization).then(
            function (result, status, headers) {
                $scope.blockInput = false;
            },
            function (reason, status, headers) {
                $scope.blockInput = false;
                $scope.error = 'Failed to remove user from organization ' + organization.name + '. Reason: ' + JSON.stringify(reason.data);
                organization.users.push(user);
            }
        );
    }
    $scope.setOrgManager = function (organization) {
        $scope.blockinput = true;
        userManager.setOrgManagers(organization).then(
            function (result, status, headers) {
                $scope.blockinput = false;
            },
            function (result, status, headers) {
                $scope.blockinput = false;
                $scope.error = 'Failed to add manager to organization ' + organization.name + '. Reason: ' + JSON.stringify(reason.data);
            }
        );
    }
    $scope.setOrgBillingManager = function (organization, user) {
        $scope.blockinput = true;
        userManager.setOrgBillingManagers(organization).then(
            function (result, status, headers) {
                $scope.blockinput = false;
            },
            function (result, status, headers) {
                $scope.blockinput = false;
                $scope.error = 'Failed to add billing manager to organization ' + organization.name + '. Reason: ' + JSON.stringify(reason.data);
            }
        );
    }
    $scope.setOrgAuditor = function (organization, user) {
        $scope.blockinput = true;
        userManager.setOrgAuditors(organization).then(
            function (result, status, headers) {
                $scope.blockinput = false;
            },
            function (result, status, headers) {
                $scope.blockinput = false;
                $scope.error = 'Failed to add auditor to organization ' + organization.name + '. Reason: ' + JSON.stringify(reason.data);
            }
        );
    }
    $scope.setSpaceManager = function (space, user) {
        $scope.blockinput = true;
        userManager.setSpaceManagers(space).then(
            function (result, status, headers) {
                $scope.blockinput = false;
            },
            function (result, status, headers) {
                $scope.blockinput = false;
                $scope.error = 'Failed to add manager to space ' + space.name + '. Reason: ' + JSON.stringify(reason.data);
            }
        );
    }
    $scope.setSpaceDeveloper = function (space, user) {
        $scope.blockinput = true;
        userManager.setSpaceDevelopers(space).then(
            function (result, status, headers) {
                $scope.blockinput = false;
            },
            function (result, status, headers) {
                $scope.error = 'Failed to add developer to space ' + space.name + '. Reason: ' + JSON.stringify(reason.data);
            }
        );
    }
    $scope.setSpaceAuditor = function (space, user) {
        $scope.blockinput = true;
        userManager.setSpaceAuditors(space).then(
            function (result, status, headers) {
                $scope.blockinput = false;
            },
            function (result, status, headers) {
                $scope.error = 'Failed to add auditor to space ' + space.name + '. Reason: ' + JSON.stringify(reason.data);
            }
        );
    }
    $scope.selectOrganizationUsers = function (organizationName) {
        $scope.selectedGroup = organizationName;
        $scope.showOrganizationUsers = true;
    }
    $scope.selectSpaceUsers = function (spaceName, organization) {
        $scope.selectedGroup = spaceName;
        angular.forEach(organization.spaces, function (item, index) {
            if (item.name === spaceName) {
                $scope.selectedSpace = item;
                $scope.showOrganizationUsers = false;
            }
        });
    }
});

styxControllers.controller('MainController', function ($scope, cloudfoundry, $location, $route) {
    if (!cloudfoundry.isAuthenticated()) {
        if ($location.path() != '/login') {
            $location.path('/login');
            return;
        }
    }
    if ($location.path() === '/' || $location.path().length === 0) {
        var organizationPromise = cloudfoundry.getOrganizations();
        organizationPromise.success(function (result) {
            $location.path('/app-spaces/' + result.resources[0].metadata.guid);
        });
        organizationPromise.error(function (result) {
            cloudfoundry.logout();
            $location.path('/login');
        });
    } else {
        $route.reload();
    }
});