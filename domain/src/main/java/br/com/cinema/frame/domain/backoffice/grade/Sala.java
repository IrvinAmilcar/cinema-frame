package br.com.cinema.frame.domain.backoffice.grade;

import java.util.UUID;

public class Sala {

    private UUID id;
    private int numero;
    private int capacidade;

    public Sala(int numero, int capacidade) {
        if (numero <= 0)
            throw new IllegalArgumentException("Número da sala deve ser positivo");

        if (capacidade <= 0)
            throw new IllegalArgumentException("Capacidade da sala deve ser positiva");

        
        this.id = UUID.randomUUID();
        this.numero = numero;
        this.capacidade = capacidade;
    }

    public UUID getId() { return id; }
    public int getNumero() { return numero; }
    public int getCapacidade() { return capacidade; }
}