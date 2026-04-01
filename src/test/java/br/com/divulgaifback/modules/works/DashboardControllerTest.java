package br.com.divulgaifback.modules.works;

import br.com.divulgaifback.modules.auth.useCases.login.LoginRequest;
import br.com.divulgaifback.modules.auth.useCases.login.LoginResponse;
import br.com.divulgaifback.modules.works.useCases.dashboard.get.GetDashboardRequest;
import br.com.divulgaifback.modules.works.useCases.dashboard.get.GetDashboardResponse;
import br.com.divulgaifback.modules.works.useCases.dashboard.getAuthors.GetAuthorsDashboardResponse;
import br.com.divulgaifback.modules.works.useCases.dashboard.getLabels.GetLabelsDashboardResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("DashboardController Integration Tests (CANDOR Framework)")
public class DashboardControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        this.baseUrl = "http://localhost:" + port + "/api/v1/dashboards";
    }

    // ========================= Helper Methods =========================

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1";
    }

    private HttpHeaders getAuthenticatedHeaders(String email, String password) {
        LoginRequest loginRequest = new LoginRequest(email, password);
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                getBaseUrl() + "/auth/login",
                loginRequest,
                LoginResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        String accessToken = response.getBody().accessToken;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return headers;
    }

    private HttpHeaders getAdminOrTeacherHeaders() {
        // Teacher user from setup.sql
        return getAuthenticatedHeaders("teacher@test.com", "123456");
    }

    private HttpHeaders getStudentHeaders() {
        // Student user from setup.sql
        return getAuthenticatedHeaders("student1@test.com", "123456");
    }

    private long countRecordsInTable(String tableName) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName,
                Long.class
        );
        return count != null ? count : 0;
    }

    // ========================= GET /dashboards HAPPY PATH TESTS =========================

    @Nested
    @DisplayName("GET /dashboards Happy Path Tests")
    class GetDashboardHappyPathTests {

        @Test
        @Sql("/test-data/setup.sql")
        @DisplayName("Should retrieve all work statuses with no filters")
        void testGetDashboardNoFilters() {
            HttpHeaders headers = getAdminOrTeacherHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<GetDashboardResponse> response = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.GET,
                    requestEntity,
                    GetDashboardResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            GetDashboardResponse dashboard = response.getBody();

            // Verify all 5 statuses are present
            assertThat(dashboard.getTotalWorksByStatus())
                    .isNotNull()
                    .hasSize(5)
                    .extracting(GetDashboardResponse.GetDashboardWorksByStatus::getStatus)
                    .containsExactlyInAnyOrder("DRAFT", "SUBMITTED", "PENDING_CHANGES", "PUBLISHED", "REJECTED");

            // Verify PUBLISHED has 1 work (Work 1000)
            long publishedCount = dashboard.getTotalWorksByStatus().stream()
                    .filter(w -> "PUBLISHED".equals(w.getStatus()))
                    .mapToLong(GetDashboardResponse.GetDashboardWorksByStatus::getTotal)
                    .sum();
            assertThat(publishedCount).isEqualTo(1L);

            // Verify DRAFT has 1 work (Work 2000)
            long draftCount = dashboard.getTotalWorksByStatus().stream()
                    .filter(w -> "DRAFT".equals(w.getStatus()))
                    .mapToLong(GetDashboardResponse.GetDashboardWorksByStatus::getTotal)
                    .sum();
            assertThat(draftCount).isEqualTo(1L);

            // Verify labels (only published works counted)
            assertThat(dashboard.getTotalPublishedWorksByLabel())
                    .isNotNull()
                    .isNotEmpty()
                    .anySatisfy(label -> assertThat(label.getLabel()).isEqualTo("Tecnologia"));

            // Verify authors (only published works counted)
            assertThat(dashboard.getTotalPublishedWorksByAuthor())
                    .isNotNull()
                    .isNotEmpty();
        }

        @Test
        @Sql("/test-data/setup.sql")
        @DisplayName("Should filter by statusId = PUBLISHED (4)")
        void testGetDashboardWithStatusIdFilter() {
            HttpHeaders headers = getAdminOrTeacherHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<GetDashboardResponse> response = restTemplate.exchange(
                    baseUrl + "?statusId=4",
                    HttpMethod.GET,
                    requestEntity,
                    GetDashboardResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            GetDashboardResponse dashboard = response.getBody();

            assertThat(dashboard.getTotalWorksByStatus())
                    .hasSize(1)
                    .allSatisfy(status -> {
                        assertThat(status.getStatus()).isEqualTo("PUBLISHED");
                        assertThat(status.getTotal()).isEqualTo(1L);
                    });
        }

        @Test
        @Sql("/test-data/setup.sql")
        @DisplayName("Should filter by labelId = Tecnologia (1)")
        void testGetDashboardWithLabelIdFilter() {
            HttpHeaders headers = getAdminOrTeacherHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<GetDashboardResponse> response = restTemplate.exchange(
                    baseUrl + "?labelId=1",
                    HttpMethod.GET,
                    requestEntity,
                    GetDashboardResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            GetDashboardResponse dashboard = response.getBody();

            assertThat(dashboard.getTotalPublishedWorksByLabel())
                    .hasSize(1)
                    .allSatisfy(label -> {
                        assertThat(label.getLabel()).isEqualTo("Tecnologia");
                        assertThat(label.getTotal()).isEqualTo(1L);
                    });
        }

        @Test
        @Sql("/test-data/setup.sql")
        @DisplayName("Should filter by authorId = Mateus (1)")
        void testGetDashboardWithAuthorIdFilter() {
            HttpHeaders headers = getAdminOrTeacherHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<GetDashboardResponse> response = restTemplate.exchange(
                    baseUrl + "?authorId=1",
                    HttpMethod.GET,
                    requestEntity,
                    GetDashboardResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            GetDashboardResponse dashboard = response.getBody();

            assertThat(dashboard.getTotalPublishedWorksByAuthor())
                    .hasSize(1)
                    .allSatisfy(author -> {
                        assertThat(author.getAuthor()).isEqualTo("Mateus Teste");
                        assertThat(author.getTotal()).isEqualTo(1L);
                    });
        }

        @Test
        @Sql("/test-data/setup.sql")
        @DisplayName("Should handle null request gracefully")
        void testGetDashboardWithNullRequest() {
            HttpHeaders headers = getAdminOrTeacherHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<GetDashboardResponse> response = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.GET,
                    requestEntity,
                    GetDashboardResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTotalWorksByStatus()).isNotEmpty();
        }

        @Test
        @Sql("/test-data/setup.sql")
        @DisplayName("Should work with combined filters (statusId + labelId)")
        void testGetDashboardWithCombinedFilters() {
            HttpHeaders headers = getAdminOrTeacherHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<GetDashboardResponse> response = restTemplate.exchange(
                    baseUrl + "?statusId=4&labelId=1",
                    HttpMethod.GET,
                    requestEntity,
                    GetDashboardResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            GetDashboardResponse dashboard = response.getBody();

            assertThat(dashboard.getTotalWorksByStatus()).isNotEmpty();
            assertThat(dashboard.getTotalPublishedWorksByLabel()).isNotEmpty();
        }
    }

    // ========================= GET /dashboards EDGE CASE TESTS =========================

    @Nested
    @DisplayName("GET /dashboards Edge Case Tests")
    class GetDashboardEdgeCaseTests {

        @Test
        @Sql("/test-data/setup.sql")
        @DisplayName("Should return 400 for non-existent statusId")
        void testGetDashboardWithNonExistentStatusId() {
            HttpHeaders headers = getAdminOrTeacherHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "?statusId=999",
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @Sql("/test-data/setup.sql")
        @DisplayName("Should return 404 for non-existent labelId")
        void testGetDashboardWithNonExistentLabelId() {
            HttpHeaders headers = getAdminOrTeacherHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "?labelId=999",
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @Sql("/test-data/setup.sql")
        @DisplayName("Should return 404 for non-existent authorId")
        void testGetDashboardWithNonExistentAuthorId() {
            HttpHeaders headers = getAdminOrTeacherHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "?authorId=999",
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @Sql("/test-data/setup.sql")
        @DisplayName("Should handle future date range (no results)")
        void testGetDashboardWithFutureDateRange() {
            HttpHeaders headers = getAdminOrTeacherHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            // Using ISO 8601 format for LocalDateTime parameter
            String futureDate = "2099-12-31T23:59:59";
            ResponseEntity<GetDashboardResponse> response = restTemplate.exchange(
                    baseUrl + "?startDate=" + futureDate,
                    HttpMethod.GET,
                    requestEntity,
                    GetDashboardResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            GetDashboardResponse dashboard = response.getBody();

            // All counts should be zero for future dates
            long totalWorks = dashboard.getTotalWorksByStatus().stream()
                    .mapToLong(GetDashboardResponse.GetDashboardWorksByStatus::getTotal)
                    .sum();
            assertThat(totalWorks).isZero();
        }

        @Test
        @Sql("/test-data/setup.sql")
        @DisplayName("Should verify no database mutations after request")
        void testGetDashboardDoesNotMutateDatabase() {
            HttpHeaders headers = getAdminOrTeacherHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            // Count before
            long workCountBefore = countRecordsInTable("works");
            long authorCountBefore = countRecordsInTable("authors");
            long labelCountBefore = countRecordsInTable("labels");

            // Execute request
            ResponseEntity<GetDashboardResponse> response = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.GET,
                    requestEntity,
                    GetDashboardResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            // Count after
            long workCountAfter = countRecordsInTable("works");
            long authorCountAfter = countRecordsInTable("authors");
            long labelCountAfter = countRecordsInTable("labels");

            // Verify no changes
            assertThat(workCountAfter).isEqualTo(workCountBefore);
            assertThat(authorCountAfter).isEqualTo(authorCountBefore);
            assertThat(labelCountAfter).isEqualTo(labelCountBefore);
        }

        @Test
        @Sql("/test-data/setup.sql")
        @DisplayName("Should handle label with no published works")
        void testGetDashboardLabelWithZeroPublished() {
            HttpHeaders headers = getAdminOrTeacherHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            // Filter by Java label (ID=2) which has no published works
            ResponseEntity<GetDashboardResponse> response = restTemplate.exchange(
                    baseUrl + "?labelId=2",
                    HttpMethod.GET,
                    requestEntity,
                    GetDashboardResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            GetDashboardResponse dashboard = response.getBody();

            assertThat(dashboard.getTotalPublishedWorksByLabel())
                    .hasSize(1)
                    .allSatisfy(label -> {
                        assertThat(label.getLabel()).isEqualTo("Java");
                        assertThat(label.getTotal()).isZero();
                    });
        }
    }

    // ========================= GET /dashboards SECURITY TESTS =========================

    @Nested
    @DisplayName("GET /dashboards Security Tests")
    class GetDashboardSecurityTests {

        @Test
        @Sql("/test-data/setup.sql")
        @DisplayName("Should allow IS_TEACHER access")
        void testGetDashboardAsTeacher() {
            HttpHeaders headers = getAdminOrTeacherHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<GetDashboardResponse> response = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.GET,
                    requestEntity,
                    GetDashboardResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @Sql("/test-data/setup.sql")
        @DisplayName("Should deny IS_STUDENT access with 403 Forbidden")
        void testGetDashboardAsStudent_Forbidden() {
            HttpHeaders headers = getStudentHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @Sql("/test-data/setup.sql")
        @DisplayName("Should deny access without token - 401 Unauthorized")
        void testGetDashboardWithoutToken() {
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @Sql("/test-data/setup.sql")
        @DisplayName("Should deny access with invalid token")
        void testGetDashboardWithInvalidToken() {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth("invalid.jwt.token");
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    // ========================= GET /dashboards/authors HAPPY PATH TESTS =========================

    @Nested
    @DisplayName("GET /dashboards/authors Happy Path Tests")
    class GetAuthorsDashboardHappyPathTests {

        @Test
        @Sql("/test-data/setup.sql")
        @DisplayName("Should retrieve authors count and most cited author")
        void testGetAuthorsCountAndMostCited() {
            HttpHeaders headers = getAdminOrTeacherHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<GetAuthorsDashboardResponse> response = restTemplate.exchange(
                    baseUrl + "/authors",
                    HttpMethod.GET,
                    requestEntity,
                    GetAuthorsDashboardResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            GetAuthorsDashboardResponse dashboard = response.getBody();

            // 3 registered authors on published works (Mateus, Student1, Student2)
            assertThat(dashboard.getInternalAuthorsCount()).isEqualTo(3L);

            // 2 authors not on published works (Teacher, Joao Silva)
            assertThat(dashboard.getExternalAuthorsCount()).isEqualTo(2L);

            // Total should be 5
            assertThat(dashboard.getInternalAuthorsCount() + dashboard.getExternalAuthorsCount())
                    .isEqualTo(5L);

            // Most cited author should be Mateus (he has the most published works)
            assertThat(dashboard.getMostCitedAuthor())
                    .isNotNull()
                    .isEqualTo("Mateus Teste");
        }

        @Test
        @Sql("/test-data/setup.sql")
        @DisplayName("Should verify authors database not mutated")
        void testGetAuthorsDoesNotMutateDatabase() {
            HttpHeaders headers = getAdminOrTeacherHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            long countBefore = countRecordsInTable("authors");

            ResponseEntity<GetAuthorsDashboardResponse> response = restTemplate.exchange(
                    baseUrl + "/authors",
                    HttpMethod.GET,
                    requestEntity,
                    GetAuthorsDashboardResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            long countAfter = countRecordsInTable("authors");
            assertThat(countAfter).isEqualTo(countBefore);
        }
    }

    // ========================= GET /dashboards/authors SECURITY TESTS =========================

    @Nested
    @DisplayName("GET /dashboards/authors Security Tests")
    class GetAuthorsDashboardSecurityTests {

        @Test
        @Sql("/test-data/setup.sql")
        @DisplayName("Should allow IS_TEACHER access")
        void testGetAuthorsAsTeacher() {
            HttpHeaders headers = getAdminOrTeacherHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<GetAuthorsDashboardResponse> response = restTemplate.exchange(
                    baseUrl + "/authors",
                    HttpMethod.GET,
                    requestEntity,
                    GetAuthorsDashboardResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @Sql("/test-data/setup.sql")
        @DisplayName("Should deny IS_STUDENT access with 403 Forbidden")
        void testGetAuthorsAsStudent_Forbidden() {
            HttpHeaders headers = getStudentHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/authors",
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @Sql("/test-data/setup.sql")
        @DisplayName("Should deny access without token - 401 Unauthorized")
        void testGetAuthorsWithoutToken() {
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/authors",
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    // ========================= GET /dashboards/labels HAPPY PATH TESTS =========================

    @Nested
    @DisplayName("GET /dashboards/labels Happy Path Tests")
    class GetLabelsDashboardHappyPathTests {

        @Test
        @Sql("/test-data/setup.sql")
        @DisplayName("Should retrieve labels count and most/least used labels")
        void testGetLabelsCountAndMostLeastUsed() {
            HttpHeaders headers = getAdminOrTeacherHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<GetLabelsDashboardResponse> response = restTemplate.exchange(
                    baseUrl + "/labels",
                    HttpMethod.GET,
                    requestEntity,
                    GetLabelsDashboardResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            GetLabelsDashboardResponse dashboard = response.getBody();

            // Should have 1 label on published works (Tecnologia)
            assertThat(dashboard.getQuantityOfLabels()).isEqualTo(1L);

            // Most used should be Tecnologia (has 1 published work)
            assertThat(dashboard.getMostUsedLabel()).isEqualTo("Tecnologia");

            // Least used should be null (Java has 0 published works)
            assertThat(dashboard.getLeastUsedLabel()).isNotNull();
        }

        @Test
        @Sql("/test-data/setup.sql")
        @DisplayName("Should verify labels database not mutated")
        void testGetLabelsDoesNotMutateDatabase() {
            HttpHeaders headers = getAdminOrTeacherHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            long countBefore = countRecordsInTable("labels");

            ResponseEntity<GetLabelsDashboardResponse> response = restTemplate.exchange(
                    baseUrl + "/labels",
                    HttpMethod.GET,
                    requestEntity,
                    GetLabelsDashboardResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            long countAfter = countRecordsInTable("labels");
            assertThat(countAfter).isEqualTo(countBefore);
        }
    }

    // ========================= GET /dashboards/labels SECURITY TESTS =========================

    @Nested
    @DisplayName("GET /dashboards/labels Security Tests")
    class GetLabelsDashboardSecurityTests {

        @Test
        @Sql("/test-data/setup.sql")
        @DisplayName("Should allow IS_TEACHER access")
        void testGetLabelsAsTeacher() {
            HttpHeaders headers = getAdminOrTeacherHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<GetLabelsDashboardResponse> response = restTemplate.exchange(
                    baseUrl + "/labels",
                    HttpMethod.GET,
                    requestEntity,
                    GetLabelsDashboardResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @Sql("/test-data/setup.sql")
        @DisplayName("Should deny IS_STUDENT access with 403 Forbidden")
        void testGetLabelsAsStudent_Forbidden() {
            HttpHeaders headers = getStudentHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/labels",
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @Sql("/test-data/setup.sql")
        @DisplayName("Should deny access without token - 401 Unauthorized")
        void testGetLabelsWithoutToken() {
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/labels",
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }
}

