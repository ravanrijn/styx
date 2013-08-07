package com.github.styx.domain;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import static org.mvel2.MVEL.evalToString;

public class UserDetails {

    private final String accessToken;

    private final String tokenType;

    private String id;

    private String username;

    public UserDetails(String accessToken, String tokenType) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public static UserDetails fromCloudFoundryModel(Object response) {
        return new UserDetails(evalToString("access_token", response), evalToString("token_type", response));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("accessToken", accessToken)
                .append("tokenType", tokenType)
                .append("id", id)
                .append("username", username).toString();
    }

}
