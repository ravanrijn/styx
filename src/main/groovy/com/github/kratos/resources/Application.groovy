package com.github.kratos.resources

import java.text.DecimalFormat

class Application {

    static final int MEGA_BYTE = 1024 * 1024

    static def listTransform = { result ->
        result.resources.collect{
            cfApplication -> [id: cfApplication.metadata.guid, name: cfApplication.entity.name]
        }.sort{
            a, b -> a.name.compareTo(b.name)
        }
    }

    static def getTransform = { getDetails, cfApplication ->
        def futures = getDetails(cfApplication)
        def services = mapServices(cfApplication, futures)
        mapApplication(cfApplication, services)
    }

    static def instancesTransform = { getStats, cfInstances ->
        def futures = getStats()
        def stats = futures.findFutureById("stats")
        def instances = []
        stats.each { key, value ->
            def state
            if (cfInstances.code) {
                state = cfInstances.code == 170001 ? 'STAGING FAILED' : 'STOPPED'
            } else {
                state = cfInstances[key].state
            }
            instances << [id: key, state: state, host: value.stats?.host, port: value.stats?.port,
                    cpu: percentage(value.stats?.usage?.cpu), memory: bytes(value.stats?.usage?.mem),
                    disk: bytes(value.stats?.usage?.disk)]
        }
        instances
    }

    static def mapApplication(cfApplication, services) {
        def application = [
                id: cfApplication.metadata.guid,
                name: cfApplication.entity.name,
                memory: cfApplication.entity.memory + ' MB',
                diskQuota: cfApplication.entity.disk_quota + ' MB',
                state: cfApplication.entity.state,
                buildpack: '',
                environment: '',
                organization: [id: cfApplication.entity.space.entity.organization.metadata.guid, name: cfApplication.entity.space.entity.organization.entity.name],
                space: [id: cfApplication.entity.space.metadata.guid, name: cfApplication.entity.space.entity.name],
                services: services,
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
            application.events << [id: event.metadata.guid, instance: event.entity.instance_index, status: event.entity.exit_status,
                    description: event.entity.exit_description, timestamp: event.entity.timestamp]
        }
        application
    }

    static def mapServices(cfApplication, futures) {
        def cfServices = futures.findFutureById("services")
        def services = []
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
            services << [id: instance.metadata.guid, name: instance.entity.name, plan: servicePlan, type: serviceType]
        }
        services
    }

    static def bytes(value) {
        if (value != null) {
            return (value > MEGA_BYTE) ? format(value / MEGA_BYTE) + " MB" : format(value) + " B"
        }
        null
    }

    static def percentage(value) {
        return (value != null) ? format(value) + " %" : value
    }

    static def format(value) {
        return (value != null) ? new DecimalFormat("0.00").format(value) : value
    }

}
