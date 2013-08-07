package com.github.styx.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class OrganizationTest {

    @Test
    public void shouldFailWhenOrganizationCannotBeBuildFromCloudfoundryModel() throws IOException {
        Object resource = new ObjectMapper().readValue(new ClassPathResource("/responses/organization.json").getInputStream(), new TypeReference<Map<String, Object>>() {});

        Organization organization = Organization.fromCloudFoundryModel(resource);
        assertEquals("Unexpected id", "c188e4c7-1641-4124-9c1f-8f17ced7bd62", organization.getId());
        assertEquals("Unexpected name", "cf.com", organization.getName());
        assertEquals("Unexpected number of users", 3, organization.getUsers().size());
        assertEquals("Unexpected number of spaces", 1, organization.getSpaces().size());
    }

}
