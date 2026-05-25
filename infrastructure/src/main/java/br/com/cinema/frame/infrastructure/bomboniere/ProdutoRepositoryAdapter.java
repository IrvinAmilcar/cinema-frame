package br.com.cinema.frame.infrastructure.bomboniere;

import br.com.cinema.frame.domain.backoffice.bomboniere.ProdutoDaBomboniere;
import br.com.cinema.frame.domain.backoffice.bomboniere.ProdutoDaBombonieresRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ProdutoRepositoryAdapter
        implements ProdutoDaBombonieresRepository {

    private final ProdutoJpaRepository jpa;

    public ProdutoRepositoryAdapter(ProdutoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(ProdutoDaBomboniere produto) {
        jpa.save(ProdutoJpa.fromDomain(produto));
    }

    @Override
    public Optional<ProdutoDaBomboniere> buscarPorId(UUID id) {
        return jpa.findById(id)
                .map(ProdutoJpa::toDomain);
    }

    @Override
    public Optional<ProdutoDaBomboniere> buscarPorNome(String nome) {
        return jpa.findByNome(nome)
                .map(ProdutoJpa::toDomain);
    }

    @Override
    public List<ProdutoDaBomboniere> listarTodos() {
        return jpa.findAll()
                .stream()
                .map(ProdutoJpa::toDomain)
                .toList();
    }

    @Override
    public void remover(UUID id) {
        jpa.deleteById(id);
    }
}