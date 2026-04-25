package br.com.cinema.frame.domain.portal.reserva;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservaRepository {

    void salvar(ReservaDeAssento reserva);
    Optional<ReservaDeAssento> buscarPorId(UUID id);
    List<ReservaDeAssento> buscarPorSessaoId(UUID sessaoId);
    void remover(UUID id);
}