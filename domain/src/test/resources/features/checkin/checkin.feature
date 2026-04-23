# language: pt

Funcionalidade: Check-in digital via QR Code

  Cenário: Check-in realizado com sucesso
    Dado que existe um ingresso cadastrado válido para a sessão de hoje às 20:00
    Quando o funcionário escaneia o ingresso cadastrado às 19:45
    Então o check-in deve ser realizado com sucesso

  Cenário: Impedir check-in com ingresso já utilizado
    Dado que existe um ingresso cadastrado válido para a sessão de hoje às 20:00
    E o ingresso já foi utilizado
    Quando o funcionário escaneia o ingresso cadastrado às 19:45
    Então o sistema deve rejeitar informando ingresso já utilizado

  Cenário: Impedir check-in com ingresso de outra sessão
    Dado que existe um ingresso cadastrado válido para a sessão de hoje às 20:00
    Quando o funcionário escaneia o ingresso cadastrado numa sessão diferente às 19:45
    Então o sistema deve rejeitar informando sessão incorreta

  Cenário: Impedir check-in fora do horário permitido
    Dado que existe um ingresso cadastrado válido para a sessão de hoje às 20:00
    Quando o funcionário escaneia o ingresso cadastrado às 17:00
    Então o sistema deve rejeitar informando fora do horário de entrada