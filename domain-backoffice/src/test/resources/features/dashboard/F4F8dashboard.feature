# language: pt

Funcionalidade: Dashboard de taxa de ocupação por sessão

  Cenário: Calcular taxa de ocupação com sala cheia
    Dado que existe uma sessão na sala padrão com capacidade para 100 pessoas
    E foram vendidos 100 ingressos para essa sessão
    Quando o dashboard calcular a ocupação da sessão
    Então a taxa de ocupação deve ser 100,0%
    E o faturamento realizado deve ser igual ao projetado

  Cenário: Calcular taxa de ocupação com sala vazia
    Dado que existe uma sessão na sala padrão com capacidade para 100 pessoas
    E foram vendidos 0 ingressos para essa sessão
    Quando o dashboard calcular a ocupação da sessão
    Então a taxa de ocupação deve ser 0,0%
    E o faturamento realizado deve ser 0,0

  Cenário: Calcular taxa de ocupação parcial
    Dado que existe uma sessão na sala padrão com capacidade para 100 pessoas
    E foram vendidos 50 ingressos para essa sessão
    Quando o dashboard calcular a ocupação da sessão
    Então a taxa de ocupação deve ser 50,0%

  Cenário: Calcular faturamento projetado para sala padrão
    Dado que existe uma sessão na sala padrão com capacidade para 100 pessoas
    E foram vendidos 0 ingressos para essa sessão
    Quando o dashboard calcular a ocupação da sessão
    Então o faturamento projetado deve ser 2000,0

  Cenário: Calcular ocupação de múltiplas sessões
    Dado que existe uma sessão na sala padrão com capacidade para 100 pessoas
    E foram vendidos 50 ingressos para essa sessão
    E existe outra sessão na sala padrão com capacidade para 100 pessoas
    E foram vendidos 80 ingressos para essa outra sessão
    Quando o dashboard calcular a ocupação da semana
    Então o resultado deve conter 2 sessões

  Cenário: Faturamento considera tipo do ingresso no cálculo
    Dado que existe uma sessão na sala padrão com capacidade para 100 pessoas
    E foram vendidos 1 ingresso inteiro e 1 ingresso meia para essa sessão
    Quando o dashboard calcular a ocupação da sessão
    Então o faturamento realizado deve ser 30,0