# language: pt

Funcionalidade: Gerenciamento de sessões na grade de exibição

  Cenário: Adicionar sessão com sucesso
    Dado que existe um filme "Oppenheimer" com duração de 180 minutos e classificação "14"
    E existe uma sala de número 1 com capacidade para 100 pessoas
    E existe uma grade de exibição para a semana
    Quando a gerente adiciona a sessão na sala 1 às 14:00
    Então a sessão deve estar na grade

  Cenário: Impedir sessão com conflito de horário
    Dado que existe um filme "Oppenheimer" com duração de 180 minutos e classificação "14"
    E existe uma sala de número 1 com capacidade para 100 pessoas
    E existe uma grade de exibição para a semana
    E a gerente já adicionou a sessão na sala 1 às 14:00
    Quando a gerente tenta adicionar uma sessão de "Barbie" com duração de 120 minutos às 16:00 na sala 1
    Então o sistema deve rejeitar informando conflito de horário

  Cenário: Impedir sessão sem respeitar intervalo de limpeza e trailers
    Dado que existe um filme "Oppenheimer" com duração de 180 minutos e classificação "14"
    E existe uma sala de número 1 com capacidade para 100 pessoas
    E existe uma grade de exibição para a semana
    E a gerente já adicionou a sessão na sala 1 às 14:00
    Quando a gerente tenta adicionar uma sessão de "Barbie" com duração de 120 minutos às 17:10 na sala 1
    Então o sistema deve rejeitar informando conflito de horário