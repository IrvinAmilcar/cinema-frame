package br.com.cinema.frame.domain.portal.reserva;

import br.com.cinema.frame.domain.backoffice.grade.Sessao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class ReservaDeAssento {

    private static final int MINUTOS_EXPIRACAO = 10;

    private final UUID id;
    private final Sessao sessao;
    private final int numeroAssento;
    private StatusReserva status;
    private final LocalDateTime criadaEm;
    private final LocalDateTime expiracaoEm;
    private final LocalDate dataOcorrencia;

    public ReservaDeAssento(Sessao sessao, int numeroAssento, LocalDateTime agora) {
        this(sessao, numeroAssento, agora, agora.toLocalDate());
    }

    public ReservaDeAssento(Sessao sessao, int numeroAssento, LocalDateTime agora, LocalDate dataOcorrencia) {
        if (sessao == null)
            throw new IllegalArgumentException("Sessão não pode ser nula");
        if (numeroAssento <= 0)
            throw new IllegalArgumentException("Número do assento deve ser positivo");
        if (agora == null)
            throw new IllegalArgumentException("Horário de criação não pode ser nulo");

        this.id = UUID.randomUUID();
        this.sessao = sessao;
        this.numeroAssento = numeroAssento;
        this.status = StatusReserva.RESERVADO;
        this.criadaEm = agora;
        this.expiracaoEm = agora.plusMinutes(MINUTOS_EXPIRACAO);
        this.dataOcorrencia = dataOcorrencia != null ? dataOcorrencia : agora.toLocalDate();
    }

    public boolean estaExpirada(LocalDateTime agora) {
        return agora.isAfter(expiracaoEm);
    }

    public boolean estaAtiva() {
        return status == StatusReserva.RESERVADO;
    }

    public boolean estaOcupado() {
        return status == StatusReserva.RESERVADO || status == StatusReserva.CONFIRMADO;
    }

    public void confirmar(LocalDateTime agora) {
        if (status == StatusReserva.CONFIRMADO) return; // já confirmado, idempotente
        if (status != StatusReserva.RESERVADO)
            throw new IllegalStateException("Reserva não está no estado RESERVADO");
        if (estaExpirada(agora))
            throw new IllegalStateException("Reserva expirada, não é possível confirmar");

        this.status = StatusReserva.CONFIRMADO;
    }

    public void expirar() {
        if (status != StatusReserva.RESERVADO)
            throw new IllegalStateException("Somente reservas RESERVADAS podem ser expiradas");

        this.status = StatusReserva.EXPIRADO;
    }

    public void cancelar() {
        if (status == StatusReserva.CONFIRMADO)
            throw new IllegalStateException("Reserva já confirmada não pode ser cancelada");
        if (status == StatusReserva.EXPIRADO)
            throw new IllegalStateException("Reserva já expirada não pode ser cancelada");

        this.status = StatusReserva.CANCELADO;
    }

    private ReservaDeAssento(UUID id, Sessao sessao, int numeroAssento,
                              StatusReserva status, LocalDateTime criadaEm,
                              LocalDateTime expiracaoEm, LocalDate dataOcorrencia) {
        this.id = id;
        this.sessao = sessao;
        this.numeroAssento = numeroAssento;
        this.status = status;
        this.criadaEm = criadaEm;
        this.expiracaoEm = expiracaoEm;
        this.dataOcorrencia = dataOcorrencia != null ? dataOcorrencia : criadaEm.toLocalDate();
    }

    public static ReservaDeAssento reconstituir(UUID id, Sessao sessao, int numeroAssento,
                                                 StatusReserva status, LocalDateTime criadaEm,
                                                 LocalDateTime expiracaoEm, LocalDate dataOcorrencia) {
        return new ReservaDeAssento(id, sessao, numeroAssento, status, criadaEm, expiracaoEm, dataOcorrencia);
    }

    public UUID getId() { return id; }
    public Sessao getSessao() { return sessao; }
    public int getNumeroAssento() { return numeroAssento; }
    public StatusReserva getStatus() { return status; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public LocalDateTime getExpiracaoEm() { return expiracaoEm; }
    public LocalDate getDataOcorrencia() { return dataOcorrencia; }
}