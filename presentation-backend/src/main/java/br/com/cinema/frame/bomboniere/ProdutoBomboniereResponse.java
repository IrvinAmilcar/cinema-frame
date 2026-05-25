package br.com.cinema.frame.bomboniere;

import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoBomboniereResponse(
        UUID id,
        String nome,
        BigDecimal preco
) {
}