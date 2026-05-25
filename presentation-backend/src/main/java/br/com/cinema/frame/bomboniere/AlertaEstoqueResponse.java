package br.com.cinema.frame.bomboniere;

public record AlertaEstoqueResponse(
        String insumo,
        Integer quantidadeAtual,
        Integer nivelCritico
) {
}