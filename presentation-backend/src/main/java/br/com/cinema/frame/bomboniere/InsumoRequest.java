package br.com.cinema.frame.bomboniere;

public record InsumoRequest(
        String nome,
        String unidade, 
        Integer quantidade,
        Integer nivelCritico
) {
}