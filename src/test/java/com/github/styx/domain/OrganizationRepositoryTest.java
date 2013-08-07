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
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@RunWith(JUnit4.class)
public class OrganizationRepositoryTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationRepositoryTest.class);

    private OrganizationRepository organizationRepository;

    private SpaceRepository spaceRepository;

    private RestTemplate restTemplate;

    @Before
    public void setUp() throws Exception {
        restTemplate = mock(RestTemplate.class);
        spaceRepository = mock(SpaceRepository.class);
        organizationRepository = new OrganizationRepository(restTemplate, new ConcurrentTaskExecutor(), "/api/", "/uaa/", new ObjectMapper(), spaceRepository);
    }

    @Test
    public void testDeleteByIdShouldFailWhenOrganizationIsNotDeleted() throws IOException {
        String organizationResponse = IOUtils.toString(new ClassPathResource("/responses/organization.json").getInputStream());

        when(restTemplate.exchange(eq("/api/v2/organizations/123?inline-relations-depth=1"), eq(HttpMethod.GET), isA(HttpEntity.class), eq(String.class))).thenReturn(new ResponseEntity(organizationResponse, HttpStatus.OK));
        when(restTemplate.exchange(eq("/api/v2/organizations/123"), eq(HttpMethod.DELETE), isA(HttpEntity.class), eq(String.class))).thenReturn(new ResponseEntity(HttpStatus.NO_CONTENT));

        organizationRepository.deleteById("bearer: 123", "123");

        verify(spaceRepository).deleteById("bearer: 123", "a4341e52-a6e4-47b5-b528-578ec95df851");
    }

    @Test
    public void testGetByIdShouldFailWhenOrganizationIsNotReturned() throws IOException {
        String organizationResponse = IOUtils.toString(new ClassPathResource("/responses/organization.json").getInputStream());
        String usersResponse = IOUtils.toString(new ClassPathResource("/responses/users.json").getInputStream());

        when(restTemplate.exchange(eq("/api/v2/organizations/123?inline-relations-depth=2"), eq(HttpMethod.GET), isA(HttpEntity.class), eq(String.class))).thenReturn(new ResponseEntity(organizationResponse, HttpStatus.OK));
        when(restTemplate.exchange(eq("/uaa/ids/Users?filter=id eq '64902aa2-9df5-4c27-827c-a6a69a568e2e' or id eq 'f939a538-c0f1-48ef-90bc-8fd2c9ce477e' or id eq '62f37ce0-c3aa-48e2-9045-15e047376eb5'"), eq(HttpMethod.GET), isA(HttpEntity.class), eq(String.class))).thenReturn(new ResponseEntity(usersResponse, HttpStatus.OK));

        Organization organization = organizationRepository.getById("bearer: 123", "123", 2);

        LOGGER.info("Found organization: {}", organization);

        assertEquals("Unexpected id", "c188e4c7-1641-4124-9c1f-8f17ced7bd62", organization.getId());
        assertEquals("Unexpected number of spaces", 1, organization.getSpaces().size());
        assertEquals("Unexpected number of users", 3, organization.getUsers().size());
    }

    @Test
    public void testGetAllShouldFailWhenOrganizationsAreNotReturned() throws IOException {
        String organizationsResponse = IOUtils.toString(new ClassPathResource("/responses/organizations.json").getInputStream());
        String usersResponse = IOUtils.toString(new ClassPathResource("/responses/users.json").getInputStream());

        when(restTemplate.exchange(eq("/api/v2/organizations?inline-relations-depth=2"), eq(HttpMethod.GET), isA(HttpEntity.class), eq(String.class))).thenReturn(new ResponseEntity(organizationsResponse, HttpStatus.OK));
        when(restTemplate.exchange(eq("/uaa/ids/Users?filter=id eq 'db309bb0-644e-49be-950f-c4b6f6a969e2' or id eq '9ab79c43-e2ce-4468-a838-300d4ba95d5c' or id eq '8ce8063c-2834-476c-b09e-3dc9760f7c99'"), eq(HttpMethod.GET), isA(HttpEntity.class), eq(String.class))).thenReturn(new ResponseEntity(usersResponse, HttpStatus.OK));

        List<Organization> organizations = organizationRepository.getAll("bearer: 123", 2);

        LOGGER.info("Found organizations: {}", organizations);

        assertEquals("Unexpected number of organizations", 1, organizations.size());
    }

}
