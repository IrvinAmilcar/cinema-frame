# 🎬 Frame

> Sistema de gestão completo para cinemas — do backoffice ao portal do cliente, construído com Domain-Driven Design e testado com Cucumber BDD.

---

## 🗺️ Visão Geral

O **CinemaFrame** é uma aplicação Java modular que cobre toda a operação de um cinema, desde a criação da grade de exibição até a compra de ingressos pelo cliente. A arquitetura segue os princípios de **DDD (Domain-Driven Design)**, com dois bounded contexts bem definidos e separação clara de responsabilidades entre as camadas.

```
frame-parent
├── domain               ← Regras de negócio puras (sem frameworks)
├── application          ← Casos de uso e orquestração
├── infrastructure       ← Persistência com Spring Data JPA
├── presentation-backend ← API REST com Spring Boot
└── presentation-frontend← Assets e templates do frontend
```

---

## 🧭 Bounded Contexts

### 🏢 BackofficeContext
Núcleo operacional e administrativo do cinema.

| Agregado | Responsabilidade |
|---|---|
| **Grade** | Filmes, salas, sessões e controle de conflitos de horário |
| **Ingresso** | Emissão por tipo: Inteira, Meia, Convite |
| **Check-in** | Validação de QR Code na entrada da sala |
| **Precificação** | Preço base por tipo de sala + desconto por dia da semana |
| **Classificação** | Validação de idade mínima indicativa |
| **Bomboniere** | Controle de insumos, receitas e notificação de estoque crítico |
| **Caixa** | Geração de borderô com repasse para distribuidora |
| **RBAC** | Controle de permissões por role (Gerente / Operador de Caixa) |
| **Dashboard** | Taxa de ocupação e faturamento por sessão |

### 🌐 PortalContext
Portal de autoatendimento do cliente.

| Agregado | Responsabilidade |
|---|---|
| **Cliente** | Cadastro, favoritos e data de nascimento |
| **Reserva** | Reserva temporária de assento com expiração automática (10 min) |
| **Pedido** | Venda casada ingresso + bomboniere, geração de QR Code e Voucher |
| **Promoção** | Motor de cupons: Leve 2 Pague 1, Parceria Cartão, Desconto Estudante |
| **Fidelidade** | Acúmulo e resgate de pontos por compra |
| **Recomendação** | Sugestão de filmes baseada no histórico de gêneros assistidos |
| **Notificação** | Pré-venda automática para clientes que favoritaram o filme |

---

## 💡 Regras de Negócio Destacadas

- 🎟️ **Precificação dinâmica**: Sala Padrão (R$ 20) → VIP (R$ 60), com desconto de até 50% às terças-feiras
- ⏱️ **Intervalo entre sessões**: Sistema respeita 25 minutos de limpeza + trailers ao adicionar sessões
- 🔞 **Classificação indicativa**: Validada tanto no check-in quanto na compra online
- 🛒 **Reserva temporária**: Assento liberado automaticamente após 10 minutos sem confirmação
- 📦 **Gestão de estoque**: Baixa automática nos insumos ao vender um combo, com alerta de nível crítico
- 🎫 **Borderô**: 50% do arrecadado é repassado automaticamente à distribuidora

---

## 🧪 Testes

Todo o domínio é coberto por testes BDD escritos em **português** com Cucumber + JUnit 5.

```bash
cd domain
mvn test
```

### Features cobertas

```
features/
├── bomboniere/       ← Gestão de estoque de insumos
├── caixa/            ← Fechamento de caixa e borderô
├── checkin/          ← Check-in digital via QR Code
├── classificacao/    ← Classificação indicativa (acesso e compra)
├── dashboard/        ← Taxa de ocupação por sessão
├── fidelidade/       ← Pontuação e resgate de pontos
├── grade/            ← Sessões e conflito de horário
├── notificacao/      ← Pré-venda automática
├── pedido/           ← Venda casada ingresso + bomboniere
├── precificacao/     ← Preços por tipo de sala e dia da semana
├── promocao/         ← Motor de cupons e descontos
├── rbac/             ← Controle de acesso por role
├── recomendacao/     ← Sugestões por perfil de gênero
└── reserva/          ← Reserva temporária de assentos
```

---

## 🛠️ Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework Web | Spring Boot 4 |
| Persistência | Spring Data JPA |
| Testes | Cucumber 7 + JUnit 6 + Mockito 5 |
| Build | Maven (multi-module) |
| Container | Docker via Jib |
| Utilitários | Lombok |

---

## 🚀 Como Rodar

### Pré-requisitos
- Java 17+
- Maven 3.8+
- Docker (opcional, para build de imagem)

### Build completo
```bash
mvn clean install
```

### Subir a aplicação
```bash
cd presentation-backend
mvn spring-boot:run
```

### Build da imagem Docker
```bash
mvn package -Pdockerbuild
```

---

## 📐 Arquitetura

O projeto segue uma **arquitetura hexagonal (Ports & Adapters)** com módulos Maven separados:

```
┌─────────────────────────────────────────┐
│         presentation-backend            │  ← Controllers REST
│         presentation-frontend           │  ← Assets estáticos
└────────────────────┬────────────────────┘
                     │
┌────────────────────▼────────────────────┐
│              application                │  ← Casos de uso
└────────────────────┬────────────────────┘
                     │
┌────────────────────▼────────────────────┐
│               domain                    │  ← Entidades, VOs, Serviços
│  ┌─────────────────┐ ┌───────────────┐  │
│  │ BackofficeContext│ │ PortalContext │  │
│  └─────────────────┘ └───────────────┘  │
└────────────────────┬────────────────────┘
                     │
┌────────────────────▼────────────────────┐
│            infrastructure               │  ← JPA, Repositórios
└─────────────────────────────────────────┘
```

O módulo `domain` não possui nenhuma dependência de framework — apenas Java puro, garantindo que as regras de negócio sejam testáveis de forma isolada e portáveis.

---

## 👥 Equipe

| Nome | Email |
|---|---|
| Amanda Montarroios | amo@cesar.school |
| Fabiana Souza Leão | fcsls@cesar.school |
| Irvin Amilcar | iabs@cesar.school |
| Julia Maria Teixeira | jmst@cesar.school |

**Professor:** Saulo — Engenharia de Requisitos · CESAR School

---

## 📄 Licença

Este projeto é de uso acadêmico/demonstrativo.
