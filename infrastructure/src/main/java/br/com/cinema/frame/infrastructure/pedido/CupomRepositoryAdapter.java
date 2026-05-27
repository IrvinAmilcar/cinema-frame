package br.com.cinema.frame.infrastructure.pedido;

import br.com.cinema.frame.domain.portal.promocao.Cupom;
import br.com.cinema.frame.domain.portal.promocao.CupomRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CupomRepositoryAdapter implements CupomRepository {

    private final CupomJpaRepository jpa;

    public CupomRepositoryAdapter(CupomJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(Cupom cupom) {
        jpa.save(CupomJpa.fromDomain(cupom));
    }

    @Override
    public Optional<Cupom> buscarPorId(UUID id) {
        return jpa.findById(id).map(CupomJpa::toDomain);
    }

    @Override
    public Optional<Cupom> buscarPorCodigo(String codigo) {
        return jpa.findByCodigo(codigo).map(CupomJpa::toDomain);
    }

    @Override
    public List<Cupom> listarTodos() {
        return jpa.findAll().stream().map(CupomJpa::toDomain).toList();
    }

    @Override
    public void remover(UUID id) {
        jpa.deleteById(id);
    }
}
