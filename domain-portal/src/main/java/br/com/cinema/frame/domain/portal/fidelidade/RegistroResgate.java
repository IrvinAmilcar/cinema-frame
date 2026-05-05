package br.com.cinema.frame.domain.portal.fidelidade;

import java.time.LocalDate;
import java.util.UUID;

public class RegistroResgate {

    private final UUID beneficioId;
    private final int pontosDebitados;
    private final LocalDate data;

    public RegistroResgate(UUID beneficioId, int pontosDebitados, LocalDate data) {
        if (beneficioId == null) throw new IllegalArgumentException("BeneficioId é obrigatório");
        if (pontosDebitados <= 0) throw new IllegalArgumentException("Pontos debitados devem ser positivos");
        if (data == null) throw new IllegalArgumentException("Data é obrigatória");
        this.beneficioId = beneficioId;
        this.pontosDebitados = pontosDebitados;
        this.data = data;
    }

    public UUID getBeneficioId() { return beneficioId; }
    public int getPontosDebitados() { return pontosDebitados; }
    public LocalDate getData() { return data; }
}
