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
            queryParams 'inline-relations-depth': 2
        }
        [id: cfOrganization.metadata.guid,
                name: cfOrganization.entity.name,
                quota: Quota.mapQuota(cfOrganization.entity.quota_definition),
                users: mapOrganizationUsers(cfOrganization),
                spaces: mapSpaces(cfOrganization.entity.spaces, token)]
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

    def mapSpaces(cfSpaces, token) {
        cfSpaces.collect { cfSpace -> [id: cfSpace.metadata.guid, name: cfSpace.entity.name, users: mapSpaceUsers(cfSpace.entity), apps: mapApplications(cfSpace.entity.apps, token)] }
    }

    def mapApplications(cfApps, token) {
        def getRequests = cfApps.collect { cfApp ->
            Closure getRequest = {
                path "${apiBaseUri}${cfApp.entity.routes_url}"
                headers authorization: token, accept: 'application/json'
                queryParams 'inline-relations-depth': 1
            }
            getRequest
        }
        final routes = httpClient.get(getRequests.toArray() as Closure[]).collect { future -> future.get() }
        cfApps.collect { cfApp ->
            def urls = []
            routes.each { routeResult ->
                routeResult.resources.each { route ->
                    if (route.entity.apps.find { app -> app.metadata.guid == cfApp.metadata.guid }) {
                        urls.add("${route.entity.host}.${route.entity.domain.entity.name}" as String)
                    }
                }
            }
            [id: cfApp.metadata.guid,
                    name: cfApp.entity.name,
                    memory: cfApp.entity.memory,
                    instances: cfApp.entity.instances,
                    state: cfApp.entity.state,
                    urls: urls]
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
