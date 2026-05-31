package br.com.cinema.frame.domain.portal.pedido;

import java.util.List;
import java.util.Optional;

public class ResultadoDoPedido {

    private final List<QRCode> qrCodes;
    private final Voucher voucher;
    private boolean limitePontosAtingido = false;

    public ResultadoDoPedido(List<QRCode> qrCodes, Voucher voucher) {
        if (qrCodes == null || qrCodes.isEmpty())
            throw new IllegalArgumentException("QRCodes não podem ser nulos ou vazios");
        this.qrCodes = List.copyOf(qrCodes);
        this.voucher = voucher;
    }

    public void setLimitePontosAtingido(boolean limitePontosAtingido) {
        this.limitePontosAtingido = limitePontosAtingido;
    }

    public boolean isLimitePontosAtingido() { return limitePontosAtingido; }
    public List<QRCode> getQrCodes() { return qrCodes; }
    public Optional<Voucher> getVoucher() { return Optional.ofNullable(voucher); }
    public boolean temVoucher() { return voucher != null; }
}
