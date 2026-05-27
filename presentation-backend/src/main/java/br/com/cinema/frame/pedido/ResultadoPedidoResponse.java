package br.com.cinema.frame.pedido;

import br.com.cinema.frame.domain.portal.pedido.ResultadoDoPedido;

import java.util.List;

public record ResultadoPedidoResponse(List<String> qrCodes, String voucher) {
    public static ResultadoPedidoResponse from(ResultadoDoPedido r) {
        return new ResultadoPedidoResponse(
                r.getQrCodes().stream().map(qr -> qr.getCodigo()).toList(),
                r.getVoucher().map(v -> v.getCodigo()).orElse(null)
        );
    }
}
