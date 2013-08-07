package com.github.styx.domain;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import static org.mvel2.MVEL.eval;
import static org.mvel2.MVEL.evalToString;

public class ServicePlan {

    private final String id;

    private final String name;

    private final String description;

    private final Service service;

    public ServicePlan(String id, String name, String description, Service service) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.service = service;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Service getService() {
        return service;
    }

    public static ServicePlan fromCloudFoundryModel(Object response) {
        Service service = null;
        Object serviceResource = eval("entity.?service", response, Object.class);
        if (serviceResource != null) {
            service = Service.fromCloudFoundryModel(serviceResource);
        }
        return new ServicePlan(evalToString("metadata.guid", response),
                evalToString("entity.name", response),
                evalToString("entity.description", response), service);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("name", name)
                .append("description", description)
                .append("service", service).toString();
    }


}
