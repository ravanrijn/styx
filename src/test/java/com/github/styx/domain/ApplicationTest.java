package com.github.styx.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class ApplicationTest {

    @Test
    public void shouldFailWhenApplicationCannotBeBuildFromCloudfoundryModel() throws IOException {
        Object resource = new ObjectMapper().readValue(new ClassPathResource("/responses/application.json").getInputStream(), new TypeReference<Map<String, Object>>() {});

        Application application = Application.fromCloudFoundryModel(resource);
        assertEquals("Unexpected id", "bc931cab-f695-40e5-93f8-5e82215e9ea8", application.getId());
        assertEquals("Unexpected name", "styx", application.getName());
        assertEquals("Unexpected buildpack", "git://github.com/cloudfoundry/cloudfoundry-buildpack-java.git", application.getBuildpack());
        assertEquals("Unexpected state", "STARTED", application.getState());
        assertEquals("Unexpected memory", 1024, application.getMemory());
    }

    @Test
    public void shouldFailWhenHealthIsNot100() {
        Application application = new Application("123", "styx", "Spring", "STARTED", 1, 512, new ImmutableMap.Builder<String, String>().put("key", "value").build(), Arrays.asList("styx.cf.com"));
        application.addInstance(new ApplicationInstance("0", "RUNNING", "127.0.0.1", 80));
        application.addInstance(new ApplicationInstance("1", "RUNNING", "127.0.0.1", 80));
        assertEquals("Unexpected health", Integer.valueOf(100), application.getHealth());
    }

    @Test
    public void shouldFailWhenHealthIsNot50() {
        Application application = new Application("123", "styx", "Spring", "STARTED", 1, 512, new ImmutableMap.Builder<String, String>().put("key", "value").build(), Arrays.asList("styx.cf.com"));
        application.addInstance(new ApplicationInstance("0", "RUNNING", "127.0.0.1", 80));
        application.addInstance(new ApplicationInstance("1", "FLAPPING", "127.0.0.1", 80));
        assertEquals("Unexpected health", Integer.valueOf(50), application.getHealth());
    }

}
