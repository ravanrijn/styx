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
public class ServiceTest {

    @Test
    public void shouldFailWhenServiceCannotBeBuildFromCloudfoundryModel() throws IOException {
        Object resource = new ObjectMapper().readValue(new ClassPathResource("/responses/service.json").getInputStream(), new TypeReference<Map<String, Object>>() {});

        Service service = Service.fromCloudFoundryModel(resource);
        assertEquals("Unexpected id", "c2538b46-89a0-47d1-9f60-15698993a650", service.getId());
        assertEquals("Unexpected name", "postgresql", service.getName());
        assertEquals("Unexpected provider", "core", service.getProvider());
        assertEquals("Unexpected version", "9.1", service.getVersion());
        assertEquals("Unexpected description", "PostgreSQL database (vFabric)", service.getDescription());
    }

}
