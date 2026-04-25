package br.com.cinema.frame.domain.portal.pedido;

import java.util.Optional;

public class ResultadoDoPedido {

    private final QRCode qrCode;
    private final Voucher voucher;

    public ResultadoDoPedido(QRCode qrCode, Voucher voucher) {
        if (qrCode == null)
            throw new IllegalArgumentException("QRCode não pode ser nulo");

        this.qrCode = qrCode;
        this.voucher = voucher;
    }

    public QRCode getQrCode() { return qrCode; }

    public Optional<Voucher> getVoucher() { return Optional.ofNullable(voucher); }

    public boolean temVoucher() { return voucher != null; }
}