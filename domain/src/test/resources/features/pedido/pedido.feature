# language: pt

Funcionalidade: Venda casada de combos (ingresso + bomboniere)

  Cenário: Finalizar pedido com ingresso e combo gera QRCode e Voucher
    Dado que existe uma sessão para o pedido
    E existe o produto "Pipoca Grande" com 1000g de "Milho" em estoque e 1 "Embalagem" em estoque
    Quando o cliente adicionar 1 ingresso inteiro ao pedido
    E o cliente adicionar o produto "Pipoca Grande" ao pedido
    E o cliente finalizar o pedido
    Então o resultado deve conter um QRCode
    E o resultado deve conter um Voucher

  Cenário: Finalizar pedido só com ingresso gera apenas QRCode
    Dado que existe uma sessão para o pedido
    Quando o cliente adicionar 1 ingresso inteiro ao pedido
    E o cliente finalizar o pedido sem produtos da bomboniere
    Então o resultado deve conter um QRCode
    E o resultado não deve conter Voucher

  Cenário: Impedir pedido sem nenhum ingresso
    Dado que existe uma sessão para o pedido
    Quando o cliente tentar finalizar o pedido sem ingressos
    Então o sistema deve rejeitar informando que o pedido precisa de ao menos um ingresso

  Cenário: Finalizar pedido desconta estoque da bomboniere
    Dado que existe uma sessão para o pedido
    E existe o produto "Pipoca Grande" com 1000g de "Milho" em estoque e 1 "Embalagem" em estoque
    Quando o cliente adicionar 1 ingresso inteiro ao pedido
    E o cliente adicionar o produto "Pipoca Grande" ao pedido
    E o cliente finalizar o pedido
    Então o estoque de "Milho" deve ter sido reduzido

  Cenário: Impedir pedido se estoque da bomboniere for insuficiente
    Dado que existe uma sessão para o pedido
    E existe o produto "Pipoca Grande" com 100g de "Milho" em estoque e 1 "Embalagem" em estoque
    Quando o cliente adicionar 1 ingresso inteiro ao pedido
    E o cliente adicionar o produto "Pipoca Grande" ao pedido
    E o cliente tentar finalizar o pedido com estoque insuficiente
    Então o sistema deve rejeitar informando estoque insuficiente