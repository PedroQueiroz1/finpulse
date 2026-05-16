# Plano de Implementação

> Passo a passo executável de cada fase. Claude Code deve seguir nessa ordem.

---

## 🎯 FASE 8.1 — Observability no notes-service

**Tempo estimado:** 4-6 horas
**Pré-requisitos:** auth-service com FASE 7 completa servindo de referência

### Etapa 1: Dependências (pom.xml)

Arquivo: `backend/notes-service/pom.xml`

Adicionar em `<dependencies>`:
- `spring-boot-starter-actuator`
- `micrometer-registry-prometheus` (runtime)
- `prometheus-metrics-exposition-formats` (runtime)
- `testcontainers-bom` (versão 1.20.4, type pom, scope import)
- `testcontainers` (test)
- `junit-jupiter` org.testcontainers (test)
- `mongodb` org.testcontainers (test) ← **diferença vs auth**
- `spring-boot-testcontainers` (test)

Adicionar em `<build><plugins>`:
- Plugin Jacoco 0.8.12 com 3 execuções:
  - `prepare-agent` (sem fase específica)
  - `report` (fase: test)
  - `check` (sem fase) com mínimos 0.50 line / 0.40 branch (inicial, aumentar depois)

**Validação:** `mvn clean install -DskipTests` retorna BUILD SUCCESS.

---

### Etapa 2: Reorganizar Configurações

#### 2.1. Reduzir `application.yml` local

Arquivo: `backend/notes-service/src/main/resources/application.yml`

Substituir conteúdo inteiro por bootstrap minimal:
```yaml
spring:
  application:
    name: notes-service
  profiles:
    active: dev
  config:
    import: optional:configserver:http://localhost:8888
server:
  port: 8082
```

#### 2.2. Criar `application-test.yml` local

Arquivo: `backend/notes-service/src/main/resources/application-test.yml`

Conteúdo deve:
- Desligar Config Server (`spring.config.import: ""`)
- Configurar MongoDB sem URI (Testcontainers injeta)
- Desligar Eureka
- Definir JWT secret de teste
- Porta aleatória (`server.port: 0`)
- Logs minimais

#### 2.3. Atualizar `notes-service.yml` no Config Server

Arquivo: `backend/config-server/src/main/resources/configs/notes-service.yml`

Manter configurações comuns a dev e prod:
- MongoDB com auto-index
- Eureka prefer-ip-address
- Bloco completo de management (Actuator, percentiles, tags)
- Bloco info: com metadados

#### 2.4. Criar `notes-service-dev.yml` no Config Server

Configurações específicas de dev:
- MongoDB URI local (`mongodb://localhost:27017/finpulse_notes`)
- Eureka URL local
- JWT secret hardcoded (dev only)
- Logs em DEBUG pra `com.finpulse.notes`

#### 2.5. Criar `notes-service-prod.yml` no Config Server

Configurações específicas de prod:
- MongoDB URI via `${MONGODB_URI}`
- Eureka URL via `${EUREKA_URL}` com fallback
- JWT secret via `${JWT_SECRET}` SEM fallback (forçar erro se ausente)
- Logs em WARN

---

### Etapa 3: Criar CorrelationIdFilter

Arquivo: `backend/notes-service/src/main/java/com/finpulse/notes/filter/CorrelationIdFilter.java`

Copiar implementação do auth-service ajustando apenas o pacote.

Pontos críticos:
- `@Order(Ordered.HIGHEST_PRECEDENCE)` — roda antes de qualquer filter
- `@Component` — Spring autodetect
- Extends `OncePerRequestFilter`
- Usa `MDC.put("correlationId", ...)` antes do `doFilter`
- `try/finally` com `MDC.remove` (CRÍTICO contra vazamento)
- Constantes: `CORRELATION_ID_HEADER = "X-Correlation-ID"`, `CORRELATION_ID_MDC_KEY = "correlationId"`

---

### Etapa 4: Configurar Logback

Arquivo: `backend/notes-service/src/main/resources/logback-spring.xml`

Copiar do auth-service, ajustando:
- Logger específico: `com.finpulse.notes` (não auth)

Estrutura:
- 2 appenders: `CONSOLE_DEV` (colorido) e `CONSOLE_PROD` (JSON)
- 4 `<springProfile>` blocks: dev, prod, test, fallback
- Pattern do MDC: `[%X{correlationId:-no-cid}]`

---

### Etapa 5: Liberar Actuator no Security (se aplicável)

**Verificar primeiro:** notes-service tem SecurityConfig?

Se SIM: adicionar `/actuator/**` em `permitAll()`.
Se NÃO: pular esta etapa.

---

### Etapa 6: Criar Classe Base de Testes

Arquivo: `backend/notes-service/src/test/java/com/finpulse/notes/AbstractIntegrationTest.java`

Diferenças do auth:
- `MongoDBContainer` ao invés de `PostgreSQLContainer`
- Imagem: `mongo:7.0`
- Injetar apenas `spring.data.mongodb.uri` (uma propriedade só)
- Método: `mongoDb::getReplicaSetUrl`

Estrutura:
```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractIntegrationTest {

    static final MongoDBContainer mongoDb =
        new MongoDBContainer(DockerImageName.parse("mongo:7.0"))
            .withReuse(true);

    static {
        mongoDb.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDb::getReplicaSetUrl);
    }
}
```

---

### Etapa 7: Teste Sanity

Arquivo: `backend/notes-service/src/test/java/com/finpulse/notes/TestcontainersSetupTest.java`

```java
class TestcontainersSetupTest extends AbstractIntegrationTest {

    @Test
    void containerDeveEstarRodando() {
        assertThat(mongoDb.isRunning()).isTrue();
        assertThat(mongoDb.getReplicaSetUrl()).startsWith("mongodb://");
    }
}
```

**Validação:** `mvn test` sobe MongoDB container e passa o sanity test.

---

### Etapa 8: Testes de Integração do NotesController

Arquivo: `backend/notes-service/src/test/java/com/finpulse/notes/controller/NotesControllerIntegrationTest.java`

Antes de escrever, **inspecionar** o NotesController atual para descobrir:
- Quais endpoints existem
- Qual autenticação (provavelmente JWT no header)
- Schema dos DTOs

Cobertura mínima desejada (20+ testes):

**GET /api/notes** (listar)
- Lista notas do usuário autenticado
- Retorna 401 sem JWT
- Retorna lista vazia se usuário não tem notas

**POST /api/notes** (criar)
- Cria nota com dados válidos → 201
- Retorna 400 sem título
- Retorna 401 sem JWT

**GET /api/notes/{id}** (detalhar)
- Retorna nota existente → 200
- Retorna 404 se nota não existe
- Retorna 403 se nota é de outro usuário

**PUT /api/notes/{id}** (atualizar)
- Atualiza nota própria → 200
- Retorna 403 se nota é de outro usuário
- Retorna 404 se nota não existe

**DELETE /api/notes/{id}** (soft delete)
- Marca nota como deletada
- Retorna 403 se nota é de outro usuário

**GET /api/notes/groups** (tags)
- Retorna grupos do usuário

Use `TestRestTemplate` e gere JWT real chamando o auth-service mock... ou crie um helper `JwtTestHelper` que gera tokens válidos no teste.

---

### Etapa 9: Subir Gate do Jacoco

Após rodar `mvn test` e ver cobertura real, atualizar `pom.xml`:

Se cobertura atingiu 70%+:
```xml
<minimum>0.70</minimum>  <!-- LINE -->
<minimum>0.60</minimum>  <!-- BRANCH -->
```

Se atingiu menos, ajustar pra valor real menos 5% (margem de regressão).

---

### Etapa 10: Commit

```bash
git add backend/notes-service/
git add backend/config-server/src/main/resources/configs/notes-service*.yml
git commit -m "feat(observability): add Actuator + Testcontainers + Correlation ID to notes-service"
git push
```

---

## 🎯 FASE 8.2 — Observability no stock-service

**Tempo estimado:** 6-8 horas (mais complexo)
**Pré-requisitos:** FASE 8.1 concluída

### Diferenças principais vs FASE 8.1

#### Dependências adicionais

```xml
<!-- WireMock para mockar APIs externas -->
<dependency>
    <groupId>org.wiremock</groupId>
    <artifactId>wiremock-standalone</artifactId>
    <version>3.9.1</version>
    <scope>test</scope>
</dependency>
```

Considerar também:
- `testcontainers/postgresql` se stock-service usar PG (provavelmente sim, pra cache de empresas)
- `testcontainers` para Redis se necessário

#### Configurações específicas

**`stock-service-prod.yml`:**
- `${REDIS_URL}` ao invés de hardcoded
- `${ALPHA_VANTAGE_API_KEY}` sem fallback
- `${FINNHUB_API_KEY}` sem fallback

#### Classe base de testes

Pode precisar herdar de uma classe que inicializa **múltiplos containers**:
```java
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> postgres = ...; // se aplicável
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);

    static {
        postgres.start();
        redis.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
        // postgres se aplicável
    }
}
```

#### WireMock setup

Criar `WireMockExtension` para mockar Alpha Vantage e Finnhub:

```java
@RegisterExtension
static WireMockExtension alphaVantage = WireMockExtension.newInstance()
    .options(wireMockConfig().port(9001))
    .build();

@RegisterExtension
static WireMockExtension finnhub = WireMockExtension.newInstance()
    .options(wireMockConfig().port(9002))
    .build();
```

E injetar URLs mockadas:
```java
@DynamicPropertySource
static void mockApis(DynamicPropertyRegistry registry) {
    registry.add("alpha-vantage.base-url", () -> "http://localhost:" + alphaVantage.getPort());
    registry.add("finnhub.base-url", () -> "http://localhost:" + finnhub.getPort());
}
```

Em cada teste, stubbar a resposta esperada:
```java
alphaVantage.stubFor(get(urlPathEqualTo("/query"))
    .willReturn(okJson(loadJson("alpha-vantage-aapl-quote.json"))));
```

#### Testes mínimos do StockController (15+)

**GET /api/stocks/{symbol}/quote**
- Cache miss → chama API externa → retorna 200
- Cache hit → não chama API externa → retorna 200 + tag "cached"
- API externa retorna 500 → testa fallback
- Symbol inválido → 400

**GET /api/stocks/{symbol}/company**
- Sucesso com Alpha Vantage
- Failover para Finnhub se Alpha Vantage falha
- Cache de 24h funciona

**GET /api/stocks/providers**
- Lista status dos providers

---

## 🎯 FASE 9 — Resilience4j

**Tempo estimado:** 4-6 horas
**Pré-requisitos:** FASE 8 completa (observability em todos os serviços)

### Etapa 1: Dependências

Em `stock-service/pom.xml`:
```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.2.0</version>
</dependency>
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-reactor</artifactId>
    <version>2.2.0</version>
</dependency>
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-micrometer</artifactId>
    <version>2.2.0</version>
</dependency>
```

### Etapa 2: Configuração no Config Server

Adicionar em `stock-service.yml`:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      alpha-vantage:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 30s
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
      finnhub:
        # mesmas configs
  retry:
    instances:
      alpha-vantage:
        maxAttempts: 3
        waitDuration: 100ms
        exponentialBackoffMultiplier: 2
  ratelimiter:
    instances:
      stock-api:
        limitForPeriod: 100
        limitRefreshPeriod: 1s
        timeoutDuration: 0
  timelimiter:
    instances:
      external-apis:
        timeoutDuration: 3s
  bulkhead:
    instances:
      external-apis:
        maxConcurrentCalls: 20
```

### Etapa 3: Aplicar Anotações nos Adapters

Nos adapters `AlphaVantageAdapter` e `FinnhubAdapter`:

```java
@CircuitBreaker(name = "alpha-vantage", fallbackMethod = "fallbackGetQuote")
@Retry(name = "alpha-vantage")
@TimeLimiter(name = "external-apis")
@Bulkhead(name = "external-apis")
public CompletableFuture<StockQuote> getQuote(String symbol) {
    // implementação
}

private CompletableFuture<StockQuote> fallbackGetQuote(String symbol, Exception ex) {
    log.warn("Fallback ativado para {}: {}", symbol, ex.getMessage());
    return CompletableFuture.completedFuture(cacheService.getLastKnownQuote(symbol));
}
```

### Etapa 4: Expor Endpoint do Actuator

Em `notes-service.yml`:
```yaml
management:
  endpoints:
    web:
      exposure:
        include:
          - health
          - info
          - metrics
          - prometheus
          - circuitbreakers      # ← NOVO
          - circuitbreakerevents # ← NOVO
```

### Etapa 5: Testes

Testar mudança de estado do CB:

```java
@Test
void circuitBreakerDeveAbrirAposFalhasConsecutivas() {
    // Stub WireMock pra retornar 500 sempre
    alphaVantage.stubFor(get(anyUrl()).willReturn(serverError()));

    // Faz 10 chamadas (atingir slidingWindowSize)
    for (int i = 0; i < 10; i++) {
        try { stockService.getQuote("AAPL"); } catch (Exception e) {}
    }

    // Verifica estado
    CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("alpha-vantage");
    assertThat(cb.getState()).isEqualTo(State.OPEN);
}
```

---

## 🎯 FASE 10 — Spring AOP

**Tempo estimado:** 3-5 horas
**Pré-requisitos:** FASE 9 completa

### Etapa 1: Dependências

Em CADA serviço:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

### Etapa 2: Criar Aspects

**LoggingAspect** em pacote compartilhado (criar `backend/commons/` ou duplicar):

```java
@Aspect
@Component
public class LoggingAspect {

    @Around("execution(* com.finpulse..controller.*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        // Implementação
    }
}
```

**MetricsAspect** registrando timer/counter via Micrometer.

**AuditAspect** captando `@Audited` e salvando no MongoDB.

### Etapa 3: Criar Annotations Customizadas

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {
    String action() default "";
    boolean sanitize() default true;
}
```

### Etapa 4: Aplicar nos Serviços

Marcar métodos sensíveis:
```java
@Audited(action = "USER_LOGIN")
public AuthResponse login(LoginRequest request) { ... }

@Timed(value = "stock.quote.fetch")
public StockQuote getQuote(String symbol) { ... }
```

### Etapa 5: Configurar por Perfil

No Config Server, criar flag:
```yaml
aop:
  logging-enabled: ${AOP_LOGGING:true}
  metrics-enabled: true
  audit-enabled: true
```

E no aspect:
```java
@ConditionalOnProperty(name = "aop.logging-enabled", havingValue = "true")
```

---

## 🎯 FASE 11 — API Gateway

**Tempo estimado:** 8-12 horas
**Pré-requisitos:** FASES 8-10 completas

### Etapa 1: Criar Novo Módulo Maven

Adicionar em `backend/pom.xml`:
```xml
<modules>
    <module>config-server</module>
    <module>eureka-server</module>
    <module>auth-service</module>
    <module>notes-service</module>
    <module>stock-service</module>
    <module>api-gateway</module>  <!-- NOVO -->
</modules>
```

Criar `backend/api-gateway/` com estrutura padrão.

### Etapa 2: Dependências Mínimas

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-config</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <!-- Resilience4j -->
    <!-- Micrometer Prometheus -->
    <!-- JWT validation -->
</dependencies>
```

### Etapa 3: Configuração de Rotas

No Config Server, criar `api-gateway.yml`:

```yaml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/api/auth/**
        - id: notes-service
          uri: lb://notes-service
          predicates:
            - Path=/api/notes/**
          filters:
            - JwtAuthFilter
        - id: stock-service
          uri: lb://stock-service
          predicates:
            - Path=/api/stocks/**
          filters:
            - JwtAuthFilter
            - name: CircuitBreaker
              args:
                name: stockCircuit
                fallbackUri: forward:/fallback/stock
```

### Etapa 4: Filtros Globais

**CorrelationIdGlobalFilter:**
```java
@Component
public class CorrelationIdGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders()
            .getFirst("X-Correlation-ID");

        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        ServerWebExchange mutated = exchange.mutate()
            .request(r -> r.header("X-Correlation-ID", correlationId))
            .build();

        mutated.getResponse().getHeaders().add("X-Correlation-ID", correlationId);

        return chain.filter(mutated);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
```

**JwtAuthGatewayFilter:** valida JWT, extrai userId, injeta no header `X-User-Id`.

### Etapa 5: Refatorar Serviços Downstream

Agora que Gateway valida JWT, serviços internos não precisam mais validar — devem **confiar** no header `X-User-Id`.

**Cuidado:** isso é mudança de segurança importante. Configurar Spring Security pra:
- Não exigir JWT em endpoints internos
- Ler usuário do `X-User-Id` header
- **Não expor portas internas externamente** (só Gateway na 8080)

### Etapa 6: Testes do Gateway

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
class GatewayRoutingTest {

    @Test
    void deveRotarParaAuthService() { ... }

    @Test
    void deveRetornar401SemJwt() { ... }

    @Test
    void deveAplicarRateLimit() { ... }
}
```

---

## 📋 Checklist Global de Conclusão

### Após FASE 8
- [ ] notes-service com 70%+ coverage
- [ ] stock-service com 65%+ coverage
- [ ] Todos os serviços expõem `/actuator/prometheus`
- [ ] Correlation ID em todos os logs
- [ ] WireMock funcionando nos testes do stock-service

### Após FASE 9
- [ ] Circuit Breakers configurados em alpha-vantage e finnhub
- [ ] Métricas de CB visíveis em `/actuator/prometheus`
- [ ] Fallbacks testados e funcionando
- [ ] `/actuator/circuitbreakers` lista todos os CBs

### Após FASE 10
- [ ] LoggingAspect ativo em controllers
- [ ] MetricsAspect gerando métricas customizadas
- [ ] Annotation `@Audited` funcional
- [ ] Configurável por perfil (dev verbose, prod silent)

### Após FASE 11
- [ ] Gateway rodando na 8080
- [ ] Todas as requisições passam pelo Gateway
- [ ] Serviços internos só aceitam tráfego via Gateway
- [ ] JWT validado uma única vez (no Gateway)
- [ ] Métricas do Gateway visíveis

---

**Próximo:** `07-plano-de-testes.md` para a estratégia detalhada de testes.
