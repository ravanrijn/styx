package com.github.kratos.http

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class ApiClient extends RestClient {

    final String apiBaseUri
    final String uaaBaseUri

    @Autowired
    def ApiClient(RestTemplate restTemplate, String apiBaseUri, String uaaBaseUri, ObjectMapper objectMapper) {
        super(restTemplate, objectMapper);
        this.apiBaseUri = apiBaseUri;
        this.uaaBaseUri = uaaBaseUri;
    }

    def application(token, id) {
        def cfApplication = get([path: "${apiBaseUri}/v2/apps/${id}", headers: ['Authorization': token]])
    }

    def quotas(String token) {
        final cfQuotas = get([path: "${apiBaseUri}/v2/quota_definitions", headers: ['Authorization': token]])
        def quotas = [:]
        cfQuotas.each({ cfQuota ->
            quotas << mapQuota(cfQuota)
        })
        return quotas
    }

    def quota(String token, String id) {
        mapQuota(get([path: "${apiBaseUri}/v2/quota_definitions/${id}", headers: ['Authorization': token], params: ['inline-relations-depth': 0]]))
    }

    def mapQuota(cfQuota) {
        [id: cfQuota.metadata.guid, name: cfQuota.entity.name, services: cfQuota.entity.total_services, memoryLimit: cfQuota.entity.memory_limit,
                trialDbAllowed: cfQuota.entity.trial_db_allowed, nonBasicServicesAllowed: cfQuota.entity.non_basic_services_allowed]
    }

}
