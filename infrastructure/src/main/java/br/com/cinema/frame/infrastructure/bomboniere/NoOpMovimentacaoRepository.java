package br.com.cinema.frame.infrastructure.bomboniere;

import br.com.cinema.frame.domain.backoffice.bomboniere.MovimentacaoEstoque;
import br.com.cinema.frame.domain.backoffice.bomboniere.MovimentacaoEstoqueRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

// Placeholder até a implementação JPA de MovimentacaoEstoque ser criada
@Repository
public class NoOpMovimentacaoRepository implements MovimentacaoEstoqueRepository {

    @Override
    public void salvar(MovimentacaoEstoque movimentacao) {
    }

    @Override
    public List<MovimentacaoEstoque> buscarPorInsumo(UUID insumoId) {
        return List.of();
    }
}
