package com.github.styx.domain;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.*;

import static org.mvel2.MVEL.eval;
import static org.mvel2.MVEL.evalToString;

public class Organization {

    private final String id;
    private final String name;
    private final OrganizationQuota quota;
    private final List<OrganizationUser> users;
    private final List<Space> spaces;
    private final List<String> domains;

    public Organization(final String id, final String name, final OrganizationQuota quota, final List<OrganizationUser> users, final List<Space> spaces, final List<String> domains) {
        this.id = id;
        this.name = name;
        this.quota = quota;
        this.users = users;
        this.spaces = spaces;
        this.domains = domains;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public OrganizationQuota getQuota() {
        return quota;
    }

    public List<OrganizationUser> getUsers() {
        return users;
    }

    public List<Space> getSpaces() {
        return spaces;
    }

    public List<String> getDomains() {
        return domains;
    }

    public static Organization fromCloudFoundryModel(final Object response) {
        final List<OrganizationUser> orgUsers = new ArrayList<>();
        final List<Space> spaces = new ArrayList<>();
        final Map<String, OrganizationUser.Builder> organizationUserBuilders = new HashMap<>();
        for (Object user : eval("entity.users", response, List.class)) {
            final String id = eval("metadata.guid", user, String.class);
            organizationUserBuilders.put(id, OrganizationUser.Builder.newBuilder(id));
        }
        for (Object user : eval("entity.managers", response, List.class)) {
            final String id = eval("metadata.guid", user, String.class);
            if(organizationUserBuilders.containsKey(id)){
                organizationUserBuilders.get(id).setManagerRole();
                continue;
            }
            organizationUserBuilders.put(id, OrganizationUser.Builder.newBuilder(id).setManagerRole());
        }
        for (Object user : eval("entity.billing_managers", response, List.class)) {
            final String id = eval("metadata.guid", user, String.class);
            if(organizationUserBuilders.containsKey(id)){
                organizationUserBuilders.get(id).setBillingManager();
                continue;
            }
            organizationUserBuilders.put(id, OrganizationUser.Builder.newBuilder(id).setBillingManager());
        }
        for (Object user : eval("entity.auditors", response, List.class)) {
            final String id = eval("metadata.guid", user, String.class);
            if(organizationUserBuilders.containsKey(id)){
                organizationUserBuilders.get(id).setAuditorRole();
                continue;
            }
            organizationUserBuilders.put(id, OrganizationUser.Builder.newBuilder(id).setAuditorRole());
        }
        for(OrganizationUser.Builder builder : organizationUserBuilders.values()){
            orgUsers.add(builder.build());
        }
        for (Object space : eval("entity.spaces", response, List.class)) {
            spaces.add(Space.fromCloudFoundryModel(space));
        }

        OrganizationQuota organizationQuota = OrganizationQuota.fromCloudFoundryModel(eval("entity.quota_definition", response, Map.class));

        List<String> domains = new ArrayList<>();
        for (Object domain : eval("entity.domains", response, List.class)) {
            domains.add(eval("entity.name", domain, String.class));
        }
        return new Organization(evalToString("metadata.guid", response), evalToString("entity.name", response), organizationQuota, Collections.unmodifiableList(orgUsers), Collections.unmodifiableList(spaces), domains);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("name", name)
                .append("quota", quota)
                .append("users", users)
                .append("spaces", spaces)
                .append("domains", domains).toString();
    }

}
