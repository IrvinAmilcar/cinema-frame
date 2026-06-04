package br.com.cinema.frame.caixa;

import java.util.UUID;

public record VendaDiaRequest(
        UUID sessaoId,
        int capacidadeSala,
        int ingressosVendidos,
        double valorArrecadado
) {}
