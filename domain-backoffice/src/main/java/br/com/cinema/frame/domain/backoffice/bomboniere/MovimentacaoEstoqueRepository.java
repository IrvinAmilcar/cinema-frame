package br.com.cinema.frame.domain.backoffice.bomboniere;

import java.util.List;
import java.util.UUID;

public interface MovimentacaoEstoqueRepository {

    void salvar(MovimentacaoEstoque movimentacao);
    List<MovimentacaoEstoque> buscarPorInsumo(UUID insumoId);
}