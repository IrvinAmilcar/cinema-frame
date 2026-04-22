package br.com.cinema.frame.domain.backoffice.sala;

import java.util.UUID;

public class Sala {

    private UUID id;
    private int numero;
    private int capacidade;
    private TipoSala tipo;

    public Sala(int numero, int capacidade, TipoSala tipo) {
        if (numero <= 0)
            throw new IllegalArgumentException("Número da sala deve ser positivo");

        if (capacidade <= 0)
            throw new IllegalArgumentException("Capacidade da sala deve ser positiva");
        
        if (tipo == null)
            throw new IllegalArgumentException("Tipo da sala não pode ser nulo");

        this.id = UUID.randomUUID();
        this.numero = numero;
        this.capacidade = capacidade;
        this.tipo = tipo;
    }

    public UUID getId() { return id; }
    public int getNumero() { return numero; }
    public int getCapacidade() { return capacidade; }
    public TipoSala getTipo() { return tipo; }
}