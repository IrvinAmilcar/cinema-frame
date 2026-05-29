package br.com.cinema.frame.domain.backoffice.checkin;

import java.time.LocalDateTime;
import java.util.UUID;

public class RegistroDeEntrada {

    private final UUID id;
    private final UUID ingressoId; // Pode ser nulo se o QR Code for totalmente inválido
    private final UUID sessaoId;
    private final LocalDateTime momentoEntrada;
    private final boolean autorizado;
    private final String motivoRecusa; // Nulo se foi autorizado

    public RegistroDeEntrada(UUID ingressoId, UUID sessaoId, LocalDateTime momentoEntrada, boolean autorizado, String motivoRecusa) {
        this.id = UUID.randomUUID();
        this.ingressoId = ingressoId;
        this.sessaoId = sessaoId;
        this.momentoEntrada = momentoEntrada;
        this.autorizado = autorizado;
        this.motivoRecusa = motivoRecusa;
    }

    // construtor privado para reconstituição do banco
    private RegistroDeEntrada(UUID id, UUID ingressoId, UUID sessaoId,
                            LocalDateTime momentoEntrada, boolean autorizado, String motivoRecusa) {
        this.id = id;
        this.ingressoId = ingressoId;
        this.sessaoId = sessaoId;
        this.momentoEntrada = momentoEntrada;
        this.autorizado = autorizado;
        this.motivoRecusa = motivoRecusa;
    }

    public static RegistroDeEntrada reconstituir(UUID id, UUID ingressoId, UUID sessaoId,
                                                LocalDateTime momentoEntrada, boolean autorizado, String motivoRecusa) {
        return new RegistroDeEntrada(id, ingressoId, sessaoId, momentoEntrada, autorizado, motivoRecusa);
    }
    

    // Getters...
    public UUID getId() { return id; }
    public UUID getIngressoId() { return ingressoId; }
    public UUID getSessaoId() { return sessaoId; }
    public LocalDateTime getMomentoEntrada() { return momentoEntrada; }
    public boolean isAutorizado() { return autorizado; }
    public String getMotivoRecusa() { return motivoRecusa; }

    
}