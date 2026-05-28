package br.com.cinema.frame.infrastructure.pedido;

import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.ingresso.Ingresso;
import br.com.cinema.frame.domain.portal.pedido.Pedido;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pedidos")
public class PedidoJpa {

    @Id
    private UUID id;
    private UUID sessaoId;
    private UUID clienteId;
    private UUID reservaId;
    private boolean finalizado;
    private LocalDate dataSessao;

    protected PedidoJpa() {}

    public static PedidoJpa fromDomain(Pedido p) {
        PedidoJpa e = new PedidoJpa();
        e.id = p.getId();
        e.sessaoId = p.getSessao().getId();
        e.clienteId = p.getClienteId();
        e.reservaId = p.getReservaId();
        e.finalizado = p.isFinalizado();
        e.dataSessao = LocalDate.now();
        return e;
    }

    public Pedido toDomain(Sessao sessao, List<Ingresso> ingressos) {
        return Pedido.reconstituirComIngressos(id, sessao, clienteId, reservaId, ingressos);
    }

    public UUID getId() { return id; }
    public UUID getSessaoId() { return sessaoId; }
    public UUID getClienteId() { return clienteId; }
    public UUID getReservaId() { return reservaId; }
    public boolean isFinalizado() { return finalizado; }
    public LocalDate getDataSessao() { return dataSessao; }
    public void setReservaId(UUID reservaId) { this.reservaId = reservaId; }
    public void setFinalizado(boolean finalizado) { this.finalizado = finalizado; }
    public void setDataSessao(LocalDate dataSessao) { this.dataSessao = dataSessao; }
}
