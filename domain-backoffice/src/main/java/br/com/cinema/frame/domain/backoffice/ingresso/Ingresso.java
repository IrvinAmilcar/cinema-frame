package br.com.cinema.frame.domain.backoffice.ingresso;

import java.util.UUID;

import br.com.cinema.frame.domain.backoffice.grade.Sessao;

public class Ingresso {

    private final UUID id;
    private final Sessao sessao;
    private final TipoIngresso tipo;
    private boolean utilizado;

    public Ingresso(Sessao sessao, TipoIngresso tipo) {
        if (sessao == null)
            throw new IllegalArgumentException("Sessão não pode ser nula");
        if (tipo == null)
            throw new IllegalArgumentException("Tipo do ingresso não pode ser nulo");

        this.id = UUID.randomUUID();
        this.sessao = sessao;
        this.tipo = tipo;
        this.utilizado = false;
    }

    private Ingresso(UUID id, Sessao sessao, TipoIngresso tipo, boolean utilizado) {
        this.id = id;
        this.sessao = sessao;
        this.tipo = tipo;
        this.utilizado = utilizado;
    }

    public static Ingresso reconstituir(UUID id, Sessao sessao, TipoIngresso tipo) {
        if (id == null) throw new IllegalArgumentException("ID do ingresso não pode ser nulo");
        if (sessao == null) throw new IllegalArgumentException("Sessão não pode ser nula");
        if (tipo == null) throw new IllegalArgumentException("Tipo do ingresso não pode ser nulo");
        return new Ingresso(id, sessao, tipo, false);
    }

    public UUID getId() { return id; }
    public Sessao getSessao() { return sessao; }
    public TipoIngresso getTipo() { return tipo; }
    public boolean isUtilizado() { return utilizado; }

    public void marcarComoUtilizado() {
        this.utilizado = true;
    }
}