package br.com.cinema.frame.infrastructure.checkin;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "registros_entrada")
public class RegistroDeEntradaEntity {

    @Id
    private UUID id;

    @Column(name = "ingresso_id")
    private UUID ingressoId;

    @Column(name = "sessao_id")
    private UUID sessaoId;

    @Column(name = "momento_entrada")
    private LocalDateTime momentoEntrada;

    private boolean autorizado;

    @Column(name = "motivo_recusa")
    private String motivoRecusa;

    public RegistroDeEntradaEntity() {}

    public RegistroDeEntradaEntity(UUID id, UUID ingressoId, UUID sessaoId,
                                    LocalDateTime momentoEntrada,
                                    boolean autorizado, String motivoRecusa) {
        this.id = id;
        this.ingressoId = ingressoId;
        this.sessaoId = sessaoId;
        this.momentoEntrada = momentoEntrada;
        this.autorizado = autorizado;
        this.motivoRecusa = motivoRecusa;
    }

    public UUID getId() { return id; }
    public UUID getIngressoId() { return ingressoId; }
    public UUID getSessaoId() { return sessaoId; }
    public LocalDateTime getMomentoEntrada() { return momentoEntrada; }
    public boolean isAutorizado() { return autorizado; }
    public String getMotivoRecusa() { return motivoRecusa; }
}