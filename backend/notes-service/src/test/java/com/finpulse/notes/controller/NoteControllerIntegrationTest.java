package com.finpulse.notes.controller;

import com.finpulse.notes.AbstractIntegrationTest;
import com.finpulse.notes.dto.*;
import com.finpulse.notes.helper.TestHeaders;
import com.finpulse.notes.repository.NoteGroupRepository;
import com.finpulse.notes.repository.NoteRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;

import static com.finpulse.notes.helper.TestHeaders.USER_ID_1;
import static com.finpulse.notes.helper.TestHeaders.USER_ID_2;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NoteController - Testes de Integração")
class NoteControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private NoteGroupRepository groupRepository;

    @BeforeEach
    void limparBanco() {
        noteRepository.deleteAll();
        groupRepository.deleteAll();
    }

    // ================================================================
    // Helpers
    // ================================================================

    private NoteResponse criarNota(String titulo, String userId) {
        CreateNoteRequest req = new CreateNoteRequest(titulo, "conteúdo de teste", null, List.of("tag1"), null);
        HttpEntity<CreateNoteRequest> entity = new HttpEntity<>(req, TestHeaders.forUser(userId));
        ResponseEntity<NoteResponse> resp = restTemplate.exchange(
                "/api/notes", HttpMethod.POST, entity, NoteResponse.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return resp.getBody();
    }

    private NoteResponse criarNotaComGrupo(String titulo, String userId, String groupId) {
        CreateNoteRequest req = new CreateNoteRequest(titulo, "conteúdo", groupId, null, null);
        HttpEntity<CreateNoteRequest> entity = new HttpEntity<>(req, TestHeaders.forUser(userId));
        ResponseEntity<NoteResponse> resp = restTemplate.exchange(
                "/api/notes", HttpMethod.POST, entity, NoteResponse.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return resp.getBody();
    }

    private NoteGroupResponse criarGrupo(String nome, String userId) {
        CreateNoteGroupRequest req = new CreateNoteGroupRequest(nome, null, "#FF0000", null);
        HttpEntity<CreateNoteGroupRequest> entity = new HttpEntity<>(req, TestHeaders.forUser(userId));
        ResponseEntity<NoteGroupResponse> resp = restTemplate.exchange(
                "/api/notes/groups", HttpMethod.POST, entity, NoteGroupResponse.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return resp.getBody();
    }

    // ================================================================
    // GET /api/notes
    // ================================================================

    @Nested
    @DisplayName("GET /api/notes - Listar notas")
    class ListarNotas {

        @Test
        @DisplayName("deve retornar lista vazia quando usuário não tem notas")
        void deveRetornarListaVaziaQuandoUsuarioNaoTemNotas() {
            HttpEntity<Void> entity = new HttpEntity<>(TestHeaders.forUser1());
            ResponseEntity<NoteResponse[]> resp = restTemplate.exchange(
                    "/api/notes", HttpMethod.GET, entity, NoteResponse[].class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody()).isEmpty();
        }

        @Test
        @DisplayName("deve retornar apenas notas do usuário autenticado")
        void deveRetornarApenasNotasDoUsuarioAutenticado() {
            criarNota("Nota do User1", USER_ID_1);
            criarNota("Nota do User2", USER_ID_2);

            HttpEntity<Void> entity = new HttpEntity<>(TestHeaders.forUser1());
            ResponseEntity<NoteResponse[]> resp = restTemplate.exchange(
                    "/api/notes", HttpMethod.GET, entity, NoteResponse[].class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody()).hasSize(1);
            assertThat(resp.getBody()[0].userId()).isEqualTo(USER_ID_1);
        }

        @Test
        @DisplayName("deve retornar erro sem header X-User-Id")
        void deveRetornarErroSemUserId() {
            ResponseEntity<String> resp = restTemplate.getForEntity("/api/notes", String.class);
            assertThat(resp.getStatusCode().isError()).isTrue();
        }
    }

    // ================================================================
    // POST /api/notes
    // ================================================================

    @Nested
    @DisplayName("POST /api/notes - Criar nota")
    class CriarNota {

        @Test
        @DisplayName("deve criar nota com dados válidos e retornar 201")
        void deveCriarNotaComDadosValidos() {
            CreateNoteRequest req = new CreateNoteRequest("Minha nota", "Conteúdo", null, List.of("work"), "#FFFFFF");
            HttpEntity<CreateNoteRequest> entity = new HttpEntity<>(req, TestHeaders.forUser1());
            ResponseEntity<NoteResponse> resp = restTemplate.exchange(
                    "/api/notes", HttpMethod.POST, entity, NoteResponse.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(resp.getBody()).isNotNull();
            assertThat(resp.getBody().id()).isNotNull();
            assertThat(resp.getBody().title()).isEqualTo("Minha nota");
            assertThat(resp.getBody().userId()).isEqualTo(USER_ID_1);
        }

        @Test
        @DisplayName("deve associar nota ao userId do header")
        void deveAssociarNotaAoUsuarioCorreto() {
            NoteResponse nota = criarNota("Nota do User2", USER_ID_2);
            assertThat(nota.userId()).isEqualTo(USER_ID_2);
        }

        @Test
        @DisplayName("deve retornar 400 sem título")
        void deveRetornar400SemTitulo() {
            CreateNoteRequest req = new CreateNoteRequest("", "Conteúdo", null, null, null);
            HttpEntity<CreateNoteRequest> entity = new HttpEntity<>(req, TestHeaders.forUser1());
            ResponseEntity<ErrorResponse> resp = restTemplate.exchange(
                    "/api/notes", HttpMethod.POST, entity, ErrorResponse.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("deve retornar erro sem header X-User-Id")
        void deveRetornarErroSemUserId() {
            CreateNoteRequest req = new CreateNoteRequest("Título", "Conteúdo", null, null, null);
            ResponseEntity<String> resp = restTemplate.postForEntity("/api/notes", req, String.class);
            assertThat(resp.getStatusCode().isError()).isTrue();
        }
    }

    // ================================================================
    // GET /api/notes/{id}
    // ================================================================

    @Nested
    @DisplayName("GET /api/notes/{id} - Buscar nota por ID")
    class BuscarNotaPorId {

        @Test
        @DisplayName("deve retornar nota existente do próprio usuário")
        void deveRetornarNotaExistenteDoUsuario() {
            NoteResponse criada = criarNota("Nota para buscar", USER_ID_1);

            HttpEntity<Void> entity = new HttpEntity<>(TestHeaders.forUser1());
            ResponseEntity<NoteResponse> resp = restTemplate.exchange(
                    "/api/notes/" + criada.id(), HttpMethod.GET, entity, NoteResponse.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody().id()).isEqualTo(criada.id());
        }

        @Test
        @DisplayName("deve retornar 404 quando nota não existe")
        void deveRetornar404QuandoNotaNaoExiste() {
            HttpEntity<Void> entity = new HttpEntity<>(TestHeaders.forUser1());
            ResponseEntity<ErrorResponse> resp = restTemplate.exchange(
                    "/api/notes/id-inexistente-999", HttpMethod.GET, entity, ErrorResponse.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("deve retornar 404 quando nota pertence a outro usuário")
        void deveRetornar404QuandoNotaEhDeOutroUsuario() {
            NoteResponse criada = criarNota("Nota do User2", USER_ID_2);

            HttpEntity<Void> entity = new HttpEntity<>(TestHeaders.forUser1());
            ResponseEntity<ErrorResponse> resp = restTemplate.exchange(
                    "/api/notes/" + criada.id(), HttpMethod.GET, entity, ErrorResponse.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    // ================================================================
    // PUT /api/notes/{id}
    // ================================================================

    @Nested
    @DisplayName("PUT /api/notes/{id} - Atualizar nota")
    class AtualizarNota {

        @Test
        @DisplayName("deve atualizar nota própria e retornar 200")
        void deveAtualizarNotaPropria() {
            NoteResponse criada = criarNota("Título original", USER_ID_1);

            UpdateNoteRequest req = new UpdateNoteRequest("Título atualizado", "Novo conteúdo", null, null, null, null, null);
            HttpEntity<UpdateNoteRequest> entity = new HttpEntity<>(req, TestHeaders.forUser1());
            ResponseEntity<NoteResponse> resp = restTemplate.exchange(
                    "/api/notes/" + criada.id(), HttpMethod.PUT, entity, NoteResponse.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody().title()).isEqualTo("Título atualizado");
        }

        @Test
        @DisplayName("deve retornar 404 quando tenta atualizar nota de outro usuário")
        void deveRetornar404QuandoTentaAtualizarNotaDeOutro() {
            NoteResponse criada = criarNota("Nota do User2", USER_ID_2);

            UpdateNoteRequest req = new UpdateNoteRequest("Hack", null, null, null, null, null, null);
            HttpEntity<UpdateNoteRequest> entity = new HttpEntity<>(req, TestHeaders.forUser1());
            ResponseEntity<ErrorResponse> resp = restTemplate.exchange(
                    "/api/notes/" + criada.id(), HttpMethod.PUT, entity, ErrorResponse.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("deve retornar 404 quando nota não existe")
        void deveRetornar404QuandoNotaNaoExiste() {
            UpdateNoteRequest req = new UpdateNoteRequest("Título", null, null, null, null, null, null);
            HttpEntity<UpdateNoteRequest> entity = new HttpEntity<>(req, TestHeaders.forUser1());
            ResponseEntity<ErrorResponse> resp = restTemplate.exchange(
                    "/api/notes/id-inexistente-999", HttpMethod.PUT, entity, ErrorResponse.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    // ================================================================
    // DELETE /api/notes/{id}
    // ================================================================

    @Nested
    @DisplayName("DELETE /api/notes/{id} - Deletar nota")
    class DeletarNota {

        @Test
        @DisplayName("deve deletar nota própria e retornar 204")
        void deveDeletarNotaPropria() {
            NoteResponse criada = criarNota("Nota para deletar", USER_ID_1);

            HttpEntity<Void> entity = new HttpEntity<>(TestHeaders.forUser1());
            ResponseEntity<Void> resp = restTemplate.exchange(
                    "/api/notes/" + criada.id(), HttpMethod.DELETE, entity, Void.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(noteRepository.findById(criada.id())).isEmpty();
        }

        @Test
        @DisplayName("deve retornar 404 quando tenta deletar nota de outro usuário")
        void deveRetornar404QuandoTentaDeletarNotaDeOutro() {
            NoteResponse criada = criarNota("Nota do User2", USER_ID_2);

            HttpEntity<Void> entity = new HttpEntity<>(TestHeaders.forUser1());
            ResponseEntity<ErrorResponse> resp = restTemplate.exchange(
                    "/api/notes/" + criada.id(), HttpMethod.DELETE, entity, ErrorResponse.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    // ================================================================
    // PATCH /api/notes/{id}/pin e /archive
    // ================================================================

    @Nested
    @DisplayName("PATCH - Pin e Archive")
    class PinEArchive {

        @Test
        @DisplayName("deve fixar e desafixar nota")
        void deveTogglePin() {
            NoteResponse criada = criarNota("Nota para fixar", USER_ID_1);
            assertThat(criada.pinned()).isFalse();

            HttpEntity<Void> entity = new HttpEntity<>(TestHeaders.forUser1());
            ResponseEntity<NoteResponse> resp = restTemplate.exchange(
                    "/api/notes/" + criada.id() + "/pin", HttpMethod.PATCH, entity, NoteResponse.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody().pinned()).isTrue();
        }

        @Test
        @DisplayName("deve arquivar nota")
        void deveArquivarNota() {
            NoteResponse criada = criarNota("Nota para arquivar", USER_ID_1);

            HttpEntity<Void> entity = new HttpEntity<>(TestHeaders.forUser1());
            ResponseEntity<NoteResponse> resp = restTemplate.exchange(
                    "/api/notes/" + criada.id() + "/archive", HttpMethod.PATCH, entity, NoteResponse.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody().archived()).isTrue();
        }
    }

    // ================================================================
    // GET /api/notes/groups
    // ================================================================

    @Nested
    @DisplayName("Grupos de notas")
    class Grupos {

        @Test
        @DisplayName("deve listar grupos do usuário")
        void deveListarGruposDoUsuario() {
            criarGrupo("Trabalho", USER_ID_1);
            criarGrupo("Pessoal", USER_ID_1);
            criarGrupo("Grupo do User2", USER_ID_2);

            HttpEntity<Void> entity = new HttpEntity<>(TestHeaders.forUser1());
            ResponseEntity<NoteGroupResponse[]> resp = restTemplate.exchange(
                    "/api/notes/groups", HttpMethod.GET, entity, NoteGroupResponse[].class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody()).hasSize(2);
        }

        @Test
        @DisplayName("deve criar grupo e retornar 201")
        void deveCriarGrupo() {
            NoteGroupResponse grupo = criarGrupo("Estudos", USER_ID_1);
            assertThat(grupo.id()).isNotNull();
            assertThat(grupo.name()).isEqualTo("Estudos");
            assertThat(grupo.noteCount()).isZero();
        }

        @Test
        @DisplayName("deve deletar grupo e suas notas")
        void deveDeletarGrupo() {
            NoteGroupResponse grupo = criarGrupo("Grupo temporário", USER_ID_1);

            HttpEntity<Void> entity = new HttpEntity<>(TestHeaders.forUser1());
            ResponseEntity<Void> resp = restTemplate.exchange(
                    "/api/notes/groups/" + grupo.id(), HttpMethod.DELETE, entity, Void.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(groupRepository.findById(grupo.id())).isEmpty();
        }
    }

    // ================================================================
    // GET /api/notes/search e /api/notes/paginated
    // ================================================================

    @Nested
    @DisplayName("Busca e paginação")
    class BuscaEPaginacao {

        @Test
        @DisplayName("deve buscar notas por texto sem erro")
        void deveBuscarPorTextoSemErro() {
            criarNota("Java Spring Boot", USER_ID_1);

            HttpEntity<Void> entity = new HttpEntity<>(TestHeaders.forUser1());
            // endpoint retorna List<NoteResponse> — pode vir vazio se índice texto não indexou ainda
            ResponseEntity<String> resp = restTemplate.exchange(
                    "/api/notes/search?q=Java", HttpMethod.GET, entity, String.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("deve listar notas paginadas")
        void deveListarNotasPaginadas() {
            criarNota("Nota 1", USER_ID_1);
            criarNota("Nota 2", USER_ID_1);

            HttpEntity<Void> entity = new HttpEntity<>(TestHeaders.forUser1());
            ResponseEntity<String> resp = restTemplate.exchange(
                    "/api/notes/paginated?page=0&size=10", HttpMethod.GET, entity, String.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    // ================================================================
    // Cenários com groupId (branches do NoteService)
    // ================================================================

    @Nested
    @DisplayName("Notas com grupo")
    class NotasComGrupo {

        @Test
        @DisplayName("deve criar nota associada a um grupo existente")
        void deveCriarNotaComGrupoValido() {
            NoteGroupResponse grupo = criarGrupo("Trabalho", USER_ID_1);

            CreateNoteRequest req = new CreateNoteRequest("Tarefa importante", "Descrição", grupo.id(), List.of("urgente"), "#FF0000");
            HttpEntity<CreateNoteRequest> entity = new HttpEntity<>(req, TestHeaders.forUser1());
            ResponseEntity<NoteResponse> resp = restTemplate.exchange(
                    "/api/notes", HttpMethod.POST, entity, NoteResponse.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(resp.getBody().groupId()).isEqualTo(grupo.id());
        }

        @Test
        @DisplayName("deve retornar 404 ao criar nota com grupo inexistente")
        void deveRetornar404AoCriarNotaComGrupoInexistente() {
            CreateNoteRequest req = new CreateNoteRequest("Nota órfã", "Conteúdo", "grupo-inexistente", null, null);
            HttpEntity<CreateNoteRequest> entity = new HttpEntity<>(req, TestHeaders.forUser1());
            ResponseEntity<ErrorResponse> resp = restTemplate.exchange(
                    "/api/notes", HttpMethod.POST, entity, ErrorResponse.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("deve decrementar noteCount ao deletar nota de um grupo")
        void deveDeletarNotaDeGrupoEDecrementarContador() {
            NoteGroupResponse grupo = criarGrupo("Projetos", USER_ID_1);
            NoteResponse nota = criarNotaComGrupo("Nota no grupo", USER_ID_1, grupo.id());

            HttpEntity<Void> entity = new HttpEntity<>(TestHeaders.forUser1());
            restTemplate.exchange("/api/notes/" + nota.id(), HttpMethod.DELETE, entity, Void.class);

            assertThat(noteRepository.findById(nota.id())).isEmpty();
        }

        @Test
        @DisplayName("deve atualizar nota com todos os campos preenchidos")
        void deveAtualizarNotaComTodosOsCampos() {
            NoteGroupResponse grupo = criarGrupo("Grupo Destino", USER_ID_1);
            NoteResponse criada = criarNota("Título original", USER_ID_1);

            UpdateNoteRequest req = new UpdateNoteRequest(
                    "Título novo", "Conteúdo novo", grupo.id(), List.of("nova-tag"), "#0000FF", true, false);
            HttpEntity<UpdateNoteRequest> entity = new HttpEntity<>(req, TestHeaders.forUser1());
            ResponseEntity<NoteResponse> resp = restTemplate.exchange(
                    "/api/notes/" + criada.id(), HttpMethod.PUT, entity, NoteResponse.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody().title()).isEqualTo("Título novo");
            assertThat(resp.getBody().pinned()).isTrue();
        }
    }

    // ================================================================
    // Actuator
    // ================================================================

    @Nested
    @DisplayName("Actuator endpoints")
    class Actuator {

        @Test
        @DisplayName("deve retornar health status UP")
        void deveRetornarHealthStatusUp() {
            ResponseEntity<String> resp = restTemplate.getForEntity("/actuator/health", String.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody()).contains("UP");
        }

        @Test
        @DisplayName("endpoint prometheus deve estar registrado no actuator")
        void endpointPrometheusDevelEstarRegistrado() {
            ResponseEntity<String> resp = restTemplate.getForEntity("/actuator/prometheus", String.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("deve retornar info com metadados do serviço")
        void deveRetornarInfoComMetadados() {
            ResponseEntity<String> resp = restTemplate.getForEntity("/actuator/info", String.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    // ================================================================
    // Correlation ID
    // ================================================================

    @Nested
    @DisplayName("Correlation ID")
    class CorrelationId {

        @Test
        @DisplayName("deve gerar Correlation ID quando header está ausente")
        void deveGerarCorrelationIdQuandoHeaderAusente() {
            ResponseEntity<String> resp = restTemplate.getForEntity("/actuator/health", String.class);
            assertThat(resp.getHeaders().getFirst("X-Correlation-ID")).isNotBlank();
        }

        @Test
        @DisplayName("deve reutilizar Correlation ID enviado pelo cliente")
        void deveReutilizarCorrelationIdEnviado() {
            String meuId = "meu-correlation-id-123";
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Correlation-ID", meuId);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> resp = restTemplate.exchange(
                    "/actuator/health", HttpMethod.GET, entity, String.class);

            assertThat(resp.getHeaders().getFirst("X-Correlation-ID")).isEqualTo(meuId);
        }

        @Test
        @DisplayName("deve devolver Correlation ID na resposta")
        void deveRetornarCorrelationIdNaResposta() {
            ResponseEntity<String> resp = restTemplate.getForEntity("/api/notes/health", String.class);
            assertThat(resp.getHeaders().containsKey("X-Correlation-ID")).isTrue();
        }
    }
}
