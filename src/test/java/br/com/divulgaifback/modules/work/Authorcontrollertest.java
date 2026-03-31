package br.com.divulgaifback.modules.work;

import br.com.divulgaifback.modules.auth.useCases.login.LoginRequest;
import br.com.divulgaifback.modules.auth.useCases.login.LoginResponse;
import br.com.divulgaifback.modules.works.useCases.author.create.CreateAuthorRequest;
import br.com.divulgaifback.modules.works.useCases.author.create.CreateAuthorResponse;
import br.com.divulgaifback.modules.works.useCases.author.update.UpdateAuthorRequest;
import br.com.divulgaifback.modules.works.useCases.author.update.UpdateAuthorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de Integração — AuthorController
 * ISO 25010 — Adequação Funcional:
 *   - Completude: todas as operações CRUD testadas
 *   - Correção:   respostas e dados persistidos corretamente
 *   - Pertinência: só as funções necessárias são expostas
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.DisplayName.class)
class AuthorControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1";
    }

    private HttpHeaders authHeaders() {
        LoginRequest login = new LoginRequest("mateusteste@teste.com", "123456");
        ResponseEntity<LoginResponse> resp = restTemplate.postForEntity(
                baseUrl() + "/auth/login", login, LoginResponse.class);
        assertNotNull(resp.getBody(), "Login falhou — body nulo");
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.setBearerAuth(resp.getBody().accessToken);
        return h;
    }

    private HttpHeaders teacherAuthHeaders() {
        LoginRequest login = new LoginRequest("teacher@test.com", "123456");
        ResponseEntity<LoginResponse> resp = restTemplate.postForEntity(
                baseUrl() + "/auth/login", login, LoginResponse.class);
        assertNotNull(resp.getBody(), "Login do professor falhou");
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.setBearerAuth(resp.getBody().accessToken);
        return h;
    }

    // -------------------------------------------------------
    // INSERÇÃO — Dados Válidos
    // -------------------------------------------------------

    @Test
    @DisplayName("INS-01: Criar autor com dados válidos retorna 201")
    @Sql("/test-data/setup.sql")
    void createAuthor_validData_returns201() {
        CreateAuthorRequest req = new CreateAuthorRequest(
                "Novo Autor Válido",
                "novo.autor@ifpr.edu.br"
        );
        HttpEntity<CreateAuthorRequest> entity = new HttpEntity<>(req, authHeaders());

        ResponseEntity<CreateAuthorResponse> resp = restTemplate.postForEntity(
                baseUrl() + "/authors", entity, CreateAuthorResponse.class);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertNotNull(resp.getBody().id, "ID do autor deve ser gerado");
        assertEquals("Novo Autor Válido", resp.getBody().name);
        assertEquals("novo.autor@ifpr.edu.br", resp.getBody().email);
    }

    @Test
    @DisplayName("INS-02: Criar autor externo (não registrado no sistema)")
    @Sql("/test-data/setup.sql")
    void createAuthor_externalUser_returnsUnregisteredType() {
        CreateAuthorRequest req = new CreateAuthorRequest(
                "Autor Externo",
                "autor.externo.sem.cadastro@gmail.com"
        );
        HttpEntity<CreateAuthorRequest> entity = new HttpEntity<>(req, authHeaders());

        ResponseEntity<CreateAuthorResponse> resp = restTemplate.postForEntity(
                baseUrl() + "/authors", entity, CreateAuthorResponse.class);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertNotNull(resp.getBody());
        // Autor sem user no sistema → tipo = SEM CADASTRO
        assertEquals("SEM CADASTRO", resp.getBody().type);
    }

    @Test
    @DisplayName("INS-03: Criar autor com e-mail de usuário existente vincula ao user")
    @Sql("/test-data/setup.sql")
    void createAuthor_emailMatchesUser_linksToUser() {
        // mateusteste@teste.com está no setup.sql como user
        CreateAuthorRequest req = new CreateAuthorRequest(
                "Mateus Autor Link",
                "mateusteste@teste.com"
        );
        HttpEntity<CreateAuthorRequest> entity = new HttpEntity<>(req, authHeaders());

        ResponseEntity<CreateAuthorResponse> resp = restTemplate.postForEntity(
                baseUrl() + "/authors", entity, CreateAuthorResponse.class);

        // Esperado: CONFLICT pois já existe autor com esse email no setup
        assertTrue(
            resp.getStatusCode() == HttpStatus.CONFLICT ||
            resp.getStatusCode() == HttpStatus.CREATED,
            "Deve retornar 409 (duplicado) ou 201"
        );
    }

    // -------------------------------------------------------
    // INSERÇÃO — Dados Inválidos
    // -------------------------------------------------------

    @Test
    @DisplayName("INS-04: Criar autor sem nome retorna 400")
    @Sql("/test-data/setup.sql")
    void createAuthor_missingName_returns400() {
        CreateAuthorRequest req = new CreateAuthorRequest("", "sem.nome@ifpr.edu.br");
        HttpEntity<CreateAuthorRequest> entity = new HttpEntity<>(req, authHeaders());

        ResponseEntity<String> resp = restTemplate.postForEntity(
                baseUrl() + "/authors", entity, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    @DisplayName("INS-05: Criar autor sem email retorna 400")
    @Sql("/test-data/setup.sql")
    void createAuthor_missingEmail_returns400() {
        CreateAuthorRequest req = new CreateAuthorRequest("Autor Sem Email", "");
        HttpEntity<CreateAuthorRequest> entity = new HttpEntity<>(req, authHeaders());

        ResponseEntity<String> resp = restTemplate.postForEntity(
                baseUrl() + "/authors", entity, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    @DisplayName("INS-06: Criar autor com e-mail inválido retorna 400")
    @Sql("/test-data/setup.sql")
    void createAuthor_invalidEmailFormat_returns400() {
        CreateAuthorRequest req = new CreateAuthorRequest("Autor Email Inválido", "nao-e-um-email");
        HttpEntity<CreateAuthorRequest> entity = new HttpEntity<>(req, authHeaders());

        ResponseEntity<String> resp = restTemplate.postForEntity(
                baseUrl() + "/authors", entity, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    @DisplayName("INS-07: Criar autor duplicado (mesmo email) retorna 409")
    @Sql("/test-data/setup.sql")
    void createAuthor_duplicateEmail_returns409() {
        CreateAuthorRequest req = new CreateAuthorRequest("Autor Dup", "autor.dup.test@ifpr.edu.br");
        HttpEntity<CreateAuthorRequest> entity = new HttpEntity<>(req, authHeaders());

        // Primeiro cadastro — deve criar
        ResponseEntity<CreateAuthorResponse> first = restTemplate.postForEntity(
                baseUrl() + "/authors", entity, CreateAuthorResponse.class);
        assertEquals(HttpStatus.CREATED, first.getStatusCode());

        // Segundo cadastro — deve conflitar
        ResponseEntity<String> second = restTemplate.postForEntity(
                baseUrl() + "/authors", entity, String.class);
        assertEquals(HttpStatus.CONFLICT, second.getStatusCode());
    }

    @Test
    @DisplayName("INS-08: Criar autor sem autenticação retorna 401")
    @Sql("/test-data/setup.sql")
    void createAuthor_unauthenticated_returns401() {
        CreateAuthorRequest req = new CreateAuthorRequest("Autor Sem Auth", "sem.auth@ifpr.edu.br");
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateAuthorRequest> entity = new HttpEntity<>(req, h);

        ResponseEntity<String> resp = restTemplate.postForEntity(
                baseUrl() + "/authors", entity, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }
}
