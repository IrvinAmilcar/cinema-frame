package br.com.cinema.frame.domain.backoffice.reserva;

import br.com.cinema.frame.domain.backoffice.grade.Sessao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class GestaoDeReservas {

    private final List<ReservaDeAssento> reservas = new ArrayList<>();

    public ReservaDeAssento reservar(Sessao sessao, int numeroAssento, LocalDateTime agora) {
        if (sessao == null)
            throw new IllegalArgumentException("Sessão não pode ser nula");
        if (agora == null)
            throw new IllegalArgumentException("Horário não pode ser nulo");

        liberarExpiradas(agora);

        boolean assentoOcupado = reservas.stream()
            .anyMatch(r -> r.getSessao().getId().equals(sessao.getId())
                       && r.getNumeroAssento() == numeroAssento
                       && r.estaAtiva());

        if (assentoOcupado)
            throw new IllegalStateException("Assento " + numeroAssento + " já está reservado para esta sessão");

        ReservaDeAssento reserva = new ReservaDeAssento(sessao, numeroAssento, agora);
        reservas.add(reserva);
        return reserva;
    }

    public void confirmar(UUID reservaId, LocalDateTime agora) {
        if (reservaId == null)
            throw new IllegalArgumentException("ID da reserva não pode ser nulo");
        if (agora == null)
            throw new IllegalArgumentException("Horário não pode ser nulo");

        ReservaDeAssento reserva = buscarPorId(reservaId);
        reserva.confirmar(agora);
    }

    public void liberarExpiradas(LocalDateTime agora) {
        reservas.stream()
            .filter(r -> r.estaAtiva() && r.estaExpirada(agora))
            .forEach(ReservaDeAssento::expirar);
    }

    private ReservaDeAssento buscarPorId(UUID id) {
        return reservas.stream()
            .filter(r -> r.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Reserva não encontrada: " + id));
    }

    public List<ReservaDeAssento> getReservas() {
        return Collections.unmodifiableList(reservas);
    }
}