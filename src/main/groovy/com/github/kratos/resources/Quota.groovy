package com.github.kratos.resources

import com.github.kratos.http.HttpClient

class Quota {

    private final HttpClient httpClient
    private final String apiBaseUri

    def Quota(HttpClient httpClient, String apiBaseUri) {
        this.apiBaseUri = apiBaseUri
        this.httpClient = httpClient
    }

    def list(String token) {
        final cfQuotas = httpClient.get ({
            path "$apiBaseUri/v2/quota_definitions"
            headers authorization: token, accept: 'application/json'
            queryParams 'inline-relations-depth': 0
        })
        final quotas = []
        cfQuotas.resources.each({ cfQuota ->
            quotas << mapQuota(cfQuota)
        })
        quotas
    }

    def get(String token, String id) {
        final cfQuota = httpClient.get {
            path "$apiBaseUri/v2/quota_definitions/$id"
            headers authorization: token, accept: 'application/json'
            queryParams 'inline-relations-depth': 0
        }
        mapQuota(cfQuota)
    }

    static def mapQuota(cfQuota) {
        [id: cfQuota.metadata.guid, name: cfQuota.entity.name, services: cfQuota.entity.total_services, memoryLimit: cfQuota.entity.memory_limit,
                trialDbAllowed: cfQuota.entity.trial_db_allowed, nonBasicServicesAllowed: cfQuota.entity.non_basic_services_allowed]
    }

}
