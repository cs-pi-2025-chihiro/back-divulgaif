package br.com.divulgaifback.modules.work;

import br.com.divulgaifback.modules.auth.useCases.login.LoginRequest;
import br.com.divulgaifback.modules.auth.useCases.login.LoginResponse;
import br.com.divulgaifback.modules.works.useCases.author.create.CreateAuthorRequest;
import br.com.divulgaifback.modules.works.useCases.author.create.CreateAuthorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AuthorControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1";
    }

    private HttpHeaders getAuthenticatedHeaders() {
        LoginRequest loginRequest = new LoginRequest("mateusteste@teste.com", "123456");
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                getBaseUrl() + "/auth/login",
                loginRequest,
                LoginResponse.class
        );
        String accessToken = response.getBody().accessToken;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return headers;
    }

    @Test
    @Sql("/test-data/setup.sql")
    void testCreateAuthor() {
        HttpHeaders headers = getAuthenticatedHeaders();
        CreateAuthorRequest authorRequest = new CreateAuthorRequest("Novo Autor Teste", "novo.autor.teste@email.com");

        HttpEntity<CreateAuthorRequest> requestEntity = new HttpEntity<>(authorRequest, headers);

        ResponseEntity<CreateAuthorResponse> response = restTemplate.postForEntity(
                getBaseUrl() + "/authors",
                requestEntity,
                CreateAuthorResponse.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        CreateAuthorResponse responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("Novo Autor Teste", responseBody.getName());
        assertEquals("novo.autor.teste@email.com", responseBody.getEmail());
    }

    @Test
    @Sql("/test-data/setup.sql")
    void testListAuthors() {
        HttpHeaders headers = getAuthenticatedHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                getBaseUrl() + "/authors/list",
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<Object>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}