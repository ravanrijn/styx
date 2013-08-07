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
public class ServicePlanTest {

    @Test
    public void shouldFailWhenServicePlanCannotBeBuildFromCloudfoundryModel() throws IOException {
        Object resource = new ObjectMapper().readValue(new ClassPathResource("/responses/service-plan.json").getInputStream(), new TypeReference<Map<String, Object>>() {});

        ServicePlan servicePlan = ServicePlan.fromCloudFoundryModel(resource);
        assertEquals("Unexpected id", "b3706b5e-3aa1-4b0e-bed1-9931b595787b", servicePlan.getId());
        assertEquals("Unexpected name", "default", servicePlan.getName());
        assertEquals("Unexpected description", "Shared server, shared VM, 1MB memory, 10MB storage, 10 connections", servicePlan.getDescription());
    }

}
