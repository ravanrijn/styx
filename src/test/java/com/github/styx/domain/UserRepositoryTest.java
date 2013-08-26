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
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
    public void testGetAllUsersShouldFailWhenUsersCannotBeRetrieved() throws IOException {
        Map<String, Object> tokenResponse = objectMapper.readValue(new ClassPathResource("/responses/token.json").getInputStream(), new TypeReference<Map<String, Object>>() {});
        Map<String, Object> apiUsersResponse = objectMapper.readValue(new ClassPathResource("/responses/api-users.json").getInputStream(), new TypeReference<Map<String, Object>>() {});
        Map<String, Object> uaaUsersResponse = objectMapper.readValue(new ClassPathResource("/responses/uaa-users.json").getInputStream(), new TypeReference<Map<String, Object>>() {});

        when(restTemplate.exchange(eq("/api/v2/users"), eq(HttpMethod.GET), isA(HttpEntity.class), isA(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity(apiUsersResponse, HttpStatus.OK));
        when(restTemplate.exchange(eq("/uaa/oauth/token"), eq(HttpMethod.POST), isA(HttpEntity.class), isA(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity(tokenResponse, HttpStatus.OK));
        when(restTemplate.exchange(eq("/uaa/ids/Users?filter=id eq '64902aa2-9df5-4c27-827c-a6a69a568e2e' or id eq 'f939a538-c0f1-48ef-90bc-8fd2c9ce477e' or id eq '62f37ce0-c3aa-48e2-9045-15e047376eb5'"), eq(HttpMethod.GET), isA(HttpEntity.class), isA(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity(uaaUsersResponse, HttpStatus.OK));

        List<User> users = userRepository.getAllUsers("bearer: 123");
        assertEquals("Unexpected number of users", 3, users.size());
    }

    @Test
    public void testGetUserInfo() throws IOException {
        Map<String, Object> userinfoResponse = objectMapper.readValue(new ClassPathResource("/responses/userinfo.json").getInputStream(), new TypeReference<Map<String, Object>>() {});
        Map<String, Object> tokenResponse = objectMapper.readValue(new ClassPathResource("/responses/token.json").getInputStream(), new TypeReference<Map<String, Object>>() {});
        Map<String, Object> uaaUserResponse = objectMapper.readValue(new ClassPathResource("/responses/uaa-user.json").getInputStream(), new TypeReference<Map<String, Object>>() {});
        Map<String, Object> apiUserResponse = objectMapper.readValue(new ClassPathResource("/responses/api-user.json").getInputStream(), new TypeReference<Map<String, Object>>() {});

        when(restTemplate.exchange(eq("/uaa/userinfo"), eq(HttpMethod.GET), isA(HttpEntity.class), isA(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity(userinfoResponse, HttpStatus.OK));
        when(restTemplate.exchange(eq("/uaa/oauth/token"), eq(HttpMethod.POST), isA(HttpEntity.class), isA(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity(tokenResponse, HttpStatus.OK));
        when(restTemplate.exchange(eq("/uaa/Users/d312cc7e-8350-4aac-a0d7-d1fbcc8e7e27"), eq(HttpMethod.GET), isA(HttpEntity.class), isA(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity(uaaUserResponse, HttpStatus.OK));
        when(restTemplate.exchange(eq("/api/v2/users/d312cc7e-8350-4aac-a0d7-d1fbcc8e7e27?inline-relations-depth=1"), eq(HttpMethod.GET), isA(HttpEntity.class), isA(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity(apiUserResponse, HttpStatus.OK));

        UserInfo userInfo = userRepository.getUserInfo("bearer: 123");
        assertNotNull("User info should not be null", userInfo);
    }

    @Test
    public void testLoginShouldFailWhenUserCannotLogIn() throws IOException {
        Map<String, Object> infoResponse = objectMapper.readValue(new ClassPathResource("/responses/info.json").getInputStream(), new TypeReference<Map<String, Object>>() {});
        Map<String, Object> tokenResponse = objectMapper.readValue(new ClassPathResource("/responses/token.json").getInputStream(), new TypeReference<Map<String, Object>>() {});
        Map<String, Object> userinfoResponse = objectMapper.readValue(new ClassPathResource("/responses/userinfo.json").getInputStream(), new TypeReference<Map<String, Object>>() {});

        when(restTemplate.exchange(eq("/api/info"), eq(HttpMethod.GET), isA(HttpEntity.class), isA(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity(infoResponse, HttpStatus.OK));
        when(restTemplate.exchange(eq("http://login.cf.com/oauth/token"), eq(HttpMethod.POST), isA(HttpEntity.class), isA(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity(tokenResponse, HttpStatus.OK));
        when(restTemplate.exchange(eq("/uaa/userinfo"), eq(HttpMethod.GET), isA(HttpEntity.class), isA(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity(userinfoResponse, HttpStatus.OK));

        AccessToken accessToken = userRepository.login("username", "password");
        assertNotNull(accessToken);
        assertEquals("Unexpected user id", "d312cc7e-8350-4aac-a0d7-d1fbcc8e7e27", accessToken.getId());
        assertEquals("Unexpected user name", "cloudfoundry", accessToken.getUsername());
        assertEquals("Unexpected token type", "bearer", accessToken.getTokenType());
        assertEquals("Unexpected access token", "1234", accessToken.getAccessToken());
    }

    @Test
    public void testRegisterShouldFailWhenRegistrationFails() throws IOException {
        Map<String, Object> tokenResponse = objectMapper.readValue(new ClassPathResource("/responses/token.json").getInputStream(), new TypeReference<Map<String, Object>>() {});
        Map<String, Object> uaaCreateUserResponse = objectMapper.readValue(new ClassPathResource("/responses/uaa-create-user.json").getInputStream(), new TypeReference<Map<String, Object>>() {});

        when(restTemplate.exchange(eq("/uaa/oauth/token"), eq(HttpMethod.POST), isA(HttpEntity.class), isA(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity(tokenResponse, HttpStatus.OK));
        when(restTemplate.exchange(eq("/uaa/Users"), eq(HttpMethod.POST), isA(HttpEntity.class), isA(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity(uaaCreateUserResponse, HttpStatus.CREATED));
        when(restTemplate.exchange(eq("/api/v2/users"), eq(HttpMethod.POST), isA(HttpEntity.class), isA(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity(HttpStatus.CREATED));

        userRepository.registerUser("username", "first", "last", "password");
    }

}
