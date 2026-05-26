package br.com.cinema.frame.bomboniere;

public record InsumoRequest(
        String nome,
        String unidade, 
        Double quantidade,
        Double nivelCritico
) {
}