package br.com.divulgaifback.modules.work;

import br.com.divulgaifback.modules.auth.useCases.login.LoginRequest;
import br.com.divulgaifback.modules.auth.useCases.login.LoginResponse;
import br.com.divulgaifback.modules.works.useCases.work.create.CreateWorkRequest;
import br.com.divulgaifback.modules.works.useCases.work.create.CreateWorkResponse;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
    void testSuccessfulWorkCreation() throws Exception {

        CreateWorkRequest.AuthorRequest authorRequest = new CreateWorkRequest.AuthorRequest(
                "joao silva",
                "joao.silva@email.com"
        );

        CreateWorkRequest.LabelRequest labelRequest = new CreateWorkRequest.LabelRequest(
                "Tecnologia",
                "#FF5733"
        );

        CreateWorkRequest.LinkRequest linkRequest = new CreateWorkRequest.LinkRequest(
                "GitHub Repository",
                "https://github.com/user/repo",
                "Repository with project source code"
        );

        CreateWorkRequest workRequest = new CreateWorkRequest(
                "Desenvolvimento de Sistema Web",
                "Sistema web para gerenciamento de projetos acadêmicos",
                "Conteúdo detalhado do trabalho...",
                "https://projeto-principal.com",
                "sistema, web, gestão",
                "https://example.com/image.jpg",
                1,
                Arrays.asList(2, 3),
                List.of(authorRequest),
                Collections.singletonList(labelRequest),
                Collections.singletonList(linkRequest),
                "ARTICLE",
                "PUBLISHED"
        );

        String jsonRequest = objectMapper.writeValueAsString(workRequest);
        System.out.println("JSON being sent: " + jsonRequest);

        HttpHeaders headers = getAuthenticatedHeaders();
        HttpEntity<CreateWorkRequest> request = new HttpEntity<>(workRequest, headers);


        ResponseEntity<CreateWorkResponse> response = restTemplate.postForEntity(
                getBaseUrl(),
                request,
                CreateWorkResponse.class
        );

        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        CreateWorkResponse workResponse = response.getBody();
        assertNotNull(workResponse.id, "Work ID should not be null");
        assertEquals("Desenvolvimento de Sistema Web", workResponse.title);
        assertEquals("Sistema web para gerenciamento de projetos acadêmicos", workResponse.description);
        assertEquals("Conteúdo detalhado do trabalho...", workResponse.content);
        assertEquals("https://projeto-principal.com", workResponse.principalLink);
        assertEquals("sistema, web, gestão", workResponse.metaTag);


        assertNotNull(workResponse.workType);
        assertEquals("ARTICLE", workResponse.workType.name);

        assertNotNull(workResponse.workStatus);
        assertEquals("PUBLISHED", workResponse.workStatus.name);

        workResponse.authors.forEach(author ->
                System.out.println("Author: '" + author.name + "' Email: '" + author.email + "'")
        );
        assertNotNull(workResponse.authors);
        assertFalse(workResponse.authors.isEmpty());
        assertTrue(workResponse.authors.stream().anyMatch(author -> "joao silva".equals(author.name)));


        assertNotNull(workResponse.labels);
        assertFalse(workResponse.labels.isEmpty());
        assertTrue(workResponse.labels.stream()
                .anyMatch(label -> "Tecnologia".equals(label.name) && "#FF5733".equals(label.color)));

        assertNotNull(workResponse.links);
        assertFalse(workResponse.links.isEmpty());
        assertTrue(workResponse.links.stream()
                .anyMatch(link -> "GitHub Repository".equals(link.name) &&
                        "https://github.com/user/repo".equals(link.url)));
    }

    @Test
    @Sql("/test-data/setup.sql")
    void testCreateWorkWithMinimalData() throws Exception {

        CreateWorkRequest workRequest = new CreateWorkRequest(
                "Trabalho Simples",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "SEARCH",
                null
        );

        HttpHeaders headers = getAuthenticatedHeaders();
        HttpEntity<CreateWorkRequest> request = new HttpEntity<>(workRequest, headers);


        ResponseEntity<CreateWorkResponse> response = restTemplate.postForEntity(
                getBaseUrl(),
                request,
                CreateWorkResponse.class
        );


        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        CreateWorkResponse workResponse = response.getBody();
        assertEquals("Trabalho Simples", workResponse.title);
        assertEquals("SEARCH", workResponse.workType.name);
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
                "INVALID_TYPE",
                null
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
                "Título do Trabalho",
                "Descrição do trabalho",
                null,
                null,
                null,
                null,
                null,
                List.of(999),
                null,
                null,
                null,
                "TCC",
                null
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
                            "Concurrent Work " + index, null, null, null, null, null, null, null,
                            null, null, null, "ARTICLE", null
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
    void testCreateWorkWithExistingLabel() throws Exception {
        CreateWorkRequest.LabelRequest label = new CreateWorkRequest.LabelRequest(
                "Technology", "#FF0000"
        );

        CreateWorkRequest workRequest1 = new CreateWorkRequest(
                "First Work", null, null, null, null, null, null, null,
                null, List.of(label), null, "ARTICLE", null
        );

        HttpHeaders headers = getAuthenticatedHeaders();
        restTemplate.postForEntity(getBaseUrl(),
                new HttpEntity<>(workRequest1, headers), CreateWorkResponse.class);

        CreateWorkRequest workRequest2 = new CreateWorkRequest(
                "Second Work", null, null, null, null, null, null, null,
                null, List.of(label), null, "ARTICLE", null
        );

        ResponseEntity<CreateWorkResponse> response = restTemplate.postForEntity(
                getBaseUrl(), new HttpEntity<>(workRequest2, headers), CreateWorkResponse.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().labels);
        assertEquals(1, response.getBody().labels.size());
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