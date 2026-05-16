# 📚 Specs do FinPulse — FASES 8-11

> **Pacote de documentação para Spec Driven Development (SDD) com Claude Code.**

---

## 🎯 O que é isso?

Conjunto de **8 documentos markdown** que descrevem **todo o trabalho a ser feito** nas próximas 4 fases do projeto FinPulse:

- **FASE 8** — Replicar observability nos serviços `notes` e `stock`
- **FASE 9** — Resilience4j (Circuit Breaker, Retry, Bulkhead)
- **FASE 10** — Spring AOP (logging, métricas, auditoria)
- **FASE 11** — API Gateway (Spring Cloud Gateway)

---

## 📂 Como Usar

### Passo 1: Colocar os specs no projeto

Copia toda a pasta `specs/` pra raiz do teu projeto:

```
C:\finpulse\
├── backend/
├── .specs/           ← Renomeia "specs" pra ".specs" (padrão SDD)
│   ├── 00-visao-geral.md
│   ├── 01-requisitos-tecnicos.md
│   ├── 05-arquitetura.md
│   ├── 06-plano-de-implementacao.md
│   ├── 07-plano-de-testes.md
│   ├── 08-modelo-de-dados.md
│   ├── 09-contrato-api.md
│   ├── 10-decisoes-tecnicas.md
│   ├── 11-guia-de-execucao.md
│   └── 12-tarefas-atuais.md
├── README.md
└── ...
```

### Passo 2: Abrir o Claude Code no projeto

```powershell
cd C:\finpulse
claude
```

### Passo 3: Inicializar com instrução clara

Na primeira mensagem do Claude Code, **NÃO** comece falando "implementa a fase 8". Em vez disso, comece assim:

```
Antes de qualquer implementação, leia TODOS os arquivos em .specs/ na seguinte ordem:

1. .specs/00-visao-geral.md (contexto do projeto)
2. .specs/01-requisitos-tecnicos.md (o que cada fase entrega)
3. .specs/05-arquitetura.md (como tudo se encaixa)
4. .specs/06-plano-de-implementacao.md (passo a passo)
5. .specs/07-plano-de-testes.md (estratégia de testes)
6. .specs/08-modelo-de-dados.md (entidades e schemas)
7. .specs/09-contrato-api.md (endpoints)
8. .specs/10-decisoes-tecnicas.md (justificativas)
9. .specs/11-guia-de-execucao.md (comandos prontos)
10. .specs/12-tarefas-atuais.md (checklist executável)

Depois de ler tudo, me confirme que entendeu o escopo e está pronto para começar a FASE 8.1.
```

### Passo 4: Iniciar fase por fase

Depois da confirmação, peça:

```
Vamos executar a FASE 8.1 (notes-service) seguindo exatamente o checklist em .specs/12-tarefas-atuais.md.

Execute bloco por bloco, marcando as tarefas conforme conclui. Pare ao final de cada bloco e me mostre o que foi feito antes de prosseguir.
```

### Passo 5: Validar e commitar

Ao final de cada **bloco** dentro da fase:

```
Mostre as mudanças feitas neste bloco. Vou validar antes de prosseguir.
```

E periodicamente:

```
Faça o commit das mudanças com a mensagem sugerida no checklist.
```

---

## 💡 Dicas de Uso com Claude Code

### ✅ Práticas Boas

- **Refira sempre aos specs:** "Segundo o spec 07, esse teste deve ter X assertion"
- **Peça pra Claude Code marcar checkboxes:** "Marque as tarefas 8.1.1 a 8.1.5 como concluídas"
- **Solicite resumos:** "Resuma o que foi feito até agora nesta fase"
- **Faça revisão antes de commitar:** "Mostre os arquivos modificados antes do commit"

### ❌ Práticas a Evitar

- **Não pular fases:** Se está na FASE 8.1, não permita que Claude Code comece a FASE 9
- **Não improvisar:** Se Claude Code sugerir algo fora do spec, pergunte "isso está no spec?"
- **Não acumular muito antes de commitar:** Commits pequenos > commits gigantes
- **Não ignorar testes falhando:** Se um teste falha, PARE e investigue

---

## 🎭 Estrutura dos Specs

| Arquivo | Propósito | Quem usa |
|---|---|---|
| `00-visao-geral.md` | Contexto e escopo | Todos |
| `01-requisitos-tecnicos.md` | O que cada fase entrega | Desenvolvedor / Claude Code |
| `05-arquitetura.md` | Diagramas e fluxos | Arquiteto / Desenvolvedor |
| `06-plano-de-implementacao.md` | Passo a passo de cada fase | Claude Code |
| `07-plano-de-testes.md` | Estratégia de testes | QA / Desenvolvedor |
| `08-modelo-de-dados.md` | Entidades e schemas | Backend dev |
| `09-contrato-api.md` | Endpoints e payloads | Backend + Frontend |
| `10-decisoes-tecnicas.md` | ADRs (Architecture Decision Records) | Arquiteto |
| `11-guia-de-execucao.md` | Comandos prontos | Desenvolvedor / Claude Code |
| `12-tarefas-atuais.md` | Checklist executável | Claude Code |

---

## 📊 Estimativa de Tempo

| Fase | Sub-fase | Tempo |
|---|---|---|
| 8 | 8.1 (notes) | 4-6 horas |
| 8 | 8.2 (stock) | 6-8 horas |
| 9 | — | 4-6 horas |
| 10 | — | 3-5 horas |
| 11 | — | 8-12 horas |
| **Total** | | **25-37 horas** |

---

## 🚦 Critérios de Conclusão

Ao final de **TODAS** as fases, você terá:

### Métricas
- ✅ ~115 testes automatizados
- ✅ ~70% cobertura média em todos os serviços
- ✅ 4 serviços de aplicação + Gateway + Eureka + Config Server

### Funcionalidades
- ✅ Observability completa (health, metrics, traces via correlation ID)
- ✅ Circuit Breaker e retry em chamadas externas
- ✅ Auditoria automática via AOP
- ✅ API Gateway com JWT validation, rate limiting, CORS
- ✅ Configuração centralizada por perfil
- ✅ Testes de integração com bancos reais e WireMock

### Qualidade
- ✅ Build pipeline com gate de cobertura
- ✅ Conventional commits
- ✅ Documentação consistente

---

## 🆘 Quando Pedir Ajuda

Use o Claude (chat) ao invés do Claude Code se:

- 🤔 **Você não entendeu um conceito** (ex: "o que é Circuit Breaker?")
- 🐛 **Encontrou um bug que não está no spec**
- 🎯 **Precisa adaptar algo pro seu contexto específico**
- ⚙️ **Tem um problema de ambiente** (Docker, Maven, etc.)

Use o Claude Code se:

- 🛠️ **Implementação direta** seguindo specs
- ✅ **Marcar checkboxes** conforme avança
- 🧪 **Rodar testes** e analisar resultados
- 📝 **Criar/editar arquivos** segundo as instruções

---

## 📝 Atualizando os Specs

Conforme avança e descobre coisas, **atualize os specs**. Specs desatualizados são piores que specs ausentes.

- Mudou alguma decisão? Edite `10-decisoes-tecnicas.md`
- Descobriu novo endpoint? Edite `09-contrato-api.md`
- Encontrou armadilha técnica? Adicione em `11-guia-de-execucao.md`

---

## 🎓 Aprendizado Esperado

Ao terminar todas as fases seguindo esses specs, você vai **dominar**:

- **Spring Boot 3.x** completo (Actuator, Security, AOP, Cloud)
- **Microsserviços profissionais** (Config Server, Discovery, Gateway)
- **Resilience patterns** (CB, Retry, Bulkhead, Rate Limit)
- **Testes profissionais** (Testcontainers, WireMock, integração real)
- **Observability moderna** (métricas, logs estruturados, correlation ID)
- **Spec Driven Development** como metodologia

Tudo isso vale **muito** em entrevistas de pleno/sênior em 2026.

---

**Boa execução! 🚀**

Quando concluir, considere atualizar o README principal do FinPulse e criar uma tag de versão (`v1.0-observability-complete`).
