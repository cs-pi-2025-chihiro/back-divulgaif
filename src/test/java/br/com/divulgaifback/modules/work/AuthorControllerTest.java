package br.com.divulgaifback.modules.work;

import br.com.divulgaifback.modules.auth.useCases.login.LoginRequest;
import br.com.divulgaifback.modules.auth.useCases.login.LoginResponse;
import br.com.divulgaifback.modules.works.useCases.author.create.CreateAuthorRequest;
import br.com.divulgaifback.modules.works.useCases.author.create.CreateAuthorResponse;
import br.com.divulgaifback.modules.works.useCases.author.update.UpdateAuthorRequest;
import br.com.divulgaifback.modules.works.useCases.author.update.UpdateAuthorResponse;
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
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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

    private HttpHeaders getAdminHeaders() {
        LoginRequest loginRequest = new LoginRequest("admin@test.com", "123456");
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
    @Sql("/test-data/setup.sql")  // ⭐ Agora usa o mesmo arquivo!
    void testUpdateAuthorAsAdmin() {
        HttpHeaders headers = getAdminHeaders();
        UpdateAuthorRequest updateRequest = new UpdateAuthorRequest(
                "Nome Atualizado",
                "email.atualizado@test.com"
        );

        HttpEntity<UpdateAuthorRequest> requestEntity = new HttpEntity<>(updateRequest, headers);

        ResponseEntity<UpdateAuthorResponse> response = restTemplate.exchange(
                getBaseUrl() + "/authors/5",
                HttpMethod.PUT,
                requestEntity,
                UpdateAuthorResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UpdateAuthorResponse responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("Nome Atualizado", responseBody.getName());
        assertEquals("email.atualizado@test.com", responseBody.getEmail());
    }

    @Test
    @Sql("/test-data/setup.sql")
    void testUpdateAuthorAsNonAdminShouldFail() {
        HttpHeaders headers = getAuthenticatedHeaders();
        UpdateAuthorRequest updateRequest = new UpdateAuthorRequest(
                "Nome Atualizado",
                "email.atualizado@test.com"
        );

        HttpEntity<UpdateAuthorRequest> requestEntity = new HttpEntity<>(updateRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/authors/5",
                HttpMethod.PUT,
                requestEntity,
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @Sql("/test-data/setup.sql")
    void testUpdateAuthorWithDuplicateEmail() {
        HttpHeaders headers = getAdminHeaders();
        UpdateAuthorRequest updateRequest = new UpdateAuthorRequest(
                "Novo Nome",
                "student1@test.com"
        );

        HttpEntity<UpdateAuthorRequest> requestEntity = new HttpEntity<>(updateRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/authors/5",
                HttpMethod.PUT,
                requestEntity,
                String.class
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @Sql("/test-data/setup.sql")
    void testUpdateNonExistentAuthor() {
        HttpHeaders headers = getAdminHeaders();
        UpdateAuthorRequest updateRequest = new UpdateAuthorRequest(
                "Nome Qualquer",
                "email@test.com"
        );

        HttpEntity<UpdateAuthorRequest> requestEntity = new HttpEntity<>(updateRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/authors/9999",
                HttpMethod.PUT,
                requestEntity,
                String.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Sql("/test-data/setup.sql")
    void testDeleteAuthorAsAdmin() {
        HttpHeaders headers = getAdminHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                getBaseUrl() + "/authors/5",
                HttpMethod.DELETE,
                requestEntity,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @Sql("/test-data/setup.sql")
    void testDeleteAuthorAsNonAdminShouldFail() {
        HttpHeaders headers = getAuthenticatedHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/authors/5",
                HttpMethod.DELETE,
                requestEntity,
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @Sql("/test-data/setup.sql")
    void testDeleteNonExistentAuthor() {
        HttpHeaders headers = getAdminHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/authors/9999",
                HttpMethod.DELETE,
                requestEntity,
                String.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
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

    @Test
    @Sql("/test-data/setup.sql")
    void testUpdateAuthorWithInvalidEmail() {
        HttpHeaders headers = getAdminHeaders();
        UpdateAuthorRequest updateRequest = new UpdateAuthorRequest(
                "Nome Válido",
                "email-invalido"
        );

        HttpEntity<UpdateAuthorRequest> requestEntity = new HttpEntity<>(updateRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/authors/5",
                HttpMethod.PUT,
                requestEntity,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Sql("/test-data/setup.sql")
    void testUpdateAuthorWithEmptyName() {
        HttpHeaders headers = getAdminHeaders();
        UpdateAuthorRequest updateRequest = new UpdateAuthorRequest(
                "",
                "email@test.com"
        );

        HttpEntity<UpdateAuthorRequest> requestEntity = new HttpEntity<>(updateRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/authors/5",
                HttpMethod.PUT,
                requestEntity,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}