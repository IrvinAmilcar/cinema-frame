package br.com.cinema.frame.infrastructure.caixa;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import br.com.cinema.frame.domain.backoffice.caixa.ResumoFinanceiroPedidoRepository;
import br.com.cinema.frame.infrastructure.pedido.PedidoResumoFinanceiroJpa;
import br.com.cinema.frame.infrastructure.pedido.PedidoResumoFinanceiroJpaRepository;

@Repository
public class ResumoFinanceiroPedidoRepositoryImpl implements ResumoFinanceiroPedidoRepository {

    private final PedidoResumoFinanceiroJpaRepository jpa;

    public ResumoFinanceiroPedidoRepositoryImpl(PedidoResumoFinanceiroJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(UUID pedidoId, double valorIngressos, double valorBomboniere,
                       double descontoCupom, double descontoPontos, double valorTotal, LocalDate dataPedido) {
        jpa.save(new PedidoResumoFinanceiroJpa(
                pedidoId, valorIngressos, valorBomboniere,
                descontoCupom, descontoPontos, valorTotal, dataPedido));
    }
}
