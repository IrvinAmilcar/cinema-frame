package br.com.cinema.frame.domain.backoffice.reserva;

import br.com.cinema.frame.domain.backoffice.grade.Sessao;

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

    public ReservaDeAssento(Sessao sessao, int numeroAssento, LocalDateTime agora) {
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
    }

    public boolean estaExpirada(LocalDateTime agora) {
        return agora.isAfter(expiracaoEm);
    }

    public boolean estaAtiva() {
        return status == StatusReserva.RESERVADO;
    }

    public void confirmar(LocalDateTime agora) {
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

    public UUID getId() { return id; }
    public Sessao getSessao() { return sessao; }
    public int getNumeroAssento() { return numeroAssento; }
    public StatusReserva getStatus() { return status; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public LocalDateTime getExpiracaoEm() { return expiracaoEm; }
}