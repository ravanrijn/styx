package com.github.kratos.http

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

import static com.github.kratos.resources.Application.getTransform as transformApplication
import static com.github.kratos.resources.Application.listTransform as transformApplications
import static com.github.kratos.resources.Application.instancesTransform as transformInstances
import static com.github.kratos.resources.Organization.getTransform as transformOrganization
import static com.github.kratos.resources.Organization.listTransform as transformOrganizations
import static com.github.kratos.resources.Quota.getTransform as transformQuota
import static com.github.kratos.resources.Quota.listTransform as transformQuotas
import static com.github.kratos.resources.User.cfGetTransform as transformCfUser
import static com.github.kratos.resources.User.uaaGetTransform as transformUaaUser
import static org.apache.commons.codec.binary.Base64.encodeBase64String
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE

@Service
class ApiClient {

    private final String apiBaseUri
    private final String uaaBaseUri
    private final String clientId
    private final String clientSecret
    private final HttpClient httpClient
    private final ObjectMapper mapper

    @Autowired
    def ApiClient(HttpClient httpClient, ObjectMapper mapper, String apiBaseUri, String uaaBaseUri, String clientId, String clientSecret) {
        this.clientId = clientId
        this.clientSecret = clientSecret
        this.httpClient = httpClient
        this.mapper = mapper
        this.apiBaseUri = apiBaseUri
        this.uaaBaseUri = uaaBaseUri
    }

    def applications(token) {
        httpClient.get {
            path "$apiBaseUri/v2/apps"
            headers authorization: token, accept: 'application/json'
            queryParams 'inline-relations-depth': 0
            transform transformApplications
        }
    }

    def application(token, appId){
        def getDetails = { cfApp ->
            def requests = []
            requests.add(
                    {->
                        id "services"
                        path "$apiBaseUri/v2/services"
                        headers authorization: token, accept: 'application/json'
                        queryParams 'inline-relations-depth': 2
                    }
            )
            if (cfApp.entity.state == 'STARTED') {
                requests.add(
                        {->
                            id "instances"
                            path "$apiBaseUri/v2/apps/$appId/stats"
                            headers authorization: token, accept: 'application/json'
                        }
                )
            }
            httpClient.get(requests.toArray() as Closure[])
        }
        httpClient.get {
            path "$apiBaseUri/v2/apps/${appId}"
            headers authorization: token, accept: 'application/json'
            queryParams 'inline-relations-depth': 3
            transform transformApplication.curry(getDetails)
        }
    }

    def deleteApplication(token, appId) {
        httpClient.delete {
            path "${apiBaseUri}/v2/apps/${appId}"
            headers authorization: token, accept: 'application/json'
        }
    }

    def updateApplication(token, app) {
        def intValue = { s -> Integer.parseInt(s.split(" ")[0]) }
        httpClient.put {
            path "${apiBaseUri}/v2/apps/${app.id}"
            headers authorization: token, accept: 'application/json'
            body mapper.writeValueAsString([name:app.name, memory: intValue(app.memory), disk_quota: intValue(app.diskQuota)])
            transform {result -> [id:result.metadata.guid, name:result.entity.name, memory: result.entity.memory, diskQuota: result.entity.disk_quota]}
        }
    }

    def instances(token, appId) {
        httpClient.get {
            path "$apiBaseUri/v2/apps/${appId}/stats"
            headers authorization: token, accept: 'application/json'
            transform transformInstances
        }
    }

    def findUserByUsername(token, query){
        httpClient.get{
            path "$uaaBaseUri/ids/Users"
            headers authorization: token, accept: "application/json"
            queryParams "filter": "userName like \'${query}%\'"
            transform {result -> result.resources.collect{item -> [id:item.id,username:item.userName]}}
        }
    }

    def deleteOrganizationUser(token, organizationId, userId){
        def appToken = appToken()
        def userDetails = httpClient.get{
            path "$apiBaseUri/v2/users/${userId}"
            headers authorization: "${appToken.tokenType} ${appToken.accessToken}", accept: 'application/json'
            queryParams 'inline-relations-depth': 1
        }
        def doesNotMatchProvidedOrganization = { item ->
            if(item.entity?.organization_guid){
                return item.entity.organization_guid != organizationId
            }
            return item.metadata.guid != organizationId
        }
        def updateUserRequest = [
                space_guids: userDetails.entity.spaces.findAll{space -> doesNotMatchProvidedOrganization(space)}.collect {space -> space.metadata.guid},
                organization_guids: userDetails.entity.organizations.findAll {organization -> doesNotMatchProvidedOrganization(organization)}.collect{org -> org.metadata.guid},
                managed_organization_guids: userDetails.entity.managed_organizations.findAll {organization -> doesNotMatchProvidedOrganization(organization)}.collect{org -> org.metadata.guid},
                billing_managed_organization_guids: userDetails.entity.billing_managed_organizations.findAll {organization -> doesNotMatchProvidedOrganization(organization)}.collect{org -> org.metadata.guid},
                audited_organization_guids: userDetails.entity.audited_organizations.findAll {organization -> doesNotMatchProvidedOrganization(organization)}.collect{org -> org.metadata.guid},
                managed_space_guids: userDetails.entity.managed_spaces.findAll {space -> doesNotMatchProvidedOrganization(space)}.collect{space -> space.metadata.guid},
                audited_space_guids: userDetails.entity.audited_spaces.findAll {space -> doesNotMatchProvidedOrganization(space)}.collect{space -> space.metadata.guid}
        ]
        httpClient.put{
            path "$apiBaseUri/v2/users/${userId}"
            headers authorization: "${appToken.tokenType} ${appToken.accessToken}", accept: 'application/json'
            body mapper.writeValueAsString(updateUserRequest)
            queryParams 'collection-method': 'replace'
            transform transformCfUser
        }
    }

    def updateOrganizationUser(token, id, user){
        def userDetails = httpClient.get {
            path "$apiBaseUri/v2/users/${user.id}"
            headers authorization: token, accept: 'application/json'
            queryParams 'inline-relations-depth': 1
        }
        def doesNotMatchProvidedOrganization = { item ->
            if(item.entity?.organization_guid){
                return item.entity.organization_guid != id
            }
            return item.metadata.guid != id
        }
        def updateUserRequest = [
                space_guids: userDetails.entity.spaces.findAll{space -> doesNotMatchProvidedOrganization(space)}.collect {space -> space.metadata.guid},
                organization_guids: userDetails.entity.organizations.findAll {organization -> doesNotMatchProvidedOrganization(organization)}.collect{org -> org.metadata.guid},
                managed_organization_guids: userDetails.entity.managed_organizations.findAll {organization -> doesNotMatchProvidedOrganization(organization)}.collect{org -> org.metadata.guid},
                billing_managed_organization_guids: userDetails.entity.billing_managed_organizations.findAll {organization -> doesNotMatchProvidedOrganization(organization)}.collect{org -> org.metadata.guid},
                audited_organization_guids: userDetails.entity.audited_organizations.findAll {organization -> doesNotMatchProvidedOrganization(organization)}.collect{org -> org.metadata.guid},
                managed_space_guids: userDetails.entity.managed_spaces.findAll {space -> doesNotMatchProvidedOrganization(space)}.collect{space -> space.metadata.guid},
                audited_space_guids: userDetails.entity.audited_spaces.findAll {space -> doesNotMatchProvidedOrganization(space)}.collect{space -> space.metadata.guid}
        ]
        if(updateUserRequest.organization_guids.find{org_id -> org_id == id} && user.roles.isEmpty()){
            return getTransformCfUser(userDetails)
        }
        updateUserRequest.organization_guids.add(id)
        user?.roles.each{role ->
            switch(role){
                case "BILLING_MANAGER":
                    updateUserRequest.billing_managed_organization_guids << id as String
                    break
                case "MANAGER":
                    updateUserRequest.managed_organization_guids << id as String
                    break
                case "AUDITOR":
                    updateUserRequest.audited_organization_guids << id as String
                    break
            }
        }
        httpClient.put {
            path "$apiBaseUri/v2/users/${user.id}"
            headers authorization: token, accept: 'application/json'
            body mapper.writeValueAsString(updateUserRequest)
            queryParams 'collection-method': 'add'
            transform {result -> [id:result.metadata.guid, name:result.entity.name]}
        }
    }

    def organizations(String token){
        httpClient.get {
            path "$apiBaseUri/v2/organizations"
            headers authorization: token, accept: 'application/json'
            queryParams 'inline-relations-depth': 0
            transform transformOrganizations
        }
    }

    def createOrganization(token, org){
        httpClient.post {
            path "${apiBaseUri}/v2/organizations"
            headers authorization: token, accept: 'application/json'
            body mapper.writeValueAsString([name:org.name, quota_definition_guid:org.quotaId])
            transform {result -> [id:result.metadata.guid, name:result.entity.name, quotaId:result.entity.quota_definition_guid]}
        }
    }

    def updateOrganization(token, org){
        httpClient.put {
            path "${apiBaseUri}/v2/organizations/${org.id}"
            headers authorization: token, accept: 'application/json'
            body mapper.writeValueAsString([name:org.name, quota_definition_guid:org.quotaId])
            transform {result -> [id:result.metadata.guid, name:result.entity.name, quotaId:result.entity.quota_definition_guid]}
        }
    }

    def deleteOrganization(token, id){
        httpClient.delete {
            path "${apiBaseUri}/v2/organizations/${id}"
            headers authorization: token, accept: 'application/json'
        }
    }

    def organization(String token, String orgId){
        def getDetails = { userIds, cfApps ->
            def appToken = appToken()
            def requests = cfApps.collect{cfApp ->
                { ->
                    path "${apiBaseUri}${cfApp.entity.routes_url}"
                    headers authorization: token, accept: 'application/json'
                    queryParams 'inline-relations-depth': 1
                }
            }
            if(userIds.size() > 0){
                String usernamesUri = "$uaaBaseUri/ids/Users?filter="
                userIds.each { userId -> usernamesUri = "${usernamesUri}id eq \'$userId\' or " }
                requests.add(
                    { ->
                        id "usernames"
                        path usernamesUri[0..-4]
                        headers authorization: "${appToken.tokenType} ${appToken.accessToken}" as String, accept: 'application/json'
                        transform {result -> result.resources.collect { item -> [id: item.id, username: item.userName] }}
                    }
                )
            }
            httpClient.get(requests.toArray() as Closure[])
        }
        httpClient.get {
            path "$apiBaseUri/v2/organizations/${orgId}"
            headers authorization: token, accept: 'application/json'
            queryParams 'inline-relations-depth': 2
            transform transformOrganization.curry(getDetails)
        }
    }    

    def createQuota(String token, quota) {
        httpClient.post{
            path "$apiBaseUri/v2/quota_definitions"
            headers authorization: token, accept: 'application/json'
            body mapper.writeValueAsString([name:quota.name, non_basic_services_allowed:quota.nonBasicServicesAllowed, total_services:quota.services, memory_limit:quota.memoryLimit, trial_db_allowed:quota.trialDbAllowed])
            transform transformQuota
        }
    }

    def deleteQuota(token, id) {
        httpClient.delete {
            path "$apiBaseUri/v2/quota_definitions/$id"
            headers authorization: token, accept: 'application/json'
        }
    }

    def updateQuota(token, quota) {
        httpClient.put{
            path "$apiBaseUri/v2/quota_definitions/${quota.id}"
            headers authorization: token, accept: 'application/json'
            body mapper.writeValueAsString([name:quota.name, non_basic_services_allowed:quota.nonBasicServicesAllowed, total_services:quota.services, memory_limit:quota.memoryLimit, trial_db_allowed:quota.trialDbAllowed])
            transform transformQuota
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

    def inactiveUser(String id) {
        def appToken = appToken()
        def user = httpClient.get {
            path "${apiBaseUri}/v2/users/${id}"
            headers Authorization: "${appToken.tokenType} ${appToken.accessToken}" as String, Accept: APPLICATION_JSON_VALUE, "Content-Type": APPLICATION_JSON_VALUE
            queryParams 'inline-relations-depth': 1
        }
        if (user.entity.active){
            throw new HttpClientException(HttpStatus.CONFLICT, [code:409, message: "The user has already been activated."])
        }
        httpClient.get {
            path "${uaaBaseUri}/Users/${id}"
            headers Authorization: "${appToken.tokenType} ${appToken.accessToken}" as String, Accept: APPLICATION_JSON_VALUE, "Content-Type": APPLICATION_JSON_VALUE
            transform {result -> [id:result.id, username:result.userName, organizations:user.entity.organizations.collect{org -> [id:org.metadata.guid]}]}
        }
    }

    def activateUser(user){
        if (user == null || user.password == null || user.firstname == null || user.lastname == null){
            throw new HttpClientException(HttpStatus.BAD_REQUEST, [message: "Invalid request, missing required fields."])
        }
        def appToken = appToken()
        def uaaUser = httpClient.get{
            id "uaaUser"
            path "${uaaBaseUri}/Users/${user.id}"
            headers Authorization: "${appToken.tokenType} ${appToken.accessToken}" as String, Accept: APPLICATION_JSON_VALUE, "Content-Type": APPLICATION_JSON_VALUE
        }
        final pwdChangeResponse = httpClient.put {
            path "${uaaBaseUri}/Users/${user.id}/password"
            headers Authorization: "${appToken.tokenType} ${appToken.accessToken}" as String, Accept: APPLICATION_JSON_VALUE, "Content-Type": APPLICATION_JSON_VALUE, "If-Match": "*"
            body mapper.writeValueAsString([oldPassword:user.email, password:user.password])
        }
        uaaUser.active = true
        uaaUser.name.givenName = user.firstname
        uaaUser.name.familyName = user.lastname
        final updatedUaaUser = httpClient.put {
            path "${uaaBaseUri}/Users/${user.id}"
            headers Authorization: "${appToken.tokenType} ${appToken.accessToken}" as String, Accept: APPLICATION_JSON_VALUE, "Content-Type": APPLICATION_JSON_VALUE, "If-Match": "*"
            body mapper.writeValueAsString(uaaUser)
        }
        [uaaUser:updatedUaaUser, pwdChange:pwdChangeResponse]
    }

    def createInactiveUser(request) {
        def appToken = appToken()
        def uaaUser = httpClient.post {
            path "${uaaBaseUri}/Users"
            headers Authorization: "${appToken.tokenType} ${appToken.accessToken}" as String, Accept: APPLICATION_JSON_VALUE, "Content-Type": APPLICATION_JSON_VALUE
            body mapper.writeValueAsString([username: request.email, emails: [[value: request.email]], password: request.email, name:[givenName: request.email, familyName: request.email], active:false])
        }
        httpClient.post {
            path "${apiBaseUri}/v2/users"
            headers Authorization: "${appToken.tokenType} ${appToken.accessToken}" as String, Accept: APPLICATION_JSON_VALUE, "Content-Type": APPLICATION_JSON_VALUE
            body mapper.writeValueAsString([guid: uaaUser.id])
        }
        def organization = httpClient.get {
            path "${apiBaseUri}/v2/organizations/${request.organization.id}"
            headers Authorization: "${appToken.tokenType} ${appToken.accessToken}" as String, Accept: APPLICATION_JSON_VALUE
            queryParams 'inline-relations-depth': 1
        }
        def users = organization.entity.users.collect {user -> user.metadata.guid}
        users.add(uaaUser.id)
        httpClient.put {
            path "${apiBaseUri}/v2/organizations/${request.organization.id}"
            headers Authorization: "${appToken.tokenType} ${appToken.accessToken}" as String, Accept: APPLICATION_JSON_VALUE, "Content-Type": APPLICATION_JSON_VALUE
            queryParams 'collection-method': 'add'
            body mapper.writeValueAsString([user_guids: users])
        }
        [id: uaaUser.id, organization: [id: organization.metadata.guid, name: organization.entity.name]]
    }

    def authorizationHeaders() {
        def authorization = encodeBase64String("$clientId:$clientSecret".getBytes())
        [accept: 'application/json', authorization: "Basic $authorization", 'content-type': 'application/x-www-form-urlencoded;charset=utf-8']
    }

}
