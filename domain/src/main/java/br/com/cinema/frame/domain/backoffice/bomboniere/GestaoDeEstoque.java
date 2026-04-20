package br.com.cinema.frame.domain.backoffice.bomboniere;

import java.util.ArrayList;
import java.util.List;

public class GestaoDeEstoque {

    public List<EstoqueNotificacao> vender(ProdutoDaBomboniere produto) {
        if (produto == null)
            throw new IllegalArgumentException("Produto não pode ser nulo");

        for (ItemDeReceita item : produto.getReceita()) {
            item.getInsumo().baixar(item.getQuantidade());
        }

        List<EstoqueNotificacao> notificacoes = new ArrayList<>();
        for (ItemDeReceita item : produto.getReceita()) {
            if (item.getInsumo().isEstoqueCritico()) {
                notificacoes.add(new EstoqueNotificacao(item.getInsumo()));
            }
        }

        return notificacoes;
    }
}