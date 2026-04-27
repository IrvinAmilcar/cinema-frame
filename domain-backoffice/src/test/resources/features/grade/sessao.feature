# language: pt

Funcionalidade: Gerenciamento de sessões na grade de exibição

  Cenário: Adicionar sessão com sucesso
    Dado que existe um filme cadastrado "Oppenheimer" com duração de 180 minutos e classificação "QUATORZE" e gênero "DRAMA"
    E existe uma sala cadastrada de número 1 com capacidade para 100 pessoas
    E existe uma grade de exibição cadastrada para a semana
    Quando a gerente adiciona a sessão na grade às 14:00
    Então a sessão deve ser salva na grade

  Cenário: Impedir sessão com conflito de horário
    Dado que existe um filme cadastrado "Oppenheimer" com duração de 180 minutos e classificação "QUATORZE" e gênero "DRAMA"
    E existe uma sala cadastrada de número 1 com capacidade para 100 pessoas
    E existe uma grade de exibição cadastrada para a semana
    E a gerente já cadastrou uma sessão na grade às 14:00
    Quando a gerente tenta cadastrar uma sessão de "Barbie" com duração de 120 minutos e gênero "COMEDIA" às 16:00
    Então o sistema deve rejeitar informando conflito de horário

  Cenário: Impedir sessão sem respeitar intervalo de limpeza e trailers
    Dado que existe um filme cadastrado "Oppenheimer" com duração de 180 minutos e classificação "QUATORZE" e gênero "DRAMA"
    E existe uma sala cadastrada de número 1 com capacidade para 100 pessoas
    E existe uma grade de exibição cadastrada para a semana
    E a gerente já cadastrou uma sessão na grade às 14:00
    Quando a gerente tenta cadastrar uma sessão de "Barbie" com duração de 120 minutos e gênero "COMEDIA" às 17:10
    Então o sistema deve rejeitar informando conflito de horário

  Cenário: Cancelar sessão futura e identificar ingressos para reembolso
    Dado que existe um filme cadastrado "Oppenheimer" com duração de 180 minutos e classificação "QUATORZE" e gênero "DRAMA"
    E existe uma sala cadastrada de número 1 com capacidade para 100 pessoas
    E existe uma grade de exibição cadastrada para a semana
    E a gerente já cadastrou uma sessão para amanhã às 14:00
    E já existem 2 ingressos comprados para essa sessão
    Quando a gerente remove a sessão
    Então a sessão deve ser removida da grade
    E o sistema identifica 2 ingressos que precisam de reembolso

  Cenário: Impedir cancelamento de sessão já iniciada
    Dado que existe um filme cadastrado "Oppenheimer" com duração de 180 minutos e classificação "QUATORZE" e gênero "DRAMA"
    E existe uma sala cadastrada de número 1 com capacidade para 100 pessoas
    E existe uma grade de exibição cadastrada para a semana
    E a gerente já cadastrou uma sessão que já foi iniciada
    Quando a gerente tenta remover a sessão já iniciada
    Então o sistema deve impedir informando que a sessão já foi iniciada
