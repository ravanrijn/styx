package com.github.kratos.resources

class Application {

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
        def instances = mapInstances(futures)
        mapApplication(cfApplication, services, instances)
    }

    static def mapApplication(cfApplication, services, instances) {
        def application = [
                id: cfApplication.metadata.guid,
                name: cfApplication.entity.name,
                memory: cfApplication.entity.memory,
                diskQuota: cfApplication.entity.disk_quota,
                state: cfApplication.entity.state,
                buildpack: '',
                environment: '',
                instances: instances,
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

    static def mapInstances(futures) {
        def cfInstances = futures.findFutureById("instances")
        def instances = []
        if (cfInstances) {
            cfInstances.each { key, value ->
                instances << [id: key, state: value.state, host: value.stats?.host, port: value.stats?.port,
                                cpu: value.stats?.usage?.cpu, memory: value.stats?.usage?.mem, disk: value.stats?.usage?.disk]
            }
        }
        instances
    }

}
