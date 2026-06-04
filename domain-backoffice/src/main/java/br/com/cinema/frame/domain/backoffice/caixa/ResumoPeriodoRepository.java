package br.com.cinema.frame.domain.backoffice.caixa;

import java.time.LocalDate;
import java.util.List;

public interface ResumoPeriodoRepository {
    int contarIngressosPorPeriodo(LocalDate dataInicio, LocalDate dataFim);
    double somarValorIngressosPorPeriodo(LocalDate dataInicio, LocalDate dataFim);
    double somarVendasBombonierePorPeriodo(LocalDate dataInicio, LocalDate dataFim);
    double somarDescontosPontosPosPeriodo(LocalDate dataInicio, LocalDate dataFim);
    int corrigirDatasSessaoNula();
    List<IngressosPorSessaoItem> ingressosPorSessao(LocalDate dataInicio, LocalDate dataFim);
    List<BombonierePorSessaoItem> bombonierePorSessao(LocalDate dataInicio, LocalDate dataFim);
    List<OcupacaoPorSessaoItem> ocupacaoPorSessao(LocalDate dataInicio, LocalDate dataFim);
}