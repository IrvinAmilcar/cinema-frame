package br.com.cinema.frame.infrastructure.sala;

import br.com.cinema.frame.domain.backoffice.sala.Sala;
import br.com.cinema.frame.domain.backoffice.sala.TipoSala;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "salas")
public class SalaJpa {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private int numero;

    @Column(nullable = false)
    private int capacidade;

    @Column(nullable = false)
    private String tipo;

    protected SalaJpa() {}

    public static SalaJpa fromDomain(Sala s) {
        SalaJpa e = new SalaJpa();
        e.id = s.getId();
        e.numero = s.getNumero();
        e.capacidade = s.getCapacidade();
        e.tipo = s.getTipo().name();
        return e;
    }

    public Sala toDomain() {
        return Sala.reconstituir(id, numero, capacidade, TipoSala.valueOf(tipo));
    }

    public UUID getId() { return id; }
}
