package br.com.cinema.frame.caixa;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record FechamentoCaixaResponse(
        UUID id,
        LocalDate data,
        double totalVendas,
        int totalIngressos,
        int totalSessoes,
        double taxaOcupacaoMedia,
        LocalDateTime momentoFechamento
) {}
