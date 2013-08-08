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
import java.util.Map;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@RunWith(JUnit4.class)
public class ApplicationRepositoryTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationRepositoryTest.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    private ApplicationRepository applicationRepository;

    private RestTemplate restTemplate;

    @Before
    public void setUp() throws Exception {
        restTemplate = mock(RestTemplate.class);
        applicationRepository = new ApplicationRepository(restTemplate, new ConcurrentTaskExecutor(), new ObjectMapper(), "/api/", "/uaa");
    }

    @Test
    public void testDeleteByIdShouldFailWhenApplicationIsNotDeleted() {
        when(restTemplate.exchange(eq("/api/v2/applications/123"), eq(HttpMethod.DELETE), isA(HttpEntity.class), isA(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity(HttpStatus.NO_CONTENT));
        applicationRepository.deleteById("bearer: 123", "123");
    }

    @Test
    public void testGetByIdShouldFailWhenApplicationIsNotReturned() throws IOException {
        Map<String, Object> applicationResponse = objectMapper.readValue(new ClassPathResource("/responses/application.json").getInputStream(), new TypeReference<Map<String, Object>>() {});
        Map<String, Object> instancesResponse = objectMapper.readValue(new ClassPathResource("/responses/instances.json").getInputStream(), new TypeReference<Map<String, Object>>() {});
        Map<String, Object> servicesResponse = objectMapper.readValue(new ClassPathResource("/responses/service-instances.json").getInputStream(), new TypeReference<Map<String, Object>>() {});

        when(restTemplate.exchange(eq("/api/v2/apps/123?inline-relations-depth=2"), eq(HttpMethod.GET), isA(HttpEntity.class), isA(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity(applicationResponse, HttpStatus.OK));
        when(restTemplate.exchange(eq("/api/v2/apps/123/instances"), eq(HttpMethod.GET), isA(HttpEntity.class), isA(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity(instancesResponse, HttpStatus.OK));
        when(restTemplate.exchange(eq("/api/v2/apps/123/service_bindings?inline-relations-depth=3"), eq(HttpMethod.GET), isA(HttpEntity.class), isA(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity(servicesResponse, HttpStatus.OK));

        Application application = applicationRepository.getById("bearer 123", "123");
        LOGGER.info("Found application: {}", application);

        assertEquals("Unexpected application id", "bc931cab-f695-40e5-93f8-5e82215e9ea8", application.getId());
        assertEquals("Unexpected number of application instances", 1, application.getApplicationInstances().size());
        assertEquals("Unexpected number of service instances", 1, application.getServiceInstances().size());
    }

}
