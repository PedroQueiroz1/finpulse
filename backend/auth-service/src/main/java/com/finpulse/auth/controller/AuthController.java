package com.finpulse.auth.controller;

import com.finpulse.auth.dto.AuthResponse;
import com.finpulse.auth.dto.ErrorResponse;
import com.finpulse.auth.dto.LoginRequest;
import com.finpulse.auth.dto.RegisterRequest;
import com.finpulse.auth.dto.UserResponse;
import com.finpulse.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
Novo AuthController pós Swagger na aplicação
@RestController — combina @Controller + @ResponseBody (retorna JSON automaticamente)
@RequestMapping("/api/auth") — prefixo de URL para todos os endpoints desta classe
@Valid — ativa a validação das anotações do Record (@NotBlank, @Email, etc.)
@RequestBody — deserializa o JSON do body para o Record Java
ResponseEntity — permite controlar o status HTTP da resposta (201 CREATED, 200 OK, etc.)

Isso é o Spring MVC em ação: o request chega no Controller → passa pro Service → o Service usa o Repository → volta a resposta pelo Controller como JSON.

Metodo cRiado por Pedro Queiroz
Projeto de Estudos!
*/
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Endpoints de registro, login e gerenciamento de tokens")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Registrar novo usuário",
               description = "Cria uma nova conta e retorna tokens de acesso")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "E-mail já cadastrado",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/auth/register - email: {}", request.email());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Fazer login",
               description = "Autentica o usuário e retorna tokens de acesso")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/auth/login - email: {}", request.email());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Renovar token",
               description = "Gera novos tokens usando o refresh token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tokens renovados com sucesso"),
        @ApiResponse(responseCode = "401", description = "Refresh token inválido")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestHeader("Authorization") String authHeader) {
        log.info("POST /api/auth/refresh");
        String refreshToken = authHeader.replace("Bearer ", "");
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obter dados do usuário logado",
               description = "Retorna os dados do usuário autenticado pelo JWT",
               security = @SecurityRequirement(name = "Bearer Token"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Dados retornados com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal String email) {
        log.info("GET /api/auth/me - email: {}", email);
        UserResponse response = authService.getUserByEmail(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Verifica se o serviço está rodando")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth Service is running!");
    }
}