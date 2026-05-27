package br.com.cinema.frame.pedido;

import java.util.UUID;

public record AdicionarProdutoRequest(UUID produtoId, int quantidade) {}
