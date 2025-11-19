package br.com.divulgaifback.modules.work;

import br.com.divulgaifback.modules.auth.useCases.login.LoginRequest;
import br.com.divulgaifback.modules.auth.useCases.login.LoginResponse;
import br.com.divulgaifback.modules.works.useCases.label.create.CreateLabelRequest;
import br.com.divulgaifback.modules.works.useCases.label.create.CreateLabelResponse;
import br.com.divulgaifback.modules.works.useCases.label.update.UpdateLabelRequest;
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
public class LabelControllerTest {

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
    void testCreateLabel() {
        HttpHeaders headers = getAuthenticatedHeaders();
        CreateLabelRequest createLabelRequest = new CreateLabelRequest("Nova Label", 1000);
        HttpEntity<CreateLabelRequest> requestEntity = new HttpEntity<>(createLabelRequest, headers);

        ResponseEntity<CreateLabelResponse> response = restTemplate.postForEntity(
                getBaseUrl() + "/labels",
                requestEntity,
                CreateLabelResponse.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Nova Label", response.getBody().name);
    }

    @Test
    @Sql("/test-data/setup.sql")
    void testListLabels() {
        HttpHeaders headers = getAuthenticatedHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                getBaseUrl() + "/labels/list",
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<Object>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @Sql("/test-data/setup.sql")
    void testUpdateLabel() {
        HttpHeaders headers = getAuthenticatedHeaders();
        UpdateLabelRequest updateLabelRequest = new UpdateLabelRequest("Label Atualizada");
        HttpEntity<UpdateLabelRequest> requestEntity = new HttpEntity<>(updateLabelRequest, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                getBaseUrl() + "/labels/1",
                HttpMethod.PUT,
                requestEntity,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @Sql("/test-data/setup.sql")
    void testDeleteLabel() {
        HttpHeaders headers = getAuthenticatedHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                getBaseUrl() + "/labels/1",
                HttpMethod.DELETE,
                requestEntity,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}