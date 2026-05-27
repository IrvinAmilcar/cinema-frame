package br.com.cinema.frame.infrastructure.pedido;

import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.ingresso.Ingresso;
import br.com.cinema.frame.domain.backoffice.ingresso.TipoIngresso;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "ingressos")
public class IngressoJpa {

    @Id
    private UUID id;
    private UUID pedidoId;
    private UUID sessaoId;
    private String tipo;
    private boolean utilizado;

    protected IngressoJpa() {}

    public static IngressoJpa fromDomain(Ingresso i, UUID pedidoId) {
        IngressoJpa e = new IngressoJpa();
        e.id = i.getId();
        e.pedidoId = pedidoId;
        e.sessaoId = i.getSessao().getId();
        e.tipo = i.getTipo().name();
        e.utilizado = i.isUtilizado();
        return e;
    }

    public Ingresso toDomain(Sessao sessao) {
        return Ingresso.reconstituir(id, sessao, TipoIngresso.valueOf(tipo));
    }

    public UUID getId() { return id; }
    public UUID getPedidoId() { return pedidoId; }
    public UUID getSessaoId() { return sessaoId; }
    public String getTipo() { return tipo; }
    public boolean isUtilizado() { return utilizado; }
    public void setUtilizado(boolean utilizado) { this.utilizado = utilizado; }
}
