package br.com.cinema.frame.domain.backoffice.checkin;

import java.time.LocalDateTime;
import java.util.UUID;

public class RegistroDeEntrada {

    private final UUID id;
    private final UUID ingressoId;
    private final UUID sessaoId;
    private final LocalDateTime momentoEntrada;

    public RegistroDeEntrada(UUID ingressoId, UUID sessaoId, LocalDateTime momentoEntrada) {
        if (ingressoId == null)
            throw new IllegalArgumentException("ID do ingresso não pode ser nulo");
        if (sessaoId == null)
            throw new IllegalArgumentException("ID da sessão não pode ser nulo");
        if (momentoEntrada == null)
            throw new IllegalArgumentException("Momento da entrada não pode ser nulo");

        this.id = UUID.randomUUID();
        this.ingressoId = ingressoId;
        this.sessaoId = sessaoId;
        this.momentoEntrada = momentoEntrada;
    }

    public UUID getId() { return id; }
    public UUID getIngressoId() { return ingressoId; }
    public UUID getSessaoId() { return sessaoId; }
    public LocalDateTime getMomentoEntrada() { return momentoEntrada; }
}