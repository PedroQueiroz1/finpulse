package com.finpulse.auth.controller;

import com.finpulse.auth.AbstractIntegrationTest;
import com.finpulse.auth.dto.AuthResponse;
import com.finpulse.auth.dto.ErrorResponse;
import com.finpulse.auth.dto.LoginRequest;
import com.finpulse.auth.dto.RegisterRequest;
import com.finpulse.auth.dto.UserResponse;
import com.finpulse.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuthController - Integration Tests")
class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    // =====================================================
    // POST /api/auth/register
    // =====================================================
    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterTests {

        @Test
        @DisplayName("Deve registrar usuário com dados válidos e retornar 201")
        void deveRegistrarUsuarioComDadosValidos() {
            RegisterRequest request = new RegisterRequest(
                    "Pedro Queiroz",
                    "pedro@finpulse.com",
                    "senha12345"
            );

            ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                    "/api/auth/register", request, AuthResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().accessToken()).isNotBlank();
            assertThat(response.getBody().refreshToken()).isNotBlank();
            assertThat(response.getBody().tokenType()).isEqualTo("Bearer");
            assertThat(response.getBody().expiresIn()).isPositive();

            UserResponse user = response.getBody().user();
            assertThat(user).isNotNull();
            assertThat(user.email()).isEqualTo("pedro@finpulse.com");
            assertThat(user.name()).isEqualTo("Pedro Queiroz");
            assertThat(user.active()).isTrue();
            assertThat(user.id()).isNotNull();
        }

        @Test
        @DisplayName("Deve retornar 409 quando email já está cadastrado")
        void deveRetornar409QuandoEmailJaExiste() {
            RegisterRequest first = new RegisterRequest(
                    "Pedro Queiroz", "duplicado@finpulse.com", "senha12345"
            );
            restTemplate.postForEntity("/api/auth/register", first, AuthResponse.class);

            RegisterRequest duplicate = new RegisterRequest(
                    "Outro Nome", "duplicado@finpulse.com", "outrasenha"
            );

            ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                    "/api/auth/register", duplicate, ErrorResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(409);
        }

        @Test
        @DisplayName("Deve retornar 400 quando email é inválido")
        void deveRetornar400QuandoEmailInvalido() {
            RegisterRequest request = new RegisterRequest(
                    "Pedro Queiroz", "email-invalido", "senha12345"
            );

            ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                    "/api/auth/register", request, ErrorResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Deve retornar 400 quando senha tem menos de 8 caracteres")
        void deveRetornar400QuandoSenhaCurta() {
            RegisterRequest request = new RegisterRequest(
                    "Pedro Queiroz", "pedro@finpulse.com", "1234"
            );

            ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                    "/api/auth/register", request, ErrorResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Deve retornar 400 quando nome está em branco")
        void deveRetornar400QuandoNomeEmBranco() {
            RegisterRequest request = new RegisterRequest(
                    "", "pedro@finpulse.com", "senha12345"
            );

            ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                    "/api/auth/register", request, ErrorResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    // =====================================================
    // POST /api/auth/login
    // =====================================================
    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        @BeforeEach
        void registerUser() {
            RegisterRequest register = new RegisterRequest(
                    "Pedro Queiroz", "pedro@finpulse.com", "senha12345"
            );
            restTemplate.postForEntity("/api/auth/register", register, AuthResponse.class);
        }

        @Test
        @DisplayName("Deve fazer login com credenciais válidas e retornar 200")
        void deveFazerLoginComCredenciaisValidas() {
            LoginRequest request = new LoginRequest("pedro@finpulse.com", "senha12345");

            ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                    "/api/auth/login", request, AuthResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().accessToken()).isNotBlank();
            assertThat(response.getBody().refreshToken()).isNotBlank();
            assertThat(response.getBody().user().email()).isEqualTo("pedro@finpulse.com");
        }

        @Test
        @DisplayName("Deve retornar 401 quando senha está errada")
        void deveRetornar401QuandoSenhaErrada() {
            LoginRequest request = new LoginRequest("pedro@finpulse.com", "senhaERRADA");

            ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                    "/api/auth/login", request, ErrorResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Deve retornar 401 quando email não existe")
        void deveRetornar401QuandoEmailNaoExiste() {
            LoginRequest request = new LoginRequest("naoexiste@finpulse.com", "senha12345");

            ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                    "/api/auth/login", request, ErrorResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    // =====================================================
    // POST /api/auth/refresh
    // =====================================================
    @Nested
    @DisplayName("POST /api/auth/refresh")
    class RefreshTests {

        private String validRefreshToken;

        @BeforeEach
        void registerAndCaptureToken() {
            RegisterRequest register = new RegisterRequest(
                    "Pedro Queiroz", "pedro@finpulse.com", "senha12345"
            );
            ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity(
                    "/api/auth/register", register, AuthResponse.class
            );
            validRefreshToken = registerResponse.getBody().refreshToken();
        }

        @Test
        @DisplayName("Deve renovar token com refresh token válido")
        void deveRenovarTokenComRefreshTokenValido() {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + validRefreshToken);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<AuthResponse> response = restTemplate.exchange(
                    "/api/auth/refresh", HttpMethod.POST, requestEntity, AuthResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().accessToken()).isNotBlank();
        }

@Test
@DisplayName("Deve retornar 401 quando refresh token é inválido")
void deveRetornar401QuandoRefreshTokenInvalido() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer token.invalido.aqui");

    HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

    ResponseEntity<ErrorResponse> response = restTemplate.exchange(
            "/api/auth/refresh", HttpMethod.POST, requestEntity, ErrorResponse.class
    );

    // JWT malformado → tratado pelo GlobalExceptionHandler como 401
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
}
    }

    // =====================================================
    // GET /api/auth/me
    // =====================================================
    @Nested
    @DisplayName("GET /api/auth/me")
    class GetCurrentUserTests {

        private String validAccessToken;

        @BeforeEach
        void registerAndCaptureToken() {
            RegisterRequest register = new RegisterRequest(
                    "Pedro Queiroz", "pedro@finpulse.com", "senha12345"
            );
            ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity(
                    "/api/auth/register", register, AuthResponse.class
            );
            validAccessToken = registerResponse.getBody().accessToken();
        }

        @Test
        @DisplayName("Deve retornar dados do usuário com JWT válido")
        void deveRetornarUsuarioComJwtValido() {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + validAccessToken);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<UserResponse> response = restTemplate.exchange(
                    "/api/auth/me", HttpMethod.GET, requestEntity, UserResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().email()).isEqualTo("pedro@finpulse.com");
            assertThat(response.getBody().name()).isEqualTo("Pedro Queiroz");
            assertThat(response.getBody().active()).isTrue();
        }

@Test
@DisplayName("Deve retornar 403 sem JWT (Spring Security default)")
void deveRetornar403SemJwt() {
    ResponseEntity<ErrorResponse> response = restTemplate.exchange(
            "/api/auth/me", HttpMethod.GET, HttpEntity.EMPTY, ErrorResponse.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
}

@Test
@DisplayName("Deve retornar 403 com JWT inválido (Spring Security default)")
void deveRetornar403ComJwtInvalido() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer jwt.totalmente.invalido");

    HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

    ResponseEntity<ErrorResponse> response = restTemplate.exchange(
            "/api/auth/me", HttpMethod.GET, requestEntity, ErrorResponse.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
}
    }

    // =====================================================
    // GET /api/auth/health (endpoint sanity)
    // =====================================================
    @Test
    @DisplayName("GET /api/auth/health deve retornar 200 OK")
    void healthDeveRetornar200() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/auth/health", String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Auth Service is running");
    }
}