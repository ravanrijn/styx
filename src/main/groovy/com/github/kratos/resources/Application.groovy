package com.github.kratos.resources

import com.github.kratos.http.HttpClient

class Application {

    final HttpClient httpClient
    final String apiBaseUri

    def Application(HttpClient httpClient, String apiBaseUri) {
        this.httpClient = httpClient
        this.apiBaseUri = apiBaseUri
    }

    def list(String token) {
        final cfApplications = httpClient.get {
            path "$apiBaseUri/v2/apps"
            headers authorization: token, accept: 'application/json'
            queryParams 'inline-relations-depth': 0
        }
        final applications = []
        cfApplications.resources.each({ cfApplication ->
            applications << [id: cfApplication.metadata.guid, name: cfApplication.entity.name]
        })
        return applications
    }

    def get(token, id) {
        final cfApplication = httpClient.get {
            path "$apiBaseUri/v2/apps/$id"
            headers authorization: token, accept: 'application/json'
            queryParams 'inline-relations-depth': 3
        }
        def application = [
                id: cfApplication.metadata.guid,
                name: cfApplication.entity.name,
                memory: cfApplication.entity.memory,
                diskQuota: cfApplication.entity.disk_quota,
                state: cfApplication.entity.state,
                buildpack: '',
                environment: '',
                instances: [],
                services: [],
                events: [],
                urls: []
        ]
        application.buildpack = cfApplication.entity.detected_buildpack != null ? cfApplication.entity.detected_buildpack : cfApplication.entity.buildpack

        cfApplication.entity.environment_json.eachWithIndex { property, value, index ->
            application.environment += "$property=$value"
            if (index < cfApplication.entity.environment_json.size() - 1) {
                application.environment += ','
            }
        }

        cfApplication.entity.routes.each { route ->
            final String url = "${route.entity.host}.${route.entity.domain.entity.name}"
            application.urls << url
        }
        cfApplication.entity.events.each { event ->
            application.events << [id: event.metadata.guid, status: event.entity.exit_status, description: event.entity.exit_description,
                    timestamp: event.entity.timestamp]
        }

        final cfServices = httpClient.get {
            path "$apiBaseUri/v2/services"
            headers authorization: token, accept: 'application/json'
            queryParams 'inline-relations-depth': 2
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
                path "$apiBaseUri/v2/apps/$id/instances"
                headers authorization: token, accept: 'application/json'
            }
            cfInstances.each { key, value ->
                application.instances << [id: key, state: value.state, consoleIp: value.console_ip, consolePort: value.console_port]
            }
        }
        application
    }

}
