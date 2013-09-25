package com.github.kratos.resources

import com.github.kratos.http.HttpClient
import com.github.kratos.http.UaaClient

class User {

    private final String apiBaseUri

    def User(HttpClient httpClient, String apiBaseUri){
        this.apiBaseUri = apiBaseUri
    }

    def get(String token){

    }

}
