package br.com.cinema.frame.domain.portal.fidelidade;

import java.time.DayOfWeek;
import java.util.Set;
import java.util.UUID;

public class Beneficio {

    private final UUID id;
    private final String nome;
    private final TipoBeneficio tipo;
    private final int pontosNecessarios;
    private final boolean combinavel;
    private final Set<TipoBeneficio> incompativeis;
    private final Set<DayOfWeek> diasPermitidos; // vazio = todos os dias

    public Beneficio(UUID id, String nome, TipoBeneficio tipo,
                     int pontosNecessarios, boolean combinavel,
                     Set<TipoBeneficio> incompativeis,
                     Set<DayOfWeek> diasPermitidos) {
        if (id == null) throw new IllegalArgumentException("Id é obrigatório");
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome é obrigatório");
        if (tipo == null) throw new IllegalArgumentException("Tipo é obrigatório");
        if (pontosNecessarios <= 0) throw new IllegalArgumentException("Pontos necessários deve ser positivo");

        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.pontosNecessarios = pontosNecessarios;
        this.combinavel = combinavel;
        this.incompativeis = incompativeis == null ? Set.of() : Set.copyOf(incompativeis);
        this.diasPermitidos = diasPermitidos == null ? Set.of() : Set.copyOf(diasPermitidos);
    }

    public boolean disponivelNoDia(DayOfWeek dia) {
        return diasPermitidos.isEmpty() || diasPermitidos.contains(dia);
    }

    public boolean incompativelCom(Beneficio outro) {
        return !this.combinavel || incompativeis.contains(outro.getTipo());
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public TipoBeneficio getTipo() { return tipo; }
    public int getPontosNecessarios() { return pontosNecessarios; }
    public boolean isCombinavel() { return combinavel; }
}
