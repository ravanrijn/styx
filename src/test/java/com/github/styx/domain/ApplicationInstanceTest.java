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
public class ApplicationInstanceTest {

    @Test
    public void shouldFailWhenApplicationInstanceCannotBeBuildFromCloudfoundryModel() throws IOException {
        Object resource = new ObjectMapper().readValue(new ClassPathResource("/responses/instance.json").getInputStream(), new TypeReference<Map<String, Object>>() {});

        ApplicationInstance instance = ApplicationInstance.fromCloudFoundryModel("0", resource);
        assertEquals("Unexpected id", "0", instance.getId());
        assertEquals("Unexpected state", "RUNNING", instance.getState());
        assertEquals("Unexpected console ip", "172.21.28.149", instance.getConsoleIp());
        assertEquals("Unexpected console port", 61012, instance.getConsolePort());
    }

}
