# language: pt

Funcionalidade: Sistema de Recomendação por Perfil

  Cenário: Recomendar filme do gênero mais assistido pelo cliente
    Dado que existe um histórico cadastrado com os gêneros "ACAO" e "ACAO" e "COMEDIA"
    E o catálogo cadastrado possui um filme "Missão Impossível" do gênero "ACAO"
    E o catálogo cadastrado possui um filme "Se Beber Não Case" do gênero "COMEDIA"
    Quando o sistema gerar as recomendações para o cliente cadastrado
    Então a lista de sugestões não deve estar vazia
    E o primeiro filme sugerido deve ser do gênero "ACAO"

  Cenário: Recomendar apenas filmes de gêneros já assistidos
    Dado que existe um histórico cadastrado com os gêneros "DRAMA"
    E o catálogo cadastrado possui um filme "Titanic" do gênero "DRAMA"
    E o catálogo cadastrado possui um filme "Exterminador do Futuro" do gênero "ACAO"
    Quando o sistema gerar as recomendações para o cliente cadastrado
    Então a lista de sugestões não deve estar vazia
    E a lista de sugestões deve conter o filme "Titanic"
    E a lista de sugestões não deve conter o filme "Exterminador do Futuro"

  Cenário: Não recomendar nada para cliente sem histórico de gêneros
    Dado que existe um histórico cadastrado vazio para o cliente
    E o catálogo cadastrado possui um filme "Avatar" do gênero "ACAO"
    Quando o sistema gerar as recomendações para o cliente cadastrado
    Então a lista de sugestões deve estar vazia

  Cenário: Não recomendar nada quando catálogo está vazio
    Dado que existe um histórico cadastrado com os gêneros "TERROR"
    E o catálogo cadastrado está vazio
    Quando o sistema gerar as recomendações para o cliente cadastrado
    Então a lista de sugestões deve estar vazia

  Cenário: Ordenar sugestões pelo gênero mais frequente no histórico
    Dado que existe um histórico cadastrado com os gêneros "TERROR" e "TERROR" e "ANIMACAO"
    E o catálogo cadastrado possui um filme "It" do gênero "TERROR"
    E o catálogo cadastrado possui um filme "Toy Story" do gênero "ANIMACAO"
    Quando o sistema gerar as recomendações para o cliente cadastrado
    Então o primeiro filme sugerido deve ser do gênero "TERROR"

  Cenário: Recomendar filmes de múltiplos gêneros assistidos
    Dado que existe um histórico cadastrado com os gêneros "ROMANCE" e "SUSPENSE"
    E o catálogo cadastrado possui um filme "Diário de uma Paixão" do gênero "ROMANCE"
    E o catálogo cadastrado possui um filme "O Silêncio dos Inocentes" do gênero "SUSPENSE"
    E o catálogo cadastrado possui um filme "Velozes e Furiosos" do gênero "ACAO"
    Quando o sistema gerar as recomendações para o cliente cadastrado
    Então a lista de sugestões deve conter o filme "Diário de uma Paixão"
    E a lista de sugestões deve conter o filme "O Silêncio dos Inocentes"
    E a lista de sugestões não deve conter o filme "Velozes e Furiosos"