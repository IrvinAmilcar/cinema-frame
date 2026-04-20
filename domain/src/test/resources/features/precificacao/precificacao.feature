# language: pt

Funcionalidade: Precificação de sessões

  # Testes de tipo de sala sem desconto (sexta-feira)
  Cenário: Preço base para sala padrão em dia sem desconto
    Dado que existe uma sessão numa sala padrão numa sexta-feira
    Quando o sistema calcular o preço
    Então o preço deve ser R$ 20,00

  Cenário: Preço para sala 3D em dia sem desconto
    Dado que existe uma sessão numa sala 3D numa sexta-feira
    Quando o sistema calcular o preço
    Então o preço deve ser R$ 30,00

  Cenário: Preço para sala IMAX em dia sem desconto
    Dado que existe uma sessão numa sala IMAX numa sexta-feira
    Quando o sistema calcular o preço
    Então o preço deve ser R$ 44,00

  Cenário: Preço para sala VIP em dia sem desconto
    Dado que existe uma sessão numa sala VIP numa sexta-feira
    Quando o sistema calcular o preço
    Então o preço deve ser R$ 60,00

  # Testes de desconto semanal com sala padrão
  Cenário: Desconto de segunda-feira para sala padrão
    Dado que existe uma sessão numa sala padrão numa segunda-feira
    Quando o sistema calcular o preço
    Então o preço deve ser R$ 14,00

  Cenário: Desconto de terça-feira para sala padrão
    Dado que existe uma sessão numa sala padrão numa terça-feira
    Quando o sistema calcular o preço
    Então o preço deve ser R$ 10,00

  Cenário: Desconto de quarta-feira para sala padrão
    Dado que existe uma sessão numa sala padrão numa quarta-feira
    Quando o sistema calcular o preço
    Então o preço deve ser R$ 14,00

  Cenário: Sem desconto na quinta-feira para sala padrão
    Dado que existe uma sessão numa sala padrão numa quinta-feira
    Quando o sistema calcular o preço
    Então o preço deve ser R$ 20,00

  Cenário: Sem desconto no sábado para sala padrão
    Dado que existe uma sessão numa sala padrão num sábado
    Quando o sistema calcular o preço
    Então o preço deve ser R$ 20,00

  Cenário: Sem desconto no domingo para sala padrão
    Dado que existe uma sessão numa sala padrão num domingo
    Quando o sistema calcular o preço
    Então o preço deve ser R$ 20,00

  # Testes combinados tipo de sala + desconto semanal
  Cenário: Sala 3D com desconto de terça-feira
    Dado que existe uma sessão numa sala 3D numa terça-feira
    Quando o sistema calcular o preço
    Então o preço deve ser R$ 15,00

  Cenário: Sala IMAX com desconto de terça-feira
    Dado que existe uma sessão numa sala IMAX numa terça-feira
    Quando o sistema calcular o preço
    Então o preço deve ser R$ 22,00

  Cenário: Sala VIP com desconto de terça-feira
    Dado que existe uma sessão numa sala VIP numa terça-feira
    Quando o sistema calcular o preço
    Então o preço deve ser R$ 30,00

  Cenário: Sala 3D com desconto de segunda-feira
    Dado que existe uma sessão numa sala 3D numa segunda-feira
    Quando o sistema calcular o preço
    Então o preço deve ser R$ 21,00

  Cenário: Sala IMAX com desconto de segunda-feira
    Dado que existe uma sessão numa sala IMAX numa segunda-feira
    Quando o sistema calcular o preço
    Então o preço deve ser R$ 30,80

  Cenário: Sala VIP com desconto de segunda-feira
    Dado que existe uma sessão numa sala VIP numa segunda-feira
    Quando o sistema calcular o preço
    Então o preço deve ser R$ 42,00