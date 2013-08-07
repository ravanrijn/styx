package com.github.styx.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@RunWith(JUnit4.class)
public class ApplicationRepositoryTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationRepositoryTest.class);

    private ApplicationRepository applicationRepository;

    private RestTemplate restTemplate;

    @Before
    public void setUp() throws Exception {
        restTemplate = mock(RestTemplate.class);
        applicationRepository = new ApplicationRepository(restTemplate, new ConcurrentTaskExecutor(), new ObjectMapper(), "/api/", "/uaa");
    }

    @Test
    public void testDeleteByIdShouldFailWhenApplicationIsNotDeleted() {
        when(restTemplate.exchange(eq("/api/v2/applications/123"), eq(HttpMethod.DELETE), isA(HttpEntity.class), eq(String.class))).thenReturn(new ResponseEntity(HttpStatus.NO_CONTENT));
        applicationRepository.deleteById("bearer: 123", "123");
    }

    @Test
    public void testGetByIdShouldFailWhenApplicationIsNotReturned() throws IOException {
        String applicationResponse = IOUtils.toString(new ClassPathResource("/responses/application.json").getInputStream());
        String instancesResponse = IOUtils.toString(new ClassPathResource("/responses/instances.json").getInputStream());
        String servicesResponse = IOUtils.toString(new ClassPathResource("/responses/service-instances.json").getInputStream());

        when(restTemplate.exchange(eq("/api/v2/apps/123?inline-relations-depth=2"), eq(HttpMethod.GET), isA(HttpEntity.class), eq(String.class))).thenReturn(new ResponseEntity<String>(applicationResponse, HttpStatus.OK));
        when(restTemplate.exchange(eq("/api/v2/apps/123/instances"), eq(HttpMethod.GET), isA(HttpEntity.class), eq(String.class))).thenReturn(new ResponseEntity<String>(instancesResponse, HttpStatus.OK));
        when(restTemplate.exchange(eq("/api/v2/apps/123/service_bindings?inline-relations-depth=3"), eq(HttpMethod.GET), isA(HttpEntity.class), eq(String.class))).thenReturn(new ResponseEntity<String>(servicesResponse, HttpStatus.OK));

        Application application = applicationRepository.getById("bearer 123", "123");
        LOGGER.info("Found application: {}", application);

        assertEquals("Unexpected application id", "bc931cab-f695-40e5-93f8-5e82215e9ea8", application.getId());
        assertEquals("Unexpected number of application instances", 1, application.getApplicationInstances().size());
        assertEquals("Unexpected number of service instances", 1, application.getServiceInstances().size());
    }

}
