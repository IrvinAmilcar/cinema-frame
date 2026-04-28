# language: pt

Funcionalidade: Explorar programação de filmes

  Cenário: Listar sessões futuras disponíveis
    Dado que existe uma sessão futura cadastrada para o filme "Oppenheimer"
    E existe uma sessão passada cadastrada para o filme "Barbie"
    Quando o cliente consultar a programação disponível
    Então o filme "Oppenheimer" deve aparecer na listagem
    E o filme "Barbie" não deve aparecer na listagem

  Cenário: Não listar nada quando não há sessões futuras
    Dado que existe uma sessão passada cadastrada para o filme "Barbie"
    Quando o cliente consultar a programação disponível
    Então a listagem de sessões deve estar vazia

  Cenário: Filtrar sessões por gênero
    Dado que existe uma sessão futura cadastrada para o filme "Oppenheimer" do gênero "DRAMA"
    E existe uma sessão futura cadastrada para o filme "Velozes e Furiosos" do gênero "ACAO"
    Quando o cliente filtrar a programação pelo gênero "DRAMA"
    Então o filme "Oppenheimer" deve aparecer na listagem
    E o filme "Velozes e Furiosos" não deve aparecer na listagem

  Cenário: Filtrar sessões por classificação indicativa
    Dado que existe uma sessão futura cadastrada para o filme "Oppenheimer" com classificação "QUATORZE"
    E existe uma sessão futura cadastrada para o filme "It" com classificação "DEZOITO"
    Quando o cliente filtrar a programação pela classificação máxima "QUATORZE"
    Então o filme "Oppenheimer" deve aparecer na listagem
    E o filme "It" não deve aparecer na listagem