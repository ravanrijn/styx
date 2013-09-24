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

    def userToken(username, password){
        final MultiValueMap<String, String> body = new LinkedMultiValueMap();
        body.add("grant_type", "password");
        body.add("username", username);
        body.add("password", password);

        final authorizationEndpoint = authorizationEndpoint()
        final token = httpClient.post {
            path "${authorizationEndpoint}/oauth/token"
            withBody body
            withHeaders defaultHeaders()
            exchange()
        }
        [tokenType: token.token_type, accessToken: token.access_token, refreshToken: token.refresh_token]
    }

    def applicationToken(){
        final MultiValueMap<String, String> body = new LinkedMultiValueMap();
        body.add("grant_type", "client_credentials");
        body.add("response_type", "token");

        final authorizationEndpoint = authorizationEndpoint()
        final token = httpClient.post {
            path "${authorizationEndpoint}/oauth/token"
            withBody body
            withHeaders defaultHeaders()
            exchange()
        }
        [tokenType: token.token_type, accessToken: token.access_token, refreshToken: token.refresh_token]
    }

    def authorizationEndpoint() {
        final info = httpClient.get {
            path "${apiBaseUri}/v2/info"
            withHeaders accept: 'application/json'
            exchange()
        }
        info.authorization_endpoint
    }

    def defaultHeaders() {
        [accept: 'application/json', authorization: "Basic ".concat(encodeBase64String(clientId.concat(":").concat(clientSecret).getBytes())), 'content-type': 'application/x-www-form-urlencoded;charset=utf-8']
    }

}
