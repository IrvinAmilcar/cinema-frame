package br.com.cinema.frame.domain.backoffice.caixa;

import java.util.UUID;

public class VendaDia {
    private final UUID sessaoId;
    private final int capacidadeSala;
    private final int ingressosVendidos;
    private final double valorArrecadado;

    public VendaDia(UUID sessaoId, int capacidadeSala, int ingressosVendidos, double valorArrecadado) {
        if (sessaoId == null) throw new IllegalArgumentException("SessaoId é obrigatório");
        if (capacidadeSala <= 0) throw new IllegalArgumentException("Capacidade deve ser positiva");
        if (ingressosVendidos < 0) throw new IllegalArgumentException("Ingressos vendidos não pode ser negativo");
        if (ingressosVendidos > capacidadeSala) throw new IllegalArgumentException("Ingressos vendidos não pode superar capacidade");
        if (valorArrecadado < 0) throw new IllegalArgumentException("Valor arrecadado não pode ser negativo");

        this.sessaoId = sessaoId;
        this.capacidadeSala = capacidadeSala;
        this.ingressosVendidos = ingressosVendidos;
        this.valorArrecadado = valorArrecadado;
    }

    public double getOcupacaoPercentual() {
        return (double) ingressosVendidos / capacidadeSala * 100.0;
    }

    public UUID getSessaoId() { return sessaoId; }
    public int getCapacidadeSala() { return capacidadeSala; }
    public int getIngressosVendidos() { return ingressosVendidos; }
    public double getValorArrecadado() { return valorArrecadado; }
}