package br.com.cinema.frame.infrastructure.fidelidade;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import br.com.cinema.frame.domain.portal.fidelidade.Beneficio;
import br.com.cinema.frame.domain.portal.fidelidade.TipoBeneficio;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "beneficios")
public class BeneficioJpa {

    @Id
    private UUID id;

    private String nome;

    @Enumerated(EnumType.STRING)
    private TipoBeneficio tipo;

    private int pontosNecessarios;
    private boolean combinavel;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "beneficio_incompativeis", joinColumns = @JoinColumn(name = "beneficio_id"))
    @Column(name = "tipo_incompativel")
    @Enumerated(EnumType.STRING)
    private Set<TipoBeneficio> incompativeis = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "beneficio_dias_permitidos", joinColumns = @JoinColumn(name = "beneficio_id"))
    @Column(name = "dia_semana")
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> diasPermitidos = new HashSet<>();

    public BeneficioJpa() {}

    public static BeneficioJpa fromDomain(Beneficio domain) {
        BeneficioJpa jpa = new BeneficioJpa();
        jpa.id = domain.getId();
        jpa.nome = domain.getNome();
        jpa.tipo = domain.getTipo();
        jpa.pontosNecessarios = domain.getPontosNecessarios();
        jpa.combinavel = domain.isCombinavel();
        jpa.incompativeis = new HashSet<>(domain.getIncompativeis());
        jpa.diasPermitidos = new HashSet<>(domain.getDiasPermitidos());
        return jpa;
    }

    public Beneficio toDomain() {
        return new Beneficio(id, nome, tipo, pontosNecessarios, combinavel, incompativeis, diasPermitidos);
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public TipoBeneficio getTipo() { return tipo; }
    public int getPontosNecessarios() { return pontosNecessarios; }
    public boolean isCombinavel() { return combinavel; }
    public Set<TipoBeneficio> getIncompativeis() { return incompativeis; }
    public Set<DayOfWeek> getDiasPermitidos() { return diasPermitidos; }
}
