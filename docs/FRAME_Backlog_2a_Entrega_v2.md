# F.R.A.M.E — Backlog da 2.ª Entrega (v2 — Revisado)

**Film Resource & Attendance Management Engine**  
Disciplina de Requisitos — CESAR School, 2026.1

---

## Visão geral da 2.ª entrega

Os requisitos da 2.ª entrega são:

- Todos os artefatos da 1.ª entrega (já entregues e aprovados)
- Implementar pelo menos 6 padrões de projeto diferentes, sendo 1 por integrante
- Implementar a camada de persistência com mapeamento objeto-relacional (JPA)
- Implementar a camada de apresentação web

**Estado atual do código — atualizado em 2026-06-02:**

| Camada | Status |
|---|---|
| Domínio + BDD + CML (1.ª entrega) | ✅ Completo e aprovado |
| Banco de dados: PostgreSQL 17.10 via Docker local (porta 5433) | ✅ Conectado e funcionando |
| `docker-compose.yml` na raiz do projeto | ✅ Feito |
| `infrastructure/pom.xml` — driver PostgreSQL adicionado | ✅ Feito |
| `presentation-backend/src/.../FrameApplication.java` — classe principal Spring Boot + CORS | ✅ Feito |
| `presentation-backend/src/main/resources/application.properties` — JPA + porta 8080 | ✅ Feito |
| `presentation-backend/src/main/resources/application-local.properties` — credenciais do banco (gitignored) | ✅ Feito (cada membro cria localmente) |
| `presentation-backend/src/main/resources/application-local.properties.example` — template para o time | ✅ Feito |
| `presentation-frontend/.env` — `VITE_TMDB_TOKEN` (gitignored) | ✅ Feito (cada membro cria localmente) |
| Estrutura do frontend React + Vite — `package.json`, `vite.config.ts`, `App.tsx`, `client.ts` | ✅ Feito |
| `mvn install -DskipTests` — todos os 8 módulos compilando | ✅ Confirmado |
| `mvn spring-boot:run` — backend sobe e conecta ao banco | ✅ Confirmado |
| **F7 — Catálogo de Filmes: JPA + REST + Proxy** | ✅ Implementado e testado (Irvin) |
| **F4 — Grade de Exibição: JPA + REST + Proxy** | ✅ Implementado e testado (Irvin) |
| Padrão **Proxy — Caching Proxy** — F7 (`FilmeRepositoryProxy`) e F4 (`GradeDeExibicaoRepositoryProxy`) interceptam `listarTodos()`/`listarTodas()` e retornam cache em memória; escrita invalida cache | ✅ Corrigido e validado pelo professor (2026-06-02) |
| Regra de conflito de horário entre grades — movida para `GradeService.adicionarSessao()` no domínio | ✅ Corrigido (era incorretamente no Proxy) |
| Regra de proteção de remoção de filme — em `FilmeService.remover()` no domínio | ✅ Já estava correto |
| Classes `@Entity` na camada `infrastructure` (Filmes, Salas, Grades, Sessões) | ✅ Feito |
| Controllers REST em `presentation-backend` (`/api/filmes`, `/api/salas`, `/api/grades`) | ✅ Feito |
| `GlobalExceptionHandler` — 409 para conflitos, 400 para argumentos inválidos | ✅ Feito |
| **Sessões recorrentes diárias** — `Sessao.inicio` é `LocalTime`; a grade define o período; sessões repetem todos os dias | ✅ Implementado |
| **Validação de data passada** — `GradeDeExibicao` rejeita `inicio` anterior a hoje | ✅ Implementado |
| **Bloqueio de remoção de grade** — `GradeService.removerGrade()` bloqueia se qualquer sessão de hoje já iniciou | ✅ Implementado |
| **CRUD completo de Grade** — criar, editar período (`PUT /api/grades/{id}`), remover grade, adicionar/editar/remover sessão | ✅ Implementado |
| **CRUD completo de Filme** — cadastrar, editar (`PUT /api/filmes/{id}`), ativar/desativar, remover | ✅ Implementado |
| **CRUD completo de Sala** — cadastrar, editar (`PUT /api/salas/{id}`), remover | ✅ Implementado |
| **Edição de sessão dentro de grade** — `PUT /api/grades/{gradeId}/sessoes/{sessaoId}` — altera filme, sala ou horário | ✅ Implementado |
| `SessaoJpa.inicio` mapeado como `TIME` no banco (`@JdbcTypeCode(SqlTypes.TIME)`) | ✅ Feito |
| Datas/horas serializadas como `String` nos `*Response` (sem dependência de Jackson feature flags) | ✅ Feito |
| **Frontend — toggle Backoffice / Portal do Cliente** (`App.tsx`) — troca contexto de navegação e cor da sidebar | ✅ Implementado (Irvin) |
| **Frontend — Tela de Grade** (`GradePage.tsx`) — design completo com cards, stats, modais, edição inline de sessão | ✅ Redesenhado (2026-06-02) |
| **Frontend — Tela de Catálogo** (`CatalogoPage.tsx`) — grid de cards com pôsteres TMDB, badges, tabs, modal de edição | ✅ Redesenhado (2026-06-02) |
| **Frontend — Tela de Salas** (`SalasPage.tsx`) — cards por tipo com barra de capacidade, modal de edição | ✅ Redesenhado (2026-06-02) |
| Integração TMDB — pôsteres automáticos em `CatalogoPage` via `src/lib/tmdb.ts` + cache localStorage 7 dias | ✅ Funcionando |
| **F5 — Bomboniere: JPA + REST + Observer** — insumos, produtos, receitas, venda, alertas, ativar/desativar produto | ✅ Implementado (Fabiana) |
| Padrão **Observer** — `EstoqueSubject` + `EstoqueObserver` + `AlertaEstoqueObserver`; `BombonieresService` implementa `EstoqueSubject` e notifica via `BomboniereConfig` | ✅ Funcionando e validado |
| **F6 — Check-in** (Fabiana) — RBAC, seleção de funcionário, Observer de ocupação | ✅ Implementado (Fabiana) |
| **RBAC — Controle de acesso de funcionários** — `FuncionarioController`, roles e permissões, `UsuariosPage.tsx` | ✅ Implementado (Fabiana) |
| **F3 — Fidelidade: JPA + REST + Iterator** — acúmulo, resgate, extrato, benefícios | ✅ Implementado (Amanda) |
| **F8 — Fechamento de Caixa** — Template Method | ❌ Pendente (Amanda) |
| **F1 — Compra de Ingresso: JPA + REST + Strategy** — pedido, reserva, cupom, ingresso, QR Code, fluxo multi-etapa | ✅ Implementado (Julia) |
| Padrão **Strategy** — `DescontoStrategy` + `DescontoLeve2Pague1` + `DescontoParceriaCartao` + `DescontoEstudante`; `MotorDePromocoes` recebe lista de estratégias e seleciona em runtime | ✅ Funcionando e validado |
| **F2 — Explorar Programação: JPA + REST + Decorator** — histórico, favoritos, filmes sugeridos, recomendação personalizada | ✅ Implementado (Julia) |
| Padrão **Decorator** — `ProgramacaoComRecomendacaoDecorator` envolve `ProgramacaoService` e adiciona recomendação sem alterar o serviço base | ✅ Funcionando e validado |
| Use Cases no módulo `application` — `IniciarPedidoUseCase`, `AplicarCupomUseCase`, `FinalizarPedidoUseCase`, `ListarFilmesUseCase`, `FavoritarFilmeUseCase` | ✅ Implementado (Julia) |

> **Atenção ao rodar o projeto:** Usar sempre `mvn install -DskipTests` (sem a flag os testes BDD de Fidelidade falham — problema pré-existente no módulo `domain-portal` que Amanda precisa corrigir). O banco usa `docker-compose up -d` na raiz. Cada membro cria `application-local.properties` e `presentation-frontend/.env` localmente (gitignored).

**Decisão de stack confirmada pelo time:**
- Banco: **PostgreSQL 17.10 via Docker local** — cada membro tem seu próprio banco, sem compartilhar dados
- Porta Docker: **5433** (5432 pode estar ocupada por instalação local do PostgreSQL)
- Frontend: **React 19 + Vite + TypeScript** (porta 5173, proxy `/api` → Spring Boot 8080)
- Backend: **Spring Boot 4.0.5 + JPA/Hibernate 7.2.7**

---

## Resumo por integrante

| Integrante | Funcionalidades | Padrão(ões) | Tarefas JPA + Web |
|---|---|---|---|
| Irvin | F4 — Grade de Exibição · F7 — Catálogo de Filmes | Proxy (2 contextos distintos) | 4 tarefas (2 JPA + 2 web) |
| Fabiana | F5 — Bomboniere · F6 — Check-in | Observer | 4 tarefas (2 JPA + 2 web) |
| Amanda | F3 — Fidelidade · F8 — Fechamento de Caixa | Iterator + Template Method | 4 tarefas (2 JPA + 2 web) |
| Julia | F1 — Compra de Ingresso · F2 — Explorar Programação | Strategy + Decorator | 4 tarefas (2 JPA + 2 web) |

**Total de padrões distintos: 6** (Proxy, Observer, Iterator, Template Method, Strategy, Decorator) — atende ao mínimo exigido.

> **Nota sobre Irvin e o Proxy:** O padrão Proxy é aplicado em dois contextos funcionalmente diferentes — F4 controla acesso ao repositório de sessões para bloquear conflito de horário; F7 protege a operação de remoção de filme verificando sessões futuras. São dois problemas diferentes resolvidos com o mesmo padrão estrutural. Isso conta como 1 padrão único implementado, não dois.

---

## Decisão Arquitetural Crítica: Onde colocar `@Entity`

**Esta é a decisão mais importante antes de qualquer integrante começar a codificar.**

O projeto foi construído com Arquitetura Limpa: o módulo `domain-*` é Java puro, **sem nenhuma dependência de framework**. Isso foi avaliado na 1.ª entrega. Adicionar `@Entity` diretamente em `Filme.java` ou `Sessao.java` violaria essa arquitetura e potencialmente perderia pontos na avaliação.

### A solução correta: Port/Adapter no módulo `infrastructure`

O padrão já existe implicitamente no projeto: o domínio define interfaces de repositório (as **portas**), e o `infrastructure` fornece as implementações (os **adaptadores**).

**O que existe no domínio:**
```java
// domain-backoffice — interface pura, sem framework
public interface FilmeRepository {
    void salvar(Filme filme);
    Optional<Filme> buscarPorId(UUID id);
    List<Filme> listarTodos();
    void remover(UUID id);
}
```

**O que você cria no infrastructure para cada entidade:**

```
infrastructure/src/main/java/br/com/cinema/frame/infrastructure/
├── grade/
│   ├── FilmeJpa.java               ← @Entity que replica os campos de Filme
│   ├── FilmeJpaRepository.java     ← extends JpaRepository<FilmeJpa, UUID>
│   └── FilmeRepositoryAdapter.java ← @Repository, implements FilmeRepository do domínio
├── sala/
│   ├── SalaJpa.java
│   ├── SalaJpaRepository.java
│   └── SalaRepositoryAdapter.java
└── ...
```

**Exemplo completo — Filme:**

```java
// infrastructure/.../grade/FilmeJpa.java
@Entity
@Table(name = "filmes")
public class FilmeJpa {

    @Id
    private UUID id;
    private String titulo;
    private long duracaoSegundos;
    private String classificacaoIndicativa;
    private String genero;
    private String trailerURL;
    private boolean ativo;

    public FilmeJpa() {} // JPA exige construtor sem argumentos

    // Converte domínio → JPA
    public static FilmeJpa fromDomain(Filme f) {
        FilmeJpa e = new FilmeJpa();
        e.id = f.getId();
        e.titulo = f.getTitulo();
        e.duracaoSegundos = f.getDuracao().getSeconds();
        e.classificacaoIndicativa = f.getClassificacaoIndicativa().name();
        e.genero = f.getGenero().name();
        e.trailerURL = f.getTrailerURL();
        e.ativo = f.isAtivo();
        return e;
    }

    // Converte JPA → domínio
    public Filme toDomain() {
        Filme f = new Filme(titulo,
            Duration.ofSeconds(duracaoSegundos),
            ClassificacaoIndicativa.valueOf(classificacaoIndicativa),
            GeneroFilme.valueOf(genero));
        if (trailerURL != null) f.atualizar(null, null, null, null, trailerURL);
        if (!ativo) f.desativar();
        return f;
    }

    // getters/setters para JPA
    public UUID getId() { return id; }
    // ...
}
```

```java
// infrastructure/.../grade/FilmeJpaRepository.java
public interface FilmeJpaRepository extends JpaRepository<FilmeJpa, UUID> {
    List<FilmeJpa> findByAtivo(boolean ativo);
}
```

```java
// infrastructure/.../grade/FilmeRepositoryAdapter.java
@Repository
public class FilmeRepositoryAdapter implements FilmeRepository {

    private final FilmeJpaRepository jpa;

    public FilmeRepositoryAdapter(FilmeJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Filme> buscarPorId(UUID id) {
        return jpa.findById(id).map(FilmeJpa::toDomain);
    }

    @Override
    public void salvar(Filme filme) {
        jpa.save(FilmeJpa.fromDomain(filme));
    }

    @Override
    public List<Filme> listarTodos() {
        return jpa.findAll().stream().map(FilmeJpa::toDomain).toList();
    }

    @Override
    public void remover(UUID id) {
        jpa.deleteById(id);
    }
}
```

> **Atenção ao problema do construtor:** A classe `Filme` em domínio valida os campos no construtor e não tem construtor sem argumentos. O `FilmeJpa.toDomain()` chama o construtor com argumentos — isso funciona normalmente. O construtor sem argumentos (`FilmeJpa()`) é somente para o JPA instanciar a entidade de infraestrutura, nunca para o domínio.

---

## Padrões de projeto — definições e como implementar no FRAME

### Proxy *(Irvin)*

**Definição:** Fornece um substituto ou representante de outro objeto para controlar o acesso a ele, podendo adicionar lógica antes ou depois da operação real.

**No FRAME (F4 — conflito de horário entre grades):**

O proxy intercepta o `salvar()` da grade. Antes de persistir, percorre todas as sessões já salvas em **outras grades cujos períodos se sobrepõem** com a grade que está sendo salva, e verifica conflito de sala+horário.

```java
// infrastructure/.../grade/GradeDeExibicaoRepositoryProxy.java
@Repository
public class GradeDeExibicaoRepositoryProxy implements GradeDeExibicaoRepository {

    private final GradeDeExibicaoRepositoryAdapter real;
    private final SessaoJpaRepository sessaoJpa;
    private final GradeJpaRepository gradeJpa;

    @Override
    public void salvar(GradeDeExibicao grade) {
        // Para cada sessão da grade a ser salva, verifica conflito com sessões de outras grades
        for (Sessao novaSessao : grade.getSessoes()) {
            List<SessaoJpa> candidatas = sessaoJpa.findBySalaId(novaSessao.getSala().getId());
            for (SessaoJpa candidataJpa : candidatas) {
                if (candidataJpa.getGradeId().equals(grade.getId())) continue; // mesma grade, pula

                // Só conflita se os períodos das duas grades se sobrepõem
                GradeJpa gradeExistente = gradeJpa.findById(candidataJpa.getGradeId()).orElse(null);
                if (gradeExistente == null) continue;
                boolean periodosSeOverpoem =
                    !grade.getInicio().isAfter(gradeExistente.getFim()) &&
                    !gradeExistente.getInicio().isAfter(grade.getFim());
                if (!periodosSeOverpoem) continue;

                // Agora verifica conflito de horário
                Sessao sessaoExistente = candidataJpa.toDomain(...);
                if (novaSessao.conflitaCom(sessaoExistente))
                    throw new IllegalStateException(
                        "Conflito de horário na sala " + novaSessao.getSala().getNumero()
                    );
            }
        }
        real.salvar(grade);
    }

    // demais métodos delegam ao real sem interceptação
}
```

> **Nota de design:** O proxy está no repositório da **grade** (não da sessão), porque a grade é o agregado raiz — todas as operações de escrita passam por ela. Isso mantém o domínio limpo sem saber de JPA.

**No FRAME (F7 — remoção protegida de filme):**

```java
// infrastructure/.../catalogo/FilmeRepositoryProxy.java
@Repository
public class FilmeRepositoryProxy implements FilmeRepository {

    private final FilmeRepositoryAdapter real;
    private final SessaoJpaRepository sessaoJpa;
    private final GradeJpaRepository gradeJpa;

    @Override
    public void remover(UUID id) {
        // Bloqueia remoção se o filme tem sessões em grades que ainda não terminaram
        LocalDate hoje = LocalDate.now();
        boolean temSessoesFuturas = sessaoJpa.findByFilmeId(id).stream()
            .anyMatch(s -> {
                GradeJpa g = gradeJpa.findById(s.getGradeId()).orElse(null);
                return g != null && !g.getFim().isBefore(hoje);
            });
        if (temSessoesFuturas)
            throw new IllegalStateException("Filme possui sessões futuras e não pode ser removido");
        real.remover(id);
    }

    // demais métodos delegam ao real
}
```

---

### Observer *(Fabiana)*

**Definição:** Define uma dependência um-para-muitos: quando um objeto muda de estado, todos os seus dependentes são notificados automaticamente.

**No FRAME (F5 — alerta de estoque crítico):**
```java
// domain-backoffice/.../bomboniere/EstoqueObserver.java
public interface EstoqueObserver {
    void onEstoqueCritico(Insumo insumo);
}

// domain-backoffice/.../bomboniere/EstoqueSubject.java
public interface EstoqueSubject {
    void adicionarObserver(EstoqueObserver obs);
    void removerObserver(EstoqueObserver obs);
    void notificarObservers(Insumo insumo);
}

// infrastructure/.../bomboniere/AlertaEstoqueObserver.java
@Component
public class AlertaEstoqueObserver implements EstoqueObserver {
    @Override
    public void onEstoqueCritico(Insumo insumo) {
        System.out.println("ALERTA: Estoque crítico — " + insumo.getNome());
        // Futuramente: enviar email, registrar log, acionar dashboard
    }
}
```

O `BombonieresService` implementa `EstoqueSubject` e chama `notificarObservers()` após cada venda quando detecta nível crítico.

---

### Iterator *(Amanda)*

**Definição:** Fornece uma maneira de acessar sequencialmente os elementos de uma coleção sem expor sua representação interna.

**No FRAME (F3 — lançamentos de pontos com expiração):**
```java
// domain-portal/.../fidelidade/LancamentosIterator.java
public class LancamentosIterator implements Iterator<LancamentoPontos> {

    private final List<LancamentoPontos> lancamentos;
    private final LocalDate hoje;
    private int cursor = 0;
    private LancamentoPontos proximo = null;

    public LancamentosIterator(List<LancamentoPontos> lancamentos, LocalDate hoje) {
        this.lancamentos = lancamentos;
        this.hoje = hoje;
        avancar();
    }

    private void avancar() {
        proximo = null;
        while (cursor < lancamentos.size()) {
            LancamentoPontos l = lancamentos.get(cursor++);
            if (!l.isExpirado() && !l.getValidade().isBefore(hoje)) {
                proximo = l;
                break;
            }
        }
    }

    @Override public boolean hasNext() { return proximo != null; }

    @Override public LancamentoPontos next() {
        LancamentoPontos atual = proximo;
        avancar();
        return atual;
    }
}
```

O `FidelidadeService` usa esse iterator para percorrer apenas os lançamentos válidos ao calcular o saldo.

---

### Template Method *(Amanda)*

**Definição:** Define o esqueleto de um algoritmo na classe base, delegando alguns passos para subclasses.

**No FRAME (F8 — fechamento de caixa):**
```java
// domain-backoffice/.../caixa/RelatorioFechamento.java
public abstract class RelatorioFechamento {

    // Método template — esqueleto fixo
    public final FechamentoCaixa gerar(LocalDate data) {
        List<Sessao> sessoes = coletarSessoes(data);
        double faturamento = consolidarVendas(sessoes);
        double ocupacao = calcularOcupacao(sessoes);
        String bordero = gerarBordero(sessoes, faturamento);
        return montar(data, faturamento, ocupacao, bordero);
    }

    protected abstract List<Sessao> coletarSessoes(LocalDate data);
    protected abstract double consolidarVendas(List<Sessao> sessoes);
    protected abstract double calcularOcupacao(List<Sessao> sessoes);
    protected abstract String gerarBordero(List<Sessao> sessoes, double total);
    protected abstract FechamentoCaixa montar(LocalDate data, double fat, double ocup, String bordero);
}

// Implementação concreta para relatório diário
public class RelatorioDiario extends RelatorioFechamento {
    // implementa os métodos abstratos para o relatório do dia inteiro
}

// Implementação concreta para relatório por sessão
public class RelatorioPorSessao extends RelatorioFechamento {
    // implementa os métodos abstratos focando em uma sessão específica
}
```

---

### Strategy *(Julia)*

**Definição:** Define uma família de algoritmos intercambiáveis. O cliente escolhe a estratégia em tempo de execução.

**No FRAME (F1 — motor de promoções):**
```java
// domain-portal/.../promocao/DescontoStrategy.java
public interface DescontoStrategy {
    AplicacaoDeDesconto aplicar(double valorTotal, int quantidadeIngressos);
    TipoPromocao getTipo();
}

public class DescontoLeve2Pague1 implements DescontoStrategy {
    @Override
    public AplicacaoDeDesconto aplicar(double valorTotal, int qtd) {
        double desconto = (qtd >= 2) ? valorTotal / qtd : 0;
        return new AplicacaoDeDesconto(valorTotal, desconto, valorTotal - desconto);
    }
    @Override public TipoPromocao getTipo() { return TipoPromocao.LEVE2_PAGUE1; }
}

public class DescontoParceriaCartao implements DescontoStrategy {
    @Override
    public AplicacaoDeDesconto aplicar(double valorTotal, int qtd) {
        double desconto = valorTotal * 0.15;
        return new AplicacaoDeDesconto(valorTotal, desconto, valorTotal - desconto);
    }
    @Override public TipoPromocao getTipo() { return TipoPromocao.PARCERIA_CARTAO; }
}

public class DescontoEstudante implements DescontoStrategy {
    @Override
    public AplicacaoDeDesconto aplicar(double valorTotal, int qtd) {
        double desconto = valorTotal * 0.50;
        return new AplicacaoDeDesconto(valorTotal, desconto, valorTotal - desconto);
    }
    @Override public TipoPromocao getTipo() { return TipoPromocao.DESCONTO_ESTUDANTE; }
}
```

O `MotorDePromocoes` recebe uma lista de estratégias e seleciona a correta pelo tipo do cupom aplicado.

---

### Decorator *(Julia)*

**Definição:** Anexa responsabilidades adicionais a um objeto de forma dinâmica, como alternativa à herança.

**No FRAME (F2 — listagem com recomendação):**
```java
// domain-portal/.../programacao/ProgramacaoService.java (interface)
public interface ProgramacaoService {
    List<Sessao> listarSessoesFuturas(LocalDateTime agora, GeneroFilme genero);
}

// Implementação base
public class ProgramacaoServiceImpl implements ProgramacaoService {
    @Override
    public List<Sessao> listarSessoesFuturas(LocalDateTime agora, GeneroFilme genero) {
        // consulta repositório por sessões futuras com filtro de gênero
    }
}

// Decorator que adiciona recomendação personalizada
public class ProgramacaoComRecomendacaoDecorator implements ProgramacaoService {

    private final ProgramacaoService base;
    private final MotorDeRecomendacao recomendacao;
    private final UUID clienteId;

    public ProgramacaoComRecomendacaoDecorator(ProgramacaoService base,
                                                MotorDeRecomendacao recomendacao,
                                                UUID clienteId) {
        this.base = base;
        this.recomendacao = recomendacao;
        this.clienteId = clienteId;
    }

    @Override
    public List<Sessao> listarSessoesFuturas(LocalDateTime agora, GeneroFilme genero) {
        List<Sessao> base = this.base.listarSessoesFuturas(agora, genero);
        return recomendacao.ordenarPorAfinidade(clienteId, base); // reordena sem alterar o serviço base
    }
}
```

---

## Tarefas detalhadas por integrante

---

### Irvin — F4 (Grade de Exibição) + F7 (Catálogo de Filmes)

#### [F4] JPA — Grade de Exibição ✅ CONCLUÍDO

Módulo `infrastructure`:

| Entidade de domínio | Classe JPA | Repositório JPA | Adaptador |
|---|---|---|---|
| `Filme` | `FilmeJpa` | `FilmeJpaRepository` | `FilmeRepositoryAdapter` |
| `Sala` | `SalaJpa` | `SalaJpaRepository` | `SalaRepositoryAdapter` |
| `Sessao` | `SessaoJpa` (campo `inicio` como `LocalTime` com `@JdbcTypeCode(SqlTypes.TIME)`) | `SessaoJpaRepository` | — (gerenciada via `GradeJpa`) |
| `GradeDeExibicao` | `GradeJpa` | `GradeJpaRepository` | `GradeDeExibicaoRepositoryProxy` ← **Proxy aqui** |

**Comportamento da sessão recorrente:** `Sessao.inicio` é `LocalTime` (horário diário). A `GradeDeExibicao` tem datas `inicio`/`fim`. Uma sessão das 20:00 em uma grade de 01/06 a 30/06 ocorre todos os dias às 20:00 durante junho. O portal filtra as sessões cujas grades estão ativas hoje e cujo horário ainda não passou.

**Padrão Proxy — F4:** O `GradeDeExibicaoRepositoryProxy` intercepta `salvar()` e verifica conflitos de sala+horário entre grades com períodos sobrepostos.

**Validações implementadas em `GradeDeExibicao`:**
- `inicio` não pode ser data passada (construtor valida)
- `removerSessao()` bloqueia remoção de sessão já iniciada
- `GradeService.removerGrade()` bloqueia remoção se alguma sessão de hoje já iniciou

#### [F4] Web — Tela de Grade ✅ CONCLUÍDO

`presentation-backend`:
- `GET /api/grades` — lista todas as grades com sessões
- `POST /api/grades` — cria grade; rejeita data passada com 400
- `POST /api/grades/{gradeId}/sessoes` — adiciona sessão diária; conflito de horário retorna 409
- `DELETE /api/grades/{gradeId}/sessoes/{sessaoId}` — remove sessão futura
- `DELETE /api/grades/{gradeId}` — remove grade (bloqueado se sessão hoje já iniciou)

`presentation-frontend` (`GradePage.tsx`):
- Formulário de criação com `min` nos campos de data (não permite passado)
- Formulário de adição de sessão diária com seleção de grade, filme, sala e horário
- Lista de grades com tabela de sessões mostrando "Início (diário)" e "Sala livre às" (`getFimComIntervalo()`)
- Botão "Remover grade" com confirmação
- Botão "Remover" por sessão individual com confirmação

---

#### [F7] JPA — Catálogo de Filmes ✅ CONCLUÍDO

Reusa `FilmeJpa`/`FilmeJpaRepository` criados em F4. Acrescentado:

- `FilmeRepositoryProxy` — intercepta `remover()` e bloqueia se há sessões em grades que ainda não terminaram

**Padrão Proxy — F7:** O `FilmeRepositoryProxy` verifica, via `SessaoJpaRepository` e `GradeJpaRepository`, se há sessões do filme em grades com `fim >= hoje`. Se houver, lança `IllegalStateException` (retornado como 409).

#### [F7] Web — Catálogo de Filmes ✅ CONCLUÍDO

`presentation-backend`:
- `GET /api/filmes` — lista todos os filmes (ativos e inativos)
- `POST /api/filmes` — cadastra novo filme
- `PUT /api/filmes/{id}` — atualiza dados e/ou trailer URL
- `PATCH /api/filmes/{id}/ativar` · `/desativar` — ativa ou desativa
- `DELETE /api/filmes/{id}` — remoção protegida pelo Proxy (409 se há sessões futuras)
- `GET /api/salas` · `POST /api/salas` — CRUD de salas

`presentation-frontend` (`FilmesPage.tsx`, `SalasPage.tsx`):
- Listagem de filmes com badges Ativo/Inativo
- Botões de ativar, desativar e remover com feedback de bloqueio (409 exibe mensagem)
- Formulário de cadastro com título, duração, classificação, gênero e URL de trailer

---

### Fabiana — F5 (Bomboniere) + F6 (Check-in)

#### [F5] JPA — Bomboniere

| Entidade de domínio | Classe JPA | Repositório JPA | Adaptador |
|---|---|---|---|
| `ProdutoDaBomboniere` | `ProdutoJpa` | `ProdutoJpaRepository` | `ProdutoRepositoryAdapter` |
| `Insumo` | `InsumoJpa` | `InsumoJpaRepository` | `InsumoRepositoryAdapter` |
| `ItemDeReceita` | mapeado como `@ElementCollection` dentro de `ProdutoJpa` | — | — |
| `MovimentacaoEstoque` | `MovimentacaoJpa` | `MovimentacaoJpaRepository` | `MovimentacaoRepositoryAdapter` |

**Padrão Observer — F5:** O `BombonieresService` implementa `EstoqueSubject`. Após cada venda, verifica nível crítico e chama `notificarObservers()`. O `AlertaEstoqueObserver` registrado recebe a notificação.

#### [F5] Web — Painel de Estoque

- `GET /api/bomboniere/produtos` — lista produtos com estoque atual
- `GET /api/bomboniere/insumos` — lista insumos com flag `estoqueCritico`
- `POST /api/bomboniere/produtos` — cadastra produto com receita
- `POST /api/bomboniere/venda/{produtoId}` — realiza venda e retorna alertas se houver
- `POST /api/bomboniere/insumos/{id}/repor` — repõe estoque

Frontend: painel com alertas visuais (vermelho) para insumos em nível crítico.

---

#### [F6] JPA — Check-in

| Entidade de domínio | Classe JPA | Repositório JPA | Adaptador |
|---|---|---|---|
| `RegistroDeEntrada` | `CheckInJpa` | `CheckInJpaRepository` | `CheckInRepositoryAdapter` |
| `Ingresso` (backoffice) | `IngressoJpa` | `IngressoJpaRepository` | `IngressoRepositoryAdapter` |

**Padrão Observer — F6:** O `CheckInService`, ao autorizar uma entrada, publica o evento. Um `OcupacaoObserver` atualiza o contador de ocupação da sessão em tempo real.

#### [F6] Web — Leitura de QR Code

- `POST /api/checkin` — corpo `{ "qrCode": "...", "sessaoId": "..." }` — retorna status `AUTORIZADO`, `INVALIDO`, `JA_UTILIZADO` ou `FORA_DA_JANELA`

Frontend: tela de leitura de código com feedback visual colorido por status.

---

### Amanda — F3 (Fidelidade) + F8 (Fechamento de Caixa)

#### [F3] JPA — Programa de Fidelidade

| Entidade de domínio | Classe JPA | Repositório JPA | Adaptador |
|---|---|---|---|
| `PontosCliente` | `PontosClienteJpa` | `PontosClienteJpaRepository` | `FidelidadeRepositoryAdapter` |
| `LancamentoPontos` | `LancamentoJpa` | (filho de PontosCliente — `@OneToMany`) | — |
| `Beneficio` | `BeneficioJpa` | `BeneficioJpaRepository` | `BeneficioRepositoryAdapter` |
| `RegistroResgate` | `ResgateJpa` | `ResgateJpaRepository` | `ResgateRepositoryAdapter` |

**Padrão Iterator — F3:** `PontosCliente` expõe um método `iteradorDeLancamentosAtivos(LocalDate hoje)` que retorna um `LancamentosIterator`, filtrando pontos expirados sem expor a lista interna.

#### [F3] Web — Extrato de Fidelidade

- `GET /api/fidelidade/{clienteId}/saldo` — saldo atual descontando expirados
- `GET /api/fidelidade/{clienteId}/extrato` — lista de lançamentos com status
- `GET /api/fidelidade/{clienteId}/beneficios?data=YYYY-MM-DD` — benefícios disponíveis no dia
- `POST /api/fidelidade/{clienteId}/resgatar/{beneficioId}` — resgata benefício

Frontend: tela de extrato com lançamentos coloridos (ativo / expirado) + botão de resgate.

---

#### [F8] JPA — Fechamento de Caixa

| Entidade de domínio | Classe JPA | Repositório JPA | Adaptador |
|---|---|---|---|
| `FechamentoCaixa` | `FechamentoCaixaJpa` | `FechamentoCaixaJpaRepository` | `CaixaRepositoryAdapter` |
| `VendaDia` | mapeado como `@Embedded` ou `@ElementCollection` | — | — |

**Padrão Template Method — F8:** `RelatorioFechamento` é a classe abstrata. `RelatorioDiario` e `RelatorioPorSessao` são as subclasses concretas.

#### [F8] Web — Dashboard de Caixa

- `POST /api/caixa/fechar?data=YYYY-MM-DD` — realiza fechamento do dia (erro 409 se duplicado)
- `GET /api/caixa/relatorio?data=YYYY-MM-DD` — retorna bordero com faturamento e ocupação

Frontend: dashboard com tabela de sessões, faturamento projetado vs. realizado e taxa de ocupação.

---

### Julia — F1 (Compra de Ingresso) + F2 (Explorar Programação)

#### [F1] JPA — Pedido e Ingresso ✅ CONCLUÍDO

| Entidade de domínio | Classe JPA | Repositório JPA | Adaptador |
|---|---|---|---|
| `Pedido` | `PedidoJpa` | `PedidoJpaRepository` | `PedidoRepositoryAdapter` |
| `ReservaDeAssento` | `ReservaJpa` | `ReservaJpaRepository` | `ReservaRepositoryAdapter` |
| `Cupom` | `CupomJpa` | `CupomJpaRepository` | `CupomRepositoryAdapter` |
| `Ingresso` (portal) | `IngressoJpa` | `IngressoJpaRepository` | `IngressoRepositoryAdapter` |

**Padrão Strategy — F1:** O `MotorDePromocoes` recebe `List<DescontoStrategy>` via construtor e seleciona a estratégia correta em runtime pelo `TipoPromocao` do cupom. Implementações: `DescontoLeve2Pague1`, `DescontoParceriaCartao`, `DescontoEstudante`. Bean registrado em `PedidoConfig` e injetado no `PedidoController`.

#### [F1] Web — Fluxo de Compra ✅ CONCLUÍDO

- `GET /api/programacao/sessoes?data=YYYY-MM-DD` — sessões disponíveis com preço dinâmico por tipo de sala
- `POST /api/reserva` — reserva assento (10 min de expiração, filtrado por data de ocorrência)
- `POST /api/pedido` — inicia pedido vinculado ao cliente
- `POST /api/pedido/{id}/ingresso` — adiciona ingresso com validação de classificação etária
- `POST /api/pedido/{id}/cupom` — aplica cupom via Strategy (`MotorDePromocoes`)
- `POST /api/pedido/{id}/finalizar` — gera QR Code por ingresso + Voucher de bomboniere

Frontend (`CompraPage.tsx`): fluxo multi-etapa (sessão → ingresso → assento → bomboniere → cupom → confirmação → sucesso) com barra de progresso, mapa de assentos, imagens automáticas via TMDB.

#### [F1] Use Cases — Compra ✅ CONCLUÍDO

- `IniciarPedidoUseCase` — inicia pedido para uma sessão
- `AplicarCupomUseCase` — aplica desconto via `MotorDePromocoes` (Strategy)
- `FinalizarPedidoUseCase` — finaliza e gera QR Codes

---

#### [F2] JPA — Programação e Recomendação ✅ CONCLUÍDO

| Entidade de domínio | Classe JPA | Repositório JPA | Adaptador |
|---|---|---|---|
| `HistoricoDeCompras` | `HistoricoJpa` | `HistoricoJpaRepository` | `HistoricoRepositoryAdapter` |
| `FilmeFavoritado` | `FavoritoJpa` | `FavoritoJpaRepository` | `FavoritoRepositoryAdapter` |
| `FilmeSugerido` | — (derivado do catálogo ativo) | — | `FilmeSugeridoRepositoryAdapter` |

**Padrão Decorator — F2:** `ProgramacaoComRecomendacaoDecorator` envolve `ProgramacaoService` (base) e adiciona recomendação personalizada via `MotorDeRecomendacao` transparentemente. O controller usa o decorado quando há `clienteId` na requisição, ou o serviço base caso contrário.

#### [F2] Web — Catálogo do Cliente ✅ CONCLUÍDO

- `GET /api/programacao/filmes?clienteId=&genero=&classificacao=&ordenar=` — lista com filtros, recomendação (Decorator) e ordenação por popularidade
- `GET /api/programacao/filmes/{id}` — detalhes do filme com trailer e sessões do dia
- `POST /api/notificacao/favoritar` — favorita filme e registra para notificação
- `GET /api/notificacao/favoritos/{clienteId}` — lista filmes favoritados
- `DELETE /api/notificacao/favoritos/{clienteId}/{filmeId}` — remove favorito

Frontend (`ProgramacaoPage.tsx`): grid de filmes com filtros de data, gênero e classificação, badges de classificação etária, seção "Para você" com recomendados, atualização automática de recomendações a cada 10s.

#### [F2] Use Cases — Programação ✅ CONCLUÍDO

- `ListarFilmesUseCase` — lista sessões por data (hoje ou futura)
- `FavoritarFilmeUseCase` — favorita filme via `NotificacaoService`

#### Integração externa — TMDB API ✅ IMPLEMENTADO

Integração com a **The Movie Database API (TMDB)** para busca automática de imagens dos filmes cadastrados no sistema, sem necessidade de upload manual.

- `src/lib/tmdb.ts` — função `fetchMovieImages(titulo)` que busca pôster (`w342`) e backdrop (`w1280`) pelo título do filme via `api.themoviedb.org/3/search/movie`
- `src/hooks/useMoviePoster.ts` — hook React que expõe `posterUrl` e `backdropUrl` para qualquer componente
- Cache automático em `localStorage` com TTL de 7 dias (evita chamadas repetidas)
- Fallback para gradiente de cor caso o filme não seja encontrado
- Token configurado via variável de ambiente `VITE_TMDB_TOKEN` (gitignored)
- Integrado em: `ProgramacaoPage.tsx` (cards e destaque), `CompraPage.tsx` (lista de sessões e cabeçalho do fluxo), `PortalHomePage.tsx` (banner cinematográfico com backdrop)

---

## Infraestrutura compartilhada (todos — fazer juntos antes de começar)

- Configuração do Spring Boot: DataSource, CORS, tratamento global de erros (`@ControllerAdvice`)
- Schema do banco via `ddl-auto` (sem criação manual de tabelas)
- Estrutura base da API REST (convenção de URLs e formato de resposta)
- Estrutura base do frontend (roteamento, layout com menu de navegação)

---

# Guia Completo de Implementação da 2.ª Entrega

Este guia define a ordem exata de execução. **Não pule fases.** Cada fase depende da anterior.

---

## Fase 0 — Preparação do ambiente ✅ CONCLUÍDA

> **Esta fase foi concluída por Irvin em 2026-05-20. Os itens abaixo são histórico — não é necessário refazer nada.**

### ~~0.1~~ ✅ Driver PostgreSQL adicionado ao `infrastructure/pom.xml`
### ~~0.2~~ ✅ `application.properties` criado com JPA + porta 8080 + import de credenciais locais
### ~~0.3~~ ✅ `FrameApplication.java` criado com CORS configurado para React (porta 5173)
### ~~0.4~~ ✅ Stack decidida: React 19 + Vite + TypeScript (estrutura criada em `presentation-frontend/`)
### ~~0.5~~ ✅ Banco Neon PostgreSQL conectado e verificado (`HikariPool-1 - Start completed`)

---

### O que cada membro precisa fazer UMA VEZ após dar `git pull`

**1. Instalar Docker Desktop** (se ainda não tiver): https://www.docker.com/products/docker-desktop

**2. Criar o banco PostgreSQL local** (rodar uma vez só no terminal):
```bash
docker run --name frame-db \
  -e POSTGRES_PASSWORD=frame \
  -e POSTGRES_DB=framedb \
  -p 5433:5432 \
  -v frame-db-data:/var/lib/postgresql/data \
  -d postgres:17
```
> O `-v frame-db-data:/var/lib/postgresql/data` garante que os dados ficam salvos mesmo se o container for parado. Para iniciar o banco nos próximos dias: `docker start frame-db`

**3. Criar o arquivo de credenciais locais** (nunca vai ao GitHub):

Copiar o arquivo exemplo e renomear:
```
presentation-backend/src/main/resources/application-local.properties
```
Conteúdo (igual para todos — cada um tem seu banco local):
```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/framedb
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=postgres
spring.datasource.password=frame
```

**4. Instalar dependências do frontend:**
```bash
cd presentation-frontend
npm install
```

**5. Verificar que o projeto compila:**
```bash
# Na raiz do projeto (use 'mvn', não './mvnw' — não há Maven wrapper)
mvn install -DskipTests
```
Esperado: `BUILD SUCCESS` com 8 módulos.

**6. Rodar o backend:**
```bash
# Passo 1 — na raiz do projeto
mvn install -DskipTests
# Passo 2 — entrar no módulo e rodar
cd presentation-backend
mvn spring-boot:run
```

**7. Rodar o frontend (em outro terminal):**
```bash
cd presentation-frontend
npm run dev
```
Acesse: `http://localhost:5173`

---

---

## Fase 1 — Persistência JPA

Ordem sugerida de implementação:

**1. Criar as classes `@Entity` no módulo `infrastructure`**

Estrutura de pacotes a adotar:
```
infrastructure/src/main/java/br/com/cinema/frame/infrastructure/
├── {área}/
│   ├── {Entidade}Jpa.java
│   ├── {Entidade}JpaRepository.java
│   └── {Entidade}RepositoryAdapter.java (ou Proxy, se for o padrão)
```

**2. Criar os adaptadores (implementam as interfaces do domínio)**

Cada adaptador recebe o repositório JPA via construtor e faz o mapeamento `toDomain()` / `fromDomain()`.

**3. Anotar os adaptadores com `@Repository`**

O Spring vai injetar automaticamente o adaptador onde o domínio espera a interface de repositório.

**4. Testar via H2 Console**

Com a aplicação rodando (`mvn spring-boot:run` no módulo `presentation-backend`):
- Abrir `http://localhost:8080/h2-console`
- URL JDBC: `jdbc:h2:mem:framedb`
- Verificar se as tabelas foram criadas corretamente

**5. Fazer commit da Fase 1**

```bash
git add .
git commit -m "feat(jpa): mapeamento JPA para [área] — [seu nome]"
git push origin main
```

---

## Fase 2 — Padrão de projeto (junto com a Fase 1)

O padrão de projeto deve ser implementado **durante** a Fase 1, não depois, porque ele faz parte da camada de persistência/domínio.

**Proxy (Irvin):** O Proxy substitui o adaptador simples para `Sessao` (F4) e para `Filme` (F7). Registre-o como `@Repository` no lugar do adaptador simples.

**Observer (Fabiana):** ✅ Implementado. `EstoqueSubject` e `EstoqueObserver` ficam no domínio. `AlertaEstoqueObserver` fica na infrastructure. `BombonieresService` implementa `EstoqueSubject` e recebe o observer via `BomboniereConfig` (bean Spring). A notificação é disparada em `venderInterno()` após detectar estoque crítico.

**Iterator (Amanda):** O `LancamentosIterator` fica no domínio (sem dependência de framework). O `FidelidadeService` o usa ao calcular saldo.

**Template Method (Amanda):** `RelatorioFechamento` (abstrata) fica no domínio. `RelatorioDiario` e `RelatorioPorSessao` também ficam no domínio. O `CaixaService` instancia a subclasse correta conforme o tipo solicitado.

**Strategy (Julia):** As interfaces e implementações de desconto ficam no domínio. O `MotorDePromocoes` recebe a lista de estratégias — injete via construtor.

**Decorator (Julia):** A interface `ProgramacaoService` fica no domínio. `ProgramacaoServiceImpl` e `ProgramacaoComRecomendacaoDecorator` ficam no domínio. O controller escolhe se usa o serviço base ou o decorado conforme há `clienteId` na requisição.

---

## Fase 3 — Camada de aplicação

O módulo `application` está vazio. Antes de criar controllers, crie os casos de uso aqui:

```
application/src/main/java/br/com/cinema/frame/application/
├── grade/
│   ├── AdicionarSessaoUseCase.java
│   └── RemoverSessaoUseCase.java
├── catalogo/
│   ├── CadastrarFilmeUseCase.java
│   └── RemoverFilmeUseCase.java
└── ...
```

**Exemplo de Use Case:**

```java
// application/.../catalogo/CadastrarFilmeUseCase.java
public class CadastrarFilmeUseCase {

    private final FilmeRepository filmeRepository;

    public CadastrarFilmeUseCase(FilmeRepository filmeRepository) {
        this.filmeRepository = filmeRepository;
    }

    public Filme executar(String titulo, Duration duracao,
                          ClassificacaoIndicativa classificacao, GeneroFilme genero) {
        Filme filme = new Filme(titulo, duracao, classificacao, genero);
        filmeRepository.salvar(filme);
        return filme;
    }
}
```

Os Use Cases recebem as dependências via construtor e são instanciados pelo Spring como `@Service` ou via `@Bean` em uma classe de configuração.

> **Por que fazer isso?** O controller não deve chamar o `FilmeService` do domínio diretamente — isso criaria acoplamento da camada de apresentação ao domínio. O Use Case é a fronteira correta.

---

## Fase 4 — Camada web

### 4.1 — Controllers REST (`presentation-backend`)

Cada controller recebe um Use Case via construtor. Nunca injete serviços de domínio diretamente no controller.

```java
// presentation-backend/.../CatalogoController.java
@RestController
@RequestMapping("/api/filmes")
public class CatalogoController {

    private final CadastrarFilmeUseCase cadastrar;
    private final RemoverFilmeUseCase remover;
    private final FilmeRepository filmeRepository;

    public CatalogoController(CadastrarFilmeUseCase cadastrar,
                               RemoverFilmeUseCase remover,
                               FilmeRepository filmeRepository) {
        this.cadastrar = cadastrar;
        this.remover = remover;
        this.filmeRepository = filmeRepository;
    }

    @GetMapping
    public List<FilmeResponse> listar() {
        return filmeRepository.listarTodos().stream()
            .map(FilmeResponse::from)
            .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmeResponse cadastrar(@RequestBody CadastrarFilmeRequest req) {
        Filme f = cadastrar.executar(req.titulo(), req.duracao(), req.classificacao(), req.genero());
        return FilmeResponse.from(f);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        try {
            remover.executar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
```

**Tratamento global de erros:**

```java
// presentation-backend/.../GlobalExceptionHandler.java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleConflito(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleValidacao(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(e.getMessage()));
    }
}
```

### 4.2 — Frontend

Com Thymeleaf: criar templates HTML em `presentation-backend/src/main/resources/templates/`.  
Com Vaadin: criar Views Java em `presentation-frontend/src/main/java/`.  
Com Angular: criar componentes em `presentation-frontend/src/app/`.

**Prioridade de telas por funcionalidade:**

| Funcionalidade | Tela mínima aceitável |
|---|---|
| F4 — Grade | Lista de sessões da semana + formulário de criação |
| F7 — Catálogo | Tabela de filmes + ativar/desativar + remover |
| F5 — Bomboniere | Painel de insumos com alerta visual + formulário de venda |
| F6 — Check-in | Campo de QR Code + resposta visual colorida |
| F3 — Fidelidade | Extrato de pontos + lista de benefícios disponíveis |
| F8 — Caixa | Botão de fechar caixa + tabela de relatório |
| F1 — Compra | Seleção de sessão + tipo de ingresso + cupom + confirmar |
| F2 — Programação | Grid de filmes com filtros + recomendados |

---

## Fase 5 — Integração final (todos juntos)

### 5.1 — Integração no main

Cada integrante commita diretamente no `main`. Antes de commitar, verificar:

- [ ] Classes `@Entity` estão no módulo `infrastructure`, não em `domain-*`
- [ ] Padrão de projeto está implementado em código Java real (não só citado)
- [ ] `mvn install -DskipTests` passa sem erros após as alterações

### 5.2 — Testes de integração manuais

Com a aplicação rodando, testar manualmente:
- Cadastrar um filme → verificar que aparece na listagem
- Tentar remover um filme com sessão futura → esperar erro 409
- Criar duas sessões no mesmo horário na mesma sala → esperar erro de conflito
- Fazer venda na bomboniere → verificar baixa de estoque
- Fazer check-in com QR Code inválido → esperar status `INVALIDO`
- Acumular pontos e resgatar benefício → verificar saldo debitado

### ~~5.3~~ ✅ Banco PostgreSQL já configurado

Cada membro usa **PostgreSQL 17 via Docker local** (porta 5433). O `docker-compose.yml` na raiz do projeto sobe o banco com um único comando. O `application-local.properties` (gitignored) aponta para `localhost:5433/framedb`. As tabelas são criadas automaticamente pelo JPA na primeira vez que cada `@Entity` for adicionada.

> **Nota:** A porta é 5433 (não 5432) porque a 5432 pode estar ocupada por uma instalação local do PostgreSQL na máquina de alguns membros.

---

## Checklist final antes da entrega

### Artefatos obrigatórios

- [ ] Código no GitHub com commits identificáveis por integrante
- [ ] 6 padrões de projeto distintos implementados em código Java real
- [ ] JPA funcionando — tabelas criadas automaticamente, dados persistidos
- [ ] Camada web funcionando — pelo menos 1 tela por funcionalidade
- [ ] `mvn clean install` passa (incluindo os testes BDD da 1.ª entrega)
- [ ] README atualizado com instruções de como rodar a aplicação

### Verificação dos padrões (o professor vai olhar o código)

| Padrão | Integrante | Onde procurar no código |
|---|---|---|
| Proxy (conflito de horário entre grades) | Irvin | `infrastructure/.../grade/GradeDeExibicaoRepositoryProxy.java` |
| Proxy (remoção protegida de filme) | Irvin | `infrastructure/.../catalogo/FilmeRepositoryProxy.java` |
| Observer (estoque) | Fabiana | `EstoqueObserver.java` + `AlertaEstoqueObserver.java` |
| Iterator (pontos) | Amanda | `domain-portal/.../fidelidade/LancamentosIterator.java` |
| Template Method (caixa) | Amanda | `domain-backoffice/.../caixa/RelatorioFechamento.java` |
| Strategy (descontos) | Julia | `DescontoStrategy.java` + 3 implementações |
| Decorator (recomendação) | Julia | `ProgramacaoComRecomendacaoDecorator.java` |

---

*F.R.A.M.E — Film Resource & Attendance Management Engine*  
*CESAR School, 2026.1 — Disciplina de Requisitos*
