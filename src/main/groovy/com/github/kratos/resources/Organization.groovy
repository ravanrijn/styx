package com.github.kratos.resources

class Organization {

    static def listTransform = { result ->
        result.resources.collect { cfOrganization -> [id: cfOrganization.metadata.guid, name: cfOrganization.entity.name, quotaId: cfOrganization.entity.quota_definition_guid] }
    }

    static def getTransform = { getDetails, cfOrganization ->
        def cfApps = cfOrganization.entity.spaces.collect { cfSpace -> cfSpace.entity.apps}.flatten()
        def userIds = cfOrganization.entity.users.collect { cfUser -> cfUser.metadata.guid }
        def futures = getDetails(userIds, cfApps)
        [id: cfOrganization.metadata.guid,
                name: cfOrganization.entity.name,
                quota: Quota.getTransform(cfOrganization.entity.quota_definition),
                users: mapOrganizationUsers(cfOrganization, futures),
                spaces: mapSpaces(cfOrganization.entity.spaces, futures)]
    }

    static def mapSpaceUsers(cfSpace, futures) {
        def usernames = futures.findFutureById("usernames")
        def merge = { List... lists ->
            def merged = [] as Set
            lists.flatten().each { item ->
                def searchResult = merged.find { result -> result.id == item.id }
                searchResult ? searchResult.roles = searchResult.roles + item.roles : merged << [id: item.id, username: usernames.find{username -> username.id == item.id}?.username, roles: item.roles]
            }
            merged
        }
        def managers = cfSpace.managers.collect { cfManager -> [id: cfManager.metadata.guid, roles: ['MANAGER']] } as List
        def developers = cfSpace.developers.collect { cfDeveloper -> [id: cfDeveloper.metadata.guid, roles: ['DEVELOPER']] } as List
        def auditors = cfSpace.auditors.collect { cfAuditor -> [id: cfAuditor.metadata.guid, roles: ['AUDITOR']] } as List
        merge(managers, developers, auditors)
    }

    static def mapSpaces(cfSpaces, futures) {
        cfSpaces.collect { cfSpace -> [id: cfSpace.metadata.guid, name: cfSpace.entity.name, users: mapSpaceUsers(cfSpace.entity, futures), apps: mapApplications(cfSpace.entity.apps, futures)] }
    }

    static def mapApplications(cfApps, futures) {
        cfApps.collect { cfApp ->
            def urls = []
            futures.list.each { future ->
                if (future.id != "usernames") {
                    future.result().resources.each { route ->
                        if (route.entity.apps.find { app -> app.metadata.guid == cfApp.metadata.guid }) {
                            urls.add("${route.entity.host}.${route.entity.domain.entity.name}" as String)
                        }
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

    static def mapOrganizationUsers(cfOrganization, futures) {
        def getRoles = { configs -> configs.collect { config -> config.assignees.find { assignee -> config.id == assignee.metadata.guid } ? config.name : null } }
        def organizationUsers = cfOrganization.entity.users.collect { cfUser ->
            def userRoles = getRoles([[id: cfUser.metadata.guid, assignees: cfOrganization.entity.managers, name: 'MANAGER'],
                    [id: cfUser.metadata.guid, assignees: cfOrganization.entity.billing_managers, name: 'BILLING_MANAGER'],
                    [id: cfUser.metadata.guid, assignees: cfOrganization.entity.auditors, name: 'AUDITOR']])
            userRoles.removeAll([null])
            [id: cfUser.metadata.guid, username: '', roles: userRoles]
        }
        futures.findFutureById("usernames").collect { result ->
            def searchResult = organizationUsers.find { user -> user.id == result.id }
            searchResult.username = result.username
            searchResult
        }
    }

}
