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

    def applications(token) {
        def cfApplications = get(path: "${apiBaseUri}/v2/apps", headers: [authorization: token])

        def applications = []
        cfApplications.resources.each({ cfApplication ->
            applications << [id: cfApplication.metadata.guid, name: cfApplication.entity.name]
        })
        return applications
    }

    def application(token, id) {
        def cfApplication = get([path: "${apiBaseUri}/v2/apps/${id}", headers: ['Authorization': token], params: ['inline-relations-depth': 3]])

        def buildpack = ''
        if (cfApplication.entity.buildpack == null) {
            cfApplication.entity.detected_buildpack
        } else {
            cfApplication.entity.buildpack
        }

        def application = [id: cfApplication.metadata.guid, name: cfApplication.entity.name, memory: cfApplication.entity.memory, diskQuota: cfApplication.entity.disk_quota,
                state: cfApplication.entity.state, buildpack: buildpack]

        def urls = []
        for (route in cfApplication.entity.routes) {
            def host = route.entity.host
            def domain = route.entity.domain.entity.name
            urls.add(host.concat('.').concat(domain))
        }
        if (!urls.isEmpty()) {
            application.put('urls', urls)
        }

        def events = []
        for (event in cfApplication.entity.events) {
            events.add([id: event.metadata.guid, status: event.entity.exit_status, description: event.entity.exit_description,
                    timestamp: event.entity.timestamp])
        }
        if (!events.isEmpty()) {
            application.put('events', events)
        }

        def cfServices = get([path: "${apiBaseUri}/v2/services", headers: ['Authorization': token]])

        def services = []
        for (binding in cfApplication.entity.service_bindings) {
            def plan = binding.entity.service_instance.entity.service_plan
            def servicePlan = [id: plan.metadata.guid, name: plan.entity.name, description: plan.entity.description]

            def serviceType = []
            for (cfService in cfServices.resources) {
                if (cfService.metadata.guid == plan.entity.service_guid) {
                    serviceType = [id: cfService.metadata.guid, name: cfService.entity.label, description: cfService.entity.description,
                            version: cfService.entity.version]
                }
            }

            def instance = binding.entity.service_instance
            services.add([id: instance.metadata.guid, name: instance.entity.name, plan: servicePlan, type: serviceType])
        }
        if (!services.isEmpty()) {
            application.put('services', services)
        }

        if (cfApplication.entity.state == 'STARTED') {
            def cfInstances = get([path: "${apiBaseUri}/v2/apps/${id}/instances", headers: ['Authorization': token]])

            def instances = []
            cfInstances.each({key, value ->
                instances << [id: key, state: value.state, consoleIp: value.console_ip, consolePort: value.console_port]
            })
            if (!instances.isEmpty()) {
                application.put('instances', instances)
            }
        }

        return application
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
