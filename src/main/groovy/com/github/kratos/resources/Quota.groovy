package com.github.kratos.resources

class Quota {

    static def listTransform = { result ->
        result.resources.collect{ cfQuota ->
            Quota.getTransform(cfQuota)
        }
    }

    static def getTransform = { result ->
        [id: result.metadata.guid, name: result.entity.name, services: result.entity.total_services, memoryLimit: result.entity.memory_limit,
                trialDbAllowed: result.entity.trial_db_allowed, nonBasicServicesAllowed: result.entity.non_basic_services_allowed]
    }

}
