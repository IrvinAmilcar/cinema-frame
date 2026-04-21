# language: pt

Funcionalidade: Notificação automática de pré-venda

  Cenário: Notificar cliente que favoritou o filme quando sessão for criada
    Dado que existe um cliente "João" que favoritou o filme "Oppenheimer"
    E existe uma sessão do filme "Oppenheimer"
    Quando o sistema processar as notificações de pré-venda
    Então o cliente "João" deve ser notificado

  Cenário: Não notificar cliente que não favoritou o filme
    Dado que existe um cliente "Maria" que não favoritou nenhum filme
    E existe uma sessão do filme "Oppenheimer"
    Quando o sistema processar as notificações de pré-venda
    Então o cliente "Maria" não deve ser notificado

  Cenário: Notificar apenas clientes interessados quando há múltiplos clientes
    Dado que existe um cliente "João" que favoritou o filme "Oppenheimer"
    E existe um cliente "Maria" que não favoritou nenhum filme
    E existe uma sessão do filme "Oppenheimer"
    Quando o sistema processar as notificações de pré-venda
    Então apenas 1 cliente deve ser notificado

  Cenário: Não notificar cliente que favoritou outro filme
    Dado que existe um cliente "Pedro" que favoritou o filme "Barbie"
    E existe uma sessão do filme "Oppenheimer"
    Quando o sistema processar as notificações de pré-venda
    Então o cliente "Pedro" não deve ser notificado