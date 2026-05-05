# language: pt
Funcionalidade: Sistema de Fidelidade e Benefícios
  Como cliente do cinema
  Quero acumular e resgatar pontos de fidelidade
  Para obter benefícios nas minhas compras

  Contexto:
    Dado que existe um cliente com id "cliente-001"
    E que existe um benefício "Ingresso Grátis" do tipo INGRESSO_GRATIS exigindo 100 pontos disponível todos os dias
    E que existe um benefício "Desconto Segunda" do tipo DESCONTO_PERCENTUAL exigindo 50 pontos disponível apenas às segundas-feiras

  Cenário: Acumular pontos após uma compra
    Quando o cliente "cliente-001" realiza uma compra de R$ 50,00 em "2025-06-01"
    Então o saldo de pontos do cliente "cliente-001" em "2025-06-01" deve ser 50

  Cenário: Pontos acumulados em múltiplas compras
    Quando o cliente "cliente-001" realiza uma compra de R$ 30,00 em "2025-06-01"
    E o cliente "cliente-001" realiza uma compra de R$ 20,00 em "2025-06-01"
    Então o saldo de pontos do cliente "cliente-001" em "2025-06-01" deve ser 50

  Cenário: Consultar benefícios disponíveis com saldo suficiente
    Dado que o cliente "cliente-001" possui 100 pontos acumulados em "2025-06-01" com validade "2026-06-01"
    Quando o cliente "cliente-001" consulta os benefícios disponíveis em "2025-06-01"
    Então o benefício "Ingresso Grátis" deve estar na lista de disponíveis

  Cenário: Benefício com restrição de dia não aparece no dia errado
    Dado que o cliente "cliente-001" possui 100 pontos acumulados em "2025-06-03" com validade "2026-06-03"
    Quando o cliente "cliente-001" consulta os benefícios disponíveis em "2025-06-03"
    Então o benefício "Desconto Segunda" não deve estar na lista de disponíveis

  Cenário: Resgatar benefício com saldo suficiente
    Dado que o cliente "cliente-001" possui 100 pontos acumulados em "2025-06-01" com validade "2026-06-01"
    Quando o cliente "cliente-001" resgata o benefício "Ingresso Grátis" em "2025-06-01"
    Então o saldo de pontos do cliente "cliente-001" em "2025-06-01" deve ser 0

  Cenário: Impedir resgate com pontos insuficientes
    Dado que o cliente "cliente-001" possui 30 pontos acumulados em "2025-06-01" com validade "2026-06-01"
    Quando o cliente "cliente-001" tenta resgatar o benefício "Ingresso Grátis" em "2025-06-01"
    Então deve ocorrer o erro "Pontos insuficientes para resgatar o benefício"

  Cenário: Impedir uso de pontos expirados
    Dado que o cliente "cliente-001" possui 100 pontos acumulados em "2024-01-01" com validade "2024-12-31"
    Quando o cliente "cliente-001" tenta resgatar o benefício "Ingresso Grátis" em "2025-06-01"
    Então deve ocorrer o erro "Pontos insuficientes para resgatar o benefício"

  Cenário: Bloquear acúmulo quando limite mensal de 500 pontos é atingido
    Dado que o cliente "cliente-001" já acumulou 500 pontos no mês "2025-06"
    Quando o cliente "cliente-001" tenta realizar uma compra de R$ 50,00 em "2025-06-20"
    Então deve ocorrer o erro "Limite mensal de 500 pontos atingido para este mês"

  Cenário: Acumular pontos normalmente quando limite mensal não foi atingido
    Dado que o cliente "cliente-001" já acumulou 400 pontos no mês "2025-06"
    Quando o cliente "cliente-001" realiza uma compra de R$ 50,00 em "2025-06-20"
    Então o saldo de pontos do cliente "cliente-001" em "2025-06-20" deve ser 450

  Cenário: Dobrar pontos no aniversário do cliente
    Dado que o aniversário do cliente "cliente-001" é em "06-15"
    Quando o cliente "cliente-001" realiza uma compra de R$ 50,00 em "2025-06-15"
    Então o saldo de pontos do cliente "cliente-001" em "2025-06-15" deve ser 100

  Cenário: Não aplicar bônus de aniversário fora da data
    Dado que o aniversário do cliente "cliente-001" é em "06-15"
    Quando o cliente "cliente-001" realiza uma compra de R$ 50,00 em "2025-06-16"
    Então o saldo de pontos do cliente "cliente-001" em "2025-06-16" deve ser 50

  Cenário: Bloquear quarto resgate do mesmo benefício no mesmo mês
    Dado que o cliente "cliente-001" possui 500 pontos acumulados em "2025-06-01" com validade "2026-06-01"
    E que o cliente "cliente-001" já resgatou o benefício "Ingresso Grátis" 3 vezes em "2025-06"
    Quando o cliente "cliente-001" tenta resgatar o benefício "Ingresso Grátis" em "2025-06-25"
    Então deve ocorrer o erro "Limite de 3 resgates do mesmo benefício por mês atingido"

  Cenário: Permitir resgate quando limite mensal não foi atingido
    Dado que o cliente "cliente-001" possui 300 pontos acumulados em "2025-06-01" com validade "2026-06-01"
    E que o cliente "cliente-001" já resgatou o benefício "Ingresso Grátis" 2 vezes em "2025-06"
    Quando o cliente "cliente-001" resgata o benefício "Ingresso Grátis" em "2025-06-25"
    Então o saldo de pontos do cliente "cliente-001" em "2025-06-25" deve ser 200

  Cenário: Registrar resgate no histórico após resgate bem-sucedido
    Dado que o cliente "cliente-001" possui 100 pontos acumulados em "2025-06-01" com validade "2026-06-01"
    Quando o cliente "cliente-001" resgata o benefício "Ingresso Grátis" em "2025-06-01"
    Então o histórico do cliente "cliente-001" deve conter 1 resgate

  Cenário: Histórico acumula múltiplos resgates
    Dado que o cliente "cliente-001" possui 300 pontos acumulados em "2025-06-01" com validade "2026-06-01"
    Quando o cliente "cliente-001" resgata o benefício "Ingresso Grátis" em "2025-06-01"
    E o cliente "cliente-001" resgata o benefício "Ingresso Grátis" em "2025-06-02"
    Então o histórico do cliente "cliente-001" deve conter 2 resgates

  Cenário: Bloquear resgate de benefício incompatível no mesmo dia
    Dado que existe um benefício "Pipoca Grátis" do tipo PIPOCA_GRATIS exigindo 30 pontos não combinável com INGRESSO_GRATIS disponível todos os dias
    E que o cliente "cliente-001" possui 200 pontos acumulados em "2025-06-01" com validade "2026-06-01"
    E que o cliente "cliente-001" já resgatou o benefício "Ingresso Grátis" em "2025-06-01"
    Quando o cliente "cliente-001" tenta resgatar o benefício "Pipoca Grátis" em "2025-06-01"
    Então deve ocorrer o erro "Benefício incompatível com"

  Cenário: Permitir resgate de benefício compatível no mesmo dia
    Dado que existe um benefício "Upgrade Assento" do tipo UPGRADE_ASSENTO exigindo 40 pontos combinável disponível todos os dias
    E que o cliente "cliente-001" possui 200 pontos acumulados em "2025-06-01" com validade "2026-06-01"
    E que o cliente "cliente-001" já resgatou o benefício "Ingresso Grátis" em "2025-06-01"
    Quando o cliente "cliente-001" resgata o benefício "Upgrade Assento" em "2025-06-01"
    Então o saldo de pontos do cliente "cliente-001" em "2025-06-01" deve ser 60

  Cenário: Aplicar multiplicador 1.5x para compras acima de R$100
    Quando o cliente "cliente-001" realiza uma compra de R$ 150,00 em "2025-06-01"
    Então o saldo de pontos do cliente "cliente-001" em "2025-06-01" deve ser 225

  Cenário: Aplicar multiplicador 2x para compras acima de R$200
    Quando o cliente "cliente-001" realiza uma compra de R$ 250,00 em "2025-06-01"
    Então o saldo de pontos do cliente "cliente-001" em "2025-06-01" deve ser 500

  Cenário: Aplicar multiplicador 3x para compras acima de R$500
    Quando o cliente "cliente-001" realiza uma compra de R$ 600,00 em "2025-06-01"
    Então o saldo de pontos do cliente "cliente-001" em "2025-06-01" deve ser 500

  Cenário: Nao aplicar bonus para compras abaixo de R$100
    Quando o cliente "cliente-001" realiza uma compra de R$ 80,00 em "2025-06-01"
    Então o saldo de pontos do cliente "cliente-001" em "2025-06-01" deve ser 80
