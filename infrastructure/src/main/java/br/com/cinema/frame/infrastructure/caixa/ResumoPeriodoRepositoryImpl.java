package br.com.cinema.frame.infrastructure.caixa;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import br.com.cinema.frame.domain.backoffice.caixa.BombonierePorSessaoItem;
import br.com.cinema.frame.domain.backoffice.caixa.IngressosPorSessaoItem;
import br.com.cinema.frame.domain.backoffice.caixa.OcupacaoPorSessaoItem;
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

    @Override
    public List<OcupacaoPorSessaoItem> ocupacaoPorSessao(LocalDate dataInicio, LocalDate dataFim) {
        return pedidoJpaRepository.ocupacaoPorSessao(dataInicio, dataFim).stream()
                .map(row -> new OcupacaoPorSessaoItem(
                        row[0] != null ? row[0].toString() : "",
                        row[1] != null ? row[1].toString() : "",
                        row[2] != null ? row[2].toString() : "",
                        row[3] != null ? ((Number) row[3]).intValue() : 0,
                        row[4] != null ? row[4].toString() : "",
                        row[5] != null ? ((Number) row[5]).intValue() : 0,
                        row[6] != null ? ((Number) row[6]).intValue() : 0,
                        row[7] != null ? ((Number) row[7]).intValue() : 1
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<BombonierePorSessaoItem> bombonierePorSessao(LocalDate dataInicio, LocalDate dataFim) {
        return pedidoProdutoJpaRepository.bombonierePorSessao(dataInicio, dataFim).stream()
                .map(row -> new BombonierePorSessaoItem(
                        row[0] != null ? row[0].toString() : "",
                        row[1] != null ? row[1].toString() : "",
                        row[2] != null ? row[2].toString() : "",
                        row[3] != null ? ((Number) row[3]).intValue() : 0,
                        row[4] != null ? row[4].toString() : "",
                        row[5] != null ? row[5].toString() : "",
                        row[6] != null ? ((Number) row[6]).intValue() : 0,
                        row[7] != null ? ((Number) row[7]).doubleValue() : 0.0
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<IngressosPorSessaoItem> ingressosPorSessao(LocalDate dataInicio, LocalDate dataFim) {
        return pedidoJpaRepository.ingressosPorSessao(dataInicio, dataFim).stream()
                .map(row -> new IngressosPorSessaoItem(
                        row[0] != null ? row[0].toString() : "",
                        row[1] != null ? row[1].toString() : "",
                        row[2] != null ? row[2].toString() : "",
                        row[3] != null ? ((Number) row[3]).intValue() : 0,
                        row[4] != null ? row[4].toString() : "",
                        row[5] != null ? ((Number) row[5]).intValue() : 0,
                        row[6] != null ? ((Number) row[6]).intValue() : 0,
                        row[7] != null ? ((Number) row[7]).doubleValue() : 0.0
                ))
                .collect(Collectors.toList());
    }
}