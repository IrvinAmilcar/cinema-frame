package br.com.cinema.frame.bomboniere;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProdutoBomboniereResponse(
        UUID id,
        String nome,
        BigDecimal preco,
        String categoria,
        boolean ativo,
        List<ItemReceitaResponse> receita
) {
    public record ItemReceitaResponse(
        String insumoId,
        String insumoNome,
        double quantidade
    ) {}
}