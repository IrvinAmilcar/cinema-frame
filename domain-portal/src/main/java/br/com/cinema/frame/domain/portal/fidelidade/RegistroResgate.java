package br.com.cinema.frame.domain.portal.fidelidade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class RegistroResgate {

    private final UUID beneficioId;
    private final int pontosDebitados;
    private final LocalDateTime dataHora;  
    private final String nomeBeneficio;    

    public RegistroResgate(UUID beneficioId, int pontosDebitados, LocalDateTime dataHora, String nomeBeneficio) {
        if (beneficioId == null) throw new IllegalArgumentException("BeneficioId é obrigatório");
        if (pontosDebitados <= 0) throw new IllegalArgumentException("Pontos debitados devem ser positivos");
        if (dataHora == null) throw new IllegalArgumentException("Data é obrigatória");
        if (nomeBeneficio == null || nomeBeneficio.isBlank()) throw new IllegalArgumentException("Nome do benefício é obrigatório");
        this.beneficioId = beneficioId;
        this.pontosDebitados = pontosDebitados;
        this.dataHora = dataHora;
        this.nomeBeneficio = nomeBeneficio;
    }

    public RegistroResgate(UUID beneficioId, int pontosDebitados, LocalDate data) {
        this(beneficioId, pontosDebitados, data.atStartOfDay(), "Benefício");
    }

    public UUID getBeneficioId() { return beneficioId; }
    public int getPontosDebitados() { return pontosDebitados; }
    public String getNomeBeneficio() { return nomeBeneficio; }
    public LocalDateTime getDataHora() { return dataHora; }

    public LocalDate getData() { return dataHora.toLocalDate(); }
}