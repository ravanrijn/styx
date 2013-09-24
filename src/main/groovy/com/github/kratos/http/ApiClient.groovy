package com.github.kratos.http

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ApiClient {

    final String apiBaseUri
    final String uaaBaseUri
    final HttpClient httpClient

    @Autowired
    def ApiClient(HttpClient httpClient, String apiBaseUri, String uaaBaseUri) {
        this.httpClient = httpClient
        this.apiBaseUri = apiBaseUri
        this.uaaBaseUri = uaaBaseUri
    }

    def applications(token) {
        final cfApplications = httpClient.get {
            path "${apiBaseUri}/v2/apps"
            withHeaders authorization: token, accept: 'application/json'
            withQueryParams 'inline-relations-depth': 0
            exchange()
        }
        final applications = []
        cfApplications.resources.each({ cfApplication ->
            applications << [id: cfApplication.metadata.guid, name: cfApplication.entity.name]
        })
        return applications
    }

    def application(token, id) {
        final cfApplication = httpClient.get {
            path "${apiBaseUri}/v2/apps/${id}"
            withHeaders authorization: token, accept: 'application/json'
            withQueryParams 'inline-relations-depth': 3
            exchange()
        }
        final cfServices = httpClient.get {
            path "${apiBaseUri}/v2/services"
            withHeaders authorization: token, accept: 'application/json'
            withQueryParams 'inline-relations-depth': 2
            exchange()
        }
        def application = [
                id: cfApplication.metadata.guid,
                name: cfApplication.entity.name,
                memory: cfApplication.entity.memory,
                diskQuota: cfApplication.entity.disk_quota,
                state: cfApplication.entity.state,
                buildpack: '',
                instances: [],
                services: [],
                events: [],
                urls: []
        ]
        if (cfApplication.entity.buildpack == null) {
            application.buildpack = cfApplication.entity.detected_buildpack
        } else {
            application.buildpack = cfApplication.entity.buildpack
        }
        cfApplication.entity.routes.each { route ->
            final String url = "${route.entity.host}.${route.entity.domain.entity.name}"
            application.urls << url
        }
        cfApplication.entity.events.each { event ->
            application.events << [id: event.metadata.guid, status: event.entity.exit_status, description: event.entity.exit_description,
                    timestamp: event.entity.timestamp]
        }
        cfApplication.entity.service_bindings.each { binding ->
            def plan = binding.entity.service_instance.entity.service_plan
            def servicePlan = [id: plan.metadata.guid, name: plan.entity.name, description: plan.entity.description]
            def serviceType = []
            cfServices.resources.each { cfService ->
                if (cfService.metadata.guid == plan.entity.service_guid) {
                    serviceType = [id: cfService.metadata.guid, name: cfService.entity.label, description: cfService.entity.description,
                            version: cfService.entity.version]
                }
            }
            def instance = binding.entity.service_instance
            application.services << [id: instance.metadata.guid, name: instance.entity.name, plan: servicePlan, type: serviceType]
        }
        if (cfApplication.entity.state == 'STARTED') {
            final cfInstances = httpClient.get {
                path "${apiBaseUri}/v2/apps/${id}/instances"
                withHeaders authorization: token, accept: 'application/json'
                exchange()
            }
            cfInstances.each({ key, value ->
                application.instances << [id: key, state: value.state, consoleIp: value.console_ip, consolePort: value.console_port]
            })
        }
        application
    }

    def quotas(String token) {
        final cfQuotas = httpClient.get {
            path "$apiBaseUri/v2/quota_definitions"
            withHeaders authorization: token, accept: 'application/json'
            withQueryParams 'inline-relations-depth': 0
            exchange()
        }
        final quotas = []
        cfQuotas.resources.each({ cfQuota ->
            quotas << mapQuota(cfQuota)
        })
        quotas
    }

    def quota(String token, String id) {
        final cfQuota = httpClient.get {
            path "$apiBaseUri/v2/quota_definitions/$id"
            withHeaders authorization: token, accept: 'application/json'
            withQueryParams 'inline-relations-depth': 0
            exchange()
        }
        mapQuota(cfQuota)
    }

    def mapQuota(cfQuota) {
        [id: cfQuota.metadata.guid, name: cfQuota.entity.name, services: cfQuota.entity.total_services, memoryLimit: cfQuota.entity.memory_limit,
                trialDbAllowed: cfQuota.entity.trial_db_allowed, nonBasicServicesAllowed: cfQuota.entity.non_basic_services_allowed]
    }

}
