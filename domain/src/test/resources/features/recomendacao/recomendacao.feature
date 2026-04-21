# language: pt

Funcionalidade: Sistema de Recomendação por Perfil

  Cenário: Recomendar filme do gênero mais assistido pelo cliente
    Dado que o cliente assistiu filmes dos gêneros "ACAO" e "ACAO" e "COMEDIA"
    E o catálogo possui um filme "Missão Impossível" do gênero "ACAO"
    E o catálogo possui um filme "Se Beber Não Case" do gênero "COMEDIA"
    Quando o sistema gerar as recomendações
    Então a lista de sugestões não deve estar vazia
    E o primeiro filme sugerido deve ser do gênero "ACAO"

  Cenário: Recomendar apenas filmes de gêneros já assistidos
    Dado que o cliente assistiu filmes dos gêneros "DRAMA"
    E o catálogo possui um filme "Titanic" do gênero "DRAMA"
    E o catálogo possui um filme "Exterminador do Futuro" do gênero "ACAO"
    Quando o sistema gerar as recomendações
    Então a lista de sugestões não deve estar vazia
    E a lista de sugestões deve conter o filme "Titanic"
    E a lista de sugestões não deve conter o filme "Exterminador do Futuro"

  Cenário: Não recomendar nada para cliente sem histórico
    Dado que o cliente não possui histórico de compras
    E o catálogo possui um filme "Avatar" do gênero "ACAO"
    Quando o sistema gerar as recomendações
    Então a lista de sugestões deve estar vazia

  Cenário: Não recomendar nada quando catálogo está vazio
    Dado que o cliente assistiu filmes dos gêneros "TERROR"
    E o catálogo está vazio
    Quando o sistema gerar as recomendações
    Então a lista de sugestões deve estar vazia

  Cenário: Ordenar sugestões pelo gênero mais frequente no histórico
    Dado que o cliente assistiu filmes dos gêneros "TERROR" e "TERROR" e "ANIMACAO"
    E o catálogo possui um filme "It" do gênero "TERROR"
    E o catálogo possui um filme "Toy Story" do gênero "ANIMACAO"
    Quando o sistema gerar as recomendações
    Então o primeiro filme sugerido deve ser do gênero "TERROR"

  Cenário: Recomendar filmes de múltiplos gêneros assistidos
    Dado que o cliente assistiu filmes dos gêneros "ROMANCE" e "SUSPENSE"
    E o catálogo possui um filme "Diário de uma Paixão" do gênero "ROMANCE"
    E o catálogo possui um filme "O Silêncio dos Inocentes" do gênero "SUSPENSE"
    E o catálogo possui um filme "Velozes e Furiosos" do gênero "ACAO"
    Quando o sistema gerar as recomendações
    Então a lista de sugestões deve conter o filme "Diário de uma Paixão"
    E a lista de sugestões deve conter o filme "O Silêncio dos Inocentes"
    E a lista de sugestões não deve conter o filme "Velozes e Furiosos"
