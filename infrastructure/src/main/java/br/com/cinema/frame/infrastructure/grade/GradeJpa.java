package br.com.cinema.frame.infrastructure.grade;

import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicao;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "grades")
public class GradeJpa {

    @Id
    private UUID id;

    @Column(nullable = false)
    private LocalDate inicio;

    @Column(nullable = false)
    private LocalDate fim;

    protected GradeJpa() {}

    public static GradeJpa fromDomain(GradeDeExibicao g) {
        GradeJpa e = new GradeJpa();
        e.id = g.getId();
        e.inicio = g.getInicio();
        e.fim = g.getFim();
        return e;
    }

    public UUID getId() { return id; }
    public LocalDate getInicio() { return inicio; }
    public LocalDate getFim() { return fim; }
}
