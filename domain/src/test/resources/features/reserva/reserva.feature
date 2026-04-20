# language: pt

Funcionalidade: Reserva temporária de assentos

  Cenário: Reservar assento com sucesso
    Dado que existe uma sessão disponível
    Quando o cliente reservar o assento 5
    Então a reserva deve estar ativa com status "RESERVADO"
    E a reserva deve expirar em 10 minutos

  Cenário: Impedir reserva de assento já reservado
    Dado que existe uma sessão disponível
    E o assento 5 já foi reservado
    Quando outro cliente tentar reservar o assento 5
    Então o sistema deve rejeitar informando assento já reservado

  Cenário: Liberar assento automaticamente após expiração
    Dado que existe uma sessão disponível
    E o assento 5 foi reservado há 11 minutos
    Quando outro cliente tentar reservar o assento 5
    Então a reserva deve estar ativa com status "RESERVADO"

  Cenário: Confirmar reserva dentro do prazo
    Dado que existe uma sessão disponível
    E o assento 5 foi reservado há 5 minutos
    Quando o cliente confirmar a reserva
    Então a reserva deve ter status "CONFIRMADO"

  Cenário: Impedir confirmação de reserva expirada
    Dado que existe uma sessão disponível
    E o assento 5 foi reservado há 11 minutos
    Quando o cliente tentar confirmar a reserva expirada
    Então o sistema deve rejeitar informando reserva expirada