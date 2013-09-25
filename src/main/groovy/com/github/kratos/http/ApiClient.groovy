package com.github.kratos.http

import com.github.kratos.resources.Organization
import com.github.kratos.resources.Application
import com.github.kratos.resources.Quota
import com.github.kratos.resources.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

import static org.apache.commons.codec.binary.Base64.encodeBase64String

@Service
class ApiClient {

    final String apiBaseUri
    final String uaaBaseUri
    final String clientId
    final String clientSecret
    final HttpClient httpClient
    final Application application
    final Quota quota
    final Organization organization
    final User user

    @Autowired
    def ApiClient(HttpClient httpClient, String apiBaseUri, String uaaBaseUri, String clientId, String clientSecret) {
        this.clientId = clientId
        this.clientSecret = clientSecret
        this.httpClient = httpClient
        this.apiBaseUri = apiBaseUri
        this.uaaBaseUri = uaaBaseUri
        this.application = new Application(httpClient, apiBaseUri)
        this.quota = new Quota(httpClient, apiBaseUri)
        this.organization = new Organization(httpClient, uaaBaseUri, apiBaseUri)
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
        def appToken = appToken()
        organization.get("${appToken.tokenType} ${appToken.accessToken}" as String, token, id)
    }    
    
    def quotas(String token) {
        quota.list(token)
    }

    def quota(String token, String id) {
        quota.get(token, id)
    }

    def info(){
        final info = httpClient.get {
            path "$apiBaseUri/v2/info"
            headers accept: 'application/json'
        }
        [description:info.description, authorizationEndpoint:info.authorization_endpoint, apiVersion:info.api_version]
    }

    def appToken(){
        def authorization = encodeBase64String("$clientId:$clientSecret".getBytes())
        final MultiValueMap<String, String> requestBody = new LinkedMultiValueMap();
        requestBody.add("grant_type", "client_credentials");
        requestBody.add("response_type", "token");
        final token = httpClient.post {
            path "${info().authorizationEndpoint}/oauth/token"
            body requestBody
            headers accept: 'application/json', authorization: "Basic $authorization", 'content-type': 'application/x-www-form-urlencoded;charset=utf-8'
        }
        [tokenType: token.token_type, accessToken: token.access_token, refreshToken: token.refresh_token]
    }

    def userToken(username, password){
        final MultiValueMap<String, String> requestBody = new LinkedMultiValueMap();
        requestBody.add("grant_type", "password");
        requestBody.add("username", username);
        requestBody.add("password", password);
        final String authorizationEndpoint = authorizationEndpoint()
        final token = httpClient.post {
            path "$authorizationEndpoint/oauth/token"
            body requestBody
            headers defaultHeaders()
        }
        [tokenType: token.token_type, accessToken: token.access_token, refreshToken: token.refresh_token]
    }

    def user(String token){
        final result = httpClient.get {
            path "$uaaBaseUri/userinfo"
            headers authorization: token, accept: 'application/json'
        }
        def appToken = appToken()
        def uaaUser = [id:result.user_id, username: result.user_name, roles:[]]
        def cfUser = user.get("${appToken.tokenType} ${appToken.accessToken}" as String, uaaUser.id)
        uaaUser.roles = uaaUser.roles + cfUser.roles
        uaaUser
    }

}
