package com.github.styx.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@RunWith(JUnit4.class)
public class SpaceRepositoryTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationRepositoryTest.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    private SpaceRepository spaceRepository;

    private RestTemplate restTemplate;

    private ApplicationRepository applicationRepository;

    @Before
    public void setUp() throws Exception {
        restTemplate = mock(RestTemplate.class);
        applicationRepository = mock(ApplicationRepository.class);
        spaceRepository = new SpaceRepository(restTemplate, new ConcurrentTaskExecutor(), new ObjectMapper(), "/api/", "/uaa/", applicationRepository);
    }

    @Test
    public void testDeleteByIdShouldFailWhenSpaceIsNotDeleted() throws IOException {
        Map<String, Object> spaceResponse = objectMapper.readValue(new ClassPathResource("/responses/space.json").getInputStream(), new TypeReference<Map<String, Object>>() {});

        when(restTemplate.exchange(eq("/api/v2/spaces/123?inline-relations-depth=1"), eq(HttpMethod.GET), isA(HttpEntity.class), isA(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity(spaceResponse, HttpStatus.OK));
        when(restTemplate.exchange(eq("/api/v2/spaces/123"), eq(HttpMethod.DELETE), isA(HttpEntity.class), isA(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity(HttpStatus.NO_CONTENT));

        spaceRepository.deleteById("bearer: 123", "123");

        verify(applicationRepository).deleteById("bearer: 123", "bc931cab-f695-40e5-93f8-5e82215e9ea8");
    }

    @Test
    public void testGetByIdShouldFailWhenSpaceIsNotReturned() throws IOException {
        Map<String, Object> spaceResponse = objectMapper.readValue(new ClassPathResource("/responses/space.json").getInputStream(), new TypeReference<Map<String, Object>>() {});
        Map<String, Object> usersResponse = objectMapper.readValue(new ClassPathResource("/responses/uaa-users.json").getInputStream(), new TypeReference<Map<String, Object>>() {});

        when(restTemplate.exchange(eq("/api/v2/spaces/123?inline-relations-depth=2"), eq(HttpMethod.GET), isA(HttpEntity.class), isA(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity(spaceResponse, HttpStatus.OK));
        when(restTemplate.exchange(eq("/uaa/ids/Users?filter=id eq 'f939a538-c0f1-48ef-90bc-8fd2c9ce477e'"), eq(HttpMethod.GET), isA(HttpEntity.class), isA(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity(usersResponse, HttpStatus.OK));

        Space space = spaceRepository.getById("bearer: 123", "123");
        LOGGER.info("Found space: {}", space);

        assertNotNull("Space should not be null", space);
    }

    @Test
    public void testGetByOrganizationIdShouldFailWhenSpacesAreNotReturned() throws IOException {
        Map<String, Object> spacesResponse = objectMapper.readValue(new ClassPathResource("/responses/spaces.json").getInputStream(), new TypeReference<Map<String, Object>>() {
        });
        Map<String, Object> usersResponse = objectMapper.readValue(new ClassPathResource("/responses/uaa-users.json").getInputStream(), new TypeReference<Map<String, Object>>() {
        });

        when(restTemplate.exchange(eq("/api/v2/organizations/123/spaces?inline-relations-depth=3"), eq(HttpMethod.GET), isA(HttpEntity.class), isA(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity(spacesResponse, HttpStatus.OK));
        when(restTemplate.exchange(eq("/uaa/ids/Users?filter=id eq 'f939a538-c0f1-48ef-90bc-8fd2c9ce477e'"), eq(HttpMethod.GET), isA(HttpEntity.class), isA(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity(usersResponse, HttpStatus.OK));

        List<Space> spaces = spaceRepository.getByOrganizationId("bearer: 123", "123");
        LOGGER.info("Found spaces: {}", spaces);

        assertEquals("Unexpected number of spaces", 1, spaces.size());
    }

    @Test
    public void testGetByOrganizationIdShouldFailWhenSpacesWithoutUsersAreNotReturned() throws IOException {
        Map<String, Object> spacesResponse = objectMapper.readValue(new ClassPathResource("/responses/spaces-no-users.json").getInputStream(), new TypeReference<Map<String, Object>>() {});

        when(restTemplate.exchange(eq("/api/v2/organizations/123/spaces?inline-relations-depth=3"), eq(HttpMethod.GET), isA(HttpEntity.class), isA(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity(spacesResponse, HttpStatus.OK));

        List<Space> spaces = spaceRepository.getByOrganizationId("bearer: 123", "123");
        LOGGER.info("Found spaces: {}", spaces);

        assertEquals("Unexpected number of spaces", 1, spaces.size());

        Space space = spaces.get(0);
        assertTrue("Expected users to be empty", space.getUsers().isEmpty());
    }

}
