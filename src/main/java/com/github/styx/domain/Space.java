package com.github.styx.domain;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.*;

import static java.util.Collections.unmodifiableList;
import static org.mvel2.MVEL.eval;
import static org.mvel2.MVEL.evalToString;

public class Space {

    private final String id;
    private final String name;
    private final List<SpaceUser> users;

    private final List<Application> applications = new ArrayList<>();

    private final List<ServiceInstance> serviceInstances = new ArrayList<>();

    public Space(final String id, final String name, final List<SpaceUser> users) {
        this.id = id;
        this.name = name;
        this.users = users;
    }

    public void addApplication(Application application) {
        applications.add(application);
    }

    public void addServiceInstance(ServiceInstance serviceInstance) {
        serviceInstances.add(serviceInstance);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Application> getApplications() {
        return Collections.unmodifiableList(applications);
    }

    public List<ServiceInstance> getServiceInstances() {
        return Collections.unmodifiableList(serviceInstances);
    }

    public List<SpaceUser> getUsers() {
        return users;
    }

    public static Space fromCloudFoundryModel(Object response) {
        final Map<String, SpaceUser.Builder> spaceUserBuilders = new HashMap<>();
        for (Object developer : eval("entity.developers", response, List.class)) {
            final String id = eval("metadata.guid", developer, String.class);
            if (spaceUserBuilders.containsKey(id)) {
                spaceUserBuilders.get(id).setDeveloperRole();
                continue;
            }
            spaceUserBuilders.put(id, SpaceUser.Builder.newBuilder(id).setDeveloperRole());
        }
        for (Object manager : eval("entity.managers", response, List.class)) {
            final String id = eval("metadata.guid", manager, String.class);
            if (spaceUserBuilders.containsKey(id)) {
                spaceUserBuilders.get(id).setManagerRole();
                continue;
            }
            spaceUserBuilders.put(id, SpaceUser.Builder.newBuilder(id).setManagerRole());
        }
        for (Object auditor : eval("entity.auditors", response, List.class)) {
            final String id = eval("metadata.guid", auditor, String.class);
            if (spaceUserBuilders.containsKey(id)) {
                spaceUserBuilders.get(id).setAuditorRole();
                continue;
            }
            spaceUserBuilders.put(id, SpaceUser.Builder.newBuilder(id).setAuditorRole());
        }
        final List<SpaceUser> spaceUsers = new ArrayList<>();
        for (SpaceUser.Builder builder : spaceUserBuilders.values()) {
            spaceUsers.add(builder.build());
        }
        return new Space(evalToString("metadata.guid", response), evalToString("entity.name", response), unmodifiableList(spaceUsers));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("name", name)
                .append("applications", applications)
                .append("serviceInstances", serviceInstances)
                .append("users", users).toString();
    }

}
