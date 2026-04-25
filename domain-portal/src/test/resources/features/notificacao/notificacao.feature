# language: pt

Funcionalidade: Notificação automática de pré-venda

  Cenário: Notificar usuário que favoritou um filme ao abrir sessão
    Dado que existe um filme cadastrado "Oppenheimer"
    E o usuário "usuario-01" favoritou o filme cadastrado "Oppenheimer"
    E existe uma sessão cadastrada para o filme "Oppenheimer"
    Quando o sistema processar as notificações para a sessão cadastrada
    Então 1 usuário deve ser notificado
    E o favorito deve ser marcado como notificado

  Cenário: Notificar múltiplos usuários que favoritaram o mesmo filme
    Dado que existe um filme cadastrado "Oppenheimer"
    E o usuário "usuario-01" favoritou o filme cadastrado "Oppenheimer"
    E o usuário "usuario-02" favoritou o filme cadastrado "Oppenheimer"
    E existe uma sessão cadastrada para o filme "Oppenheimer"
    Quando o sistema processar as notificações para a sessão cadastrada
    Então 2 usuários devem ser notificados

  Cenário: Não notificar quando nenhum usuário favoritou o filme
    Dado que existe um filme cadastrado "Oppenheimer"
    E nenhum usuário favoritou o filme cadastrado "Oppenheimer"
    E existe uma sessão cadastrada para o filme "Oppenheimer"
    Quando o sistema processar as notificações para a sessão cadastrada
    Então 0 usuários devem ser notificados

  Cenário: Salvar favorito com sucesso
    Dado que existe um filme cadastrado "Oppenheimer"
    Quando o usuário "usuario-01" favoritar o filme cadastrado "Oppenheimer"
    Então o favorito deve ser salvo no sistema

  Cenário: Impedir favoritar com filme inexistente
    Dado que o filme "Inexistente" não está cadastrado no sistema
    Quando o usuário "usuario-01" tentar favoritar o filme não cadastrado
    Então o sistema deve rejeitar informando filme não encontrado