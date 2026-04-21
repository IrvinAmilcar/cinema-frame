package br.com.cinema.frame.domain.portal.cliente;

import br.com.cinema.frame.domain.backoffice.grade.Filme;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Cliente {

    private final UUID id;
    private final String nome;
    private final String email;
    private final LocalDate dataNascimento;
    private final List<Filme> filmesFavoritos;

    public Cliente(String nome, String email, LocalDate dataNascimento) {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome não pode ser vazio");

        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Email não pode ser vazio");

        if (dataNascimento == null)
            throw new IllegalArgumentException("Data de nascimento não pode ser nula");
        
        if (dataNascimento.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Data de nascimento não pode ser no futuro");

        this.id = UUID.randomUUID();
        this.nome = nome;
        this.email = email;
        this.dataNascimento = dataNascimento;
        this.filmesFavoritos = new ArrayList<>();
    }

    public void favoritarFilme(Filme filme) {
        if (filme == null)
            throw new IllegalArgumentException("Filme não pode ser nulo");
        if (!filmesFavoritos.contains(filme))
            filmesFavoritos.add(filme);
    }

    public void desfavoritarFilme(Filme filme) {
        filmesFavoritos.remove(filme);
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public List<Filme> getFilmesFavoritos() { return Collections.unmodifiableList(filmesFavoritos); }
}