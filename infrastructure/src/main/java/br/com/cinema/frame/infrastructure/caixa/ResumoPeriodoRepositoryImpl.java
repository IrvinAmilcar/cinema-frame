package br.com.cinema.frame.infrastructure.caixa;

import java.time.LocalDate;

import org.springframework.stereotype.Repository;

import br.com.cinema.frame.domain.backoffice.caixa.ResumoPeriodoRepository;
import br.com.cinema.frame.infrastructure.pedido.PedidoDescontoPontosJpaRepository;
import br.com.cinema.frame.infrastructure.pedido.PedidoJpaRepository;
import br.com.cinema.frame.infrastructure.pedido.PedidoProdutoJpaRepository;

@Repository
public class ResumoPeriodoRepositoryImpl implements ResumoPeriodoRepository {

    private final PedidoJpaRepository pedidoJpaRepository;
    private final PedidoProdutoJpaRepository pedidoProdutoJpaRepository;
    private final PedidoDescontoPontosJpaRepository descontoPontosJpaRepository;

    public ResumoPeriodoRepositoryImpl(PedidoJpaRepository pedidoJpaRepository,
                                        PedidoProdutoJpaRepository pedidoProdutoJpaRepository,
                                        PedidoDescontoPontosJpaRepository descontoPontosJpaRepository) {
        this.pedidoJpaRepository = pedidoJpaRepository;
        this.pedidoProdutoJpaRepository = pedidoProdutoJpaRepository;
        this.descontoPontosJpaRepository = descontoPontosJpaRepository;
    }

    @Override
    public int contarIngressosPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return pedidoJpaRepository.contarIngressosPorPeriodo(dataInicio, dataFim);
    }

    @Override
    public double somarValorIngressosPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return pedidoJpaRepository.somarValorIngressosPorPeriodo(dataInicio, dataFim);
    }

    @Override
    public double somarVendasBombonierePorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return pedidoProdutoJpaRepository.somarVendasBombonierePorPeriodo(dataInicio, dataFim);
    }

    @Override
    public double somarDescontosPontosPosPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return descontoPontosJpaRepository.somarDescontosPorPeriodo(dataInicio, dataFim);
    }

    @Override
    public int corrigirDatasSessaoNula() {
        return pedidoJpaRepository.preencherDataSessaoNula();
    }
}