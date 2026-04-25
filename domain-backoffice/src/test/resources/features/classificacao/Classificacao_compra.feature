# language: pt

Funcionalidade: Validação de classificação indicativa na compra de ingresso

  Cenário: Permitir compra para cliente com idade suficiente
    Dado que existe um cliente cadastrado nascido em "2000-01-01"
    E existe um filme cadastrado com classificação "QUATORZE"
    Quando o sistema validar a compra do cliente
    Então a compra deve ser permitida

  Cenário: Permitir compra para filme livre independente da idade
    Dado que existe um cliente cadastrado nascido em "2015-01-01"
    E existe um filme cadastrado com classificação "LIVRE"
    Quando o sistema validar a compra do cliente
    Então a compra deve ser permitida

  Cenário: Permitir compra para cliente com idade exata
    Dado que existe um cliente cadastrado com exatamente 16 anos
    E existe um filme cadastrado com classificação "DEZESSEIS"
    Quando o sistema validar a compra do cliente
    Então a compra deve ser permitida

  Cenário: Bloquear compra para cliente menor de idade
    Dado que existe um cliente cadastrado nascido em "2015-01-01"
    E existe um filme cadastrado com classificação "QUATORZE"
    Quando o sistema tentar validar a compra do cliente
    Então a compra deve ser bloqueada com mensagem de idade insuficiente

  Cenário: Bloquear compra para cliente menor para filme 18 anos
    Dado que existe um cliente cadastrado nascido em "2010-01-01"
    E existe um filme cadastrado com classificação "DEZOITO"
    Quando o sistema tentar validar a compra do cliente
    Então a compra deve ser bloqueada com mensagem de idade insuficiente