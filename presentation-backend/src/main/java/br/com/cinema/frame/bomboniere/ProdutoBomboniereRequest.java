package br.com.cinema.frame.bomboniere;

import java.math.BigDecimal;

public record ProdutoBomboniereRequest(
        String nome,
        BigDecimal preco
) {
}