# Guia de Execução

> Comandos prontos para o Claude Code executar. Copie e cole no terminal.

---

## 🚀 Setup Inicial (uma vez)

### Verificar pré-requisitos

```powershell
# Java 17
java --version

# Maven
mvn --version

# Docker
docker --version
docker ps

# Git
git --version

# Confirmar diretório do projeto
cd C:\finpulse
git status
```

**Esperado:**
- Java 17.x
- Maven 3.9+
- Docker 28.x rodando
- Git up-to-date

---

## 🐳 Docker Setup (apenas uma vez)

### Habilitar reuse de containers (importante!)

```powershell
Set-Content -Path "$HOME\.testcontainers.properties" -Value "testcontainers.reuse.enable=true"
```

**Por que:** containers PostgreSQL/MongoDB ficam em standby entre execuções de `mvn test`, reduzindo tempo de build de ~30s pra ~5s.

### Verificar que reuse está ativo

```powershell
Get-Content "$HOME\.testcontainers.properties"
```

Deve mostrar: `testcontainers.reuse.enable=true`

---

## 🎬 Subindo a Aplicação (ordem importa)

### Em terminais separados:

**Terminal 1 - Config Server:**
```powershell
cd C:\finpulse\backend\config-server
mvn spring-boot:run
```
**Aguardar:** `Started ConfigServerApplication`

**Terminal 2 - Eureka:**
```powershell
cd C:\finpulse\backend\eureka-server
mvn spring-boot:run
```
**Aguardar:** `Started EurekaServerApplication`

**Terminal 3 - Auth Service:**
```powershell
cd C:\finpulse\backend\auth-service
mvn spring-boot:run
```
**Aguardar:** `Started AuthServiceApplication`

**Terminal 4 - Notes Service** *(após FASE 8.1)*:
```powershell
cd C:\finpulse\backend\notes-service
mvn spring-boot:run
```

**Terminal 5 - Stock Service** *(após FASE 8.2)*:
```powershell
cd C:\finpulse\backend\stock-service
mvn spring-boot:run
```

**Terminal 6 - API Gateway** *(após FASE 11)*:
```powershell
cd C:\finpulse\backend\api-gateway
mvn spring-boot:run
```

### Validação rápida que tudo subiu

```powershell
# Eureka Dashboard
Start-Process http://localhost:8761

# Health checks
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
```

---

## 🧪 Comandos de Teste

### Rodar testes de um serviço específico

```powershell
cd C:\finpulse\backend\notes-service
mvn clean test
```

### Rodar apenas uma classe de teste

```powershell
mvn test -Dtest=NotesControllerIntegrationTest
```

### Rodar apenas um método específico

```powershell
mvn test -Dtest=NotesControllerIntegrationTest#deveCriarNotaComDadosValidos
```

### Rodar testes sem o gate de cobertura (debug)

```powershell
mvn test -Djacoco.skip=true
```

### Gerar e abrir relatório Jacoco

```powershell
mvn clean test
Start-Process C:\finpulse\backend\notes-service\target\site\jacoco\index.html
```

### Rodar todos os testes do projeto

```powershell
cd C:\finpulse\backend
mvn clean test
```

---

## 🔄 Build Commands

### Build completo (sem testes)

```powershell
cd C:\finpulse\backend\notes-service
mvn clean install -DskipTests
```

### Build com testes

```powershell
mvn clean install
```

### Build de TODOS os módulos

```powershell
cd C:\finpulse\backend
mvn clean install
```

### Limpar caches do Maven (se algo estranho)

```powershell
mvn clean
Remove-Item -Recurse -Force ~\.m2\repository\com\finpulse  # cuidado!
```

---

## 🐳 Comandos Docker Úteis

### Ver containers do Testcontainers rodando

```powershell
docker ps --filter "label=org.testcontainers=true"
```

### Parar todos os containers do Testcontainers

```powershell
docker ps -aq --filter "label=org.testcontainers=true" | ForEach-Object { docker stop $_ }
```

### Limpar completamente (apaga TUDO)

```powershell
docker stop $(docker ps -aq)
docker container prune -f
docker volume prune -f
docker image prune -f
```

### Ver logs de um container

```powershell
docker logs <container-id> --follow
```

---

## 🎯 Comandos por Fase

### FASE 8.1 — notes-service observability

**1. Adicionar dependências:**
Editar `backend/notes-service/pom.xml` manualmente seguindo `06-plano-de-implementacao.md`.

**2. Reorganizar configurações:**
Editar 5 arquivos YAML (1 local + 3 Config Server + 1 logback) seguindo specs.

**3. Criar classes:**
- `CorrelationIdFilter.java`
- `AbstractIntegrationTest.java`
- `TestcontainersSetupTest.java`
- `NotesControllerIntegrationTest.java`

**4. Build e teste:**
```powershell
cd C:\finpulse\backend\notes-service
mvn clean install -DskipTests
```

**5. Reiniciar Config Server:**
```powershell
# Ctrl+C no terminal do Config Server
cd C:\finpulse\backend\config-server
mvn spring-boot:run
```

**6. Rodar testes:**
```powershell
cd C:\finpulse\backend\notes-service
mvn clean test
```

**7. Validar endpoints:**
```powershell
# Subir o serviço
mvn spring-boot:run

# Em outro terminal
curl http://localhost:8082/actuator/health
curl http://localhost:8082/actuator/prometheus
curl http://localhost:8082/actuator/info
```

**8. Commit:**
```powershell
cd C:\finpulse
git add backend/notes-service/
git add backend/config-server/src/main/resources/configs/notes-service*.yml
git status  # validar antes
git commit -m "feat(observability): add Actuator and Testcontainers to notes-service"
git push
```

---

### FASE 8.2 — stock-service observability

Mesma sequência da 8.1, com:
- WireMock setup adicional
- Múltiplos containers (Redis + PG se aplicável)
- Stubs JSON em `src/test/resources/wiremock/`

**Validação extra com WireMock:**
```powershell
# Verificar que stubs estão sendo respeitados
mvn test -Dtest=AlphaVantageAdapterTest
```

---

### FASE 9 — Resilience4j

**1. Adicionar dependências no stock-service.**

**2. Configurar no Config Server (`stock-service.yml`):**
Adicionar bloco `resilience4j:` seguindo `06-plano-de-implementacao.md`.

**3. Aplicar annotations nos adapters:**
- `@CircuitBreaker(name="alpha-vantage", fallbackMethod="...")`
- `@Retry(name="alpha-vantage")`
- `@TimeLimiter(name="external-apis")`
- `@Bulkhead(name="external-apis")`

**4. Reiniciar e validar:**
```powershell
cd C:\finpulse\backend\stock-service
mvn clean install -DskipTests
mvn spring-boot:run

# Em outro terminal
curl http://localhost:8083/actuator/circuitbreakers
curl http://localhost:8083/actuator/prometheus | Select-String "resilience4j"
```

**5. Forçar abertura do CB pra testar (com WireMock):**
```powershell
# Rodar teste específico que simula falhas
mvn test -Dtest=CircuitBreakerIntegrationTest
```

---

### FASE 10 — Spring AOP

**1. Adicionar dependência em CADA serviço:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

**2. Criar aspects em `com.finpulse.{servico}.aspect/`:**
- `LoggingAspect.java`
- `MetricsAspect.java`
- `AuditAspect.java`

**3. Criar annotations em `com.finpulse.{servico}.annotation/`:**
- `Audited.java`

**4. Configurar perfil:**
Em `application.yml` ou Config Server:
```yaml
aop:
  logging-enabled: true
  metrics-enabled: true
  audit-enabled: true
```

**5. Build e validar:**
```powershell
mvn clean test
```

**6. Verificar logs durante uma requisição:**
```powershell
# Fazer login
$body = '{"email":"pedro@finpulse.com","password":"senha12345"}'
curl -Method Post -Uri http://localhost:8081/api/auth/login -ContentType "application/json" -Body $body

# Ver logs no terminal do auth-service
# Deve aparecer: "Entering method: login with args: [...]"
# E: "Exiting method: login in 234ms"
```

---

### FASE 11 — API Gateway

**1. Criar novo módulo Maven:**
```powershell
cd C:\finpulse\backend
mkdir api-gateway
cd api-gateway
```

Criar `pom.xml`, estrutura Maven, classe principal.

**2. Adicionar ao `backend/pom.xml` parent:**
```xml
<modules>
    ...
    <module>api-gateway</module>
</modules>
```

**3. Criar `bootstrap.yml` no Gateway:**
```yaml
spring:
  application:
    name: api-gateway
  config:
    import: optional:configserver:http://localhost:8888
```

**4. Criar configurações no Config Server:**
- `api-gateway.yml` (rotas, filtros)
- `api-gateway-dev.yml`
- `api-gateway-prod.yml`

**5. Implementar filtros globais:**
- `CorrelationIdGlobalFilter.java`
- `JwtAuthGatewayFilter.java`

**6. Reiniciar Config Server:**
```powershell
# Ctrl+C
cd C:\finpulse\backend\config-server
mvn spring-boot:run
```

**7. Subir Gateway:**
```powershell
cd C:\finpulse\backend\api-gateway
mvn clean install -DskipTests
mvn spring-boot:run
```

**8. Validar rotas:**
```powershell
# Listar rotas do Gateway
curl http://localhost:8080/actuator/gateway/routes

# Testar roteamento
curl http://localhost:8080/api/auth/health

# Testar rate limit (fazer 100 requests rapidamente)
1..100 | ForEach-Object { curl http://localhost:8080/api/auth/health }
```

---

## 📊 Comandos de Validação Pós-Fase

### Verificar cobertura

```powershell
cd C:\finpulse\backend\notes-service
Start-Process target\site\jacoco\index.html
```

Procurar valor de **Total** na tabela principal.

### Verificar métricas Prometheus

```powershell
# Geral
curl http://localhost:8082/actuator/prometheus | Out-File metrics.txt
notepad metrics.txt

# Específico do Circuit Breaker (após FASE 9)
curl http://localhost:8083/actuator/prometheus | Select-String "resilience4j"
```

### Verificar header de Correlation ID

```powershell
$response = curl -Uri http://localhost:8082/actuator/health -Method Get
$response.Headers["X-Correlation-ID"]
```

Deve retornar um UUID.

### Verificar propagação entre serviços (FASE 11)

```powershell
$correlationId = [guid]::NewGuid().ToString()

curl -Uri http://localhost:8080/api/auth/health `
     -Headers @{ "X-Correlation-ID" = $correlationId }

# Verificar nos logs de TODOS os serviços que o ID aparece
```

---

## 🔍 Troubleshooting

### Erro: "Cannot connect to Docker daemon"

```powershell
# Iniciar Docker Desktop
Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"

# Aguardar uns 30 segundos
docker ps  # validar
```

### Erro: "Port 8081 already in use"

```powershell
# Encontrar processo na porta
netstat -ano | findstr :8081

# Matar processo (substitua <PID>)
taskkill /F /PID <PID>
```

### Erro: "Failed to determine driver class" em prod

Lembre de passar variáveis via `-Dspring-boot.run.jvmArguments`:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=prod" "-Dspring-boot.run.jvmArguments=-DDB_URL=jdbc:postgresql://localhost:5432/finpulse_auth -DDB_USER=finpulse -DDB_PASSWORD=finpulse123 -DJWT_SECRET=test-secret-com-pelo-menos-256-bits-de-comprimento-para-passar-validacao -DEUREKA_URL=http://localhost:8761/eureka/"
```

### Erro: "Config Server não responde"

```powershell
# Verificar que Config Server está UP
curl http://localhost:8888/actuator/health

# Verificar que YAML está acessível
curl http://localhost:8888/notes-service/dev
```

### Build falha com "coverage below threshold"

Significa que adicionou código sem teste. Duas opções:

1. **Escrever mais testes** (correto)
2. **Baixar threshold temporariamente** (não recomendado em PRs)

Em `pom.xml`:
```xml
<minimum>0.60</minimum>  <!-- ao invés de 0.70 -->
```

### Container do Testcontainers não reusa

```powershell
# Verificar configuração
Get-Content "$HOME\.testcontainers.properties"

# Deve ter: testcontainers.reuse.enable=true

# Se não tem, configurar:
Set-Content -Path "$HOME\.testcontainers.properties" -Value "testcontainers.reuse.enable=true"
```

---

## 📦 Git Workflow

### Branch por fase

```powershell
git checkout -b feature/fase-8.1-notes-observability

# Trabalhar...

git add .
git commit -m "feat(notes): add Actuator + Testcontainers"

# Push da branch
git push -u origin feature/fase-8.1-notes-observability
```

### Merge na main

```powershell
git checkout main
git merge feature/fase-8.1-notes-observability
git push
```

### Convenção de commits

| Tipo | Quando usar |
|---|---|
| `feat(escopo)` | Nova feature |
| `fix(escopo)` | Correção de bug |
| `test(escopo)` | Adição/modificação de testes |
| `docs(escopo)` | Documentação |
| `refactor(escopo)` | Refatoração sem mudança de comportamento |
| `chore(escopo)` | Manutenção (deps, configs) |
| `perf(escopo)` | Performance |

**Escopos:** `auth`, `notes`, `stock`, `gateway`, `observability`, `resilience`, `aop`, `commons`

---

## 🎓 Comandos de Aprendizado

### Ver árvore de dependências

```powershell
cd C:\finpulse\backend\notes-service
mvn dependency:tree
```

### Procurar dependências específicas

```powershell
mvn dependency:tree "-Dincludes=io.micrometer,io.prometheus"
```

### Ver configurações que o Spring carregou

```powershell
# Após subir o serviço
curl http://localhost:8082/actuator/configprops
curl http://localhost:8082/actuator/env
```

### Ver beans Spring registrados

```powershell
curl http://localhost:8082/actuator/beans
```

---

**Próximo:** `12-tarefas-atuais.md` para checklist executável.
