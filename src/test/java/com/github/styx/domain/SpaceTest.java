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
public class SpaceTest {

    @Test
    public void shouldFailWhenSpaceCannotBeBuildFromCloudfoundryModel() throws IOException {
        Object resource = new ObjectMapper().readValue(new ClassPathResource("/responses/space.json").getInputStream(), new TypeReference<Map<String, Object>>() {});

        Space space = Space.fromCloudFoundryModel(resource);
        assertEquals("Unexpected id", "a4341e52-a6e4-47b5-b528-578ec95df851", space.getId());
        assertEquals("Unexpected state", "development", space.getName());
        assertEquals("Unexpected number of users", 1, space.getUsers().size());
    }

}
