package com.github.styx.domain;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mvel2.MVEL.eval;
import static org.mvel2.MVEL.evalToString;

public class Application {

    private final String id;

    private final String name;

    private final String buildpack;

    private final String state;

    private final int instances;

    private final int memory;

    private final Map<String, String> environment;

    private final List<String> urls;

    private final List<ServiceInstance> serviceInstances = new ArrayList<>();

    private final List<ApplicationInstance> applicationInstances = new ArrayList<>();

    public Application(String id, String name, String buildpack, String state, int instances, int memory, Map<String, String> environment, List<String> urls) {
        this.id = id;
        this.name = name;
        this.buildpack = buildpack;
        this.state = state;
        this.instances = instances;
        this.memory = memory;
        this.environment = environment;
        this.urls = urls;
    }

    public void addInstance(ApplicationInstance instance) {
        applicationInstances.add(instance);
    }

    public void addServiceInstance(ServiceInstance instance) {
        serviceInstances.add(instance);
    }

    public String getId() {
        return id;
    }

    public String getBuildpack() {
        return buildpack;
    }

    public int getInstances() {
        return instances;
    }

    public int getMemory() {
        return memory;
    }

    public String getName() {
        return name;
    }

    public String getState() {
        return state;
    }

    public Integer getHealth() {
        if (applicationInstances.size() > 0) {
            int instancesRunning = 0;
            for (ApplicationInstance instance : applicationInstances) {
                if (instance.isRunning()) {
                    instancesRunning++;
                }
            }
            return (100 / applicationInstances.size() * instancesRunning);
        }
        return null;
    }

    public Map<String, String> getEnvironment() {
        return Collections.unmodifiableMap(environment);
    }

    public List<String> getUrls() {
        return urls;
    }

    public List<ApplicationInstance> getApplicationInstances() {
        return Collections.unmodifiableList(applicationInstances);
    }

    public List<ServiceInstance> getServiceInstances() {
        return Collections.unmodifiableList(serviceInstances);
    }

    public static Application fromCloudFoundryModel(Object response) {
        List<String> urls = new ArrayList<>();
        for (Object route : eval("entity.routes", response, List.class)) {
            String host = evalToString("entity.host", route);
            String domain = evalToString("entity.domain.entity.name", route);
            urls.add(host.concat(".").concat(domain));
        }

        Application application = new Application(evalToString("metadata.guid", response),
                evalToString("entity.name", response),
                evalToString("entity.buildpack", response),
                evalToString("entity.state", response),
                eval("entity.instances", response, int.class),
                eval("entity.memory", response, int.class),
                eval("entity.environment_json", response, Map.class), urls);
        return application;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("name", name)
                .append("buildpack", buildpack)
                .append("memory", memory)
                .append("environment", environment)
                .append("urls", urls)
                .append("applicationInstances", applicationInstances)
                .append("serviceInstances", serviceInstances).toString();
    }

}
