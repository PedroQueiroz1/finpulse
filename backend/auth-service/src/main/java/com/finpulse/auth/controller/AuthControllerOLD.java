package com.finpulse.auth.controller;

import com.finpulse.auth.dto.AuthResponse;
import com.finpulse.auth.dto.LoginRequest;
import com.finpulse.auth.dto.RegisterRequest;
import com.finpulse.auth.dto.UserResponse;
import com.finpulse.auth.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
@RestController — combina @Controller + @ResponseBody (retorna JSON automaticamente)
@RequestMapping("/api/auth") — prefixo de URL para todos os endpoints desta classe
@Valid — ativa a validação das anotações do Record (@NotBlank, @Email, etc.)
@RequestBody — deserializa o JSON do body para o Record Java
ResponseEntity — permite controlar o status HTTP da resposta (201 CREATED, 200 OK, etc.)

Isso é o Spring MVC em ação: o request chega no Controller → passa pro Service → o Service usa o Repository → volta a resposta pelo Controller como JSON.

Metodo cRiado por Pedro Queiroz
Projeto de Estudos!
*/
//@RestController
//@RequestMapping("/api/auth")
public class AuthControllerOLD {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthControllerOLD(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/auth/register - email: {}", request.email());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/auth/login - email: {}", request.email());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestHeader("Authorization") String authHeader) {
        log.info("POST /api/auth/refresh");

        // Remove o prefixo "Bearer " do header
        String refreshToken = authHeader.replace("Bearer ", "");
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth Service is running!");
    }

    // ADICIONADO!! endpoint que só funciona com token válido.
    /*@GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal String email) {
        log.info("GET /api/auth/me - email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        return ResponseEntity.ok(userMapper.toResponse(user));
    }*/

    // ADICIONADO!! endpoint que só funciona com token válido.
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @org.springframework.security.core.annotation.AuthenticationPrincipal String email) {
        log.info("GET /api/auth/me - email: {}", email);
        UserResponse response = authService.getUserByEmail(email);
        return ResponseEntity.ok(response);
    }
}