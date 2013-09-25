package com.github.kratos.resources

import com.github.kratos.http.HttpClient

class User {

    private final String apiBaseUri
    private final HttpClient httpClient

    def User(HttpClient httpClient, String apiBaseUri) {
        this.apiBaseUri = apiBaseUri
        this.httpClient = httpClient
    }

    def get(String token, String id) {
        def cfUser = httpClient.get{
            path "${apiBaseUri}/v2/users/$id"
            headers authorization: token, accept: 'application/json'
            queryParams 'inline-relations-depth': 0
        }
        [id: cfUser.metadata.guid, roles: cfUser.entity.admin ? ['ADMIN'] : []]
    }

}
