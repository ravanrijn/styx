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
import static org.junit.Assert.assertNotNull;

@RunWith(JUnit4.class)
public class ServiceInstanceTest {

    @Test
    public void shouldFailWhenServicePlanCannotBeBuildFromCloudfoundryModel() throws IOException {
        Object resource = new ObjectMapper().readValue(new ClassPathResource("/responses/service-instance.json").getInputStream(), new TypeReference<Map<String, Object>>() {});

        ServiceInstance serviceInstance = ServiceInstance.fromCloudFoundryModel(resource);
        assertEquals("Unexpected id", "4bb5ef8f-d37f-481a-992f-9b30c780187c", serviceInstance.getId());
        assertEquals("Unexpected name", "mysql", serviceInstance.getName());
        assertNotNull("Service plan should not be null", serviceInstance.getServicePlan());
    }

}
