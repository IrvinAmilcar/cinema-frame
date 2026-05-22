package br.com.cinema.frame.sala;

public record SalaRequest(
    int numero,
    int capacidade,
    String tipo
) {}
