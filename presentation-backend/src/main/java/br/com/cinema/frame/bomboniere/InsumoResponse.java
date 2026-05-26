package br.com.cinema.frame.bomboniere;

import java.util.UUID;

public record InsumoResponse(
        UUID id,
        String nome,
        String unidade, 
        Double quantidadeEmEstoque,
        Double nivelCritico
) {
}