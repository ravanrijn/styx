package com.github.kratos.http

import com.github.kratos.resources.Organization
import com.github.kratos.resources.Application
import com.github.kratos.resources.Quota
import com.github.kratos.resources.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ApiClient {

    final String apiBaseUri
    final UaaClient uaaClient
    final HttpClient httpClient
    final Application application
    final Quota quota
    final Organization organization
    final User user

    @Autowired
    def ApiClient(HttpClient httpClient, String apiBaseUri, UaaClient uaaClient) {
        this.httpClient = httpClient
        this.apiBaseUri = apiBaseUri
        this.uaaClient = uaaClient
        this.application = new Application(httpClient, apiBaseUri)
        this.quota = new Quota(httpClient, apiBaseUri)
        this.organization = new Organization(httpClient, uaaClient, apiBaseUri)
        this.user = new User(httpClient, apiBaseUri)
    }

    def applications(token) {
        application.list(token)
    }

    def application(token, id) {
        application.get(token, id)
    }

    def organizations(String token){
        organization.list(token)
    }

    def organization(String token, String id){
        organization.get(token, id)
    }    
    
    def quotas(String token) {
        quota.list(token)
    }

    def quota(String token, String id) {
        quota.get(token, id)
    }

    def mergeUser(String token, Map uaaUser){
        def cfUser = user.get(token, uaaUser.id)
        uaaUser.roles = uaaUser.roles + cfUser.roles
        uaaUser
    }

    def user(String token, String id) {
        user.get(token, id)
    }

}
