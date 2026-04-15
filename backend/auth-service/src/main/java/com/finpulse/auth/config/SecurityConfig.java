package com.finpulse.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/*
csrf.disable() — CSRF protection é para aplicações com formulários HTML. APIs REST usam tokens (JWT) para autenticação, então CSRF não se aplica
SessionCreationPolicy.STATELESS — o servidor não cria sessão HTTP. Cada requisição é independente e carrega seu próprio JWT. Isso é o que torna a API RESTful de verdade
BCryptPasswordEncoder — algoritmo de hash adaptativo para senhas. Cada hash é diferente mesmo para a mesma senha (usa salt interno)
Os endpoints de auth são permitAll() porque o usuário precisa acessá-los antes de ter um token

Metodo criado por Pedro Queiroz
PROJETO de Estudos
*/
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Desabilita CSRF — em APIs REST stateless não usamos CSRF
            .csrf(csrf -> csrf.disable())

            // Define quais endpoints são públicos e quais exigem autenticação
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos (sem token)
                .requestMatchers(
                    "/api/auth/register",
                    "/api/auth/login",
                    "/api/auth/refresh",
                    "/actuator/health",
                    "/actuator/info",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                // Qualquer outra requisição precisa de autenticação
                .anyRequest().authenticated()
            )

            // Stateless — o servidor não guarda sessão, cada request carrega o JWT
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}