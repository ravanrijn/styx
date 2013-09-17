package com.github.styx.domain;

public class ServiceBinding {

    private final String id;
    private final Service service;
    private final ServicePlan servicePlan;

    public ServiceBinding(String id, Service service, ServicePlan servicePlan) {
        this.id = id;
        this.service = service;
        this.servicePlan = servicePlan;
    }

    public String getId() {
        return id;
    }

    public Service getService() {
        return service;
    }

    public ServicePlan getServicePlan() {
        return servicePlan;
    }

}
