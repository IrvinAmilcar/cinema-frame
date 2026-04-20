# language: pt

Funcionalidade: Fechamento de caixa e borderô por sessão

  Cenário: Gerar borderô com apenas ingressos inteiros
    Dado que existe uma sessão numa sala padrão numa sexta-feira às 20:00
    E foram vendidos 3 ingressos inteiros para essa sessão
    Quando o sistema gerar o borderô
    Então o total arrecadado deve ser R$ 60,00
    E o repasse para a distribuidora deve ser R$ 30,00
    E a receita do cinema deve ser R$ 30,00

  Cenário: Gerar borderô com meia-entrada
    Dado que existe uma sessão numa sala padrão numa sexta-feira às 20:00
    E foram vendidos 2 ingressos inteiros para essa sessão
    E foram vendidos 2 ingressos meia para essa sessão
    Quando o sistema gerar o borderô
    Então o total arrecadado deve ser R$ 60,00
    E o repasse para a distribuidora deve ser R$ 30,00
    E a receita do cinema deve ser R$ 30,00

  Cenário: Gerar borderô com convites não afeta total arrecadado
    Dado que existe uma sessão numa sala padrão numa sexta-feira às 20:00
    E foram vendidos 2 ingressos inteiros para essa sessão
    E foram vendidos 1 ingressos convite para essa sessão
    Quando o sistema gerar o borderô
    Então o total arrecadado deve ser R$ 40,00
    E o repasse para a distribuidora deve ser R$ 20,00

  Cenário: Impedir borderô com lista vazia de ingressos
    Dado que existe uma sessão numa sala padrão numa sexta-feira às 20:00
    Quando o sistema tentar gerar o borderô sem ingressos
    Então o sistema deve rejeitar informando lista de ingressos inválida