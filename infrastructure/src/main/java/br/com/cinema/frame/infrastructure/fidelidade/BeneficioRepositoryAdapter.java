package br.com.cinema.frame.infrastructure.fidelidade;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import br.com.cinema.frame.domain.portal.fidelidade.Beneficio;
import br.com.cinema.frame.domain.portal.fidelidade.BeneficioRepository;

@Repository
public class BeneficioRepositoryAdapter implements BeneficioRepository {

    private final BeneficioJpaRepository jpa;

    public BeneficioRepositoryAdapter(BeneficioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Beneficio> buscarPorId(UUID id) {
        return jpa.findById(id).map(BeneficioJpa::toDomain);
    }

    @Override
    public List<Beneficio> listarTodos() {
        return jpa.findAll().stream()
                .map(BeneficioJpa::toDomain)
                .toList();
    }

    @Override
    public void salvar(Beneficio beneficio) {
        jpa.save(BeneficioJpa.fromDomain(beneficio));
    }
}