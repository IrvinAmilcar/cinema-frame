package br.com.cinema.frame.bomboniere;

public record InsumoRequest(
        String nome,
        Integer quantidade,
        Integer nivelCritico
) {
}