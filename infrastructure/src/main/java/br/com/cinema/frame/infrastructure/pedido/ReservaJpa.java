package br.com.cinema.frame.infrastructure.pedido;

import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.portal.reserva.ReservaDeAssento;
import br.com.cinema.frame.domain.portal.reserva.StatusReserva;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservas")
public class ReservaJpa {

    @Id
    private UUID id;
    private UUID sessaoId;
    private int numeroAssento;
    private String status;
    private LocalDateTime criadaEm;
    private LocalDateTime expiracaoEm;
    private LocalDate dataOcorrencia;

    protected ReservaJpa() {}

    public static ReservaJpa fromDomain(ReservaDeAssento r) {
        ReservaJpa e = new ReservaJpa();
        e.id = r.getId();
        e.sessaoId = r.getSessao().getId();
        e.numeroAssento = r.getNumeroAssento();
        e.status = r.getStatus().name();
        e.criadaEm = r.getCriadaEm();
        e.expiracaoEm = r.getExpiracaoEm();
        e.dataOcorrencia = r.getDataOcorrencia();
        return e;
    }

    public ReservaDeAssento toDomain(Sessao sessao) {
        return ReservaDeAssento.reconstituir(id, sessao, numeroAssento,
                StatusReserva.valueOf(status), criadaEm, expiracaoEm, dataOcorrencia);
    }

    public UUID getId() { return id; }
    public UUID getSessaoId() { return sessaoId; }
    public int getNumeroAssento() { return numeroAssento; }
    public String getStatus() { return status; }
    public LocalDate getDataOcorrencia() { return dataOcorrencia; }
}
