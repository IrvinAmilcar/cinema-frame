# F.R.A.M.E

> Sistema de gestão cinematográfica completo — do backoffice ao portal do cliente — construído com Domain-Driven Design, arquitetura limpa e testes BDD com Cucumber.

Projeto acadêmico desenvolvido na disciplina de **Requisitos, Projeto de Software e Validação 2026.1** — CESAR School.

---

## Visão Geral

O **F.R.A.M.E** (Film Resource & Attendance Management Engine) é uma aplicação Java modular que cobre toda a operação de um cinema: criação da grade de exibição, venda de ingressos, controle de bomboniere, programa de fidelidade, controle de acesso e portal de autoatendimento do cliente.

A arquitetura segue os princípios de **DDD (Domain-Driven Design)** em seus quatro níveis — preliminar, estratégico, tático e operacional — com separação clara de responsabilidades entre os três subdomínios e suas camadas.

---

## Estrutura de Módulos

```
frame-parent
├── domain-backoffice    ← Core Domain  — regras de negócio do backoffice
├── domain-portal        ← Supporting Domain — regras do portal do cliente
├── domain-shared        ← Shared Kernel — primitivos compartilhados (ClienteId, enums)
├── infrastructure       ← Persistência com Spring Data JPA
├── presentation-backend ← API REST com Spring Boot
└── presentation-frontend← Camada de apresentação web
```

> O módulo `domain-*` não possui nenhuma dependência de framework — apenas Java puro, garantindo que as regras de negócio sejam testáveis de forma isolada e portáveis.

---

## Subdomínios e Bounded Contexts

### BackofficeContext — Core Domain

Núcleo operacional e administrativo do cinema.

| Agregado | Responsabilidade |
|---|---|
| **Grade** | Filmes, salas, sessões e controle de conflitos de horário |
| **Filme** | Catálogo com classificação indicativa e gênero |
| **Sala** | Tipos de sala (Padrão, 3D, IMAX, VIP) |
| **Ingresso** | Emissão por tipo: Inteira, Meia, Convite |
| **Check-in** | Validação de QR Code na entrada da sala |
| **Precificação** | Preço base por tipo de sala + desconto por dia da semana |
| **Classificação** | Validação de idade mínima indicativa |
| **Bomboniere** | Controle de insumos, receitas e notificação de estoque crítico |
| **Caixa** | Geração de borderô com repasse para distribuidora |
| **RBAC** | Controle de permissões por role (Gerente / Operador de Caixa) |
| **Dashboard** | Taxa de ocupação e faturamento projetado vs. realizado |

### PortalContext — Supporting Domain

Portal de autoatendimento do cliente.

| Agregado | Responsabilidade |
|---|---|
| **Cliente** | Cadastro, filmes favoritos e data de nascimento |
| **Reserva** | Reserva temporária de assento com expiração automática (10 min) |
| **Pedido** | Venda casada ingresso + bomboniere, geração de QR Code e Voucher |
| **Promoção** | Motor de cupons: Leve 2 Pague 1, Parceria Cartão, Desconto Estudante |
| **Fidelidade** | Acúmulo e resgate de pontos por valor gasto |
| **Recomendação** | Sugestão de filmes baseada no histórico de gêneros assistidos |
| **Notificação** | Alerta automático de pré-venda para clientes que favoritaram o filme |

### SharedKernelContext — Shared Kernel

Primitivos de domínio compartilhados entre os dois contextos: `ClienteId`, `ClassificacaoIndicativa`, `GeneroFilme`.

---

## Funcionalidades Não Triviais

Uma funcionalidade é considerada **não trivial** quando não se resume a uma operação de leitura e quando possui regras de negócio de complexidade média ou alta. O projeto implementa **14 funcionalidades** distribuídas entre os dois subsistemas.

### F.R.A.M.E — Backoffice

#### 1. Grade de Programação com Validação de Conflito
Ao cadastrar uma sessão, o sistema impede a sobreposição de horários na mesma sala. O cálculo considera **duração do filme + tempo de limpeza (15 min) + tempo de trailers (10 min)**, detectando conflitos antes que a sessão seja persistida.

**Por que não é trivial:** envolve cálculo de intervalos de tempo, validação de disponibilidade de recurso (sala) e lógica de conflito que vai além de um simples cadastro.

---

#### 2. Gestão Dinâmica de Preços por Atributos
O preço de cada sessão é calculado automaticamente com base em duas regras sobrepostas: o **multiplicador do tipo de sala** (Padrão ×1,0 → VIP ×3,0) e o **desconto por dia da semana** (ex.: terça-feira com até 50% de desconto).

**Por que não é trivial:** combina lógica de herança de valores com sobreposição de regras no cálculo final, sem permitir que nenhuma delas seja ignorada.

---

#### 3. Controle de Classificação Indicativa Restrita
O sistema possui um serviço de domínio que valida se uma pessoa com determinada data de nascimento tem idade mínima permitida para um filme com determinada classificação indicativa. A integração ao fluxo de compra barra o checkout quando a idade do cliente é inferior à classificação.

**Por que não é trivial:** combina validação de regra etária com bloqueio transacional em duas etapas (domínio e portal), exigindo lógica de cálculo de idade e integração entre contextos.

---

#### 4. Check-in Digital via QR Code
O funcionário escaneia o ingresso na entrada. O sistema valida se o QR Code pertence àquela sessão específica, se **já foi utilizado** (idempotência) e se a sessão ainda está dentro do horário permitido de entrada.

**Por que não é trivial:** exige validação de integridade referencial, controle de estado (utilizado/não utilizado) e verificação temporal, impedindo o duplo uso do mesmo ingresso.

---

#### 5. Gestão de Inventário de Insumos da Bomboniere
Cada combo vendido dispara uma **baixa automática no estoque de insumos** com base em uma receita (ex.: 1 pipoca grande = −200 g de milho + −1 embalagem). O sistema notifica quando o estoque de qualquer insumo atinge o nível crítico.

**Por que não é trivial:** requer o relacionamento entre produto final e matérias-primas (receita), baixa proporcional com validação de estoque mínimo e geração de notificação de alerta.

---

#### 6. Fechamento de Caixa e Borderô por Sessão
Ao fechar o caixa de uma sessão, o sistema consolida todas as vendas (inteira, meia, convite) e gera o **Borderô**, que calcula o repasse de 50% do arrecadado para a distribuidora do filme e os 50% retidos pelo cinema.

**Por que não é trivial:** envolve agregação de dados financeiros heterogêneos, cálculo percentual preciso e geração de relatório contábil estruturado por sessão.

---

### Portal do Cliente

#### 7. Reserva Temporária de Assentos (Seat Locking)
Ao selecionar um assento, ele é bloqueado por **10 minutos**. Se o pagamento não for concluído nesse período, o sistema libera o assento automaticamente para outros usuários. O estado da reserva percorre o ciclo: `RESERVADO → CONFIRMADO / EXPIRADO / CANCELADO`.

**Por que não é trivial:** exige gestão de estados com transições válidas, controle de concorrência para evitar dupla reserva e lógica de expiração automática baseada em tempo.

---

#### 8. Venda Casada de Combos (Ingresso + Bomboniere)
O cliente adiciona ingressos e produtos de bomboniere no mesmo pedido. O sistema gera um **QR Code** único para validação na entrada da sala e um **Voucher** separado para retirada dos produtos no balcão — dois documentos com finalidades distintas a partir de um único fluxo de compra.

**Por que não é trivial:** gerencia múltiplas entidades com tipos de entrega diferentes dentro de uma mesma transação, exigindo lógica de composição de pedido e geração de dois artefatos distintos.

---

#### 9. Motor de Promoções Acoplado (Cupons e Campanhas)
O sistema aplica descontos com base em regras de negócio: **"Leve 2 Pague 1"**, **"Parceria com Cartão"** e **"Desconto Estudante"**. Valida a validade do cupom, verifica se é cumulativo e impede que descontos não cumulativos sejam combinados.

**Por que não é trivial:** implementa um motor de regras de negócio com validação de expiração, controle de cumulatividade e cálculo de subtotais com múltiplos descontos simultâneos.

---

### Funcionalidades Integradas (Núcleo de Domínio)

#### 10. Programa de Fidelidade — Pontuação e Resgate
Cada real gasto gera pontos na conta do cliente (1 ponto/R$). O cliente pode resgatar pontos por ingressos ou produtos, e o sistema valida o **saldo disponível no momento da transação**, impedindo resgates sem cobertura.

**Por que não é trivial:** simula transações de moeda virtual com validação de saldo, operações de crédito/débito e histórico de pontos — equivalente a uma mini-conta corrente dentro do domínio.

---

#### 11. Controle de Permissões Baseado em Roles (RBAC)
O sistema diferencia o que um **Operador de Caixa** pode fazer (vender ingressos) do que um **Gerente** pode fazer (estornar venda, alterar preços, fazer check-in). O acesso é verificado por uma política de domínio antes de qualquer operação sensível.

**Por que não é trivial:** implementa um modelo de segurança baseado em roles com mapeamento estático de permissões, verificação antes de execução e auditoria de ações por perfil.

---

#### 12. Sistema de Recomendação por Perfil
Com base no histórico de gêneros assistidos pelo cliente, o portal exibe **filmes sugeridos** ordenados por afinidade. O motor cruza o histórico de compras com o catálogo de filmes disponíveis, priorizando os gêneros mais frequentes.

**Por que não é trivial:** exige consulta cruzada entre histórico de comportamento e catálogo atual, cálculo de frequência por gênero e ordenação por relevância — sem ser uma simples listagem.

---

#### 13. Notificação Automática de Pré-Venda
Quando a primeira sessão de um filme é aberta no F.R.A.M.E, todos os clientes que **favoritaram** aquele filme recebem uma notificação automática. O sistema registra quais clientes já foram notificados para evitar reenvios.

**Por que não é trivial:** implementa um sistema de eventos com rastreamento de estado (notificado/não notificado), gatilho baseado em ação de outro contexto e controle de idempotência da notificação.

---

#### 14. Dashboard de Taxa de Ocupação em Tempo Real
Painel administrativo que exibe, por sessão, a **porcentagem de ocupação** (ingressos vendidos / capacidade da sala) e o **faturamento projetado vs. realizado**, permitindo ao gerente visualizar a performance comercial do cinema.

**Por que não é trivial:** realiza cálculos matemáticos sobre dados de vendas em tempo real, cruza informações de capacidade de sala, ingressos emitidos e valores arrecadados, gerando métricas compostas por sessão.

---

## Testes BDD

Todo o domínio é coberto por testes comportamentais escritos em **português** com Cucumber + JUnit 5, um arquivo `.feature` por funcionalidade.

```bash
# Executar todos os testes de domínio
mvn test -pl domain-backoffice
mvn test -pl domain-portal
```

```
features/
├── grade/            ← Sessões e validação de conflito de horário
├── precificacao/     ← Preços por tipo de sala e dia da semana
├── classificacao/    ← Classificação indicativa (domínio e compra)
├── checkin/          ← Check-in digital via QR Code
├── bomboniere/       ← Gestão de estoque de insumos
├── caixa/            ← Fechamento de caixa e borderô
├── reserva/          ← Reserva temporária de assentos
├── pedido/           ← Venda casada ingresso + bomboniere
├── promocao/         ← Motor de cupons e descontos
├── fidelidade/       ← Pontuação e resgate de pontos
├── rbac/             ← Controle de acesso por role
├── recomendacao/     ← Sugestões por perfil de gênero
├── notificacao/      ← Pré-venda automática
└── dashboard/        ← Taxa de ocupação por sessão
```

---

## Arquitetura

O projeto segue **Arquitetura Limpa (Clean Architecture)** com módulos Maven separados por camada:

```
┌─────────────────────────────────────────────────────┐
│   presentation-backend (REST)                       │
│   presentation-frontend (Web)                       │
└────────────────────────┬────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────┐
│               infrastructure                        │  ← JPA, repositórios
└────────────────────────┬────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────┐
│                  domain-backoffice                  │  ← Core Domain
│                  domain-portal                      │  ← Supporting Domain
│                  domain-shared                      │  ← Shared Kernel
└─────────────────────────────────────────────────────┘
```

**Regra de dependência:** as camadas externas dependem das internas. O domínio não conhece framework algum.

---

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework Web | Spring Boot |
| Persistência | Spring Data JPA |
| Testes BDD | Cucumber 7 + JUnit 5 |
| Build | Maven (multi-module) |
| Modelagem | Context Mapper (CML) |

---

## Como Rodar

**Pré-requisitos:** Java 17+ e Maven 3.8+

```bash
# Build completo
mvn clean install

# Subir a aplicação
cd presentation-backend
mvn spring-boot:run
```

---

## Equipe

| Nome | GitHub |
|---|---|
| Amanda Montarroios de Oliveira | Amo@cesar.school |
| Fabiana Coelho de Souza Leão Silveira | Fcsls@cesar.school |
| Irvin Amilcar de F. B. da Silva | Ervinhu.silva@gmail.com |
| Julia Maria Santos Teixeira | Jmst@cesar.school |

**Professor:** Saulo Meira Araujo (`@profsauloaraujo`) — Disciplina de Requisitos, Projeto de Software e Validação · CESAR School

---

*Projeto acadêmico — CESAR School, 2026.1*
