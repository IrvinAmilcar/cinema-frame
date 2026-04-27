# language: pt

Funcionalidade: Gerenciamento do catálogo de filmes

  Cenário: Cadastrar novo filme no catálogo
    Dado que a gerente tem um novo filme "Duna: Parte 2" com duração de 166 minutos classificação "QUATORZE" e gênero "FICCAO_CIENTIFICA"
    Quando a gerente cadastra o filme no catálogo
    Então o filme deve ser salvo no repositório

  Cenário: Atualizar título de um filme existente
    Dado que existe um filme "Duna" com duração de 155 minutos e classificação "QUATORZE" e gênero "FICCAO_CIENTIFICA" no catálogo
    Quando a gerente atualiza o título do filme para "Duna: Parte 2"
    Então o título do filme deve ser "Duna: Parte 2"

  Cenário: Remover filme do catálogo
    Dado que existe um filme "Barbie" com duração de 114 minutos e classificação "DOZE" e gênero "COMEDIA" no catálogo
    Quando a gerente remove o filme do catálogo
    Então o filme deve ser removido do repositório

  Cenário: Impedir cadastro de filme com título vazio
    Quando a gerente tenta cadastrar um filme com título vazio
    Então o sistema deve rejeitar o cadastro informando título inválido
