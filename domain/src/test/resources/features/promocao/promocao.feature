# language: pt

Funcionalidade: Motor de promoções acoplado (cupons e campanhas)

  Cenário: Aplicar cupom "Leve 2 Pague 1" com 2 ingressos
    Dado que o valor total do pedido é R$ 40,00 com 2 ingressos
    E existe um cupom cadastrado "LEVE2" do tipo "LEVE2_PAGUE1" não cumulativo e válido
    Quando o motor de promoções aplicar os cupons cadastrados
    Então o valor de desconto deve ser R$ 20,00
    E o valor final deve ser R$ 20,00

  Cenário: Cupom "Leve 2 Pague 1" não se aplica com apenas 1 ingresso
    Dado que o valor total do pedido é R$ 20,00 com 1 ingressos
    E existe um cupom cadastrado "LEVE2" do tipo "LEVE2_PAGUE1" não cumulativo e válido
    Quando o motor de promoções aplicar os cupons cadastrados
    Então o valor de desconto deve ser R$ 0,00
    E o valor final deve ser R$ 20,00

  Cenário: Aplicar desconto de parceria com cartão
    Dado que o valor total do pedido é R$ 40,00 com 2 ingressos
    E existe um cupom cadastrado "CARTAO10" do tipo "PARCERIA_CARTAO" não cumulativo e válido
    Quando o motor de promoções aplicar os cupons cadastrados
    Então o valor de desconto deve ser R$ 6,00
    E o valor final deve ser R$ 34,00

  Cenário: Aplicar desconto para estudante
    Dado que o valor total do pedido é R$ 40,00 com 2 ingressos
    E existe um cupom cadastrado "ESTUDANTE" do tipo "DESCONTO_ESTUDANTE" não cumulativo e válido
    Quando o motor de promoções aplicar os cupons cadastrados
    Então o valor de desconto deve ser R$ 8,00
    E o valor final deve ser R$ 32,00

  Cenário: Impedir dois cupons não cumulativos no mesmo pedido
    Dado que o valor total do pedido é R$ 40,00 com 2 ingressos
    E existe um cupom cadastrado "CARTAO10" do tipo "PARCERIA_CARTAO" não cumulativo e válido
    E existe um cupom cadastrado "ESTUDANTE" do tipo "DESCONTO_ESTUDANTE" não cumulativo e válido
    Quando o motor de promoções tentar aplicar os cupons cadastrados
    Então o sistema deve rejeitar informando que cupons não cumulativos não podem ser combinados

  Cenário: Permitir dois cupons cumulativos no mesmo pedido
    Dado que o valor total do pedido é R$ 40,00 com 2 ingressos
    E existe um cupom cadastrado "CARTAO10" do tipo "PARCERIA_CARTAO" cumulativo e válido
    E existe um cupom cadastrado "ESTUDANTE" do tipo "DESCONTO_ESTUDANTE" cumulativo e válido
    Quando o motor de promoções aplicar os cupons cadastrados
    Então o valor de desconto deve ser R$ 14,00
    E o valor final deve ser R$ 26,00

  Cenário: Rejeitar cupom expirado
    Dado que o valor total do pedido é R$ 40,00 com 2 ingressos
    E existe um cupom cadastrado "VELHO" do tipo "PARCERIA_CARTAO" não cumulativo e expirado
    Quando o motor de promoções tentar aplicar os cupons cadastrados
    Então o sistema deve rejeitar informando que o cupom está expirado