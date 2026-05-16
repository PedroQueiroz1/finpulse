# Plano de Testes

> Estratégia de testes para FASES 8-11. Critérios objetivos de cobertura e tipos de teste.

---

## 🎯 Filosofia de Testes

### Pirâmide ideal por serviço

```
        ╱─╲           ~15% Integration (Testcontainers + WireMock)
       ╱───╲          ~25% Slice (@WebMvcTest, @DataJpaTest)
      ╱─────╲         ~60% Unit (JUnit + Mockito puro)
```

### Regras de Ouro

1. **NUNCA usar H2** — só PostgreSQL/MongoDB reais via Testcontainers
2. **Mocks externos com WireMock** — não chamar Alpha Vantage/Finnhub real nos testes
3. **Limpar estado entre testes** — `@BeforeEach` com `repository.deleteAll()`
4. **Testar caso feliz E caso de erro** — para cada endpoint, mínimo 2 testes
5. **`@DisplayName` descritivo** — em português, descrevendo comportamento
6. **`@Nested` para agrupar** — organizar por endpoint ou cenário

---

## 📋 FASE 8.1 — notes-service

### Cobertura Alvo

| Tipo | Quantidade mínima | Cobertura alvo |
|---|---|---|
| Sanity (Testcontainers) | 1 | — |
| Integration (Controller) | 20+ | 80%+ no controller |
| Unit (Service) | 5+ | 70%+ no service |
| **TOTAL** | **26+** | **70% line / 60% branch** |

### Testes Obrigatórios

#### `TestcontainersSetupTest`
- [ ] Container MongoDB sobe e está rodando
- [ ] URL contém `mongodb://`

#### `NotesControllerIntegrationTest`

**@Nested ListNotes:**
- [ ] `deveRetornarListaVaziaQuandoUsuarioNaoTemNotas`
- [ ] `deveRetornarApenasNotasDoUsuarioAutenticado`
- [ ] `deveRetornar401SemJwt`

**@Nested CreateNote:**
- [ ] `deveCriarNotaComDadosValidos`
- [ ] `deveRetornar400SemTitulo`
- [ ] `deveRetornar400SemConteudo` (se aplicável)
- [ ] `deveRetornar401SemJwt`
- [ ] `deveAssociarNotaAoUsuarioCorreto`

**@Nested GetNoteById:**
- [ ] `deveRetornarNotaExistenteDoUsuario`
- [ ] `deveRetornar404QuandoNotaNaoExiste`
- [ ] `deveRetornar403QuandoNotaEhDeOutroUsuario`

**@Nested UpdateNote:**
- [ ] `deveAtualizarNotaPropria`
- [ ] `deveRetornar403QuandoTentaAtualizarNotaDeOutro`
- [ ] `deveRetornar404QuandoNotaNaoExiste`

**@Nested DeleteNote:**
- [ ] `deveDeletarNotaPropriaSoftDelete` (verifica flag `deleted=true`)
- [ ] `deveRetornar403QuandoTentaDeletarNotaDeOutro`

**@Nested SearchAndGroups:**
- [ ] `deveListarGruposDoUsuario`
- [ ] `deveBuscarPorTermo` (se houver endpoint)

#### Testes do Actuator
- [ ] `deveRetornarHealthStatusUp`
- [ ] `deveRetornarMetricasPrometheus`
- [ ] `deveRetornarInfoComMetadados`

#### Testes de Correlation ID
- [ ] `deveGerarCorrelationIdQuandoHeaderAusente`
- [ ] `deveReutilizarCorrelationIdQuandoHeaderPresente`
- [ ] `deveDevolverCorrelationIdNaResposta`

---

## 📋 FASE 8.2 — stock-service

### Cobertura Alvo

| Tipo | Quantidade mínima | Cobertura alvo |
|---|---|---|
| Sanity | 1 | — |
| Integration (Controller) | 15+ | 75%+ no controller |
| Unit (Service + Strategy) | 8+ | 75%+ |
| Adapter (com WireMock) | 6+ | 70%+ |
| **TOTAL** | **30+** | **65% line / 55% branch** |

### Testes Obrigatórios

#### `WireMockSetupTest`
- [ ] WireMock sobe na porta esperada
- [ ] Stub básico responde corretamente

#### `StockControllerIntegrationTest`

**@Nested GetQuote:**
- [ ] `deveRetornarCotacaoComCacheMiss` (verifica que chamou Alpha Vantage)
- [ ] `deveRetornarCotacaoComCacheHit` (segunda chamada não bate na API)
- [ ] `deveFazerFailoverParaFinnhubQuandoAlphaVantageFalha`
- [ ] `deveRetornar400ParaSymbolInvalido`
- [ ] `deveAplicarTtlDe60sNoCache` (verifica timing)

**@Nested GetCompany:**
- [ ] `deveRetornarDadosDaEmpresaComCache24h`
- [ ] `deveBuscarDeAlphaVantagePrimeiro`
- [ ] `deveFailoverParaFinnhub`

**@Nested Providers:**
- [ ] `deveListarStatusDosProviders`

#### `AlphaVantageAdapterTest` (com WireMock)
- [ ] `deveParsearRespostaJsonCorretamente`
- [ ] `deveTratarErro429RateLimit`
- [ ] `deveTratarErro500`
- [ ] `deveTimeoutEm5s`

#### `FinnhubAdapterTest` (com WireMock)
- [ ] Mesmos testes do AlphaVantage

#### `StockStrategyTest` (unit)
- [ ] `deveEscolherAlphaVantageComoPrimario`
- [ ] `deveFazerFailoverParaSecundario`
- [ ] `deveRetornarErroQuandoAmbosFalham`

---

## 📋 FASE 9 — Resilience4j

### Cobertura Alvo

Adicionais aos da FASE 8.2: **10+ testes** específicos de resilience.

### Testes Obrigatórios

#### `CircuitBreakerIntegrationTest`

**@Nested EstadoFechado:**
- [ ] `deveCircuitBreakerEstarFechadoInicialmente`
- [ ] `devePermitirChamadasNoEstadoFechado`

**@Nested TransicaoParaAberto:**
- [ ] `deveAbrirAposFalhasConsecutivasAcimaDoLimite`
  - Stubbar WireMock pra retornar 500 em 6+ chamadas seguidas
  - Verificar `cb.getState() == OPEN`

**@Nested EstadoAberto:**
- [ ] `naoDevePermitirChamadasQuandoAberto`
  - Forçar CB pra OPEN manualmente
  - Verificar que próxima chamada retorna fallback sem bater na API

**@Nested TransicaoParaHalfOpen:**
- [ ] `deveTransicionarParaHalfOpenAposEspera`
  - Aguardar `waitDurationInOpenState`
  - Verificar `cb.getState() == HALF_OPEN`

**@Nested Recuperacao:**
- [ ] `deveFecharNovamenteAposChamadasBemSucedidasEmHalfOpen`

#### `RetryIntegrationTest`
- [ ] `deveRetentar3VezesAposFalhaTransitoria`
- [ ] `deveAplicarBackoffExponencial` (verificar timestamps)
- [ ] `naoDeveRetentarApos3Tentativas`

#### `TimeLimiterTest`
- [ ] `deveTimeoutChamadaMaisLongaQue3s`

#### `RateLimiterTest`
- [ ] `deveRejeitarChamadasAcimaDoLimite`

#### `BulkheadTest`
- [ ] `deveIsolarThreadsEntreEndpoints`

---

## 📋 FASE 10 — Spring AOP

### Cobertura Alvo

**5+ testes** focados em verificar o comportamento dos aspects.

### Testes Obrigatórios

#### `LoggingAspectTest`
- [ ] `deveLogarEntradaEMetodoDoController`
- [ ] `deveLogarSaidaComTempoExecucao`
- [ ] `deveLogarExceptionStackTrace`
- [ ] `naoDeveLogarSenhasEmParametros` (sanitização)

#### `MetricsAspectTest`
- [ ] `deveIncrementarCounterAcadaChamada`
- [ ] `deveRegistrarTimerComDuracao`
- [ ] `deveCriarMetricaComNomeDoMetodo`

#### `AuditAspectTest`
- [ ] `deveSalvarAuditoriaQuandoMetodoTemAnnotation`
- [ ] `naoDeveSalvarSenhaNaAuditoria`
- [ ] `deveSalvarMesmoQuandoMetodoFalha` (auditoria de erro)

---

## 📋 FASE 11 — API Gateway

### Cobertura Alvo

**15+ testes** cobrindo roteamento, filtros e segurança.

### Testes Obrigatórios

#### `GatewayRoutingTest`

**@Nested AuthRoutes:**
- [ ] `deveRotearParaAuthService`
- [ ] `deveAceitarLoginSemJwt` (endpoint público)
- [ ] `deveAceitarRegisterSemJwt`

**@Nested NotesRoutes:**
- [ ] `deveRotearParaNotesServiceComJwtValido`
- [ ] `deveRejeitar401SemJwt`
- [ ] `deveRejeitar403ComJwtExpirado`

**@Nested StockRoutes:**
- [ ] `deveRotearParaStockServiceComJwt`
- [ ] `deveAplicarCircuitBreakerNoStock`

#### `GlobalFiltersTest`
- [ ] `deveGerarCorrelationIdQuandoHeaderAusente`
- [ ] `devePropagarCorrelationIdParaDownstream`
- [ ] `deveAdicionarHeaderXUserIdAposValidarJwt`

#### `RateLimitTest`
- [ ] `deveRejeitarRequestsAcimaDoLimite`
- [ ] `deveContarSeparadoPorIp`

#### `CorsTest`
- [ ] `devePermitirRequestsCorsConfigurados`
- [ ] `deveRejeitarOriginsNaoPermitidas`

#### `GatewayObservabilityTest`
- [ ] `deveExporRotasNoActuator`
- [ ] `deveExporMetricasPorRota`

---

## 🛠️ Padrões de Implementação de Testes

### Padrão 1: Classe Base de Integração

Toda classe de teste de integração deve estender uma `AbstractIntegrationTest` específica do serviço, que:

1. Inicia container(s) Docker
2. Configura `@DynamicPropertySource`
3. Define `@ActiveProfiles("test")`
4. Usa `@SpringBootTest(webEnvironment = RANDOM_PORT)`

### Padrão 2: Helper de JWT

Criar `JwtTestHelper` que gera tokens válidos pra testes:

```java
public class JwtTestHelper {

    private static final String SECRET = "test-secret-key-...";

    public static String generateToken(String userId, String email, String role) {
        return Jwts.builder()
            .setSubject(email)
            .claim("userId", userId)
            .claim("role", role)
            .setExpiration(new Date(System.currentTimeMillis() + 900000))
            .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
            .compact();
    }

    public static HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }
}
```

### Padrão 3: Builder Pattern em Testes

Evitar construir DTOs com muitos `null`:

```java
private RegisterRequest registerRequest() {
    return new RegisterRequest("Pedro", "pedro@finpulse.com", "senha12345");
}

private NoteRequest noteRequest() {
    return new NoteRequest("Título", "Conteúdo", List.of("trabalho"));
}
```

### Padrão 4: Limpeza Entre Testes

```java
@BeforeEach
void cleanDatabase() {
    notesRepository.deleteAll();
}
```

### Padrão 5: Stubs WireMock Reutilizáveis

```java
class StockApiStubs {

    static void stubAlphaVantageQuoteSuccess(WireMockExtension wm, String symbol) {
        wm.stubFor(get(urlPathMatching("/query.*"))
            .withQueryParam("symbol", equalTo(symbol))
            .willReturn(okJson("""
                {
                  "Global Quote": {
                    "01. symbol": "%s",
                    "05. price": "182.34"
                  }
                }
                """.formatted(symbol))));
    }

    static void stubAlphaVantageError(WireMockExtension wm) {
        wm.stubFor(get(anyUrl()).willReturn(serverError()));
    }
}
```

---

## 📊 Métricas de Sucesso

### Por fase

| Fase | Testes mínimos | Coverage line | Coverage branch |
|---|---|---|---|
| 8.1 | 26 | 70% | 60% |
| 8.2 | 30 | 65% | 55% |
| 9 | 40 (acumulado) | 70% | 60% |
| 10 | 45 (acumulado) | 70% | 60% |
| 11 | 60+ (acumulado, novo módulo) | 70% | 60% |

### Tempo de execução

- `mvn test` em qualquer serviço: **< 90 segundos** com Testcontainers reuse
- Suite completa (todos os serviços): **< 5 minutos**

---

## 🚨 Anti-padrões (NÃO FAZER)

❌ **Não usar `@MockBean` para repositories em testes de integração**
   - Se está usando `@SpringBootTest` + Testcontainers, use o repository real
   - `@MockBean` é pra **slice tests** (`@WebMvcTest`)

❌ **Não chamar APIs externas reais nos testes**
   - Sempre WireMock pra Alpha Vantage, Finnhub, etc.

❌ **Não compartilhar estado entre testes**
   - Cada teste deve criar seus próprios dados
   - `@BeforeEach` limpa o banco

❌ **Não testar getters/setters**
   - É código gerado, não precisa coverage

❌ **Não escrever testes que dependem de ordem**
   - JUnit não garante ordem de execução
   - Cada teste deve ser independente

❌ **Não esquecer de cobrir caminhos de erro**
   - 50% line coverage sem testar exceções = falsa segurança

---

**Próximo:** `08-modelo-de-dados.md` para esquemas de dados.
