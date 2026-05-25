package br.com.cinema.frame.infrastructure.bomboniere;

import br.com.cinema.frame.domain.backoffice.bomboniere.Insumo;
import br.com.cinema.frame.domain.backoffice.bomboniere.InsumoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class InsumoRepositoryAdapter
        implements InsumoRepository {

    private final InsumoJpaRepository jpa;

    public InsumoRepositoryAdapter(
            InsumoJpaRepository jpa
    ) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(Insumo insumo) {
        jpa.save(InsumoJpa.fromDomain(insumo));
    }

    @Override
    public Optional<Insumo> buscarPorId(UUID id) {

        return jpa.findById(id)
                .map(InsumoJpa::toDomain);
    }

    @Override
    public Optional<Insumo> buscarPorNome(String nome) {

        return jpa.findByNome(nome)
                .map(InsumoJpa::toDomain);
    }

    @Override
    public List<Insumo> listarTodos() {

        return jpa.findAll()
                .stream()
                .map(InsumoJpa::toDomain)
                .toList();
    }

    @Override
    public List<Insumo> listarEstoqueCritico() {

        return jpa.findAll()
                .stream()
                .map(InsumoJpa::toDomain)
                .filter(Insumo::isEstoqueCritico)
                .toList();
    }
}