package com.github.styx.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.styx.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
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

    private Application mapApplication(final Map<String, Object> applicationResponse, final Map<String, Object> instancesResponse) {
        final String appId = evalToString(RESOURCE_ID, applicationResponse);
        final String appName = evalToString(ENTITY_NAME, applicationResponse);
        final String buildPack = evalToString("entity.buildpack", applicationResponse);
        final String environment = mapEnvironment(applicationResponse);
        final String memory = evalToString("entity.memory", applicationResponse);
        final String diskQuota = evalToString("entity.disk_quota", applicationResponse);
        final ApplicationState state = ApplicationState.valueOf(evalToString("entity.state", applicationResponse));

        final List<String> urls = new ArrayList<>();
        for (Object route : eval("entity.routes", applicationResponse, List.class)) {
            final String host = evalToString("entity.host", route);
            final String domain = evalToString("entity.domain.entity.name", route);
            urls.add(host.concat(".").concat(domain));
        }
        final List<Event> events = new ArrayList<>();
        for (Object event : eval("entity.events", applicationResponse, List.class)) {
            events.add(new Event(evalToString("id", event), evalToString("status", event), evalToString("description", event), evalToString("timestamp", event)));
        }

        final List<ServiceBinding> serviceBindings = mapServiceBindings(applicationResponse);
        final List<Instance> instances = mapInstances(instancesResponse);
        return new Application(appId, appName, buildPack, environment, memory, diskQuota, urls, serviceBindings, instances, events, state);
    }

    private String mapEnvironment(final Object application) {
        final Map<String, Object> environmentJson = eval("application.environment_json", application, Map.class);

        final StringBuilder environment = new StringBuilder();
        for (Map.Entry<String, Object> entry : environmentJson.entrySet()) {
            environment.append(entry.getKey()).append("=").append(entry.getValue()).append(", ");
        }
        return environment.substring(0, environment.length() - 2).toString();
    }

    private List<ServiceBinding> mapServiceBindings(final Object application) {
        final List<ServiceBinding> serviceBindings = new ArrayList<>();
        for (Object serviceBinding : eval("application.service_bindings", application, List.class)) {
            final String serviceBindingId = evalToString(RESOURCE_ID, serviceBinding);

            serviceBindings.add(new ServiceBinding(serviceBindingId, null, null)); // TODO
        }
        return serviceBindings;
    }

    private List<Instance> mapInstances(final Map<String, Object> instancesResponse) {
        final List<Instance> instances = new ArrayList<>();
        for (Map.Entry<String, Object> entry : instancesResponse.entrySet()) {
            final String instanceId = entry.getKey();
            final Object instance = entry.getValue();
            instances.add(new Instance(instanceId, InstanceState.valueOf(evalToString("state", instance)), evalToString("since", instance), evalToString("consoleIp", instance), eval("consolePort", instance, Integer.class)));
        }
        return instances;
    }

    private Organization mapOrganization(final String token, final Object organization) {
        final String orgId = evalToString(RESOURCE_ID, organization);
        final String orgName = evalToString(ENTITY_NAME, organization);
        final Quota quota = new Quota(evalToString("entity.quota_definition_guid", organization),
                evalToString("entity.quota_definition.entity.name", organization),
                eval("entity.quota_definition.entity.total_services", organization, Integer.class),
                eval("entity.quota_definition.entity.memory_limit", organization, Integer.class),
                eval("entity.quota_definition.entity.non_basic_services_allowed", organization, Boolean.class),
                eval("entity.quota_definition.entity.trial_db_allowed", organization, Boolean.class));
        final List<User> organizationUsers = mapOrganizationUsers(token, organization);
        final List<Space> spaces = new ArrayList<>();
        for (final Object spaceResponse : eval("entity.spaces", organization, List.class)) {

            final List<SimpleApplication> applications = new ArrayList<>();
            for (Object applicationResponse : eval("entity.apps", spaceResponse, List.class)) {
                final List<String> urls = new ArrayList<>();
                for (Object route : eval("entity.routes", applicationResponse, List.class)) {
                    final String host = evalToString("entity.host", route);
                    final String domain = evalToString("entity.domain.entity.name", route);
                    urls.add(host.concat(".").concat(domain));
                }
                final List<String> serviceBindings = new ArrayList<>();
                for(Object serviceBinding : eval("entity.service_bindings", applicationResponse, List.class)){
                    serviceBindings.add(evalToString(RESOURCE_ID, serviceBinding));
                }
                applications.add(new SimpleApplication(evalToString("metadata.guid", applicationResponse), evalToString(ENTITY_NAME, applicationResponse), evalToString("entity.memory", applicationResponse), urls, serviceBindings, eval("entity.instances", applicationResponse, Integer.class), ApplicationState.valueOf(evalToString("entity.state", applicationResponse))));
            }
            spaces.add(new Space(evalToString(RESOURCE_ID, spaceResponse), evalToString(ENTITY_NAME, spaceResponse), mapSpaceUsers(spaceResponse, organizationUsers), applications));
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
    public List<SimpleOrganization> getOrganizations(String token) {
        final Map<String, Object> organizationsResponse = get(token, baseApiUri.concat("v2/organizations?inline-relations-depth=".concat(valueOf(0))));
        final List<SimpleOrganization> organizations = new ArrayList<>();
        for (Object organization : eval("resources", organizationsResponse, List.class)) {
            organizations.add(new SimpleOrganization(evalToString(RESOURCE_ID, organization), evalToString(ENTITY_NAME, organization), evalToString("entity.quota_definition_guid", organization)));
        }
        return unmodifiableList(organizations);
    }

    @Override
    public List<Quota> getPlans(String token) {
        final Map<String, Object> quotaDefinitionsResponse = get(token, baseApiUri.concat("v2/quota_definitions"));
        final List<Quota> plans = new ArrayList<>();
        for(Object quotaDefinition : eval("resources", quotaDefinitionsResponse, List.class)){
            plans.add(new Quota(evalToString(RESOURCE_ID, quotaDefinition),
                    evalToString(ENTITY_NAME, quotaDefinition),
                    eval("entity.total_services", quotaDefinition, Integer.class),
                    eval("entity.memory_limit", quotaDefinition, Integer.class),
                    eval("entity.non_basic_services_allowed", quotaDefinition, Boolean.class),
                    eval("entity.trial_db_allowed", quotaDefinition, Boolean.class)));
        }
        return plans;
    }

    @Override
    public ResponseEntity createQuota(String token, Quota quota) {
        final String quotaRequest = "{\"name\":\"".concat(quota.getName()).concat("\",\"non_basic_services_allowed\":").concat(Boolean.toString(quota.isNonBasicServicesAllowed())).concat(",\"total_services\":").concat(Integer.toString(quota.getServices())).concat(",\"memory_limit\":").concat(Integer.toString(quota.getMemoryLimit())).concat(",\"trial_db_allowed\":").concat(Boolean.toString(quota.isTrialDbAllowed())).concat("}");
        return post(baseApiUri.concat("v2/quota_definitions"), getDefaultHeaders(token), quotaRequest);
    }

    @Override
    public ResponseEntity deleteQuota(String token, String id) {
        return delete(baseApiUri.concat("v2/quota_definitions/").concat(id), getDefaultHeaders(token), null);
    }

    @Override
    public ResponseEntity updateQuota(String token, Quota quota) {
        final String quotaRequest = "{\"name\":\"".concat(quota.getName()).concat("\",\"non_basic_services_allowed\":").concat(Boolean.toString(quota.isNonBasicServicesAllowed())).concat(",\"total_services\":").concat(Integer.toString(quota.getServices())).concat(",\"memory_limit\":").concat(Integer.toString(quota.getMemoryLimit())).concat(",\"trial_db_allowed\":").concat(Boolean.toString(quota.isTrialDbAllowed())).concat("}");
        return put(token, baseApiUri.concat("v2/quota_definitions/").concat(quota.getId()), quotaRequest);
    }

    @Override
    public Application getApplication(String token, String id) {
        final Map<String, Object> applicationResponse = get(token, baseApiUri.concat("v2/apps/").concat(id).concat("?inline-relations-depth=".concat(valueOf(2))));
        final Map<String, Object> instancesResponse = get(token, baseApiUri.concat("v2/apps/".concat(id).concat("/instances")));
        return mapApplication(applicationResponse, instancesResponse);
    }

    @Override
    public Organization getOrganization(final String token, final String id) {
        return mapOrganization(token, get(token, baseApiUri.concat("v2/organizations/").concat(id).concat("?inline-relations-depth=".concat(valueOf(4)))));
    }

    @Override
    public ResponseEntity createOrganization(String token, SimpleOrganization organization) {
        final String organizationRequest = "{\"name\":\"".concat(organization.getName()).concat("\",\"quota_definition_guid\":\"").concat(organization.getQuotaId()).concat("\"}");
        return post(baseApiUri.concat("v2/organizations"), getDefaultHeaders(token), organizationRequest);
    }

    @Override
    public ResponseEntity updateOrganization(String token, SimpleOrganization organization) {
        final String request = "{\"name\":\"".concat(organization.getName()).concat("\",\"quota_definition_guid\":\"").concat(organization.getQuotaId()).concat("\"}");
        return put(token, baseApiUri.concat("v2/organizations/").concat(organization.getId()), request);
    }

    @Override
    public ResponseEntity deleteOrganization(String token, String id) {
        return delete(baseApiUri.concat("v2/organizations/").concat(id), getDefaultHeaders(token), null);
    }

    private HttpHeaders getDefaultHeaders(String token){
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Accept", "application/json;charset=utf-8");
        httpHeaders.add("Content-Type", "application/json;charset=utf-8");
        httpHeaders.add("Authorization", token);
        return httpHeaders;
    }

}
