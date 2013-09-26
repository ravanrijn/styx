package com.github.kratos.http

import static com.github.kratos.resources.Organization.listTransform as transformOrganizations
import static com.github.kratos.resources.Organization.getTransform as transformOrganization
import com.github.kratos.resources.Application
import static com.github.kratos.resources.Quota.getTransform as transformQuota
import static com.github.kratos.resources.Quota.listTransform as transformQuotas
import static com.github.kratos.resources.User.uaaGetTransform as transformUaaUser
import static com.github.kratos.resources.User.cfGetTransform as transformCfUser
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

    @Autowired
    def ApiClient(HttpClient httpClient, String apiBaseUri, String uaaBaseUri, String clientId, String clientSecret) {
        this.clientId = clientId
        this.clientSecret = clientSecret
        this.httpClient = httpClient
        this.apiBaseUri = apiBaseUri
        this.uaaBaseUri = uaaBaseUri
        this.application = new Application(httpClient, apiBaseUri)
    }

    def applications(token) {
        application.list(token)
    }

    def application(token, id) {
        application.get(token, id)
    }

    def organizations(String token){
        httpClient.get {
            path "$apiBaseUri/v2/organizations"
            headers authorization: token, accept: 'application/json'
            queryParams 'inline-relations-depth': 0
            transform transformOrganizations
        }
    }

    def organization(String token, String orgId){
        def getDetails = { userIds, cfApps ->
            def appToken = appToken()
            String usernamesUri = "$uaaBaseUri/ids/Users?filter="
            userIds.each { userId -> usernamesUri = "${usernamesUri}id eq \'$userId\' or " }
            def requests = cfApps.collect{cfApp ->
                { ->
                    path "${apiBaseUri}${cfApp.entity.routes_url}"
                    headers authorization: token, accept: 'application/json'
                    queryParams 'inline-relations-depth': 1
                }
            }
            requests.add(
                { ->
                    id "usernames"
                    path usernamesUri[0..-4]
                    headers authorization: "${appToken.tokenType} ${appToken.accessToken}" as String, accept: 'application/json'
                    transform {result -> result.resources.collect { item -> [id: item.id, username: item.userName] }}
                }
            )
            httpClient.get(requests.toArray() as Closure[])
        }
        final transformOrg = transformOrganization.curry(getDetails)



        httpClient.get {
            path "$apiBaseUri/v2/organizations/${orgId}"
            headers authorization: token, accept: 'application/json'
            queryParams 'inline-relations-depth': 2
            transform transformOrg
        }
    }    
    
    def quotas(String token) {
        httpClient.get {
            path "$apiBaseUri/v2/quota_definitions"
            headers authorization: token, accept: 'application/json'
            queryParams 'inline-relations-depth': 0
            transform transformQuotas
        }
    }

    def quota(String token, String id) {
        httpClient.get {
            path "$apiBaseUri/v2/quota_definitions/$id"
            headers authorization: token, accept: 'application/json'
            queryParams 'inline-relations-depth': 0
            transform transformQuota
        }
    }

    def info(){
        final info = httpClient.get {
            path "$apiBaseUri/v2/info"
            headers accept: 'application/json'
        }
        [description:info.description, authorizationEndpoint:info.authorization_endpoint, apiVersion:info.api_version]
    }

    def appToken(){
        final MultiValueMap<String, String> requestBody = new LinkedMultiValueMap();
        requestBody.add("grant_type", "client_credentials");
        requestBody.add("response_type", "token");
        final token = httpClient.post {
            path "${info().authorizationEndpoint}/oauth/token"
            body requestBody
            headers authorizationHeaders()
        }
        [tokenType: token.token_type, accessToken: token.access_token, refreshToken: token.refresh_token]
    }

    def userToken(username, password){
        final MultiValueMap<String, String> requestBody = new LinkedMultiValueMap();
        requestBody.add("grant_type", "password");
        requestBody.add("username", username);
        requestBody.add("password", password);
        httpClient.post {
            path "${info().authorizationEndpoint}/oauth/token"
            body requestBody
            headers authorizationHeaders()
            transform {result -> [tokenType: result.token_type, accessToken: result.access_token, refreshToken: result.refresh_token]}
        }
    }

    def user(String token){
        def appToken = appToken()
        final uaaUser = httpClient.get {
            path "$uaaBaseUri/userinfo"
            headers authorization: token, accept: 'application/json'
            transform transformUaaUser
        }
        httpClient.get {
            path "${apiBaseUri}/v2/users/${uaaUser.id}"
            headers authorization: "${appToken.tokenType} ${appToken.accessToken}" as String, accept: 'application/json'
            queryParams 'inline-relations-depth': 0
            transform {result ->
                uaaUser.roles = transformCfUser(result).roles + uaaUser.roles
                uaaUser
            }
        }
    }

    def authorizationHeaders() {
        def authorization = encodeBase64String("$clientId:$clientSecret".getBytes())
        [accept: 'application/json', authorization: "Basic $authorization", 'content-type': 'application/x-www-form-urlencoded;charset=utf-8']
    }

}
