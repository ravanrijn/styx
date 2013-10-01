package com.github.styx.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimpleOrganization extends Identifiable {

    private final String quotaId;

    @JsonCreator
    public SimpleOrganization(@JsonProperty("id") String id,@JsonProperty("name") String name,@JsonProperty("quotaId") String quotaId) {
        super(id, name);
        this.quotaId = quotaId;
    }

    public String getQuotaId() {
        return quotaId;
    }

}
