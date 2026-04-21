package br.com.cinema.frame.domain.portal.notificacao;

import br.com.cinema.frame.domain.backoffice.grade.Filme;
import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.portal.cliente.Cliente;

import java.time.LocalDateTime;
import java.util.UUID;

public class NotificacaoDePrevenda {

    private final UUID id;
    private final Cliente cliente;
    private final Filme filme;
    private final Sessao sessao;
    private final LocalDateTime criadaEm;

    public NotificacaoDePrevenda(Cliente cliente, Filme filme, Sessao sessao) {
        if (cliente == null)
            throw new IllegalArgumentException("Cliente não pode ser nulo");
        if (filme == null)
            throw new IllegalArgumentException("Filme não pode ser nulo");
        if (sessao == null)
            throw new IllegalArgumentException("Sessão não pode ser nula");

        this.id = UUID.randomUUID();
        this.cliente = cliente;
        this.filme = filme;
        this.sessao = sessao;
        this.criadaEm = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public Filme getFilme() { return filme; }
    public Sessao getSessao() { return sessao; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
}