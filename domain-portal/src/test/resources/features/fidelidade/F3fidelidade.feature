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