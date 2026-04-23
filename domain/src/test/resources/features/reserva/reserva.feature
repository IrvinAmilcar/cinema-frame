# language: pt

Funcionalidade: Reserva temporária de assentos

  Cenário: Reservar assento com sucesso
    Dado que existe uma sessão cadastrada disponível
    Quando o cliente reservar o assento 5 na sessão cadastrada
    Então a reserva deve estar ativa com status "RESERVADO"
    E a reserva deve expirar em 10 minutos

  Cenário: Impedir reserva de assento já reservado
    Dado que existe uma sessão cadastrada disponível
    E o assento 5 já foi reservado para a sessão cadastrada
    Quando outro cliente tentar reservar o assento 5 na sessão cadastrada
    Então o sistema deve rejeitar informando assento já reservado

  Cenário: Liberar assento automaticamente após expiração
    Dado que existe uma sessão cadastrada disponível
    E o assento 5 foi reservado há 11 minutos para a sessão cadastrada
    Quando outro cliente tentar reservar o assento 5 na sessão cadastrada
    Então a reserva deve estar ativa com status "RESERVADO"

  Cenário: Confirmar reserva dentro do prazo
    Dado que existe uma sessão cadastrada disponível
    E o assento 5 foi reservado há 5 minutos para a sessão cadastrada
    Quando o cliente confirmar a reserva cadastrada
    Então a reserva deve ter status "CONFIRMADO"

  Cenário: Impedir confirmação de reserva expirada
    Dado que existe uma sessão cadastrada disponível
    E o assento 5 foi reservado há 11 minutos para a sessão cadastrada
    Quando o cliente tentar confirmar a reserva cadastrada expirada
    Então o sistema deve rejeitar informando reserva expirada