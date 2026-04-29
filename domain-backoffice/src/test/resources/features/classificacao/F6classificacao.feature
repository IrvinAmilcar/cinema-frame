# language: pt

Funcionalidade: Validação de classificação indicativa

  Cenário: Permitir acesso a filme livre para qualquer idade
    Dado que o filme possui classificação "LIVRE"
    E a pessoa nasceu em "2015-01-01"
    Quando o sistema validar a classificação
    Então o acesso deve ser permitido

  Cenário: Permitir acesso a filme livre para adulto
    Dado que o filme possui classificação "LIVRE"
    E a pessoa nasceu em "1990-01-01"
    Quando o sistema validar a classificação
    Então o acesso deve ser permitido

  Cenário: Permitir acesso quando pessoa tem exatamente a idade mínima
    Dado que o filme possui classificação "DOZE"
    E a pessoa nasceu há exatamente 12 anos
    Quando o sistema validar a classificação
    Então o acesso deve ser permitido

  Cenário: Permitir acesso a filme 10 anos para pessoa com 15 anos
    Dado que o filme possui classificação "DEZ"
    E a pessoa nasceu em "2010-01-01"
    Quando o sistema validar a classificação
    Então o acesso deve ser permitido

  Cenário: Permitir acesso a filme 14 anos para pessoa com 18 anos
    Dado que o filme possui classificação "QUATORZE"
    E a pessoa nasceu em "2006-01-01"
    Quando o sistema validar a classificação
    Então o acesso deve ser permitido

  Cenário: Permitir acesso a filme 18 anos para pessoa com 18 anos exatos
    Dado que o filme possui classificação "DEZOITO"
    E a pessoa nasceu há exatamente 18 anos
    Quando o sistema validar a classificação
    Então o acesso deve ser permitido

  Cenário: Bloquear acesso a filme 10 anos para pessoa com 9 anos
    Dado que o filme possui classificação "DEZ"
    E a pessoa nasceu em "2017-01-01"
    Quando o sistema validar a classificação
    Então o acesso deve ser negado

  Cenário: Bloquear acesso a filme 12 anos para pessoa com 11 anos
    Dado que o filme possui classificação "DOZE"
    E a pessoa nasceu em "2015-01-01"
    Quando o sistema validar a classificação
    Então o acesso deve ser negado

  Cenário: Bloquear acesso a filme 14 anos para pessoa com 13 anos
    Dado que o filme possui classificação "QUATORZE"
    E a pessoa nasceu em "2013-01-01"
    Quando o sistema validar a classificação
    Então o acesso deve ser negado

  Cenário: Bloquear acesso a filme 16 anos para pessoa com 15 anos
    Dado que o filme possui classificação "DEZESSEIS"
    E a pessoa nasceu em "2011-01-01"
    Quando o sistema validar a classificação
    Então o acesso deve ser negado

  Cenário: Bloquear acesso a filme 18 anos para pessoa com 17 anos
    Dado que o filme possui classificação "DEZOITO"
    E a pessoa nasceu em "2009-01-01"
    Quando o sistema validar a classificação
    Então o acesso deve ser negado