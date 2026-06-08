# F.R.A.M.E

> Sistema de gestão cinematográfica completo — do backoffice ao portal do cliente — construído com Domain-Driven Design, Arquitetura Limpa, persistência JPA, API REST e frontend web, com testes BDD em Cucumber.

---

## Visão Geral

O **F.R.A.M.E** (Film Resource & Attendance Management Engine) é uma aplicação Java modular que cobre toda a operação de um cinema: criação da grade de exibição, venda de ingressos, controle de bomboniere, programa de fidelidade, controle de acesso e portal de autoatendimento do cliente.

A arquitetura segue os princípios de **DDD (Domain-Driven Design)** em seus quatro níveis — preliminar, estratégico, tático e operacional — com separação clara de responsabilidades entre os três subdomínios e suas camadas.

> **Para o professor:** as seções [Mapa por Integrante](#mapa-por-integrante) e [Padrões de Projeto — Mapa de Arquivos](#padrões-de-projeto--mapa-de-arquivos) foram criadas para localizar rapidamente, por aluno, os arquivos de cada funcionalidade e — principalmente — os arquivos onde cada padrão de projeto está implementado.

---

## Entregáveis

- **Protótipo do Cliente:** https://laptop-lid-95728362.figma.site
- **Protótipo do Administrador:** https://upbeat-fill-92439671.figma.site
- **Mapa de Histórias do Usuário:** https://cinema-frame-1.avion.io/share/TThewBZaSiEavTsHx
- **Descrição do Domínio:** https://docs.google.com/document/d/13dxD7cTTPYoqs5jBu6f-LrumLTHMguilH8nVoNQUx6s/edit?tab=t.0

---

## Padrões de Projeto — Mapa de Arquivos

São **6 padrões de projeto distintos** implementados (mínimo exigido). Os padrões Proxy e Observer aparecem em dois contextos diferentes cada, mas contam como **um padrão distinto cada**.

| Padrão | Integrante | Arquivos | Camada |
|---|---|---|---|
| **Strategy** | Julia | `DescontoStrategy` + `DescontoLeve2Pague1`, `DescontoParceriaCartao`, `DescontoEstudante` + `MotorDePromocoes` | Domínio |
| **Decorator** | Julia | `ProgramacaoService` (componente) + `ProgramacaoComRecomendacaoDecorator` | Domínio |
| **Iterator** | Amanda | `LancamentosIterator` + `PontosCliente` | Domínio |
| **Template Method** | Amanda | `RelatorioFechamento` + `RelatorioDiario`, `RelatorioPorSessao` | Domínio |
| **Proxy** | Irvin | `FilmeServiceProxy`, `GradeServiceProxy` | Domínio |
| | | `FilmeRepositoryProxy`, `GradeDeExibicaoRepositoryProxy` | Infraestrutura |
| **Observer** | Fabiana | `EstoqueSubject`, `EstoqueObserver`, `AlertaEstoqueObserver` (F5) · `CheckInObserver`, `LotaCargaObserver` (F6) | Domínio |

---

## Mapa por Integrante

Cada integrante é responsável por **2 funcionalidades** (2 tarefas JPA + 2 tarefas web) e por seu(s) padrão(ões) de projeto.

| Integrante | Funcionalidades | Padrão(ões) | Telas (frontend) |
|---|---|---|---|
| **Irvin** | F4 — Grade de Exibição · F7 — Catálogo de Filmes | Proxy | `GradePage`, `CatalogoPage`, `SalasPage` |
| **Fabiana** | F5 — Bomboniere · F6 — Check-in | Observer | `BombonierePage`, `CardapioPage`, `CheckInPage`, `UsuariosPage` |
| **Amanda** | F3 — Fidelidade · F8 — Fechamento de Caixa | Iterator · Template Method | `FidelidadePage`, `FechamentoCaixaPage`, `DashboardPage` |
| **Julia** | F1 — Compra de Ingresso · F2 — Explorar Programação | Strategy · Decorator | `CompraPage`, `ProgramacaoPage`, `PortalHomePage`, `MeusIngressosPage` |

> Os arquivos de cada funcionalidade ficam nos pacotes de mesmo nome em cada camada: domínio (`domain-backoffice` / `domain-portal`), persistência (`infrastructure`) e API (`presentation-backend`). Os arquivos dos padrões estão na tabela acima.

---

## APIs e Integrações Externas

| Integração | Onde é usada | Para quê | Arquivo |
|---|---|---|---|
| **TMDB — The Movie Database API** (`api.themoviedb.org/3` · `image.tmdb.org`) | Frontend | Busca automática de pôsteres e backdrops dos filmes pelo título, com cache de 7 dias em `localStorage`. Token via `VITE_TMDB_TOKEN` (gitignored) | [`lib/tmdb.ts`](presentation-frontend/src/lib/tmdb.ts) · [`hooks/useMoviePoster.ts`](presentation-frontend/src/hooks/useMoviePoster.ts) |
| **qrcode.react** | Frontend | Renderização do QR Code dos ingressos e do voucher de bomboniere na tela de compra | [`CompraPage.tsx`](presentation-frontend/src/pages/CompraPage.tsx) |
| **Chart.js** | Frontend | Gráficos do dashboard de bomboniere/caixa | [`BombonieireChart.tsx`](presentation-frontend/src/pages/BombonieireChart.tsx) |
| **Google Fonts** | Frontend | Tipografia (DM Sans / DM Mono) | telas do portal |

> O QR Code é gerado no backend como identificador único do ingresso ([`QRCode.java`](domain-portal/src/main/java/br/com/cinema/frame/domain/portal/pedido/QRCode.java)) e apenas **renderizado** no frontend pela biblioteca `qrcode.react`.

### API REST interna (Spring Boot)

Toda a comunicação frontend ↔ backend ocorre via REST em `/api/*` (proxy do Vite encaminha para `localhost:8080`):

| Recurso | Endpoints principais |
|---|---|
| Catálogo (F7) | `GET/POST/PUT/DELETE /api/filmes` · `PATCH /api/filmes/{id}/ativar`·`/desativar` |
| Salas (F7) | `GET/POST/PUT/DELETE /api/salas` |
| Grade (F4) | `GET/POST/PUT/DELETE /api/grades` · `POST/PUT/DELETE /api/grades/{id}/sessoes` |
| Bomboniere (F5) | `GET/POST /api/bomboniere/produtos` · `/insumos` · `POST /api/bomboniere/venda` |
| Check-in (F6) | `POST /api/checkin` · RBAC em `/api/funcionarios` |
| Fidelidade (F3) | `GET /api/fidelidade/{clienteId}/saldo`·`/extrato`·`/beneficios` · `POST .../resgatar` |
| Caixa (F8) | `POST /api/caixa/fechar` · `GET /api/caixa/relatorio` |
| Compra (F1) | `POST /api/reserva` · `/api/pedido` · `/api/pedido/{id}/cupom`·`/ingresso`·`/finalizar` |
| Programação (F2) | `GET /api/programacao/sessoes`·`/filmes` · `POST /api/notificacao/favoritar` |

---

## Estrutura de Módulos

```
frame-parent
├── domain-shared        ← Shared Kernel — primitivos compartilhados (ClienteId, enums)
├── domain-backoffice    ← Core Domain  — regras de negócio do backoffice
├── domain-portal        ← Supporting Domain — regras do portal do cliente
├── application          ← Camada de aplicação (casos de uso)
├── infrastructure       ← Persistência com Spring Data JPA + Caching Proxy
├── presentation-frontend← Camada de apresentação web (React + Vite)
└── presentation-backend ← API REST com Spring Boot
```

> Os módulos `domain-*` não possuem nenhuma dependência de framework — apenas Java puro, garantindo regras de negócio testáveis de forma isolada. Os padrões **Strategy, Decorator, Iterator, Template Method e o Protection Proxy** vivem no domínio; o **Caching Proxy** e os adaptadores JPA vivem na `infrastructure`.

---

## Subdomínios e Bounded Contexts

### BackofficeContext — Core Domain

Núcleo operacional e administrativo do cinema.

| Agregado | Responsabilidade |
|---|---|
| **Grade** | Filmes, salas, sessões e controle de conflitos de horário |
| **Filme** | Catálogo com classificação indicativa, gênero, trailer e estado ativo/inativo |
| **Sala** | Tipos de sala (Padrão, 3D, IMAX, VIP) |
| **Ingresso** | Emissão por tipo: Inteira, Meia, Convite |
| **Check-in** | Validação de QR Code na entrada da sala |
| **Precificação** | Preço base por tipo de sala + desconto por dia da semana |
| **Classificação** | Validação de idade mínima indicativa |
| **Bomboniere** | Controle de insumos, receitas e notificação de estoque crítico |
| **Caixa** | Fechamento de caixa com consolidação de vendas e relatórios |
| **RBAC** | Controle de permissões por role (Gerente / Operador de Caixa) |
| **Dashboard** | Taxa de ocupação e faturamento projetado vs. realizado |

### PortalContext — Supporting Domain

Portal de autoatendimento do cliente.

| Agregado | Responsabilidade |
|---|---|
| **Cliente** | Cadastro, filmes favoritos e data de nascimento |
| **Programação** | Consulta de filmes e sessões disponíveis com filtros por gênero e classificação |
| **Reserva** | Reserva temporária de assento com expiração automática (10 min) |
| **Pedido** | Venda casada ingresso + bomboniere, geração de QR Code e Voucher |
| **Promoção** | Motor de cupons: Leve 2 Pague 1, Parceria Cartão, Desconto Estudante |
| **Fidelidade** | Acúmulo e resgate de pontos por valor gasto |
| **Recomendação** | Sugestão de filmes baseada no histórico de gêneros assistidos |
| **Notificação** | Alerta automático de pré-venda para clientes que favoritaram o filme |

### SharedKernelContext — Shared Kernel

Primitivos de domínio compartilhados entre os dois contextos: `ClienteId`, `ClassificacaoIndicativa`, `GeneroFilme`.

---

## Funcionalidades

O projeto implementa **8 funcionalidades** consideradas fortes — cada uma envolve múltiplas regras de negócio, coordenação entre domínios e vai além de operações triviais de leitura ou CRUD simples.

| F | Funcionalidade | Responsável | Padrão | Resumo |
|---|---|---|---|---|
| **F1** | Comprar Ingresso | Julia | Strategy | Jornada completa: sessão → assento → ingresso → produtos → cupom → pagamento → QR Code |
| **F2** | Explorar Programação | Julia | Decorator | Listagem de sessões futuras com filtros, recomendação personalizada e notificação de favoritos |
| **F3** | Fidelidade e Benefícios | Amanda | Iterator | Acúmulo de pontos, expiração automática, resgate de benefícios |
| **F4** | Grade de Exibição | Irvin | Proxy | Sessões diárias recorrentes, conflito de horário entre salas/grades, regras de remoção |
| **F5** | Bomboniere | Fabiana | Observer | Estoque por receita, baixa automática na venda, alerta de nível crítico |
| **F6** | Check-in | Fabiana | Observer | Validação de QR Code, idempotência, janela de acesso, ocupação em tempo real |
| **F7** | Catálogo de Filmes | Irvin | Proxy | CRUD com classificação, trailer, ativar/desativar, remoção protegida |
| **F8** | Fechamento de Caixa | Amanda | Template Method | Consolidação de vendas, faturamento, taxa de ocupação, relatórios por data |

<details>
<summary><strong>Regras de negócio detalhadas por funcionalidade</strong></summary>

### F1 — Comprar Ingresso (Julia)
- Assentos só são selecionáveis se disponíveis — reserva expira em 10 min, impedindo dupla ocupação
- Tipos de ingresso (meia/inteira) com regras de elegibilidade — sem comprovação, o pedido é bloqueado
- Cupons validam validade e cumulatividade — combinações inválidas são rejeitadas
- Pagamento precisa ser aprovado para gerar ingresso
- Ingresso gerado é único e não reutilizável (idempotência no check-in)

### F2 — Explorar Programação (Julia)
- Apenas filmes ativos e com sessões futuras são exibidos
- Filtros por gênero e classificação retornam apenas resultados válidos
- Disponibilidade considera o tempo atual — nenhuma sessão passada é apresentada
- Sugestões ordenadas por afinidade com o histórico de gêneros
- Clientes que favoritaram um filme são notificados quando a primeira sessão é aberta

### F3 — Fidelidade e Benefícios (Amanda)
- Pontos acumulados pelo valor gasto (1 ponto por real)
- Validade de 12 meses — pontos expirados descartados automaticamente na consulta de saldo
- Benefícios exigem mínimo de pontos — resgate bloqueado se saldo insuficiente
- Benefícios podem ter restrição por dia da semana
- Ao resgatar, pontos são debitados corretamente do saldo ativo

### F4 — Grade de Exibição (Irvin)
- Sessão não é criada se houver conflito de horário na mesma sala (inclusive entre grades distintas)
- Criação respeita duração do filme + limpeza (15 min) + trailers (10 min)
- Apenas filmes ativos podem ser adicionados à grade
- Remoção de sessão já iniciada é bloqueada
- Cancelar sessão futura identifica os ingressos que precisam de reembolso

### F5 — Bomboniere (Fabiana)
- Produto só é vendido quando ativo e com estoque > 0
- Venda exige quantidade ≤ estoque disponível
- Toda venda gera movimentação de saída, baixando estoque pela receita (ex.: 1 pipoca = −200 g milho + −1 embalagem)
- Alerta de reposição disparado ao atingir o nível mínimo

### F6 — Check-in (Fabiana)
- Acesso autorizado apenas com QR Code válido correspondente a ingresso existente
- Ingresso deve pertencer à sessão correta
- Sessão deve estar na janela de entrada (30 min ao redor do início)
- Ingresso já utilizado bloqueia a entrada (anti-reuso)
- Sucesso registra check-in e marca o ingresso como utilizado

### F7 — Catálogo de Filmes (Irvin)
- Informações obrigatórias (título, duração, classificação, gênero); título vazio é rejeitado
- Apenas filmes ativos podem ir para a grade
- Filme com sessões futuras não pode ser removido — deve ser desativado (preserva histórico)
- Suporte a URL de trailer

### F8 — Fechamento de Caixa (Amanda)
- Vendas consolidadas por sessão; fechamento duplicado no mesmo dia é impedido
- Faturamento por tipo de ingresso (inteira, meia, convite) e preço da sessão
- Taxa de ocupação = ingressos vendidos / capacidade da sala
- Relatório de dia sem fechamento é rejeitado com mensagem clara
- Dashboard com faturamento projetado vs. realizado por sessão

</details>

---

## Testes BDD

Todo o domínio é coberto por testes comportamentais escritos em **português** com Cucumber + JUnit 5, distribuídos entre os dois módulos de domínio.

```bash
# Executar todos os testes
mvn test
```

### Mapeamento: Funcionalidade → Features

| Funcionalidade | Features BDD |
|---|---|
| F1 — Comprar ingresso | `reserva` · `pedido` · `promocao` · `checkin` · `classificacao_compra` |
| F2 — Explorar programação | `programacao` · `recomendacao` · `notificacao` |
| F3 — Fidelidade e benefícios | `fidelidade` |
| F4 — Grade de exibição | `sessao` · `precificacao` · `dashboard` |
| F5 — Bomboniere | `bomboniere` |
| F6 — Controle de acesso | `checkin` · `classificacao` |
| F7 — Catálogo de filmes | `catalogo` |
| F8 — Fechamento de caixa | `caixa` · `dashboard` · `precificacao` |
| Transversal | `rbac` |

> `checkin`, `classificacao`, `dashboard`, `precificacao` e `rbac` são compartilhados entre mais de uma funcionalidade por natureza transversal.

---

## Arquitetura

O projeto segue **Arquitetura Limpa (Clean Architecture)** com módulos Maven separados por camada:

```
┌─────────────────────────────────────────────────────┐
│   presentation-backend (REST)                       │
│   presentation-frontend (React + Vite)              │
└────────────────────────┬────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────┐
│               infrastructure                        │  ← JPA, adaptadores, Caching Proxy
└────────────────────────┬────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────┐
│                  domain-backoffice                  │  ← Core Domain
│                  domain-portal                      │  ← Supporting Domain
│                  domain-shared                      │  ← Shared Kernel
└─────────────────────────────────────────────────────┘
```

**Regra de dependência:** as camadas externas dependem das internas. O domínio não conhece framework algum. As entidades `@Entity` ficam exclusivamente na `infrastructure` (padrão Port/Adapter), mantendo o domínio puro.

---

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework Web | Spring Boot 4.0.5 |
| Persistência | Spring Data JPA / Hibernate |
| Banco de dados | PostgreSQL 17 (Docker, porta 5433) |
| Testes BDD | Cucumber 7.34 + JUnit 6 + Mockito 5 |
| Build | Maven (multi-module) |
| Frontend | React 19 + Vite 6 + TypeScript 5.6 |
| HTTP / Rotas / QR | axios · react-router-dom 7 · qrcode.react 4 |
| Integração externa | TMDB API (pôsteres) |
| Modelagem | Context Mapper (CML) |

---

## Como Rodar

**Pré-requisitos:** Java 17+, Maven 3.8+, Docker e Node.js 18+

```bash
# 1. Subir o banco PostgreSQL (na raiz do projeto)
docker compose up -d

# 2. Build de todos os módulos (na raiz do projeto)
mvn install -DskipTests

# 3. Subir o backend (em um terminal)
cd presentation-backend
mvn spring-boot:run

# 4. Subir o frontend (em outro terminal)
cd presentation-frontend
npm install
npm run dev
```

Acesse: **http://localhost:5173**

> Cada integrante cria localmente (gitignored) o `presentation-backend/src/main/resources/application-local.properties` (apontando para `localhost:5433/framedb`) e o `presentation-frontend/.env` (com `VITE_TMDB_TOKEN`). Templates `*.example` estão versionados no repositório.

---

## Equipe

| Nome | Contato | Funcionalidades · Padrão |
|---|---|---|
| Amanda Montarroios de Oliveira | Amo@cesar.school | F3 + F8 · Iterator + Template Method |
| Fabiana Coelho de Souza Leão Silveira | Fcsls@cesar.school | F5 + F6 · Observer |
| Irvin Amilcar de F. B. da Silva | Ervinhu.silva@gmail.com | F4 + F7 · Proxy |
| Julia Maria Santos Teixeira | Jmst@cesar.school | F1 + F2 · Strategy + Decorator |

**Professor:** Saulo Meira Araujo (`@profsauloaraujo`) — Disciplina de Requisitos, Projeto de Software e Validação · CESAR School

---

*Projeto acadêmico — CESAR School, 2026.1*
