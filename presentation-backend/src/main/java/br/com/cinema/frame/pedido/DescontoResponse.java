package br.com.cinema.frame.pedido;

import br.com.cinema.frame.domain.portal.promocao.AplicacaoDeDesconto;

public record DescontoResponse(double valorOriginal, double valorDesconto, double valorFinal, boolean cumulativo) {
    public static DescontoResponse from(AplicacaoDeDesconto d, boolean cumulativo) {
        return new DescontoResponse(d.getValorOriginal(), d.getValorDesconto(), d.getValorFinal(), cumulativo);
    }
}
