package br.com.cinema.frame.pedido;

import java.util.List;

import br.com.cinema.frame.domain.portal.pedido.ResultadoDoPedido;

public record ResultadoPedidoResponse(
        List<String> qrCodes,
        String voucher,
        boolean limitePontosAtingido) {

    public static ResultadoPedidoResponse from(ResultadoDoPedido r) {
        return new ResultadoPedidoResponse(
                r.getQrCodes().stream().map(qr -> qr.getCodigo()).toList(),
                r.getVoucher().map(v -> v.getCodigo()).orElse(null),
                r.isLimitePontosAtingido()
        );
    }
}
