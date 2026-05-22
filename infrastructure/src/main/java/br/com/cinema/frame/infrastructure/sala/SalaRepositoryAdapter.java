package br.com.cinema.frame.infrastructure.sala;

import br.com.cinema.frame.domain.backoffice.sala.Sala;
import br.com.cinema.frame.domain.backoffice.sala.SalaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SalaRepositoryAdapter implements SalaRepository {

    private final SalaJpaRepository jpa;

    public SalaRepositoryAdapter(SalaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(Sala sala) {
        jpa.save(SalaJpa.fromDomain(sala));
    }

    @Override
    public Optional<Sala> buscarPorId(UUID id) {
        return jpa.findById(id).map(SalaJpa::toDomain);
    }

    @Override
    public Optional<Sala> buscarPorNumero(int numero) {
        return jpa.findByNumero(numero).map(SalaJpa::toDomain);
    }

    @Override
    public List<Sala> listarTodas() {
        return jpa.findAll().stream().map(SalaJpa::toDomain).toList();
    }

    @Override
    public void remover(UUID id) {
        jpa.deleteById(id);
    }
}
