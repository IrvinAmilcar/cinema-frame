package br.com.cinema.frame.domain.portal.pedido;

import br.com.cinema.frame.domain.backoffice.bomboniere.ProdutoDaBomboniere;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Voucher {

    private final String codigo;
    private final List<ProdutoDaBomboniere> produtos;

    public Voucher(UUID pedidoId, List<ProdutoDaBomboniere> produtos) {
        if (pedidoId == null)
            throw new IllegalArgumentException("ID do pedido não pode ser nulo");
        if (produtos == null || produtos.isEmpty())
            throw new IllegalArgumentException("Voucher deve ter ao menos um produto");

        this.codigo = "VCH-" + pedidoId.toString().toUpperCase();
        this.produtos = Collections.unmodifiableList(produtos);
    }

    public String getCodigo() { return codigo; }
    public List<ProdutoDaBomboniere> getProdutos() { return produtos; }
}