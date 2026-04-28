package br.com.cinema.frame.domain.portal.fidelidade;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BeneficioRepository {
    Optional<Beneficio> buscarPorId(UUID id);
    List<Beneficio> listarTodos();
}
