package com.github.styx.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.styx.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static java.lang.String.valueOf;
import static java.util.Collections.unmodifiableList;
import static org.mvel2.MVEL.eval;
import static org.mvel2.MVEL.evalToBoolean;
import static org.mvel2.MVEL.evalToString;

@Service
class DefaultCloudFoundryServices extends RemoteServices implements CloudFoundryServices {

    private static final String RESOURCE_ID = "metadata.guid";
    private static final String ENTITY_NAME = "entity.name";
    private final String baseApiUri;
    private final UaaServices uaaServices;

    @Autowired
    protected DefaultCloudFoundryServices(RestTemplate restTemplate, ObjectMapper objectMapper, String apiBaseUri, UaaServices uaaServices) {
        super(restTemplate, objectMapper);
        this.baseApiUri = sanitizeBaseUri(apiBaseUri);
        this.uaaServices = uaaServices;
    }

    private List<User> mapOrganizationUsers(final String token, final Object organization) {
        final Set<String> orgManagers = new HashSet<>();
        for (final Object managerResponse : eval("entity.managers", organization, List.class)) {
            orgManagers.add(evalToString(RESOURCE_ID, managerResponse));
        }
        final Set<String> orgAuditors = new HashSet<>();
        for (final Object auditorResponse : eval("entity.auditors", organization, List.class)) {
            orgAuditors.add(evalToString(RESOURCE_ID, auditorResponse));
        }
        final Set<String> orgBillingManagers = new HashSet<>();
        for (final Object billingManagerResponse : eval("entity.billing_managers", organization, List.class)) {
            orgBillingManagers.add(evalToString(RESOURCE_ID, billingManagerResponse));
        }
        final List<User> users = new ArrayList<>();
        for (final Object userResponse : eval("entity.users", organization, List.class)) {
            final String userId = evalToString(RESOURCE_ID, userResponse);
            final boolean admin = eval("entity.admin", userResponse, Boolean.class);
            final Set<Role> roles = new HashSet<>();
            if (orgAuditors.contains(userId)) {
                roles.add(Role.ORGANIZATION_AUDITOR);
            }
            if (orgManagers.contains(userId)) {
                roles.add(Role.ORGANIZATION_MANAGER);
            }
            if (orgBillingManagers.contains(userId)) {
                roles.add(Role.ORGANIZATION_BILLING_MANAGER);
            }
            if(admin){
                roles.add(Role.EINDBAAS);
            }
            users.add(new User(userId, null, roles));
        }
        return uaaServices.appendUserNames(token, users);
    }

    private List<User> mapSpaceUsers(final Object space, List<User> organizationUsers) {
        final Set<String> spaceDevelopers = new HashSet<>();
        for (Object developerResponse : eval("entity.developers", space, List.class)) {
            spaceDevelopers.add(evalToString(RESOURCE_ID, developerResponse));
        }
        final Set<String> spaceManagers = new HashSet<>();
        for (Object managerResponse : eval("entity.managers", space, List.class)) {
            spaceManagers.add(evalToString(RESOURCE_ID, managerResponse));
        }
        final Set<String> spaceAuditors = new HashSet<>();
        for (Object auditorResponse : eval("entity.managers", space, List.class)) {
            spaceAuditors.add(evalToString(RESOURCE_ID, auditorResponse));
        }
        final List<User> spaceUsers = new ArrayList<>();
        for (User user : organizationUsers) {
            final Set<Role> roles = new HashSet<>();
            if (spaceDevelopers.contains(user.getId())) {
                roles.add(Role.SPACE_DEVELOPER);
            }
            if (spaceManagers.contains(user.getId())) {
                roles.add(Role.SPACE_MANAGER);
            }
            if (spaceAuditors.contains(user.getId())) {
                roles.add(Role.SPACE_AUDITOR);
            }
            if (!roles.isEmpty()) {
                spaceUsers.add(new User(user.getId(), user.getUsername(), roles));
            }
        }
        return unmodifiableList(spaceUsers);
    }

    private Organization mapOrganization(final String token, final Object organization) {
        final String orgId = evalToString(RESOURCE_ID, organization);
        final String orgName = evalToString(ENTITY_NAME, organization);
        final Quota quota = new Quota(evalToString("entity.quota_definition.entity.name", organization),
                eval("entity.quota_definition.entity.total_services", organization, Integer.class),
                eval("entity.quota_definition.entity.memory_limit", organization, Integer.class),
                eval("entity.quota_definition.entity.non_basic_services_allowed", organization, Boolean.class),
                eval("entity.quota_definition.entity.trial_db_allowed", organization, Boolean.class));
        final List<User> organizationUsers = mapOrganizationUsers(token, organization);
        final List<Space> spaces = new ArrayList<>();
        for (final Object spaceResponse : eval("entity.spaces", organization, List.class)) {

            final List<Application> applications = new ArrayList<>();
            for (Object applicationResponse : eval("entity.apps", spaceResponse, List.class)) {
                applications.add(new Application(evalToString("metadata.guid", applicationResponse), evalToString(ENTITY_NAME, applicationResponse), evalToString("entity.memory", applicationResponse), eval("entity.instances", applicationResponse, Integer.class), ApplicationState.valueOf(evalToString("entity.state", applicationResponse))));
            }
            spaces.add(new Space(evalToString(RESOURCE_ID, spaceResponse), evalToString(ENTITY_NAME, spaceResponse), null, /*mapSpaceUsers(spaceResponse, organizationUsers),*/ applications));
        }
        final List<Domain> domains = new ArrayList<>();
        for (final Object domainResponse : eval("entity.domains", organization, List.class)) {
            domains.add(new Domain(evalToString(RESOURCE_ID, domainResponse), evalToString(ENTITY_NAME, domainResponse)));
        }
        return new Organization(orgId, orgName, quota, domains, spaces, organizationUsers);
    }

    @Override
    public boolean isUserAdmin(String id){
        final Map<String, Object> userResponse = get(uaaServices.getApplicationAccessToken(), baseApiUri.concat("v2/users/".concat(id).concat("?inline-relations-depth=".concat(valueOf(0)))));
        return evalToBoolean("entity.admin", userResponse);
    }

    @Override
    public List<Organization> getOrganizations(String token) {
        final Map<String, Object> organizationsResponse = get(token, baseApiUri.concat("v2/organizations?inline-relations-depth=".concat(valueOf(0))));
        final List<Organization> organizations = new ArrayList<>();
        for (Object organization : eval("resources", organizationsResponse, List.class)) {
            organizations.add(new Organization(evalToString(RESOURCE_ID, organization), evalToString(ENTITY_NAME, organization), null, null, null, null));
        }
        return unmodifiableList(organizations);
    }

    @Override
    public Organization getOrganization(final String token, final String id) {
        return mapOrganization(token, get(token, baseApiUri.concat("v2/organizations/").concat(id).concat("?inline-relations-depth=".concat(valueOf(3)))));
    }

}
