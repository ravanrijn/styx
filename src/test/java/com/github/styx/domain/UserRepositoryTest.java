package com.github.styx.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class UserRepositoryTest {

    private UserRepository userRepository;

    private RestTemplate restTemplate;

    @Before
    public void setUp() throws Exception {
        restTemplate = mock(RestTemplate.class);
        userRepository = new UserRepository(restTemplate, new ConcurrentTaskExecutor(), new ObjectMapper(), "/api/", "/uaa/");
    }

    @Test
    public void testLoginShouldFailWhenUserCannotLogIn() throws IOException {
        String tokenResponse = IOUtils.toString(new ClassPathResource("/responses/token.json").getInputStream());
        String userinfoResponse = IOUtils.toString(new ClassPathResource("/responses/userinfo.json").getInputStream());

        when(restTemplate.exchange(eq("/uaa/oauth/token"), eq(HttpMethod.POST), isA(HttpEntity.class), eq(String.class))).thenReturn(new ResponseEntity<String>(tokenResponse, HttpStatus.OK));
        when(restTemplate.exchange(eq("/uaa/userinfo"), eq(HttpMethod.GET), isA(HttpEntity.class), eq(String.class))).thenReturn(new ResponseEntity<String>(userinfoResponse, HttpStatus.OK));

        UserDetails userDetails = userRepository.login("username", "password");
        assertNotNull(userDetails);
        assertEquals("Unexpected user id", "d312cc7e-8350-4aac-a0d7-d1fbcc8e7e27", userDetails.getId());
        assertEquals("Unexpected user name", "cloudfoundry", userDetails.getUsername());
        assertEquals("Unexpected token type", "bearer", userDetails.getTokenType());
        assertEquals("Unexpected access token", "1234", userDetails.getAccessToken());
    }

}
