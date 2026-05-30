package br.com.cinema.frame.infrastructure.fidelidade;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import br.com.cinema.frame.domain.portal.fidelidade.FidelidadeRepository;
import br.com.cinema.frame.domain.portal.fidelidade.PontosCliente;

@Repository
public class FidelidadeRepositoryAdapter implements FidelidadeRepository {

    private final PontosClienteJpaRepository jpa;

    public FidelidadeRepositoryAdapter(PontosClienteJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(PontosCliente pontosCliente) {
        jpa.save(PontosClienteJpa.fromDomain(pontosCliente));
    }

    @Override
    public Optional<PontosCliente> buscarPorCliente(UUID clienteId) {
        return jpa.findById(clienteId).map(PontosClienteJpa::toDomain);
    }
}
