package com.finpulse.auth.service;

import com.finpulse.auth.dto.AuthResponse;
import com.finpulse.auth.dto.LoginRequest;
import com.finpulse.auth.dto.RegisterRequest;
import com.finpulse.auth.dto.UserResponse;
import com.finpulse.auth.entity.User;
import com.finpulse.auth.enums.Role;
import com.finpulse.auth.exception.EmailAlreadyExistsException;
import com.finpulse.auth.exception.InvalidCredentialsException;
import com.finpulse.auth.exception.UserNotFoundException;
import com.finpulse.auth.mapper.UserMapper;
import com.finpulse.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;


/*
--- LÓGICA DE NEGÓCIO ---
@Transactional — garante que o register é atômico: se algo falhar no meio, o banco faz rollback
@Transactional(readOnly = true) — otimização: avisa ao Hibernate que não haverá escrita
passwordEncoder.encode() — nunca salvamos senhas em texto puro, sempre hash com BCrypt
passwordEncoder.matches() — compara a senha digitada com o hash salvo
Injeção via construtor (sem @Autowired) — é a forma recomendada, facilita testes

Note a mensagem genérica no InvalidCredentialsException: "E-mail ou senha inválidos" — nunca dizemos "e-mail não encontrado" ou "senha incorreta" separadamente. 
Isso é uma prática de segurança: impede que atacantes descubram quais e-mails existem no sistema.

Metodo criado por Pedro Queiroz
Projeto de estudos...
*/
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    // Injeção de dependência via construtor (SOLID - D: Dependency Inversion)
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registrando novo usuário: {}", request.email());

        // Verifica se e-mail já existe
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        // Cria o usuário com senha criptografada
        User user = new User(
                request.name(),
                request.email(),
                passwordEncoder.encode(request.password()),
                Role.USER
        );

        User savedUser = userRepository.save(user);
        log.info("Usuário registrado com sucesso: {}", savedUser.getId());

        return generateAuthResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Tentativa de login: {}", request.email());

        // Busca o usuário pelo e-mail
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        // Verifica se está ativo
        if (!user.isActive()) {
            throw new InvalidCredentialsException();
        }

        // Verifica a senha
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        log.info("Login realizado com sucesso: {}", user.getId());
        return generateAuthResponse(user);
    }

    public AuthResponse refreshToken(String refreshToken) {
        String email = jwtService.extractEmail(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        if (!jwtService.isTokenValid(refreshToken, email)) {
            throw new InvalidCredentialsException();
        }

        log.info("Token renovado para: {}", email);
        return generateAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id.toString()));
        return userMapper.toResponse(user);
    }

    // ============================================================
    // Método privado auxiliar
    // ============================================================

    private AuthResponse generateAuthResponse(User user) {
        Map<String, Object> claims = Map.of(
                "role", user.getRole().name(),
                "name", user.getName()
        );

        String accessToken = jwtService.generateAccessToken(user.getEmail(), claims);
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());
        UserResponse userResponse = userMapper.toResponse(user);

        return new AuthResponse(
                accessToken,
                refreshToken,
                jwtService.getAccessTokenExpiration(),
                userResponse
        );
    }
    
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        return userMapper.toResponse(user);
    }
}