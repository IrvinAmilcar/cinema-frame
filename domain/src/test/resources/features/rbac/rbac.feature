# language: pt

Funcionalidade: Controle de Permissões Baseado em Roles (RBAC)

  # Permissões do Operador de Caixa
  Cenário: Operador de caixa pode vender ingresso
    Dado que existe um funcionário cadastrado "João" com o role "OPERADOR_DE_CAIXA"
    Quando o sistema verificar a permissão "VENDER_INGRESSO" para o funcionário cadastrado
    Então o acesso deve ser permitido

  Cenário: Operador de caixa não pode estornar venda
    Dado que existe um funcionário cadastrado "João" com o role "OPERADOR_DE_CAIXA"
    Quando o sistema tentar verificar a permissão "ESTORNAR_VENDA" para o funcionário cadastrado
    Então o sistema deve rejeitar informando acesso negado

  Cenário: Operador de caixa não pode alterar preço
    Dado que existe um funcionário cadastrado "João" com o role "OPERADOR_DE_CAIXA"
    Quando o sistema tentar verificar a permissão "ALTERAR_PRECO" para o funcionário cadastrado
    Então o sistema deve rejeitar informando acesso negado

  # Permissões do Gerente
  Cenário: Gerente pode vender ingresso
    Dado que existe um funcionário cadastrado "Maria" com o role "GERENTE"
    Quando o sistema verificar a permissão "VENDER_INGRESSO" para o funcionário cadastrado
    Então o acesso deve ser permitido

  Cenário: Gerente pode estornar venda
    Dado que existe um funcionário cadastrado "Maria" com o role "GERENTE"
    Quando o sistema verificar a permissão "ESTORNAR_VENDA" para o funcionário cadastrado
    Então o acesso deve ser permitido

  Cenário: Gerente pode alterar preço
    Dado que existe um funcionário cadastrado "Maria" com o role "GERENTE"
    Quando o sistema verificar a permissão "ALTERAR_PRECO" para o funcionário cadastrado
    Então o acesso deve ser permitido

  # Consulta direta de permissão (sem lançar exceção)
  Cenário: Consultar se operador tem permissão retorna falso para estorno
    Dado que existe um funcionário cadastrado "João" com o role "OPERADOR_DE_CAIXA"
    Quando o sistema consultar se o funcionário cadastrado possui a permissão "ESTORNAR_VENDA"
    Então o resultado da consulta deve ser falso

  Cenário: Consultar se gerente tem permissão retorna verdadeiro para estorno
    Dado que existe um funcionário cadastrado "Maria" com o role "GERENTE"
    Quando o sistema consultar se o funcionário cadastrado possui a permissão "ESTORNAR_VENDA"
    Então o resultado da consulta deve ser verdadeiro