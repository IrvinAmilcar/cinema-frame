# language: pt

Funcionalidade: Gerenciar Bomboniere

  # ── Cadastro de Produto ──────────────────────────────────

  Cenário: Cadastrar produto com atributos obrigatórios
    Quando o sistema cadastrar o produto "Pipoca Grande" com preço 20,0 e categoria "COMBO"
    Então o produto deve ser salvo no repositório

  Cenário: Impedir cadastro de produto sem nome
    Quando o sistema tentar cadastrar um produto com nome vazio
    Então o sistema deve rejeitar informando nome inválido

  # ── Venda e baixa de estoque ─────────────────────────────

  Cenário: Venda de produto ativo dá baixa automática nos insumos
    Dado que existe o insumo cadastrado "Milho" com 1000g em estoque e nível crítico de 200g
    E existe o insumo cadastrado "Embalagem" com 50 unidades em estoque e nível crítico de 10 unidades
    E existe o produto ativo cadastrado "Pipoca Grande" com receita de 200g de "Milho" e 1 "Embalagem"
    Quando o sistema registrar a venda do produto "Pipoca Grande"
    Então o estoque de "Milho" deve ser 800g
    E o estoque de "Embalagem" deve ser 49 unidades

  Cenário: Venda registra movimentação de saída para rastreabilidade
    Dado que existe o insumo cadastrado "Milho" com 1000g em estoque e nível crítico de 200g
    E existe o insumo cadastrado "Embalagem" com 50 unidades em estoque e nível crítico de 10 unidades
    E existe o produto ativo cadastrado "Pipoca Grande" com receita de 200g de "Milho" e 1 "Embalagem"
    Quando o sistema registrar a venda do produto "Pipoca Grande"
    Então deve ter sido registrada uma movimentação de saída para "Milho"

  # ── Produto inativo ──────────────────────────────────────

  Cenário: Impedir venda de produto inativo
    Dado que existe o insumo cadastrado "Milho" com 1000g em estoque e nível crítico de 200g
    E existe o insumo cadastrado "Embalagem" com 50 unidades em estoque e nível crítico de 10 unidades
    E existe o produto inativo cadastrado "Pipoca Grande" com receita de 200g de "Milho" e 1 "Embalagem"
    Quando o sistema tentar registrar a venda do produto "Pipoca Grande"
    Então o sistema deve rejeitar informando produto indisponível

  # ── Estoque insuficiente ─────────────────────────────────

  Cenário: Impedir venda com estoque insuficiente
    Dado que existe o insumo cadastrado "Milho" com 100g em estoque e nível crítico de 50g
    E existe o insumo cadastrado "Embalagem" com 50 unidades em estoque e nível crítico de 10 unidades
    E existe o produto ativo cadastrado "Pipoca Grande" com receita de 200g de "Milho" e 1 "Embalagem"
    Quando o sistema tentar registrar a venda do produto "Pipoca Grande"
    Então o sistema deve rejeitar informando estoque insuficiente

  # ── Cancelamento / Estorno ───────────────────────────────

  Cenário: Estorno de venda restaura o estoque dos insumos
    Dado que existe o insumo cadastrado "Milho" com 800g em estoque e nível crítico de 200g
    E existe o insumo cadastrado "Embalagem" com 49 unidades em estoque e nível crítico de 10 unidades
    E existe o produto ativo cadastrado "Pipoca Grande" com receita de 200g de "Milho" e 1 "Embalagem"
    Quando o sistema estornar a venda do produto "Pipoca Grande"
    Então o estoque de "Milho" deve ser 1000g
    E o estoque de "Embalagem" deve ser 50 unidades

  Cenário: Estorno registra movimentação de entrada para rastreabilidade
    Dado que existe o insumo cadastrado "Milho" com 800g em estoque e nível crítico de 200g
    E existe o insumo cadastrado "Embalagem" com 49 unidades em estoque e nível crítico de 10 unidades
    E existe o produto ativo cadastrado "Pipoca Grande" com receita de 200g de "Milho" e 1 "Embalagem"
    Quando o sistema estornar a venda do produto "Pipoca Grande"
    Então deve ter sido registrada uma movimentação de entrada para "Milho"

  # ── Alerta de estoque crítico ────────────────────────────

  Cenário: Notificar quando estoque atingir nível crítico após venda
    Dado que existe o insumo cadastrado "Milho" com 200g em estoque e nível crítico de 200g
    E existe o insumo cadastrado "Embalagem" com 50 unidades em estoque e nível crítico de 10 unidades
    E existe o produto ativo cadastrado "Pipoca Grande" com receita de 200g de "Milho" e 1 "Embalagem"
    Quando o sistema registrar a venda do produto "Pipoca Grande"
    Então o sistema deve emitir notificação de estoque crítico para "Milho"