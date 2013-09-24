package com.github.kratos.resources

import com.github.kratos.http.HttpClient
import com.github.kratos.http.UaaClient

class Organization {

    private final HttpClient httpClient
    private final UaaClient uaaClient
    private final String apiBaseUri

    def Organization(HttpClient httpClient, UaaClient uaaClient, String apiBaseUri) {
        this.httpClient = httpClient
        this.uaaClient = uaaClient
        this.apiBaseUri = apiBaseUri
    }

    def list(String token) {
        final cfOrganizations = httpClient.get {
            path "$apiBaseUri/v2/organizations"
            headers authorization: token, accept: 'application/json'
            queryParams 'inline-relations-depth': 0
        }
        cfOrganizations.resources.collect { cfOrganization -> [id: cfOrganization.metadata.guid, name: cfOrganization.entity.name, quotaId: cfOrganization.entity.quota_definition_guid] }
    }

    def get(String token, String id) {
        final cfOrganization = httpClient.get {
            path "$apiBaseUri/v2/organizations/$id"
            headers authorization: token, accept: 'application/json'
            queryParams 'inline-relations-depth': 4
        }
        [id: cfOrganization.metadata.guid,
                name: cfOrganization.entity.name,
                quota: Quota.mapQuota(cfOrganization.entity.quota_definition),
                users: mapOrganizationUsers(cfOrganization),
                spaces: mapSpaces(cfOrganization.entity.spaces)]
    }

    def mapSpaceUsers(cfSpace) {
        def merge = { List... lists ->
            def merged = [] as Set
            lists.flatten().each { item ->
                def searchResult = merged.find { result -> result.id == item.id }
                searchResult ? searchResult.roles = searchResult.roles + item.roles : merged << [id: item.id, roles: item.roles]
            }
            merged
        }
        def managers = cfSpace.managers.collect { cfManager -> [id: cfManager.metadata.guid, roles: ['MANAGER']] } as List
        def developers = cfSpace.developers.collect { cfDeveloper -> [id: cfDeveloper.metadata.guid, roles: ['DEVELOPER']] } as List
        def auditors = cfSpace.auditors.collect { cfAuditor -> [id: cfAuditor.metadata.guid, roles: ['AUDITOR']] } as List
        merge(managers, developers, auditors)
    }

    def mapSpaces(cfSpaces) {
        cfSpaces.collect { cfSpace -> [id: cfSpace.metadata.guid, name: cfSpace.entity.name, users: mapSpaceUsers(cfSpace.entity), apps: mapApplications(cfSpace.entity.apps)] }
    }

    def mapApplications(cfApps) {
        cfApps.collect { cfApp ->
            [id: cfApp.metadata.guid,
                    name: cfApp.entity.name,
                    memory: cfApp.entity.memory,
                    instances: cfApp.entity.instances,
                    state: cfApp.entity.state,
                    urls: cfApp.entity.routes.collect { route -> String result = "${route.entity.host}.${route.entity.domain.entity.name}" },
                    services: cfApp.entity.service_bindings.size()]
        }
    }

    def mapOrganizationUsers(cfOrganization) {
        def getRoles = { configs -> configs.collect { config -> config.assignees.find { assignee -> config.id == assignee.metadata.guid } ? config.name : null } }
        def organizationUsers = cfOrganization.entity.users.collect { cfUser ->
            def userRoles = getRoles([[id: cfUser.metadata.guid, assignees: cfOrganization.entity.managers, name: 'MANAGER'],
                    [id: cfUser.metadata.guid, assignees: cfOrganization.entity.billing_managers, name: 'BILLING_MANAGER'],
                    [id: cfUser.metadata.guid, assignees: cfOrganization.entity.auditors, name: 'AUDITOR']])
            userRoles.removeAll([null])
            [id: cfUser.metadata.guid, username: '', roles: userRoles]
        }
        uaaClient.userNames(organizationUsers.collect { user -> user.id }).collect { result ->
            def searchResult = organizationUsers.find { user -> user.id == result.id }
            searchResult.username = result.username
            searchResult
        }
    }

}
