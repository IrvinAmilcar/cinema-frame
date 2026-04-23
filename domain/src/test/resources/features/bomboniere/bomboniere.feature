# language: pt

Funcionalidade: Gestão de inventário de insumos da bomboniere

  Cenário: Venda de combo dá baixa automática nos insumos
    Dado que existe o insumo cadastrado "Milho" com 1000g em estoque e nível crítico de 200g
    E existe o insumo cadastrado "Embalagem" com 50 unidades em estoque e nível crítico de 10 unidades
    E existe o produto cadastrado "Pipoca Grande" com receita de 200g de "Milho" e 1 "Embalagem"
    Quando o sistema registrar a venda do produto cadastrado "Pipoca Grande"
    Então o estoque de "Milho" deve ser 800g
    E o estoque de "Embalagem" deve ser 49 unidades

  Cenário: Notificar quando estoque atingir nível crítico
    Dado que existe o insumo cadastrado "Milho" com 200g em estoque e nível crítico de 200g
    E existe o insumo cadastrado "Embalagem" com 50 unidades em estoque e nível crítico de 10 unidades
    E existe o produto cadastrado "Pipoca Grande" com receita de 200g de "Milho" e 1 "Embalagem"
    Quando o sistema registrar a venda do produto cadastrado "Pipoca Grande"
    Então o sistema deve emitir notificação de estoque crítico para "Milho"

  Cenário: Impedir venda com estoque insuficiente
    Dado que existe o insumo cadastrado "Milho" com 100g em estoque e nível crítico de 50g
    E existe o insumo cadastrado "Embalagem" com 50 unidades em estoque e nível crítico de 10 unidades
    E existe o produto cadastrado "Pipoca Grande" com receita de 200g de "Milho" e 1 "Embalagem"
    Quando o sistema tentar registrar a venda do produto cadastrado "Pipoca Grande"
    Então o sistema deve rejeitar informando estoque insuficiente