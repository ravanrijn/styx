package com.github.styx.domain;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import static org.mvel2.MVEL.eval;
import static org.mvel2.MVEL.evalToString;

public class OrganizationQuota {

    private final String name;

    private final int totalServices;

    private final int memoryLimit;

    private final boolean nonBasicServicesAllowed;

    private final boolean trialDbAllowed;

    public OrganizationQuota(String name, int totalServices, int memoryLimit, boolean nonBasicServicesAllowed, boolean trialDbAllowed) {
        this.name = name;
        this.totalServices = totalServices;
        this.memoryLimit = memoryLimit;
        this.nonBasicServicesAllowed = nonBasicServicesAllowed;
        this.trialDbAllowed = trialDbAllowed;
    }

    public String getName() {
        return name;
    }

    public int getTotalServices() {
        return totalServices;
    }

    public int getMemoryLimit() {
        return memoryLimit;
    }

    public boolean isNonBasicServicesAllowed() {
        return nonBasicServicesAllowed;
    }

    public boolean isTrialDbAllowed() {
        return trialDbAllowed;
    }

    public static OrganizationQuota fromCloudFoundryModel(Object response) {
        return new OrganizationQuota(evalToString("entity.name", response),
                eval("entity.total_services", response, int.class),
                eval("entity.memory_limit", response, int.class),
                eval("entity.non_basic_services_allowed", response, boolean.class),
                eval("entity.trial_db_allowed", response, boolean.class));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("name", name)
                .append("totalServices", totalServices)
                .append("memoryLimit", memoryLimit)
                .append("nonBasicServicesAllowed", nonBasicServicesAllowed)
                .append("trialDbAllowed", trialDbAllowed).toString();
    }

}
