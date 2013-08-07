package com.github.styx.domain;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mvel2.MVEL.eval;
import static org.mvel2.MVEL.evalToString;

public class Service {

    private final String id;

    private final String name;

    private final String provider;

    private final String version;

    private final String description;

    private final List<ServicePlan> plans;

    public Service(String id, String name, String provider, String version, String description, List<ServicePlan> plans) {
        this.id = id;
        this.name = name;
        this.provider = provider;
        this.version = version;
        this.description = description;
        this.plans = plans;
    }

    public void addPlan(ServicePlan servicePlan) {
        plans.add(servicePlan);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProvider() {
        return provider;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public List<ServicePlan> getPlans() {
        return Collections.unmodifiableList(plans);
    }

    public static Service fromCloudFoundryModel(Object response) {
        List<ServicePlan> plans = new ArrayList<>();

        List servicePlanResource = eval("entity.?service_plans", response, List.class);
        if (servicePlanResource != null) {
            for (Object servicePlan : eval("entity.service_plans", response, List.class)) {
                plans.add(ServicePlan.fromCloudFoundryModel(servicePlan));
            }
        }
        return new Service(evalToString("metadata.guid", response),
                evalToString("entity.label", response),
                evalToString("entity.provider", response),
                evalToString("entity.version", response),
                evalToString("entity.description", response), plans);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("name", name)
                .append("provider", provider)
                .append("version", version)
                .append("description", description)
                .append("plans", plans).toString();
    }

}
