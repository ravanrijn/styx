package com.github.kratos.http

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

import static org.apache.commons.codec.binary.Base64.encodeBase64String

@Service
class UaaClient {

    private final HttpClient httpClient
    private final String apiBaseUri
    private final String uaaBaseUri
    private final String clientSecret
    private final String clientId

    @Autowired
    UaaClient(HttpClient httpClient, String apiBaseUri, String uaaBaseUri, String clientId, String clientSecret) {
        this.httpClient = httpClient
        this.apiBaseUri = apiBaseUri
        this.uaaBaseUri = uaaBaseUri
        this.clientId = clientId
        this.clientSecret = clientSecret
    }

    def userDetails(token) {
        final userDetails = httpClient.get {
            path "$uaaBaseUri/userinfo"
            headers authorization: token, accept: 'application/json'
        }
        return [id:userDetails.user_id, username: userDetails.user_name, roles:[]]
    }

    def userNames(userIds) {
        String uri = "$uaaBaseUri/ids/Users?filter="
        userIds.each{id -> uri = "${uri}id eq \'$id\' or "}
        def appToken = applicationToken()
        final cfUserNames = httpClient.get {
            path uri[0..-4]
            headers authorization: "$appToken.tokenType $appToken.accessToken", accept: 'application/json'
        }
        cfUserNames.resources.collect{cfUserName -> [id: cfUserName.id, username: cfUserName.userName]}
    }

    def userToken(username, password){
        final MultiValueMap<String, String> body = new LinkedMultiValueMap();
        body.add("grant_type", "password");
        body.add("username", username);
        body.add("password", password);

        final String authorizationEndpoint = authorizationEndpoint()
        final token = httpClient.post {
            path "$authorizationEndpoint/oauth/token"
            body body
            headers defaultHeaders()
        }
        [tokenType: token.token_type, accessToken: token.access_token, refreshToken: token.refresh_token]
    }

    def applicationToken(){
        final MultiValueMap<String, String> requestBody = new LinkedMultiValueMap();
        requestBody.add("grant_type", "client_credentials");
        requestBody.add("response_type", "token");
        final String baseUri = authorizationEndpoint()
        final token = httpClient.post {
            path "$baseUri/oauth/token"
            body requestBody
            headers defaultHeaders()
        }
        [tokenType: token.token_type, accessToken: token.access_token, refreshToken: token.refresh_token]
    }

    def authorizationEndpoint() {
        final info = httpClient.get {
            path "$apiBaseUri/v2/info"
            headers accept: 'application/json'
        }
        info.authorization_endpoint
    }

    def defaultHeaders() {
        def authorization = encodeBase64String("$clientId:$clientSecret".getBytes())
        [accept: 'application/json', authorization: "Basic $authorization", 'content-type': 'application/x-www-form-urlencoded;charset=utf-8']
    }

}
