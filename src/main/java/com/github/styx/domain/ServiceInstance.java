package com.github.styx.domain;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.List;

import static org.mvel2.MVEL.eval;
import static org.mvel2.MVEL.evalToString;

public class ServiceInstance {

    private final String id;

    private final String name;

    private final int boundApplications;

    private final ServicePlan servicePlan;

    public ServiceInstance(String id, String name, int boundApplications, ServicePlan servicePlan) {
        this.id = id;
        this.name = name;
        this.boundApplications = boundApplications;
        this.servicePlan = servicePlan;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getBoundApplications() {
        return boundApplications;
    }

    public ServicePlan getServicePlan() {
        return servicePlan;
    }

    public static ServiceInstance fromCloudFoundryModel(Object response) {
        ServicePlan servicePlan = null;
        Object servicePlanResource = eval("entity.?service_plan", response, Object.class);
        if (servicePlanResource != null) {
            servicePlan = ServicePlan.fromCloudFoundryModel(servicePlanResource);
        }

        int serviceBindings = 0;
        List serviceBindingsResource = eval("entity.?service_bindings", response, List.class);
        if (serviceBindingsResource != null) {
            serviceBindings = serviceBindingsResource.size();
        }

        return new ServiceInstance(evalToString("metadata.guid", response),
                evalToString("entity.name", response),
                serviceBindings,
                servicePlan);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("name", name)
                .append("boundApplications", boundApplications)
                .append("servicePlan", servicePlan).toString();
    }

}
