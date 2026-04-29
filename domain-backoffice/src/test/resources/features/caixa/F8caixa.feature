# language: pt

Funcionalidade: Fechamento de Caixa e Relatórios
  Como gerente do cinema
  Quero fechar o caixa ao final do dia
  Para obter relatório consolidado de vendas e ocupação

  Cenário: Fechar caixa com vendas do dia
    Dado que existem as seguintes vendas para o dia "2025-06-01":
      | sessaoId    | capacidade | ingressos | valor  |
      | sessao-001  | 100        | 80        | 1600.0 |
      | sessao-002  | 100        | 60        | 1200.0 |
    Quando o caixa é fechado para o dia "2025-06-01" às "2025-06-01T23:00:00"
    Então o total de vendas do fechamento deve ser 2800
    E o total de ingressos do fechamento deve ser 140
    E o número de sessões do fechamento deve ser 2
    E a taxa de ocupação média do fechamento deve ser 70

  Cenário: Impedir fechamento duplicado para o mesmo dia
    Dado que já existe um fechamento para o dia "2025-06-01"
    Quando o caixa tenta ser fechado novamente para o dia "2025-06-01" às "2025-06-01T23:59:00"
    Então deve ocorrer o erro "Já existe um fechamento de caixa para a data"

  Cenário: Consultar relatório de fechamento
    Dado que já existe um fechamento para o dia "2025-06-01" com total de vendas 3000
    Quando o relatório do dia "2025-06-01" é consultado
    Então o total de vendas do relatório deve ser 3000

  Cenário: Consultar relatório de dia sem fechamento
    Quando o relatório do dia "2025-06-15" é consultado sem fechamento registrado
    Então deve ocorrer o erro "Não há fechamento registrado para a data"

  Cenário: Calcular ocupação corretamente
    Dado que existem as seguintes vendas para o dia "2025-06-02":
      | sessaoId   | capacidade | ingressos | valor |
      | sessao-003 | 200        | 200       | 4000.0|
      | sessao-004 | 200        | 100       | 2000.0|
    Quando o caixa é fechado para o dia "2025-06-02" às "2025-06-02T22:00:00"
    Então a taxa de ocupação média do fechamento deve ser 75