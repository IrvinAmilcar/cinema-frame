package br.com.cinema.frame.domain.portal.pedido;

import br.com.cinema.frame.domain.backoffice.bomboniere.EstoqueNotificacao;
import br.com.cinema.frame.domain.backoffice.bomboniere.GestaoDeEstoque;
import br.com.cinema.frame.domain.backoffice.bomboniere.ProdutoDaBomboniere;
import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.ingresso.Ingresso;
import br.com.cinema.frame.domain.backoffice.ingresso.TipoIngresso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Pedido {

    private final UUID id;
    private final Sessao sessao;
    private final List<Ingresso> ingressos;
    private final List<ProdutoDaBomboniere> produtos;

    public Pedido(Sessao sessao) {
        if (sessao == null)
            throw new IllegalArgumentException("Sessão não pode ser nula");

        this.id = UUID.randomUUID();
        this.sessao = sessao;
        this.ingressos = new ArrayList<>();
        this.produtos = new ArrayList<>();
    }

    public void adicionarIngresso(TipoIngresso tipo) {
        if (tipo == null)
            throw new IllegalArgumentException("Tipo do ingresso não pode ser nulo");

        ingressos.add(new Ingresso(sessao, tipo));
    }

    public void adicionarProduto(ProdutoDaBomboniere produto) {
        if (produto == null)
            throw new IllegalArgumentException("Produto não pode ser nulo");

        produtos.add(produto);
    }

    public ResultadoDoPedido finalizar(GestaoDeEstoque gestaoDeEstoque) {
        if (ingressos.isEmpty())
            throw new IllegalStateException("Pedido deve ter ao menos um ingresso");

        if (gestaoDeEstoque == null)
            throw new IllegalArgumentException("Gestão de estoque não pode ser nula");

        for (ProdutoDaBomboniere produto : produtos) {
            gestaoDeEstoque.vender(produto);
        }

        QRCode qrCode = new QRCode(ingressos.get(0).getId());

        Voucher voucher = null;
        if (!produtos.isEmpty()) {
            voucher = new Voucher(id, produtos);
        }

        return new ResultadoDoPedido(qrCode, voucher);
    }

    public UUID getId() { return id; }
    public Sessao getSessao() { return sessao; }
    public List<Ingresso> getIngressos() { return Collections.unmodifiableList(ingressos); }
    public List<ProdutoDaBomboniere> getProdutos() { return Collections.unmodifiableList(produtos); }
}