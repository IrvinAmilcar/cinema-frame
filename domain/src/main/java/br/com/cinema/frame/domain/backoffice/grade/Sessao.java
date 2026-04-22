package br.com.cinema.frame.domain.backoffice.grade;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.cinema.frame.domain.backoffice.sala.Sala;

public class Sessao {

    private static final int TEMPO_LIMPEZA_MINUTOS = 15;
    private static final int TEMPO_TRAILERS_MINUTOS = 10;

    private UUID id;
    private Filme filme;
    private Sala sala;
    private LocalDateTime inicio;

    public Sessao(Filme filme, Sala sala, LocalDateTime inicio) {
        if (filme == null)
            throw new IllegalArgumentException("Filme não pode ser nulo");

        if (sala == null)
            throw new IllegalArgumentException("Sala não pode ser nula");
        
        if (inicio == null)
            throw new IllegalArgumentException("Horário de início não pode ser nulo");

        this.id = UUID.randomUUID();
        this.filme = filme;
        this.sala = sala;
        this.inicio = inicio;
    }

    public LocalDateTime getFim() {
        return inicio.plus(filme.getDuracao());
    }

    public LocalDateTime getFimComIntervalo() {
        return getFim().plusMinutes(TEMPO_LIMPEZA_MINUTOS).plusMinutes(TEMPO_TRAILERS_MINUTOS);
    }

    public boolean conflitaCom(Sessao outra) {
        if (this.sala.getNumero() != outra.sala.getNumero())
            return false;

        return this.inicio.isBefore(outra.getFimComIntervalo()) && outra.inicio.isBefore(this.getFimComIntervalo());
    }

    public UUID getId() { return id; }
    public Filme getFilme() { return filme; }
    public Sala getSala() { return sala; }
    public LocalDateTime getInicio() { return inicio; }
}