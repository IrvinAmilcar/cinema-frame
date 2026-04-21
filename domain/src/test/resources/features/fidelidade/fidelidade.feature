# language: pt

Funcionalidade: Programa de Fidelidade (Pontuação e Resgate)

  Cenário: Pontuar cliente ao realizar uma compra
    Dado que o cliente possui uma conta de fidelidade com 0 pontos
    Quando o cliente realizar uma compra no valor de R$ 40,00
    Então o saldo de pontos deve ser 40

  Cenário: Acumular pontos em compras sucessivas
    Dado que o cliente possui uma conta de fidelidade com 100 pontos
    Quando o cliente realizar uma compra no valor de R$ 60,00
    Então o saldo de pontos deve ser 160

  Cenário: Resgatar pontos por ingresso com saldo suficiente
    Dado que o cliente possui uma conta de fidelidade com 200 pontos
    Quando o cliente resgatar 100 pontos
    Então o saldo de pontos deve ser 100

  Cenário: Impedir resgate com saldo insuficiente
    Dado que o cliente possui uma conta de fidelidade com 50 pontos
    Quando o cliente tentar resgatar 100 pontos
    Então o sistema deve rejeitar informando saldo insuficiente

  Cenário: Impedir resgate de valor zero ou negativo
    Dado que o cliente possui uma conta de fidelidade com 200 pontos
    Quando o cliente tentar resgatar 0 pontos
    Então o sistema deve rejeitar informando quantidade inválida de pontos

  Cenário: Compra com valor zero não gera pontos
    Dado que o cliente possui uma conta de fidelidade com 50 pontos
    Quando o cliente realizar uma compra no valor de R$ 0,00
    Então o saldo de pontos deve ser 50

  Cenário: Resgatar exatamente o saldo disponível
    Dado que o cliente possui uma conta de fidelidade com 300 pontos
    Quando o cliente resgatar 300 pontos
    Então o saldo de pontos deve ser 0
