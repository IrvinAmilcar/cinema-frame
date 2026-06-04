package br.com.cinema.frame.infrastructure.caixa;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import br.com.cinema.frame.domain.backoffice.caixa.DescontoPontosRepository;
import br.com.cinema.frame.infrastructure.pedido.PedidoDescontoPontosJpa;
import br.com.cinema.frame.infrastructure.pedido.PedidoDescontoPontosJpaRepository;

@Repository
public class DescontoPontosRepositoryImpl implements DescontoPontosRepository {

    private final PedidoDescontoPontosJpaRepository jpa;

    public DescontoPontosRepositoryImpl(PedidoDescontoPontosJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(UUID pedidoId, double valorDesconto, LocalDate dataPedido) {
        jpa.save(new PedidoDescontoPontosJpa(pedidoId, valorDesconto, dataPedido));
    }
}
