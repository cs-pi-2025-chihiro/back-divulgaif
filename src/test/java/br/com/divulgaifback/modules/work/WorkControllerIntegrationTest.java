package br.com.divulgaifback.modules.work;

import br.com.divulgaifback.modules.auth.useCases.login.LoginRequest;
import br.com.divulgaifback.modules.auth.useCases.login.LoginResponse;
import br.com.divulgaifback.modules.works.useCases.work.create.CreateWorkRequest;
import br.com.divulgaifback.modules.works.useCases.work.create.CreateWorkResponse;
import br.com.divulgaifback.modules.works.useCases.work.get.GetWorkResponse;
import br.com.divulgaifback.modules.works.useCases.work.update.UpdateWorkRequest;
import br.com.divulgaifback.modules.works.useCases.work.update.UpdateWorkResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração para WorkController.
 *
 * Cobre os endpoints:
 *   POST   /works           – criação de trabalho
 *   PUT    /works/{id}      – atualização de trabalho
 *   GET    /works/{id}      – busca de trabalho por ID
 *   POST   /works/publish/{id} – publicação de trabalho
 *
 * Abordagem: CANDOR-inspired — cada teste valida o oráculo (asserção)
 * com base na especificação funcional do sistema (regras de negócio),
 * não apenas no comportamento atual da implementação.
 *
 * Padrão: AAA (Arrange → Act → Assert)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class WorkControllerIntegrationTest {

    // -------------------------------------------------------------------------
    // Infraestrutura de teste
    // -------------------------------------------------------------------------

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------------------------------
    // Helpers de URL
    // -------------------------------------------------------------------------

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1";
    }

    private String worksUrl() {
        return baseUrl() + "/works";
    }

    // -------------------------------------------------------------------------
    // Helpers de autenticação
    // -------------------------------------------------------------------------

    /**
     * Retorna headers com Bearer token de um estudante (IS_STUDENT).
     * Credenciais definidas em /test-data/setup.sql
     */
    private HttpHeaders getStudentHeaders() {
        LoginRequest login = new LoginRequest("mateusteste@teste.com", "123456");
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                baseUrl() + "/auth/login",
                new HttpEntity<>(login, jsonHeaders()),
                LoginResponse.class
        );
        assertNotNull(response.getBody(), "Login do estudante falhou – verifique o setup.sql");
        HttpHeaders headers = jsonHeaders();
        headers.setBearerAuth(response.getBody().accessToken);
        return headers;
    }

    /**
     * Retorna headers com Bearer token de um professor (IS_TEACHER).
     * Credenciais definidas em /test-data/setup.sql
     */
    private HttpHeaders getTeacherHeaders() {
        LoginRequest login = new LoginRequest("teacher@test.com", "123456");
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                baseUrl() + "/auth/login",
                new HttpEntity<>(login, jsonHeaders()),
                LoginResponse.class
        );
        assertNotNull(response.getBody(), "Login do professor falhou – verifique o setup.sql");
        HttpHeaders headers = jsonHeaders();
        headers.setBearerAuth(response.getBody().accessToken);
        return headers;
    }

    /** Headers sem autenticação (somente Content-Type). */
    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /** Monta uma requisição de criação de trabalho com dados mínimos válidos. */
    private CreateWorkRequest buildValidCreateRequest() {
        return new CreateWorkRequest(
                "Trabalho de Teste",
                "Descrição completa do trabalho",
                "Conteúdo detalhado do trabalho",
                "https://link-principal.com",
                "tag1, tag2",
                "https://example.com/imagem.jpg",
                null,
                null,
                null,
                null,
                null,
                "ARTICLE",
                "DRAFT"
        );
    }

    // =========================================================================
    // 1. CASOS DE SUCESSO (Happy Path)
    // =========================================================================

    @Test
    @Sql("/test-data/setup.sql")
    @DisplayName("Deve criar trabalho com status 201 quando dados válidos e usuário autenticado")
    void deveCriarTrabalhoComSucesso() {
        // ARRANGE
        CreateWorkRequest request = buildValidCreateRequest();
        HttpEntity<CreateWorkRequest> entity = new HttpEntity<>(request, getStudentHeaders());

        // ACT
        ResponseEntity<CreateWorkResponse> response = restTemplate.postForEntity(
                worksUrl(), entity, CreateWorkResponse.class
        );

        // ASSERT
        assertEquals(HttpStatus.CREATED, response.getStatusCode(),
                "Status HTTP deve ser 201 CREATED");
        assertNotNull(response.getBody(), "Corpo da resposta não deve ser nulo");

        CreateWorkResponse body = response.getBody();
        assertEquals("Trabalho de Teste", body.title,
                "Título deve corresponder ao enviado");
        assertNotNull(body.workStatus, "Status do trabalho não deve ser nulo");
        assertEquals("DRAFT", body.workStatus.name,
                "Trabalho recém-criado deve ter status DRAFT");
        assertNotNull(body.authors, "Lista de autores não deve ser nula");
        assertFalse(body.authors.isEmpty(),
                "Criador deve ser automaticamente adicionado como autor (regra de negócio)");
    }

    @Test
    @Sql("/test-data/setup.sql")
    @DisplayName("Deve retornar trabalho com status 200 quando ID existente é consultado")
    void deveBuscarTrabalhoPorIdComSucesso() {
        // ARRANGE – trabalho de ID 1000 inserido pelo setup.sql
        HttpEntity<Void> entity = new HttpEntity<>(getStudentHeaders());

        // ACT
        ResponseEntity<GetWorkResponse> response = restTemplate.exchange(
                worksUrl() + "/1000",
                HttpMethod.GET,
                entity,
                GetWorkResponse.class
        );

        // ASSERT
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Status HTTP deve ser 200 OK");
        assertNotNull(response.getBody(), "Corpo da resposta não deve ser nulo");
        assertEquals(1000, response.getBody().id,
                "ID do trabalho retornado deve ser 1000");
        assertEquals("Desenvolvimento de Sistema Web", response.getBody().title,
                "Título deve corresponder ao cadastrado no setup.sql");
    }

    @Test
    @Sql("/test-data/setup.sql")
    @DisplayName("Deve atualizar trabalho com status 200 quando dados válidos e usuário autenticado")
    void deveAtualizarTrabalhoComSucesso() {
        // ARRANGE – trabalho de ID 2000 (status DRAFT) inserido pelo setup.sql
        UpdateWorkRequest request = new UpdateWorkRequest(
                "Título Atualizado",
                "Nova descrição",
                "Novo conteúdo",
                "https://novo-link.com",
                "nova-tag",
                "https://example.com/nova-imagem.jpg",
                null,
                null,
                null,
                null,
                null,
                "ARTICLE",
                "DRAFT"
        );
        HttpEntity<UpdateWorkRequest> entity = new HttpEntity<>(request, getStudentHeaders());

        // ACT
        ResponseEntity<UpdateWorkResponse> response = restTemplate.exchange(
                worksUrl() + "/2000",
                HttpMethod.PUT,
                entity,
                UpdateWorkResponse.class
        );

        // ASSERT
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Status HTTP deve ser 200 OK");
        assertNotNull(response.getBody(), "Corpo da resposta não deve ser nulo");
        assertEquals("Título Atualizado", response.getBody().title,
                "Título deve ter sido atualizado");
        assertEquals("DRAFT", response.getBody().workStatus.name,
                "Status deve permanecer DRAFT após edição simples");
    }

    @Test
    @Sql("/test-data/setup.sql")
    @DisplayName("Deve publicar trabalho com status 200 quando professor autentica e trabalho existe")
    void devePublicarTrabalhoComSucesso() {
        // ARRANGE – trabalho de ID 1000 existe no banco (setup.sql)
        HttpEntity<Void> entity = new HttpEntity<>(getTeacherHeaders());

        // ACT
        ResponseEntity<Void> response = restTemplate.exchange(
                worksUrl() + "/publish/1000",
                HttpMethod.POST,
                entity,
                Void.class
        );

        // ASSERT
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Status HTTP deve ser 200 OK após publicação");

        // Verifica persistência: busca o trabalho e confere o status
        ResponseEntity<GetWorkResponse> getResponse = restTemplate.exchange(
                worksUrl() + "/1000",
                HttpMethod.GET,
                new HttpEntity<>(getTeacherHeaders()),
                GetWorkResponse.class
        );
        assertNotNull(getResponse.getBody());
        assertEquals("PUBLISHED", getResponse.getBody().workStatus.name,
                "Status do trabalho deve ser PUBLISHED após publicação (verificação de persistência)");
    }

    @Test
    @Sql("/test-data/setup.sql")
    @DisplayName("Deve criar trabalho com autores externos quando lista newAuthors é fornecida")
    void deveCriarTrabalhoComAutoresExternos() {
        // ARRANGE
        CreateWorkRequest.AuthorRequest autorExterno = new CreateWorkRequest.AuthorRequest(
                "Colaborador Externo",
                "externo@universidade.edu.br"
        );
        CreateWorkRequest request = new CreateWorkRequest(
                "Projeto Colaborativo",
                "Trabalho com autores externos",
                "Conteúdo do projeto",
                null, null, null, null, null,
                List.of(autorExterno),
                null, null,
                "SEARCH",
                "DRAFT"
        );
        HttpEntity<CreateWorkRequest> entity = new HttpEntity<>(request, getStudentHeaders());

        // ACT
        ResponseEntity<CreateWorkResponse> response = restTemplate.postForEntity(
                worksUrl(), entity, CreateWorkResponse.class
        );

        // ASSERT
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().authors.size() >= 2,
                "Deve ter o autor externo + o criador (adicionado automaticamente pela regra de negócio)");
    }

    // =========================================================================
    // 2. CASOS DE ERRO – AUTENTICAÇÃO / AUTORIZAÇÃO
    // =========================================================================

    @Test
    @Sql("/test-data/setup.sql")
    @DisplayName("Deve retornar 401 quando criar trabalho sem token JWT")
    void deveRetornar401AoCriarSemAutenticacao() {
        // ARRANGE
        CreateWorkRequest request = buildValidCreateRequest();
        HttpEntity<CreateWorkRequest> entity = new HttpEntity<>(request, jsonHeaders());

        // ACT
        ResponseEntity<String> response = restTemplate.postForEntity(
                worksUrl(), entity, String.class
        );

        // ASSERT
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(),
                "Deve retornar 401 quando não há token JWT");
    }

    @Test
    @Sql("/test-data/setup.sql")
    @DisplayName("Deve retornar 401 quando token JWT é inválido")
    void deveRetornar401ComTokenInvalido() {
        // ARRANGE
        HttpHeaders headers = jsonHeaders();
        headers.setBearerAuth("token.invalido.assinatura");
        HttpEntity<CreateWorkRequest> entity = new HttpEntity<>(buildValidCreateRequest(), headers);

        // ACT
        ResponseEntity<String> response = restTemplate.postForEntity(
                worksUrl(), entity, String.class
        );

        // ASSERT
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(),
                "Token mal-formado deve resultar em 401");
    }

    @Test
    @Sql("/test-data/setup.sql")
    @DisplayName("Deve retornar 403 quando estudante tenta publicar trabalho")
    void deveRetornar403QuandoEstudanteTentaPublicar() {
        // ARRANGE – somente IS_TEACHER e IS_ADMIN podem publicar (regra de negócio)
        HttpEntity<Void> entity = new HttpEntity<>(getStudentHeaders());

        // ACT
        ResponseEntity<String> response = restTemplate.exchange(
                worksUrl() + "/publish/1000",
                HttpMethod.POST,
                entity,
                String.class
        );

        // ASSERT
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
                "Estudante não tem permissão para publicar trabalhos – deve retornar 403");
    }

    @Test
    @Sql("/test-data/setup.sql")
    @DisplayName("Deve retornar 403 quando estudante tenta criar trabalho com status PUBLISHED")
    void deveRetornar403QuandoEstudanteTentaCriarComoPublicado() {
        // ARRANGE – status PUBLISHED é restrito a professores/admin
        CreateWorkRequest request = new CreateWorkRequest(
                "Trabalho Publicado Direto",
                "Tentativa indevida",
                "Conteúdo",
                null, null, null, null, null, null, null, null,
                "ARTICLE",
                "PUBLISHED"   // <-- violação de regra de negócio
        );
        HttpEntity<CreateWorkRequest> entity = new HttpEntity<>(request, getStudentHeaders());

        // ACT
        ResponseEntity<String> response = restTemplate.postForEntity(
                worksUrl(), entity, String.class
        );

        // ASSERT
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
                "Estudante não pode criar trabalho já publicado – deve retornar 403");
    }

    // =========================================================================
    // 3. CASOS DE ERRO – VALIDAÇÃO DE ENTRADA
    // =========================================================================

    @Test
    @Sql("/test-data/setup.sql")
    @DisplayName("Deve retornar 400 quando título está vazio")
    void deveRetornar400ComTituloVazio() {
        // ARRANGE
        CreateWorkRequest request = new CreateWorkRequest(
                "",  // título vazio — campo obrigatório
                "Descrição",
                "Conteúdo",
                null, null, null, null, null, null, null, null,
                "ARTICLE",
                "DRAFT"
        );
        HttpEntity<CreateWorkRequest> entity = new HttpEntity<>(request, getStudentHeaders());

        // ACT
        ResponseEntity<String> response = restTemplate.postForEntity(
                worksUrl(), entity, String.class
        );

        // ASSERT
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
                "Título vazio deve ser rejeitado com 400");
    }

    @Test
    @Sql("/test-data/setup.sql")
    @DisplayName("Deve retornar 400 quando tipo de trabalho está ausente")
    void deveRetornar400ComTipoDeTrabalhoAusente() {
        // ARRANGE
        CreateWorkRequest request = new CreateWorkRequest(
                "Título Válido",
                "Descrição",
                "Conteúdo",
                null, null, null, null, null, null, null, null,
                "",  // workType vazio — campo obrigatório
                "DRAFT"
        );
        HttpEntity<CreateWorkRequest> entity = new HttpEntity<>(request, getStudentHeaders());

        // ACT
        ResponseEntity<String> response = restTemplate.postForEntity(
                worksUrl(), entity, String.class
        );

        // ASSERT
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
                "Tipo de trabalho vazio deve ser rejeitado com 400");
    }

    @Test
    @Sql("/test-data/setup.sql")
    @DisplayName("Deve retornar 400 quando email do autor tem formato inválido")
    void deveRetornar400ComEmailDeAutorInvalido() {
        // ARRANGE
        CreateWorkRequest.AuthorRequest autorInvalido = new CreateWorkRequest.AuthorRequest(
                "Autor Inválido",
                "nao-e-um-email"  // formato inválido
        );
        CreateWorkRequest request = new CreateWorkRequest(
                "Título",
                "Descrição",
                "Conteúdo",
                null, null, null, null, null,
                List.of(autorInvalido),
                null, null,
                "ARTICLE",
                "DRAFT"
        );
        HttpEntity<CreateWorkRequest> entity = new HttpEntity<>(request, getStudentHeaders());

        // ACT
        ResponseEntity<String> response = restTemplate.postForEntity(
                worksUrl(), entity, String.class
        );

        // ASSERT
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
                "Email de autor mal-formatado deve ser rejeitado com 400");
    }

    @Test
    @Sql("/test-data/setup.sql")
    @DisplayName("Deve retornar 400 quando URL do link é inválida")
    void deveRetornar400ComUrlDeLinkInvalida() {
        // ARRANGE
        CreateWorkRequest.LinkRequest linkInvalido = new CreateWorkRequest.LinkRequest(
                "Link Inválido",
                "isso-nao-e-uma-url",  // sem protocolo http/https
                "Descrição"
        );
        CreateWorkRequest request = new CreateWorkRequest(
                "Título",
                "Descrição",
                "Conteúdo",
                null, null, null, null, null, null, null,
                List.of(linkInvalido),
                "ARTICLE",
                "DRAFT"
        );
        HttpEntity<CreateWorkRequest> entity = new HttpEntity<>(request, getStudentHeaders());

        // ACT
        ResponseEntity<String> response = restTemplate.postForEntity(
                worksUrl(), entity, String.class
        );

        // ASSERT
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
                "URL mal-formatada deve ser rejeitada com 400");
    }

    @Test
    @Sql("/test-data/setup.sql")
    @DisplayName("Deve retornar 400 quando título excede 255 caracteres")
    void deveRetornar400ComTituloMuitoLongo() {
        // ARRANGE
        String tituloLongo = "A".repeat(256);  // limite é 255
        CreateWorkRequest request = new CreateWorkRequest(
                tituloLongo,
                "Descrição",
                "Conteúdo",
                null, null, null, null, null, null, null, null,
                "ARTICLE",
                "DRAFT"
        );
        HttpEntity<CreateWorkRequest> entity = new HttpEntity<>(request, getStudentHeaders());

        // ACT
        ResponseEntity<String> response = restTemplate.postForEntity(
                worksUrl(), entity, String.class
        );

        // ASSERT
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
                "Título com mais de 255 caracteres deve ser rejeitado com 400");
    }

    // =========================================================================
    // 4. CASOS DE ERRO – REGRAS DE NEGÓCIO
    // =========================================================================

    @Test
    @Sql("/test-data/setup.sql")
    @DisplayName("Deve retornar 404 quando buscar trabalho com ID inexistente")
    void deveRetornar404QuandoTrabalhNaoEncontrado() {
        // ARRANGE
        HttpEntity<Void> entity = new HttpEntity<>(getStudentHeaders());

        // ACT
        ResponseEntity<String> response = restTemplate.exchange(
                worksUrl() + "/99999",
                HttpMethod.GET,
                entity,
                String.class
        );

        // ASSERT
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
                "ID inexistente deve retornar 404");
    }

    @Test
    @Sql("/test-data/setup.sql")
    @DisplayName("Deve retornar 404 quando publicar trabalho com ID inexistente")
    void deveRetornar404AoPublicarTrabalhoInexistente() {
        // ARRANGE
        HttpEntity<Void> entity = new HttpEntity<>(getTeacherHeaders());

        // ACT
        ResponseEntity<String> response = restTemplate.exchange(
                worksUrl() + "/publish/99999",
                HttpMethod.POST,
                entity,
                String.class
        );

        // ASSERT
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
                "Publicar ID inexistente deve retornar 404");
    }

    @Test
    @Sql("/test-data/setup.sql")
    @DisplayName("Deve retornar 404 quando tipo de trabalho não existe no sistema")
    void deveRetornar404ComTipoDeTrabalhoInexistente() {
        // ARRANGE
        CreateWorkRequest request = new CreateWorkRequest(
                "Título Válido",
                "Descrição",
                "Conteúdo",
                "https://link.com",
                "meta",
                "https://img.com/foto.jpg",
                null, null, null, null, null,
                "TIPO_INEXISTENTE",  // não cadastrado na base
                "DRAFT"
        );
        HttpEntity<CreateWorkRequest> entity = new HttpEntity<>(request, getStudentHeaders());

        // ACT
        ResponseEntity<String> response = restTemplate.postForEntity(
                worksUrl(), entity, String.class
        );

        // ASSERT
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
                "Tipo de trabalho inválido deve retornar 404 (WorkType não encontrado)");
    }

    @Test
    @Sql("/test-data/setup.sql")
    @DisplayName("Deve retornar 404 quando estudante referenciado não existe")
    void deveRetornar404ComEstudanteInexistente() {
        // ARRANGE
        CreateWorkRequest request = new CreateWorkRequest(
                "Título",
                "Descrição",
                "Conteúdo",
                "https://link.com",
                "meta",
                "https://img.com/foto.jpg",
                null,
                List.of(99999),  // ID de estudante inexistente
                null, null, null,
                "ARTICLE",
                "DRAFT"
        );
        HttpEntity<CreateWorkRequest> entity = new HttpEntity<>(request, getStudentHeaders());

        // ACT
        ResponseEntity<String> response = restTemplate.postForEntity(
                worksUrl(), entity, String.class
        );

        // ASSERT
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
                "Estudante inexistente deve retornar 404");
    }

    // =========================================================================
    // 5. CASOS EXTREMOS (Edge Cases)
    // =========================================================================

    @Test
    @Sql("/test-data/setup.sql")
    @DisplayName("Deve criar trabalho com sucesso quando listas opcionais são nulas")
    void deveCriarTrabalhoComListasOpcionaisNulas() {
        // ARRANGE – campos opcionais explicitamente nulos
        CreateWorkRequest request = new CreateWorkRequest(
                "Trabalho Sem Opcionais",
                "Descrição mínima",
                "Conteúdo mínimo",
                null,  // principalLink nulo
                null,  // metaTag nulo
                null,  // imageUrl nulo
                null,  // teacherId nulo
                null,  // studentIds nulo
                null,  // newAuthors nulo
                null,  // workLabels nulo
                null,  // workLinks nulo
                "FINAL_THESIS",
                "DRAFT"
        );
        HttpEntity<CreateWorkRequest> entity = new HttpEntity<>(request, getStudentHeaders());

        // ACT
        ResponseEntity<CreateWorkResponse> response = restTemplate.postForEntity(
                worksUrl(), entity, CreateWorkResponse.class
        );

        // ASSERT
        assertEquals(HttpStatus.CREATED, response.getStatusCode(),
                "Campos opcionais nulos não devem impedir a criação");
        assertNotNull(response.getBody());
        assertEquals("Trabalho Sem Opcionais", response.getBody().title);
    }

    @Test
    @Sql("/test-data/setup.sql")
    @DisplayName("Deve criar trabalho com sucesso quando listas opcionais são vazias")
    void deveCriarTrabalhoComListasOpcionaisVazias() {
        // ARRANGE – listas vazias (diferente de nulo — comportamento distinto)
        CreateWorkRequest request = new CreateWorkRequest(
                "Trabalho Listas Vazias",
                "Descrição",
                "Conteúdo",
                null, null, null, null,
                Collections.emptyList(),   // studentIds vazio
                Collections.emptyList(),   // newAuthors vazio
                Collections.emptyList(),   // workLabels vazio
                Collections.emptyList(),   // workLinks vazio
                "DISSERTATION",
                "DRAFT"
        );
        HttpEntity<CreateWorkRequest> entity = new HttpEntity<>(request, getStudentHeaders());

        // ACT
        ResponseEntity<CreateWorkResponse> response = restTemplate.postForEntity(
                worksUrl(), entity, CreateWorkResponse.class
        );

        // ASSERT
        assertEquals(HttpStatus.CREATED, response.getStatusCode(),
                "Listas vazias não devem impedir a criação");
    }

    @Test
    @Sql("/test-data/setup.sql")
    @DisplayName("Deve criar trabalho com sucesso quando título contém caracteres especiais")
    void deveCriarTrabalhoComCaracteresEspeciaisNoTitulo() {
        // ARRANGE
        CreateWorkRequest request = new CreateWorkRequest(
                "Título com acentos: ção, à, é, ü & \"aspas\"",
                "Descrição",
                "Conteúdo",
                null, null, null, null, null, null, null, null,
                "EXTENSION",
                "DRAFT"
        );
        HttpEntity<CreateWorkRequest> entity = new HttpEntity<>(request, getStudentHeaders());

        // ACT
        ResponseEntity<CreateWorkResponse> response = restTemplate.postForEntity(
                worksUrl(), entity, CreateWorkResponse.class
        );

        // ASSERT
        assertEquals(HttpStatus.CREATED, response.getStatusCode(),
                "Caracteres especiais e acentos devem ser aceitos no título");
        assertNotNull(response.getBody());
        assertEquals("Título com acentos: ção, à, é, ü & \"aspas\"",
                response.getBody().title,
                "Título com caracteres especiais deve ser preservado intacto");
    }

    @Test
    @Sql("/test-data/setup.sql")
    @DisplayName("Deve retornar 400 quando nome do autor está em branco nos newAuthors")
    void deveRetornar400ComNomeDeAutorEmBranco() {
        // ARRANGE
        CreateWorkRequest.AuthorRequest autorSemNome = new CreateWorkRequest.AuthorRequest(
                "   ",  // apenas espaços — deve falhar na validação
                "valido@email.com"
        );
        CreateWorkRequest request = new CreateWorkRequest(
                "Título",
                "Descrição",
                "Conteúdo",
                null, null, null, null, null,
                List.of(autorSemNome),
                null, null,
                "ARTICLE",
                "DRAFT"
        );
        HttpEntity<CreateWorkRequest> entity = new HttpEntity<>(request, getStudentHeaders());

        // ACT
        ResponseEntity<String> response = restTemplate.postForEntity(
                worksUrl(), entity, String.class
        );

        // ASSERT
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
                "Nome de autor com apenas espaços deve ser rejeitado com 400");
    }

    @Test
    @Sql("/test-data/setup.sql")
    @DisplayName("Deve retornar 400 quando cor da label está em branco")
    void deveRetornar400ComCorDeLabelEmBranco() {
        // ARRANGE
        CreateWorkRequest.LabelRequest labelSemCor = new CreateWorkRequest.LabelRequest(
                "Tecnologia",
                ""  // cor obrigatória — campo em branco
        );
        CreateWorkRequest request = new CreateWorkRequest(
                "Título",
                "Descrição",
                "Conteúdo",
                null, null, null, null, null, null,
                List.of(labelSemCor),
                null,
                "ARTICLE",
                "DRAFT"
        );
        HttpEntity<CreateWorkRequest> entity = new HttpEntity<>(request, getStudentHeaders());

        // ACT
        ResponseEntity<String> response = restTemplate.postForEntity(
                worksUrl(), entity, String.class
        );

        // ASSERT
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
                "Cor de label vazia deve ser rejeitada com 400");
    }
}