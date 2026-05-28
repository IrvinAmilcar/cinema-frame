package br.com.cinema.frame.infrastructure.programacao;

import br.com.cinema.frame.domain.portal.recomendacao.HistoricoDeCompras;
import br.com.cinema.frame.domain.portal.recomendacao.HistoricoDeComprasRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class HistoricoRepositoryAdapter implements HistoricoDeComprasRepository {

    private final HistoricoJpaRepository jpa;

    public HistoricoRepositoryAdapter(HistoricoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(HistoricoDeCompras historico) {
        jpa.save(HistoricoJpa.fromDomain(historico));
    }

    @Override
    public Optional<HistoricoDeCompras> buscarPorClienteId(UUID clienteId) {
        return jpa.findByClienteId(clienteId).map(HistoricoJpa::toDomain);
    }
}
