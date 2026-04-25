package br.com.cinema.frame.domain.portal.fidelidade;

import java.util.Optional;
import java.util.UUID;

public interface ContaDeFidelidadeRepository {

    void salvar(ContaDeFidelidade conta);
    Optional<ContaDeFidelidade> buscarPorId(UUID id);
    Optional<ContaDeFidelidade> buscarPorClienteId(UUID clienteId);
}