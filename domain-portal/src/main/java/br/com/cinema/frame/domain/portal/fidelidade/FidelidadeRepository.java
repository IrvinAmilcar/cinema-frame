package br.com.cinema.frame.domain.portal.fidelidade;

import java.util.Optional;
import java.util.UUID;

public interface FidelidadeRepository {
    void salvar(PontosCliente pontosCliente);
    Optional<PontosCliente> buscarPorCliente(UUID clienteId);
}
