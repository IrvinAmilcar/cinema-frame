package br.com.cinema.frame.domain.backoffice.pedido;

import java.util.UUID;

public class QRCode {

    private final String codigo;

    public QRCode(UUID ingressoId) {
        if (ingressoId == null)
            throw new IllegalArgumentException("ID do ingresso não pode ser nulo");

        this.codigo = "QR-" + ingressoId.toString().toUpperCase();
    }

    public String getCodigo() { return codigo; }
}