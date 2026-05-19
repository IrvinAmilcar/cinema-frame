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

**Estado atual do código:** A 1.ª entrega está completa — domínio, BDD e CML. A 2.ª entrega ainda não foi implementada. Nenhuma classe tem `@Entity`, nenhum controller existe, nenhum padrão de projeto foi codificado.

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

**No FRAME (F4 — conflito de horário):**
```java
// infrastructure/.../grade/SessaoRepositoryProxy.java
@Repository
public class SessaoRepositoryProxy implements SessaoRepository {

    private final SessaoRepositoryAdapter real;

    @Override
    public void salvar(Sessao novaSessao) {
        List<Sessao> existentes = real.buscarPorSala(novaSessao.getSala().getId());
        for (Sessao s : existentes) {
            if (s.conflitaCom(novaSessao))
                throw new IllegalStateException("Conflito de horário na sala " + novaSessao.getSala().getNumero());
        }
        real.salvar(novaSessao);
    }

    // demais métodos delegam ao real sem interceptação
}
```

**No FRAME (F7 — remoção protegida de filme):**
```java
// infrastructure/.../grade/FilmeRepositoryProxy.java
@Repository
public class FilmeRepositoryProxy implements FilmeRepository {

    private final FilmeRepositoryAdapter real;
    private final SessaoRepository sessaoRepository;

    @Override
    public void remover(UUID id) {
        List<Sessao> futuras = sessaoRepository.buscarSessoesFuturasPorFilme(id, LocalDateTime.now());
        if (!futuras.isEmpty())
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

#### [F4] JPA — Grade de Exibição

Criar no módulo `infrastructure`:

| Entidade de domínio | Classe JPA | Repositório JPA | Adaptador |
|---|---|---|---|
| `Filme` | `FilmeJpa` | `FilmeJpaRepository` | `FilmeRepositoryAdapter` |
| `Sala` | `SalaJpa` | `SalaJpaRepository` | `SalaRepositoryAdapter` |
| `Sessao` | `SessaoJpa` | `SessaoJpaRepository` | `SessaoRepositoryProxy` ← **Proxy aqui** |
| `GradeDeExibicao` | `GradeJpa` | `GradeJpaRepository` | `GradeRepositoryAdapter` |

**Padrão Proxy — F4:** O `SessaoRepositoryProxy` intercepta o método `salvar()` e verifica conflito de horário antes de delegar ao adaptador real.

#### [F4] Web — Tela de Grade Semanal

No módulo `presentation-backend`, criar:
- `GET /api/grade/{data}` — retorna a grade do dia como JSON (lista de sessões com filme + sala + horário)
- `POST /api/grade/{gradeId}/sessoes` — adiciona sessão; se conflito, retorna `409 Conflict` com mensagem clara
- `DELETE /api/grade/{gradeId}/sessoes/{sessaoId}` — remove sessão futura

No módulo `presentation-frontend`:
- Tela com grid semanal de sessões por sala
- Formulário de criação de sessão com feedback visual de conflito

---

#### [F7] JPA — Catálogo de Filmes

Reusa `FilmeJpa`/`FilmeJpaRepository` criados em F4. Acrescentar:

- `FilmeRepositoryProxy` — intercepta `remover()` e bloqueia se há sessões futuras

**Padrão Proxy — F7:** O `FilmeRepositoryProxy` verifica sessões futuras via `SessaoRepository` antes de permitir exclusão.

#### [F7] Web — Catálogo de Filmes

No módulo `presentation-backend`, criar:
- `GET /api/filmes` — lista todos os filmes (ativos e inativos)
- `GET /api/filmes/ativos` — lista apenas filmes ativos
- `POST /api/filmes` — cadastra novo filme
- `PUT /api/filmes/{id}` — atualiza dados e/ou trailer URL
- `PATCH /api/filmes/{id}/ativar` · `/desativar` — ativa ou desativa
- `DELETE /api/filmes/{id}` — remoção protegida pelo Proxy

No módulo `presentation-frontend`:
- Listagem de filmes com badges de status (ativo/inativo)
- Botões de ativar/desativar e remover (com feedback de bloqueio quando há sessões)
- Formulário de cadastro com campo para URL de trailer

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

#### [F1] JPA — Pedido e Ingresso

| Entidade de domínio | Classe JPA | Repositório JPA | Adaptador |
|---|---|---|---|
| `Pedido` | `PedidoJpa` | `PedidoJpaRepository` | `PedidoRepositoryAdapter` |
| `ReservaDeAssento` | `ReservaJpa` | `ReservaJpaRepository` | `ReservaRepositoryAdapter` |
| `Cupom` | `CupomJpa` | `CupomJpaRepository` | `CupomRepositoryAdapter` |
| `Ingresso` (portal) | `IngressoPortalJpa` | `IngressoPortalJpaRepository` | — |

**Padrão Strategy — F1:** O `MotorDePromocoes` injeta a lista de `DescontoStrategy` disponíveis e seleciona a correta com base no `TipoPromocao` do cupom.

#### [F1] Web — Fluxo de Compra

- `GET /api/programacao/sessoes?data=YYYY-MM-DD` — sessões disponíveis
- `POST /api/reserva` — reserva assento (10 min de expiração)
- `POST /api/pedido` — inicia pedido
- `POST /api/pedido/{id}/cupom` — aplica cupom com Strategy
- `POST /api/pedido/{id}/finalizar` — gera QR Code + Voucher

Frontend: fluxo multi-etapa com barra de progresso.

---

#### [F2] JPA — Programação e Recomendação

| Entidade de domínio | Classe JPA | Repositório JPA | Adaptador |
|---|---|---|---|
| `HistoricoDeCompras` | `HistoricoJpa` | `HistoricoJpaRepository` | `HistoricoRepositoryAdapter` |
| `FilmeFavoritado` | `FavoritoJpa` | `FavoritoJpaRepository` | `FavoritoRepositoryAdapter` |
| `FilmeSugerido` | `FilmeSugeridoJpa` | `FilmeSugeridoJpaRepository` | — |

**Padrão Decorator — F2:** `ProgramacaoComRecomendacaoDecorator` envolve `ProgramacaoServiceImpl` e adiciona ordenação por afinidade transparentemente.

#### [F2] Web — Catálogo do Cliente

- `GET /api/programacao/filmes?clienteId=&genero=&classificacao=` — lista com filtros + recomendação
- `GET /api/programacao/filmes/{id}` — detalhes do filme com trailer
- `POST /api/notificacao/favoritar` — favorita filme e registra para notificação

Frontend: grid de filmes com filtros, badges de classificação e seção de recomendados.

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

## Fase 0 — Preparação do ambiente (todos juntos, 1 reunião)

### 0.1 — Adicionar driver do banco no `infrastructure/pom.xml`

Abrir o arquivo [infrastructure/pom.xml](../infrastructure/pom.xml) e adicionar dentro de `<dependencies>`:

```xml
<!-- Banco H2 para desenvolvimento local -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- PostgreSQL para entrega final (deixe comentado por enquanto) -->
<!--
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
-->
```

**Quem faz:** Irvin (já tem mais contexto do projeto). Faz o commit. Todos dão `git pull`.

---

### 0.2 — Criar `application.properties` no módulo `presentation-backend`

Criar o arquivo em:  
`presentation-backend/src/main/resources/application.properties`

```properties
# Banco H2 em memória (desenvolvimento)
spring.datasource.url=jdbc:h2:mem:framedb;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.h2.console.enabled=true

# CORS — permite requisições do frontend local
spring.web.cors.allowed-origins=http://localhost:4200,http://localhost:3000
```

**Quem faz:** Irvin. Faz o commit. Todos dão `git pull`.

---

### 0.3 — Criar classe principal do Spring Boot

Criar em `presentation-backend/src/main/java/br/com/cinema/frame/`:

```java
@SpringBootApplication
public class FrameApplication {
    public static void main(String[] args) {
        SpringApplication.run(FrameApplication.class, args);
    }
}
```

**Quem faz:** Irvin. Commit + push. Todos dão `git pull`.

---

### 0.4 — Decidir tecnologia do frontend

**Opção A — Vaadin (recomendada para quem tem menos experiência com frontend):**
- Tudo em Java — sem HTML/CSS/JS separados
- Adicionar ao `presentation-frontend/pom.xml`:
  ```xml
  <dependency>
      <groupId>com.vaadin</groupId>
      <artifactId>vaadin-spring-boot-starter</artifactId>
      <version>24.4.0</version>
  </dependency>
  ```

**Opção B — Thymeleaf (mais simples, integrado ao Spring Boot):**
- Templates HTML com dados do servidor
- Adicionar ao `presentation-backend/pom.xml`:
  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-thymeleaf</artifactId>
  </dependency>
  ```

**Opção C — Angular (separado do backend, requer build separado):**
- Mais trabalho de configuração, mas mais completo visualmente
- Grupo deve ter experiência prévia com Node.js e TypeScript

> **Recomendação:** Use Thymeleaf se o tempo for curto. Use Vaadin se quiserem ficar 100% em Java. Use Angular somente se já tiverem experiência.

**O grupo decide juntos antes de cada um começar a implementar a camada web.**

---

### 0.5 — Criar branches individuais

Cada integrante cria sua branch a partir da `main` atualizada:

```bash
git checkout main
git pull origin main
git checkout -b feature/irvin-jpa-grade      # Irvin
git checkout -b feature/fabiana-jpa-bomboniere  # Fabiana
git checkout -b feature/amanda-jpa-fidelidade   # Amanda
git checkout -b feature/julia-jpa-pedido        # Julia
```

---

### 0.6 — Verificar que o projeto compila

```bash
mvn clean install -DskipTests
```

Se compilar sem erros, todos estão prontos para começar.

---

## Fase 1 — Persistência JPA (cada um na sua branch)

Ordem sugerida dentro de cada branch:

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
```

---

## Fase 2 — Padrão de projeto (junto com a Fase 1, na mesma branch)

O padrão de projeto deve ser implementado **durante** a Fase 1, não depois, porque ele faz parte da camada de persistência/domínio.

**Proxy (Irvin):** O Proxy substitui o adaptador simples para `Sessao` (F4) e para `Filme` (F7). Registre-o como `@Repository` no lugar do adaptador simples.

**Observer (Fabiana):** A interface `EstoqueObserver` fica no domínio. O `AlertaEstoqueObserver` fica na infrastructure como `@Component`. O `BombonieresService` precisa aceitar uma lista de observers — injete via construtor ou via `@Autowired`.

**Iterator (Amanda):** O `LancamentosIterator` fica no domínio (sem dependência de framework). O `FidelidadeService` o usa ao calcular saldo.

**Template Method (Amanda):** `RelatorioFechamento` (abstrata) fica no domínio. `RelatorioDiario` e `RelatorioPorSessao` também ficam no domínio. O `CaixaService` instancia a subclasse correta conforme o tipo solicitado.

**Strategy (Julia):** As interfaces e implementações de desconto ficam no domínio. O `MotorDePromocoes` recebe a lista de estratégias — injete via construtor.

**Decorator (Julia):** A interface `ProgramacaoService` fica no domínio. `ProgramacaoServiceImpl` e `ProgramacaoComRecomendacaoDecorator` ficam no domínio. O controller escolhe se usa o serviço base ou o decorado conforme há `clienteId` na requisição.

---

## Fase 3 — Camada de aplicação (cada um na sua branch)

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

## Fase 4 — Camada web (cada um na sua branch)

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

### 5.1 — Pull Requests

Cada integrante abre um PR da sua branch para `main`. Um colega revisa e aprova.

Checklist de revisão por PR:
- [ ] Classes `@Entity` estão no módulo `infrastructure`, não em `domain-*`
- [ ] Padrão de projeto está implementado em código Java real (não só citado)
- [ ] Controller não chama serviço de domínio diretamente (vai pelo Use Case)
- [ ] `mvn clean install` passa sem erros após o merge

### 5.2 — Testes de integração manuais

Com a aplicação rodando, testar manualmente:
- Cadastrar um filme → verificar que aparece na listagem
- Tentar remover um filme com sessão futura → esperar erro 409
- Criar duas sessões no mesmo horário na mesma sala → esperar erro de conflito
- Fazer venda na bomboniere → verificar baixa de estoque
- Fazer check-in com QR Code inválido → esperar status `INVALIDO`
- Acumular pontos e resgatar benefício → verificar saldo debitado

### 5.3 — Trocar banco para PostgreSQL antes da entrega

Quando o grupo estiver pronto para a demo ao professor:

1. Instalar PostgreSQL (ou subir via Docker: `docker run -e POSTGRES_PASSWORD=frame -e POSTGRES_DB=frame_db -p 5432:5432 postgres`)
2. Comentar as dependências H2 no `infrastructure/pom.xml` e descomentar PostgreSQL
3. Atualizar `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/frame_db
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=postgres
spring.datasource.password=frame
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.h2.console.enabled=false
```

O código JPA **não muda** — apenas o `application.properties` e o driver.

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
| Proxy (conflito de horário) | Irvin | `infrastructure/.../grade/SessaoRepositoryProxy.java` |
| Proxy (remoção protegida) | Irvin | `infrastructure/.../grade/FilmeRepositoryProxy.java` |
| Observer (estoque) | Fabiana | `EstoqueObserver.java` + `AlertaEstoqueObserver.java` |
| Iterator (pontos) | Amanda | `domain-portal/.../fidelidade/LancamentosIterator.java` |
| Template Method (caixa) | Amanda | `domain-backoffice/.../caixa/RelatorioFechamento.java` |
| Strategy (descontos) | Julia | `DescontoStrategy.java` + 3 implementações |
| Decorator (recomendação) | Julia | `ProgramacaoComRecomendacaoDecorator.java` |

---

*F.R.A.M.E — Film Resource & Attendance Management Engine*  
*CESAR School, 2026.1 — Disciplina de Requisitos*
