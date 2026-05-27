package br.com.cinema.frame.pedido;

public record AplicarCupomRequest(String codigo, double valorTotal, int quantidadeIngressos) {}
