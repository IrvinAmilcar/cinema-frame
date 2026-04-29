# language: pt

Funcionalidade: Venda casada de combos (ingresso + bomboniere)

  Cenário: Finalizar pedido com ingresso e combo gera QRCode e Voucher
    Dado que existe uma sessão cadastrada para o pedido
    E existe o produto cadastrado "Pipoca Grande" com 1000g de "Milho" em estoque e 1 "Embalagem" em estoque
    Quando o cliente adicionar 1 ingresso inteiro ao pedido cadastrado
    E o cliente adicionar o produto cadastrado "Pipoca Grande" ao pedido cadastrado
    E o cliente finalizar o pedido cadastrado
    Então o resultado deve conter um QRCode
    E o resultado deve conter um Voucher

  Cenário: Finalizar pedido só com ingresso gera apenas QRCode
    Dado que existe uma sessão cadastrada para o pedido
    Quando o cliente adicionar 1 ingresso inteiro ao pedido cadastrado
    E o cliente finalizar o pedido cadastrado sem produtos da bomboniere
    Então o resultado deve conter um QRCode
    E o resultado não deve conter Voucher

  Cenário: Impedir pedido sem nenhum ingresso
    Dado que existe uma sessão cadastrada para o pedido
    Quando o cliente tentar finalizar o pedido cadastrado sem ingressos
    Então o sistema deve rejeitar informando que o pedido precisa de ao menos um ingresso

  Cenário: Finalizar pedido desconta estoque da bomboniere
    Dado que existe uma sessão cadastrada para o pedido
    E existe o produto cadastrado "Pipoca Grande" com 1000g de "Milho" em estoque e 1 "Embalagem" em estoque
    Quando o cliente adicionar 1 ingresso inteiro ao pedido cadastrado
    E o cliente adicionar o produto cadastrado "Pipoca Grande" ao pedido cadastrado
    E o cliente finalizar o pedido cadastrado
    Então o estoque de "Milho" deve ter sido reduzido

  Cenário: Impedir pedido se estoque da bomboniere for insuficiente
    Dado que existe uma sessão cadastrada para o pedido
    E existe o produto cadastrado "Pipoca Grande" com 100g de "Milho" em estoque e 1 "Embalagem" em estoque
    Quando o cliente adicionar 1 ingresso inteiro ao pedido cadastrado
    E o cliente adicionar o produto cadastrado "Pipoca Grande" ao pedido cadastrado
    E o cliente tentar finalizar o pedido cadastrado com estoque insuficiente
    Então o sistema deve rejeitar informando estoque insuficiente

  Cenário: Impedir finalização de pedido com pagamento recusado
    Dado que existe uma sessão cadastrada para o pedido
    Quando o cliente adicionar 1 ingresso inteiro ao pedido cadastrado
    E o cliente tentar finalizar o pedido cadastrado com pagamento recusado
    Então o sistema deve rejeitar informando pagamento não aprovado

  Cenário: Impedir meia-entrada sem elegibilidade comprovada
    Dado que existe uma sessão cadastrada para o pedido
    Quando o cliente tentar adicionar ingresso meia sem elegibilidade ao pedido cadastrado
    Então o sistema deve rejeitar informando elegibilidade não comprovada

  Cenário: Ingressos são persistidos no repositório ao finalizar pedido
    Dado que existe uma sessão cadastrada para o pedido
    E existe um cliente identificado com ID válido para o pedido
    Quando o cliente adicionar 1 ingresso inteiro ao pedido cadastrado
    E o cliente finalizar o pedido cadastrado com valor 20,0 e data atual
    Então o ingresso deve ser salvo no repositório de ingressos

  Cenário: Pontos de fidelidade são acumulados ao finalizar pedido
    Dado que existe uma sessão cadastrada para o pedido
    E existe um cliente identificado com ID válido para o pedido
    Quando o cliente adicionar 1 ingresso inteiro ao pedido cadastrado
    E o cliente finalizar o pedido cadastrado com valor 20,0 e data atual
    Então o cliente deve ter acumulado pontos de fidelidade

  Cenário: Gênero do filme é registrado no histórico de compras ao finalizar pedido
    Dado que existe uma sessão cadastrada para o pedido
    E existe um cliente identificado com ID válido para o pedido
    Quando o cliente adicionar 1 ingresso inteiro ao pedido cadastrado
    E o cliente finalizar o pedido cadastrado com valor 20,0 e data atual
    Então o gênero do filme deve ser registrado no histórico do cliente

  Cenário: Impedir compra de ingresso para filme com restrição de idade
    Dado que existe uma sessão com filme classificado para maiores de 16 anos para o pedido
    Quando o cliente tentar adicionar ingresso com data de nascimento de 14 anos ao pedido
    Então o sistema deve rejeitar informando idade insuficiente para o filme

  Cenário: Reserva de assento é confirmada ao finalizar pedido
    Dado que existe uma sessão cadastrada para o pedido
    E existe um cliente identificado com ID válido para o pedido
    E o cliente tem uma reserva ativa vinculada ao pedido
    Quando o cliente adicionar 1 ingresso inteiro ao pedido cadastrado
    E o cliente finalizar o pedido cadastrado com valor 20,0 e data atual
    Então a reserva vinculada deve ter status CONFIRMADO