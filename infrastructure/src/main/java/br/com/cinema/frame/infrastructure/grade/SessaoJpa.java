package br.com.cinema.frame.infrastructure.grade;

import br.com.cinema.frame.domain.backoffice.grade.Filme;
import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.sala.Sala;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "sessoes")
public class SessaoJpa {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID gradeId;

    @Column(nullable = false)
    private UUID filmeId;

    @Column(nullable = false)
    private UUID salaId;

    @Column(nullable = false, columnDefinition = "TIME")
    @JdbcTypeCode(SqlTypes.TIME)
    private LocalTime inicio;

    protected SessaoJpa() {}

    public static SessaoJpa fromDomain(UUID gradeId, Sessao s) {
        SessaoJpa e = new SessaoJpa();
        e.id = s.getId();
        e.gradeId = gradeId;
        e.filmeId = s.getFilme().getId();
        e.salaId = s.getSala().getId();
        e.inicio = s.getInicio();
        return e;
    }

    public Sessao toDomain(Filme filme, Sala sala) {
        return Sessao.reconstituir(id, filme, sala, inicio);
    }

    public UUID getId() { return id; }
    public UUID getGradeId() { return gradeId; }
    public UUID getFilmeId() { return filmeId; }
    public UUID getSalaId() { return salaId; }
    public LocalTime getInicio() { return inicio; }
}
