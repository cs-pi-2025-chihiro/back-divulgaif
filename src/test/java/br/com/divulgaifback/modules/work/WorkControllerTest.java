package br.com.divulgaifback.modules.work;

import br.com.divulgaifback.modules.auth.useCases.login.LoginRequest;
import br.com.divulgaifback.modules.auth.useCases.login.LoginResponse;
import br.com.divulgaifback.modules.works.useCases.work.create.CreateWorkRequest;
import br.com.divulgaifback.modules.works.useCases.work.create.CreateWorkResponse;
import br.com.divulgaifback.modules.works.useCases.work.update.UpdateWorkRequest;
import br.com.divulgaifback.modules.works.useCases.work.update.UpdateWorkResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.annotation.DirtiesContext;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class WorkControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1/works";
    }

    private HttpHeaders getAuthenticatedHeaders() throws Exception {
        LoginRequest loginRequest = new LoginRequest("mateusteste@teste.com", "123456");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/auth/login",
                request,
                LoginResponse.class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Login failed with status: " + response.getStatusCode() +
                    ", body: " + response.getBody());
        }

        LoginResponse loginResponse = response.getBody();
        if (loginResponse == null || loginResponse.accessToken == null) {
            throw new RuntimeException("Login response is null or missing access token");
        }

        String accessToken = loginResponse.accessToken;

        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);
        authHeaders.setBearerAuth(accessToken);
        return authHeaders;
    }

    @Test
    @Sql("/test-data/setup.sql")
    void testPasswordHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String storedHash = "$2a$10$50IuCYfQIsWieK3IBZ1nauiEStJh639sJrkAdWBv18IIOYuYCEPZi";
        boolean matches = encoder.matches("123456", storedHash);
        System.out.println("Password matches: " + matches);
    }

    @Test
    @Sql("/test-data/setup.sql")
    void testSuccessfulWorkUpdate() throws Exception {
        UpdateWorkRequest.AuthorRequest authorRequest = new UpdateWorkRequest.AuthorRequest(
                "Joao silva",
                "joao.silva@email.com"
        );

        UpdateWorkRequest.LabelRequest labelRequest = new UpdateWorkRequest.LabelRequest(
                "Tecnologia",
                "#FF5733"
        );

        UpdateWorkRequest.LinkRequest linkRequest = new UpdateWorkRequest.LinkRequest(
                "GitHub Repository",
                "https://github.com/user/repo",
                "Repository with project source code"
        );

        UpdateWorkRequest workRequest = new UpdateWorkRequest(
                "Trabalho Editado",
                "Descrição editada",
                "Conteúdo editado",
                "https://projeto-principal-editado.com",
                "sistema, web, gestão, edição",
                "https://example.com/image-editada.jpg",
                1,
                Arrays.asList(3, 4),
                List.of(authorRequest),
                Collections.singletonList(labelRequest),
                Collections.singletonList(linkRequest),
                "ARTICLE",
                "DRAFT"
        );

        String jsonRequest = objectMapper.writeValueAsString(workRequest);
        System.out.println("JSON being sent: " + jsonRequest);

        HttpHeaders headers = getAuthenticatedHeaders();
        HttpEntity<UpdateWorkRequest> request = new HttpEntity<>(workRequest, headers);


        ResponseEntity<UpdateWorkResponse> response = restTemplate.exchange(
                getBaseUrl() + "/2000",
                HttpMethod.PUT,
                request,
                UpdateWorkResponse.class
        );

        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        UpdateWorkResponse workResponse = response.getBody();
        assertEquals("Trabalho Editado", workResponse.title);
        assertEquals("Descrição editada", workResponse.description);
        assertEquals("Conteúdo editado", workResponse.content);
        assertEquals("DRAFT", workResponse.workStatus.name);
    }
    
    @Test
    @Sql("/test-data/setup.sql")
    void testCreateWorkWithMissingTitle() throws Exception {
        CreateWorkRequest workRequest = new CreateWorkRequest(
                "",
                "Descrição do trabalho",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "FINAL_THESIS",
                null
        );

        HttpHeaders headers = getAuthenticatedHeaders();
        HttpEntity<CreateWorkRequest> request = new HttpEntity<>(workRequest, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl(),
                request,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Sql("/test-data/setup.sql")
    void testCreateWorkWithMissingWorkType() throws Exception {
        CreateWorkRequest workRequest = new CreateWorkRequest(
                "Título do Trabalho",
                "Descrição do trabalho",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "",
                null
        );

        HttpHeaders headers = getAuthenticatedHeaders();
        HttpEntity<CreateWorkRequest> request = new HttpEntity<>(workRequest, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl(),
                request,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Sql("/test-data/setup.sql")
    void testCreateWorkWithInvalidWorkType() throws Exception {
        CreateWorkRequest workRequest = new CreateWorkRequest(
                "Trabalho Simples",
                "Minimal description",
                "Minimal content",      
                "https://example.com",
                "minimal meta",       
                "https://example.com/image.jpg",
                1,
                null,
                null,
                null,
                null,
                "INVALID TYPE",
                "DRAFT" 
        );

        HttpHeaders headers = getAuthenticatedHeaders();
        HttpEntity<CreateWorkRequest> request = new HttpEntity<>(workRequest, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl(),
                request,
                String.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Sql("/test-data/setup.sql")
    void testCreateWorkWithInvalidAuthor() throws Exception {
        CreateWorkRequest.AuthorRequest invalidAuthor = new CreateWorkRequest.AuthorRequest(
                "",
                "email@test.com"
        );

        CreateWorkRequest workRequest = new CreateWorkRequest(
                "Título do Trabalho",
                "Descrição do trabalho",
                null,
                null,
                null,
                null,
                null,
                null,
                Collections.singletonList(invalidAuthor),
                null,
                null,
                "DISSERTATION",
                null
        );

        HttpHeaders headers = getAuthenticatedHeaders();
        HttpEntity<CreateWorkRequest> request = new HttpEntity<>(workRequest, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl(),
                request,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Sql("/test-data/setup.sql")
    void testCreateWorkWithInvalidLabel() throws Exception {
        CreateWorkRequest.LabelRequest invalidLabel = new CreateWorkRequest.LabelRequest(
                "Label Name",
                ""
        );

        CreateWorkRequest workRequest = new CreateWorkRequest(
                "Título do Trabalho",
                "Descrição do trabalho",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Collections.singletonList(invalidLabel),
                null,
                "EXTENSION",
                null
        );

        HttpHeaders headers = getAuthenticatedHeaders();
        HttpEntity<CreateWorkRequest> request = new HttpEntity<>(workRequest, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl(),
                request,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Sql("/test-data/setup.sql")
    void testCreateWorkWithNonExistentStudent() throws Exception {
        CreateWorkRequest workRequest = new CreateWorkRequest(
                "Trabalho Simples",
                "Minimal description",
                "Minimal content",      
                "https://example.com",
                "minimal meta",       
                "https://example.com/image.jpg",
                1,
                List.of(999),
                null,
                null,
                null,
                "SEARCH",
                "DRAFT"              
        );

        HttpHeaders headers = getAuthenticatedHeaders();
        HttpEntity<CreateWorkRequest> request = new HttpEntity<>(workRequest, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl(),
                request,
                String.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Sql("/test-data/setup.sql")
    void testCreateWorkWithoutAuthentication() {
        CreateWorkRequest workRequest = new CreateWorkRequest(
                "Título do Trabalho",
                "Descrição do trabalho",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "TCC",
                null
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateWorkRequest> request = new HttpEntity<>(workRequest, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl(),
                request,
                String.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Sql("/test-data/setup.sql")
    void testCreateWorkRollbackOnError() throws Exception {
        CreateWorkRequest.AuthorRequest validAuthor = new CreateWorkRequest.AuthorRequest(
                "Valid Author", "valid@example.com"
        );
        CreateWorkRequest.AuthorRequest invalidAuthor = new CreateWorkRequest.AuthorRequest(
                "", "invalid@example.com"
        );

        CreateWorkRequest workRequest = new CreateWorkRequest(
                "Test Rollback", null, null, null, null, null, null, null,
                List.of(validAuthor, invalidAuthor), null, null, "ARTICLE", null
        );

        HttpHeaders headers = getAuthenticatedHeaders();
        HttpEntity<CreateWorkRequest> request = new HttpEntity<>(workRequest, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl(), request, String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Sql("/test-data/setup.sql")
    void testConcurrentWorkCreation() throws Exception {
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<ResponseEntity<CreateWorkResponse>> responses = Collections.synchronizedList(new ArrayList<>());

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    CreateWorkRequest workRequest = new CreateWorkRequest(
                            "Concurrent Work " + index,
                            "Description for work " + index,
                            "Content for work " + index,    
                            "https://example.com/work" + index,
                            "meta-tag-" + index,            
                            "https://example.com/image" + index + ".jpg",
                            1,
                            null,
                            null,
                            null,
                            null,
                            "ARTICLE",
                            "DRAFT"                         
                    );

                    HttpHeaders headers = getAuthenticatedHeaders();
                    HttpEntity<CreateWorkRequest> request = new HttpEntity<>(workRequest, headers);

                    ResponseEntity<CreateWorkResponse> response = restTemplate.postForEntity(
                            getBaseUrl(), request, CreateWorkResponse.class
                    );

                    responses.add(response);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        assertEquals(threadCount, responses.size());
        responses.forEach(response ->
                assertEquals(HttpStatus.CREATED, response.getStatusCode())
        );
    }

    @Test
    @Sql("/test-data/setup.sql")
    void testCreateWorkWithExpiredToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("expired.jwt.token");

        CreateWorkRequest workRequest = new CreateWorkRequest(
                "Test Work", null, null, null, null, null, null, null,
                null, null, null, "ARTICLE", null
        );

        HttpEntity<CreateWorkRequest> request = new HttpEntity<>(workRequest, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl(), request, String.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Sql("/test-data/setup.sql")
    void testCreateWorkWithInvalidBearerToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("invalid-token-format");

        CreateWorkRequest workRequest = new CreateWorkRequest(
                "Test Work", null, null, null, null, null, null, null,
                null, null, null, "ARTICLE", null
        );

        HttpEntity<CreateWorkRequest> request = new HttpEntity<>(workRequest, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl(), request, String.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }


    @Test
    @Sql("/test-data/setup.sql")
    void testCreateWorkWithVeryLongTitle() throws Exception {
        String longTitle = "A".repeat(1000);
        CreateWorkRequest workRequest = new CreateWorkRequest(
                longTitle, "Description", null, null, null, null, null, null,
                null, null, null, "ARTICLE", null
        );

        HttpHeaders headers = getAuthenticatedHeaders();
        HttpEntity<CreateWorkRequest> request = new HttpEntity<>(workRequest, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl(), request, String.class
        );

        assertTrue(response.getStatusCode() == HttpStatus.BAD_REQUEST ||
                response.getStatusCode() == HttpStatus.CREATED);
    }

    @Test
    @Sql("/test-data/setup.sql")
    void testCreateWorkWithInvalidEmailFormat() throws Exception {
        CreateWorkRequest.AuthorRequest invalidAuthor = new CreateWorkRequest.AuthorRequest(
                "John Doe", "invalid-email-format"
        );

        CreateWorkRequest workRequest = new CreateWorkRequest(
                "Test Work", null, null, null, null, null, null, null,
                List.of(invalidAuthor), null, null, "ARTICLE", null
        );

        HttpHeaders headers = getAuthenticatedHeaders();
        HttpEntity<CreateWorkRequest> request = new HttpEntity<>(workRequest, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl(), request, String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Sql("/test-data/setup.sql")
    void testCreateWorkWithInvalidURL() throws Exception {
        CreateWorkRequest.LinkRequest invalidLink = new CreateWorkRequest.LinkRequest(
                "Invalid Link", "not-a-valid-url", "Description"
        );

        CreateWorkRequest workRequest = new CreateWorkRequest(
                "Test Work", null, null, null, null, null, null, null,
                null, null, List.of(invalidLink), "ARTICLE", null
        );

        HttpHeaders headers = getAuthenticatedHeaders();
        HttpEntity<CreateWorkRequest> request = new HttpEntity<>(workRequest, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl(), request, String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}