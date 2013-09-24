package com.github.kratos.http

import com.github.kratos.resources.Application
import com.github.kratos.resources.Quota
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ApiClient {

    final String apiBaseUri
    final String uaaBaseUri
    final HttpClient httpClient
    final Application application
    final Quota quota

    @Autowired
    def ApiClient(HttpClient httpClient, String apiBaseUri, String uaaBaseUri) {
        this.httpClient = httpClient
        this.apiBaseUri = apiBaseUri
        this.uaaBaseUri = uaaBaseUri
        this.application = new Application(httpClient, apiBaseUri)
        this.quota = new Quota(httpClient, apiBaseUri)
    }

    def applications(token) {
        application.list(token)
    }

    def application(token, id) {
        application.get(token, id)
    }

    def quotas(String token) {
        quota.list(token)
    }

    def quota(String token, String id) {
        quota.get(token, id)
    }

}
