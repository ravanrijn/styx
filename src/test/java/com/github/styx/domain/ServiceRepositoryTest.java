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

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@RunWith(JUnit4.class)
public class ServiceRepositoryTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationRepositoryTest.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    private ServiceRepository serviceRepository;

    private RestTemplate restTemplate;

    @Before
    public void setUp() throws Exception {
        restTemplate = mock(RestTemplate.class);
        serviceRepository = new ServiceRepository(restTemplate, new ConcurrentTaskExecutor(), new ObjectMapper(), "/api/", "/uaa");
    }

    @Test
    public void testGetAllShouldFailWhenServicesAreNotReturned() throws IOException {
        Map<String, Object> servicesResponse = objectMapper.readValue(new ClassPathResource("/responses/services.json").getInputStream(), new TypeReference<Map<String, Object>>() {});

        when(restTemplate.exchange(eq("/api/v2/services?inline-relations-depth=1"), eq(HttpMethod.GET), isA(HttpEntity.class), isA(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity(servicesResponse, HttpStatus.OK));

        List<Service> services = serviceRepository.getAll("bearer 123");
        LOGGER.info("Found services: {}", services);

        assertEquals("Unexpected number of services", 2, services.size());
    }

}
