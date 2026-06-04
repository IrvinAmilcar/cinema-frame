package br.com.cinema.frame.caixa;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record FecharCaixaRequest(
        LocalDate data,
        LocalDateTime momentoFechamento,
        List<VendaDiaRequest> vendas
) {}