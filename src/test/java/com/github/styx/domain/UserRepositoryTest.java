package com.github.styx.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class UserRepositoryTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private UserRepository userRepository;

    private RestTemplate restTemplate;

    @Before
    public void setUp() throws Exception {
        restTemplate = mock(RestTemplate.class);
        userRepository = new UserRepository(restTemplate, new ConcurrentTaskExecutor(), new ObjectMapper(), "/api/", "/uaa/", "styx", "styxsecret");
    }

    @Test
    public void testLoginShouldFailWhenUserCannotLogIn() throws IOException {
        Map<String, Object> tokenResponse = objectMapper.readValue(new ClassPathResource("/responses/token.json").getInputStream(), new TypeReference<Map<String, Object>>() {});
        Map<String, Object> userinfoResponse = objectMapper.readValue(new ClassPathResource("/responses/userinfo.json").getInputStream(), new TypeReference<Map<String, Object>>() {});

        when(restTemplate.exchange(eq("/uaa/oauth/token"), eq(HttpMethod.POST), isA(HttpEntity.class), isA(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity(tokenResponse, HttpStatus.OK));
        when(restTemplate.exchange(eq("/uaa/userinfo"), eq(HttpMethod.GET), isA(HttpEntity.class), isA(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity(userinfoResponse, HttpStatus.OK));

        UserDetails userDetails = userRepository.login("username", "password");
        assertNotNull(userDetails);
        assertEquals("Unexpected user id", "d312cc7e-8350-4aac-a0d7-d1fbcc8e7e27", userDetails.getId());
        assertEquals("Unexpected user name", "cloudfoundry", userDetails.getUsername());
        assertEquals("Unexpected token type", "bearer", userDetails.getTokenType());
        assertEquals("Unexpected access token", "1234", userDetails.getAccessToken());
    }

}
