package br.com.divulgaifback.modules.auth.controllers;

import br.com.divulgaifback.modules.auth.useCases.login.LoginRequest;
import br.com.divulgaifback.modules.auth.useCases.login.LoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LoginControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1/auth";
    }

    @Test
    @Sql("/test-data/setup.sql")
    void testSuccessfulLoginWithEmail() throws Exception {
        
        LoginRequest loginRequest = new LoginRequest("mateusteste@teste.com", "123456");

        String jsonRequest = objectMapper.writeValueAsString(loginRequest);
        System.out.println("JSON being sent: " + jsonRequest);
        System.out.println("LoginRequest object: " + loginRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                getBaseUrl() + "/login",
                request,
                LoginResponse.class,
                String.class
        );

        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        LoginResponse loginResponse = response.getBody();
        assertNotNull(loginResponse.accessToken, "Access token should not be null");
        assertNotNull(loginResponse.refreshToken, "Refresh token should not be null");
        assertNotNull(loginResponse.user, "User should not be null");

        
        assertEquals("Mateus Teste", loginResponse.user.name);
        assertEquals("mateusteste@teste.com", loginResponse.user.email);
        assertEquals("202.108.850-25", loginResponse.user.cpf);

        assertNotNull(loginResponse.user.roles);
        assertFalse(loginResponse.user.roles.isEmpty());
        assertTrue(loginResponse.user.roles.stream()
                .anyMatch(role -> "IS_STUDENT".equals(role.name)));
    }

    @Test
    void testLoginWithInvalidCredentials() {
        
        LoginRequest loginRequest = new LoginRequest("invalid@email.com", "wrongpassword");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);

        
        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/login",
                request,
                String.class
        );

        
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testLoginWithMissingIdentifier() {
        
        LoginRequest loginRequest = new LoginRequest("", "password123");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);

        
        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/login",
                request,
                String.class
        );

        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testLoginWithMissingPassword() {
        
        LoginRequest loginRequest = new LoginRequest("joao.silva@ifpr.edu.br", "");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);

        
        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/login",
                request,
                String.class
        );

        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
