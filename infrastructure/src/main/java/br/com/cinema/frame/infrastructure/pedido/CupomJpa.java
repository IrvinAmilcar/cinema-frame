package br.com.cinema.frame.infrastructure.pedido;

import br.com.cinema.frame.domain.portal.promocao.Cupom;
import br.com.cinema.frame.domain.portal.promocao.TipoPromocao;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "cupons")
public class CupomJpa {

    @Id
    private UUID id;
    private String codigo;
    private String tipo;
    private boolean cumulativo;
    private LocalDate validade;

    protected CupomJpa() {}

    public static CupomJpa fromDomain(Cupom c) {
        CupomJpa e = new CupomJpa();
        e.id = c.getId();
        e.codigo = c.getCodigo();
        e.tipo = c.getTipo().name();
        e.cumulativo = c.isCumulativo();
        e.validade = c.getValidade();
        return e;
    }

    public Cupom toDomain() {
        return Cupom.reconstituir(id, codigo, TipoPromocao.valueOf(tipo), cumulativo, validade);
    }

    public UUID getId() { return id; }
    public String getCodigo() { return codigo; }
}
