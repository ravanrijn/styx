package com.github.styx.domain;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

import static org.mvel2.MVEL.eval;
import static org.mvel2.MVEL.evalToString;

public class UserInfo {

    private final String id;

    private final String userName;

    private final String firstName;

    private final String lastName;

    private final List<String> emailAddresses;

    private final List<String> organizations;

    public UserInfo(String id, String userName, String firstName, String lastName, List<String> emailAddresses, List<String> organizations) {
        this.id = id;
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddresses = emailAddresses;
        this.organizations = organizations;
    }

    public String getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public List<String> getEmailAddresses() {
        return emailAddresses;
    }

    public List<String> getOrganizations() {
        return organizations;
    }

    public static UserInfo fromCloudFoundryModel(Object uaaResponse, Object apiResponse) {
        List<String> emailAddresses = new ArrayList<>();
        for (Object emailAddress : eval("emails", uaaResponse, List.class)) {
            emailAddresses.add(evalToString("value", emailAddress));
        }

        List<String> organizations = new ArrayList<>();
        for (Object organization : eval("entity.organizations", apiResponse, List.class)) {
            organizations.add(evalToString("entity.name", organization));
        }

        return new UserInfo(evalToString("id", uaaResponse), evalToString("userName", uaaResponse),
                evalToString("name.?firstName", uaaResponse), evalToString("name.?lastName", uaaResponse),
                emailAddresses, organizations);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("userName", userName)
                .append("firstName", firstName)
                .append("lastName", lastName)
                .append("emailAddresses", emailAddresses)
                .append("organizations", organizations).toString();
    }

}
