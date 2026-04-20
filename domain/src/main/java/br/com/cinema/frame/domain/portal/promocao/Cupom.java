package br.com.cinema.frame.domain.portal.promocao;

import java.time.LocalDate;
import java.util.UUID;

public class Cupom {

    private final UUID id;
    private final String codigo;
    private final TipoPromocao tipo;
    private final boolean cumulativo;
    private final LocalDate validade;

    public Cupom(String codigo, TipoPromocao tipo, boolean cumulativo, LocalDate validade) {
        if (codigo == null || codigo.isBlank())
            throw new IllegalArgumentException("Código do cupom não pode ser vazio");
        if (tipo == null)
            throw new IllegalArgumentException("Tipo da promoção não pode ser nulo");
        if (validade == null)
            throw new IllegalArgumentException("Validade do cupom não pode ser nula");

        this.id = UUID.randomUUID();
        this.codigo = codigo;
        this.tipo = tipo;
        this.cumulativo = cumulativo;
        this.validade = validade;
    }

    public boolean estaValido(LocalDate hoje) {
        return !hoje.isAfter(validade);
    }

    public UUID getId() { return id; }
    public String getCodigo() { return codigo; }
    public TipoPromocao getTipo() { return tipo; }
    public boolean isCumulativo() { return cumulativo; }
    public LocalDate getValidade() { return validade; }
}