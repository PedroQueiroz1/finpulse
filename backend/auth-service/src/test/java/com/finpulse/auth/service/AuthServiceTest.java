package com.finpulse.auth.service;

import com.finpulse.auth.dto.AuthResponse;
import com.finpulse.auth.dto.LoginRequest;
import com.finpulse.auth.dto.RegisterRequest;
import com.finpulse.auth.dto.UserResponse;
import com.finpulse.auth.entity.User;
import com.finpulse.auth.enums.Role;
import com.finpulse.auth.exception.EmailAlreadyExistsException;
import com.finpulse.auth.exception.InvalidCredentialsException;
import com.finpulse.auth.mapper.UserMapper;
import com.finpulse.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
Teste unitário — testa uma classe isolada, substituindo dependências por mocks (objetos falsos)
Mockito — cria esses mocks. Quando seu service chama userRepository.save(), o Mockito intercepta e retorna o que você mandou, sem tocar no banco

Anatomia de um teste — padrão Given/When/Then (ou Arrange/Act/Assert):

Given — configura o cenário e os comportamentos dos mocks com when().thenReturn()
When — executa o método que está sendo testado
Then — verifica o resultado com assertEquals, assertNotNull, assertThrows

Anotações importantes:

@Mock — cria um objeto falso que simula o comportamento real
@InjectMocks — cria a classe real e injeta os mocks nela
@BeforeEach — roda antes de cada teste, garantindo estado limpo
@Nested — agrupa testes relacionados (fica bonito no relatório)
@DisplayName — nome legível no relatório de testes
verify() — confirma que um método do mock foi (ou não foi) chamado

Método criado por PEDRO QUEIROZ projeto de estudos
*/
@ExtendWith(MockitoExtension.class)  // Ativa o Mockito no JUnit 5
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock  // Cria um mock (objeto falso) do UserRepository
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks  // Cria o AuthService real, injetando os mocks acima
    private AuthService authService;

    // Objetos reutilizáveis nos testes
    private User testUser;
    private UserResponse testUserResponse;

    @BeforeEach  // Executa antes de CADA teste
    void setUp() {
        testUser = new User("Pedro", "pedro@finpulse.com", "encodedPassword", Role.USER);
        testUser.setId(UUID.randomUUID());

        testUserResponse = new UserResponse(
                testUser.getId(),
                testUser.getName(),
                testUser.getEmail(),
                testUser.getRole(),
                testUser.isActive()
        );
    }

    // Agrupando testes por funcionalidade com @Nested
    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Deve registrar usuário com sucesso")
        void shouldRegisterSuccessfully() {
            // GIVEN (Arrange) — prepara o cenário
            RegisterRequest request = new RegisterRequest("Pedro", "pedro@finpulse.com", "12345678");

            when(userRepository.existsByEmail(request.email())).thenReturn(false);
            when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(jwtService.generateAccessToken(anyString(), anyMap())).thenReturn("access-token");
            when(jwtService.generateRefreshToken(anyString())).thenReturn("refresh-token");
            when(jwtService.getAccessTokenExpiration()).thenReturn(900000L);
            when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

            // WHEN (Act) — executa a ação
            AuthResponse response = authService.register(request);

            // THEN (Assert) — verifica o resultado
            assertNotNull(response);
            assertEquals("access-token", response.accessToken());
            assertEquals("refresh-token", response.refreshToken());
            assertEquals("Bearer", response.tokenType());
            assertNotNull(response.user());
            assertEquals("pedro@finpulse.com", response.user().email());

            // Verifica que os métodos foram chamados
            verify(userRepository).existsByEmail(request.email());
            verify(passwordEncoder).encode(request.password());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando e-mail já existe")
        void shouldThrowWhenEmailExists() {
            // GIVEN
            RegisterRequest request = new RegisterRequest("Pedro", "pedro@finpulse.com", "12345678");
            when(userRepository.existsByEmail(request.email())).thenReturn(true);

            // WHEN & THEN
            assertThrows(EmailAlreadyExistsException.class, () ->
                    authService.register(request)
            );

            // Verifica que NUNCA salvou no banco (parou antes)
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Deve fazer login com sucesso")
        void shouldLoginSuccessfully() {
            // GIVEN
            LoginRequest request = new LoginRequest("pedro@finpulse.com", "12345678");

            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(request.password(), testUser.getPassword())).thenReturn(true);
            when(jwtService.generateAccessToken(anyString(), anyMap())).thenReturn("access-token");
            when(jwtService.generateRefreshToken(anyString())).thenReturn("refresh-token");
            when(jwtService.getAccessTokenExpiration()).thenReturn(900000L);
            when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

            // WHEN
            AuthResponse response = authService.login(request);

            // THEN
            assertNotNull(response);
            assertEquals("access-token", response.accessToken());
            verify(passwordEncoder).matches(request.password(), testUser.getPassword());
        }

        @Test
        @DisplayName("Deve lançar exceção quando e-mail não encontrado")
        void shouldThrowWhenEmailNotFound() {
            // GIVEN
            LoginRequest request = new LoginRequest("inexistente@email.com", "12345678");
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThrows(InvalidCredentialsException.class, () ->
                    authService.login(request)
            );
        }

        @Test
        @DisplayName("Deve lançar exceção quando senha está incorreta")
        void shouldThrowWhenPasswordIsWrong() {
            // GIVEN
            LoginRequest request = new LoginRequest("pedro@finpulse.com", "senhaerrada");
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(request.password(), testUser.getPassword())).thenReturn(false);

            // WHEN & THEN
            assertThrows(InvalidCredentialsException.class, () ->
                    authService.login(request)
            );
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário está inativo")
        void shouldThrowWhenUserIsInactive() {
            // GIVEN
            testUser.setActive(false);
            LoginRequest request = new LoginRequest("pedro@finpulse.com", "12345678");
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(testUser));

            // WHEN & THEN
            assertThrows(InvalidCredentialsException.class, () ->
                    authService.login(request)
            );
        }
    }

    @Nested
    @DisplayName("GetUserByEmail Tests")
    class GetUserByEmailTests {

        @Test
        @DisplayName("Deve retornar usuário pelo e-mail")
        void shouldReturnUserByEmail() {
            // GIVEN
            when(userRepository.findByEmail("pedro@finpulse.com")).thenReturn(Optional.of(testUser));
            when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

            // WHEN
            UserResponse response = authService.getUserByEmail("pedro@finpulse.com");

            // THEN
            assertNotNull(response);
            assertEquals("Pedro", response.name());
            assertEquals("pedro@finpulse.com", response.email());
        }
    }
}