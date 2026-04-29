# F.R.A.M.E

> Sistema de gestão cinematográfica completo — do backoffice ao portal do cliente — construído com Domain-Driven Design, arquitetura limpa e testes BDD com Cucumber.

---

## Visão Geral

O **F.R.A.M.E** (Film Resource & Attendance Management Engine) é uma aplicação Java modular que cobre toda a operação de um cinema: criação da grade de exibição, venda de ingressos, controle de bomboniere, programa de fidelidade, controle de acesso e portal de autoatendimento do cliente.

A arquitetura segue os princípios de **DDD (Domain-Driven Design)** em seus quatro níveis — preliminar, estratégico, tático e operacional — com separação clara de responsabilidades entre os três subdomínios e suas camadas.

---

## Entregáveis

- **Protótipo do Cliente:**  
  https://laptop-lid-95728362.figma.site

- **Protótipo do Administrador:**  
  https://upbeat-fill-92439671.figma.site

- **Mapa de Histórias do Usuário:**  
  https://cinema-frame-1.avion.io/share/TThewBZaSiEavTsHx

- **Descrição do Domínio:**  
  https://docs.google.com/document/d/13dxD7cTTPYoqs5jBu6f-LrumLTHMguilH8nVoNQUx6s/edit?tab=t.0

---

## Estrutura de Módulos

```
frame-parent
├── domain-shared        ← Shared Kernel — primitivos compartilhados (ClienteId, enums)
├── domain-backoffice    ← Core Domain  — regras de negócio do backoffice
├── domain-portal        ← Supporting Domain — regras do portal do cliente
├── application          ← Camada de aplicação (casos de uso) — módulo scaffold, sem implementação ainda
├── infrastructure       ← Persistência com Spring Data JPA
├── presentation-frontend← Camada de apresentação web
└── presentation-backend ← API REST com Spring Boot
```

> O módulo `domain-*` não possui nenhuma dependência de framework — apenas Java puro, garantindo que as regras de negócio sejam testáveis de forma isolada e portáveis.

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

---

### F1 — Comprar Ingresso
**Responsável:** Julia

**Fluxo:** Visualizar filmes → Selecionar filme → Escolher sessão → Selecionar assentos → Definir tipo de ingresso → Adicionar produtos → Aplicar cupons (opcional) → Realizar pagamento → Gerar ingresso (QR Code)

**Regras de negócio:**
- Assentos só podem ser selecionados se estiverem disponíveis — reserva com expiração automática de 10 minutos impede dupla ocupação
- Tipos de ingresso (meia/inteira) seguem regras de elegibilidade — meia-entrada exige comprovação; sem ela, o pedido é bloqueado
- Cupons possuem validação de validade e regras de cumulatividade — combinações inválidas são rejeitadas antes do desconto ser aplicado
- O pagamento precisa ser aprovado para gerar o ingresso — pagamento recusado encerra o fluxo sem emissão
- O ingresso gerado é único e não pode ser reutilizado — idempotência garantida no check-in

**Por que não é trivial:** É a principal jornada do cliente e envolve múltiplos domínios ao mesmo tempo — sessões, assentos, pagamento, promoções e estoque. Exige coordenação de várias regras simultâneas e garante o principal valor de negócio: a venda.

**Features BDD relacionadas:** `reserva` · `pedido` · `promocao` · `checkin` · `classificacao_compra`

---

### F2 — Explorar Programação de Filmes
**Responsável:** Julia

**Fluxo:** Acessar sessões disponíveis → Aplicar filtros (gênero / classificação indicativa) → Ordenar por popularidade → Selecionar sessão → Visualizar detalhes do filme e horário

**Regras de negócio:**
- Apenas filmes ativos e com sessões futuras são exibidos — sessões já iniciadas ou encerradas são automaticamente ocultadas
- Filtros por gênero e classificação indicativa retornam apenas resultados válidos
- A disponibilidade de sessões considera o tempo atual — nenhuma sessão passada é apresentada ao cliente
- Informações exibidas são consistentes com o catálogo, incluindo trailer quando disponível
- Sugestões de filmes são ordenadas por afinidade com o histórico de gêneros assistidos pelo cliente
- Clientes que favoritaram um filme são notificados automaticamente quando a primeira sessão é aberta

**Por que não é trivial:** Não se trata de uma consulta simples, mas de um processo que envolve regras de disponibilidade temporal, consistência com a grade de exibição, filtragem ativa de conteúdo inativo e critérios de ordenação relevantes para o negócio. Impacta diretamente a decisão do cliente, influenciando a conversão em vendas.

**Features BDD relacionadas:** `programacao` · `recomendacao` · `notificacao`

---

### F3 — Sistema de Fidelidade e Benefícios
**Responsável:** Amanda

**Fluxo:** Acumular pontos em compras → Consultar saldo (pontos expirados descartados automaticamente) → Verificar benefícios disponíveis → Resgatar benefício (pontos debitados)

**Regras de negócio:**
- Pontos são acumulados com base no valor gasto (1 ponto por real)
- Pontos possuem validade de 12 meses — pontos expirados são descartados automaticamente na consulta de saldo
- Benefícios exigem quantidade mínima de pontos — resgate bloqueado se saldo for insuficiente
- Benefícios podem ter restrições por dia da semana — benefícios fora do dia permitido não são listados nem resgatáveis
- Ao resgatar, os pontos são debitados corretamente do saldo ativo

**Por que não é trivial:** Não se trata apenas de gerenciar dados do usuário, mas de um sistema de regras que controla acúmulo, expiração e uso de benefícios ao longo do tempo. Envolve estado, validações e impacto direto na retenção de clientes, sendo estratégico para o negócio.

**Features BDD relacionadas:** `fidelidade`

---

### F4 — Gerenciar Grade de Exibição
**Responsável:** Irvin

**Fluxo:** Selecionar filme → Selecionar sala → Definir horários → Configurar período → Validar restrições → Salvar/atualizar grade

**Regras de negócio:**
- Uma sessão não é criada se houver conflito de horário com outra sessão na mesma sala
- A criação de sessões respeita a duração do filme + tempo mínimo de limpeza (15 min) + tempo de trailers (10 min)
- Apenas filmes ativos podem ser adicionados à grade — filmes desativados são rejeitados na tentativa de agendamento
- Remoção de sessões já iniciadas é bloqueada pelo sistema
- Ao cancelar uma sessão futura, o sistema identifica automaticamente todos os ingressos vendidos que precisam de reembolso

**Por que não é trivial:** Envolve gestão de recursos limitados (salas e horários), restrições temporais complexas e impacto direto na venda de ingressos. Exige validações consistentes, tratamento de conflitos e controle de integridade da operação, caracterizando alta complexidade no domínio.

**Features BDD relacionadas:** `sessao` · `precificacao` · `dashboard`

---

### F5 — Gerenciar Bomboniere
**Responsável:** Fabiana

**Fluxo:** Cadastrar insumo (nome, unidade, quantidade inicial, nível crítico) → Cadastrar produto (nome, preço, categoria) → Definir receita do produto (insumos e quantidades) → Realizar venda → Baixar estoque automaticamente por receita → Registrar movimentação de saída → Verificar nível crítico → Disparar alerta de reposição

**Regras de negócio:**
- Um produto só pode ser vendido quando estiver ativo e com estoque maior que zero
- A venda exige que a quantidade solicitada seja menor ou igual ao estoque disponível no momento da transação
- Toda venda confirmada gera uma movimentação de saída, reduzindo o estoque de forma consistente — baseada na receita do produto (ex.: 1 pipoca = −200 g de milho + −1 embalagem)
- O sistema aciona um alerta de reposição ao atingir ou ficar abaixo do nível mínimo definido

**Por que não é trivial:** Não se trata apenas de cadastro de produtos, mas de um controle integrado entre estoque e vendas, com regras de validação, atualização automática e consistência em tempo real. Envolve controle de estado, rastreabilidade de movimentações e impacto direto financeiro.

**Features BDD relacionadas:** `bomboniere`

---

### F6 — Controle de Acesso de Clientes com Validação
**Responsável:** Fabiana

**Fluxo:** Ler QR Code → Validar autenticidade → Verificar uso → Registrar entrada → Atualizar status

**Regras de negócio:**
- O acesso é autorizado somente se o QR Code for válido e corresponder a um ingresso existente no sistema
- O ingresso deve estar associado à sessão correta — ingressos de outras sessões são rejeitados
- A sessão deve estar dentro do período permitido de entrada (janela de 30 minutos ao redor do início)
- O ingresso deve estar marcado como não utilizado — ingressos já usados bloqueiam a entrada por tentativa de reutilização
- Se todas as condições forem atendidas, o sistema autoriza a entrada, registra o check-in e marca o ingresso como utilizado

**Por que não é trivial:** Garante segurança e controle de acesso, evitando fraudes e inconsistências. Exige validação de integridade referencial, controle de estado, verificação temporal e idempotência — impedindo o duplo uso do mesmo ingresso.

**Features BDD relacionadas:** `checkin` · `classificacao`

---

### F7 — Gerenciar Catálogo de Filmes
**Responsável:** Irvin

**Fluxo:** Cadastrar filme (título, duração, classificação indicativa, gênero) → Adicionar URL de trailer (opcional) → Ativar/desativar filme → Atualizar dados → Remover filme (quando permitido)

**Regras de negócio:**
- Filmes devem possuir informações obrigatórias (título, duração, classificação, gênero) para serem válidos — cadastro com título vazio é rejeitado
- Apenas filmes ativos podem ser utilizados na grade de exibição
- Um filme não pode ser removido se possuir sessões futuras cadastradas — deve ser desativado em vez de removido, preservando o histórico
- Filmes suportam URL de trailer para exibição ao cliente no portal
- Um filme pode ser desativado em vez de removido, mantendo rastreabilidade histórica de sessões passadas

**Por que não é trivial:** Apesar de envolver cadastro, essa funcionalidade possui regras que garantem consistência com outras partes do sistema — grade de exibição e venda de ingressos. Alterações no catálogo impactam diretamente o funcionamento do sistema, exigindo validações e restrições que vão além de um simples CRUD.

**Features BDD relacionadas:** `catalogo`

---

### F8 — Realizar Fechamento de Caixa e Relatórios
**Responsável:** Amanda

**Fluxo:** Consolidar vendas do dia por sessão → Calcular total arrecadado e total de ingressos → Calcular taxa de ocupação média → Registrar fechamento (impedindo duplicatas) → Consultar relatório por data

**Regras de negócio:**
- Vendas são consolidadas por sessão — o sistema impede fechamento duplicado para o mesmo dia
- O faturamento é calculado com base nos ingressos vendidos por tipo (inteira, meia, convite) e no preço de cada sessão
- A taxa de ocupação é calculada como ingressos vendidos sobre a capacidade total da sala
- Relatórios de dias sem fechamento são rejeitados com mensagem clara
- O dashboard exibe faturamento projetado vs. realizado por sessão, permitindo análise de performance

**Por que não é trivial:** Essencial para análise e tomada de decisão, envolve agregação de dados financeiros heterogêneos, cálculo percentual preciso e geração de relatório contábil estruturado — cruzando informações de capacidade de sala, ingressos emitidos e valores arrecadados.

**Features BDD relacionadas:** `caixa` · `dashboard` · `precificacao`

---

## Testes BDD

Todo o domínio é coberto por testes comportamentais escritos em **português** com Cucumber + JUnit 5. São **17 arquivos `.feature`** distribuídos entre os dois módulos de domínio, cobrindo **131 cenários** no total.

```bash
# Executar todos os testes
mvn test
```

### Mapeamento: Funcionalidade → Features

| Funcionalidade | Features BDD | Cenários |
|---|---|---|
| F1 — Comprar ingresso | `reserva` · `pedido` · `promocao` · `checkin` · `classificacao_compra` | 31 |
| F2 — Explorar programação | `programacao` · `recomendacao` · `notificacao` | 16 |
| F3 — Fidelidade e benefícios | `fidelidade` | 7 |
| F4 — Grade de exibição | `sessao` · `precificacao` · `dashboard` | 27 |
| F5 — Bomboniere | `bomboniere` | 12 |
| F6 — Controle de acesso | `checkin` · `classificacao` | 17 |
| F7 — Catálogo de filmes | `catalogo` | 7 |
| F8 — Fechamento de caixa | `caixa` · `dashboard` · `precificacao` | 26 |
| **Transversal** | `rbac` | 10 |

> `checkin`, `classificacao`, `dashboard`, `precificacao` e `rbac` são compartilhados entre mais de uma funcionalidade por natureza transversal.

### Estrutura completa de features

```
domain-backoffice/features/
├── grade/            → F4sessao.feature            — sessões, conflito de horário, filme inativo
├── catalogo/         → F7catalogo.feature          — cadastro, ativação, trailer, remoção protegida
├── checkin/          → F1F6checkin.feature         — QR Code, idempotência, janela de acesso
├── classificacao/    → F6classificacao.feature     — validação de idade indicativa
│                     → F1classificacao_compra.feature — validação de idade na compra
├── bomboniere/       → F5bomboniere.feature        — estoque, baixa automática, alertas
├── caixa/            → F8caixa.feature             — fechamento, relatório, impedimento de duplicata
├── precificacao/     → F4F8precificacao.feature    — preço por tipo de sala e dia da semana
├── dashboard/        → F4F8dashboard.feature       — ocupação e faturamento por sessão
└── rbac/             → rbac.feature                — transversal — permissões por role (Gerente / Operador)

domain-portal/features/
├── programacao/      → F2programacao.feature       — sessões futuras, filtros por gênero e classificação
├── reserva/          → F1reserva.feature           — seat locking com expiração automática
├── pedido/           → F1pedido.feature            — venda casada, QR Code, Voucher, pagamento
├── promocao/         → F1promocao.feature          — cupons, cumulatividade, reembolso
├── fidelidade/       → F3fidelidade.feature        — pontos, expiração, benefícios por dia
├── recomendacao/     → F2recomendacao.feature      — sugestões por histórico de gêneros
└── notificacao/      → F2notificacao.feature       — alertas automáticos de pré-venda
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
