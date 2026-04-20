package br.com.cinema.frame.domain.backoffice.grade;

import java.util.UUID;

public class Ingresso {

    private final UUID id;
    private final Sessao sessao;
    private boolean utilizado;

    public Ingresso(Sessao sessao) {
        if (sessao == null)
            throw new IllegalArgumentException("Sessão não pode ser nula");

        this.id = UUID.randomUUID();
        this.sessao = sessao;
        this.utilizado = false;
    }

    public UUID getId() { return id; }
    public Sessao getSessao() { return sessao; }
    public boolean isUtilizado() { return utilizado; }

    public void marcarComoUtilizado() {
        this.utilizado = true;
    }
}