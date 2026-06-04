package br.com.cinema.frame.domain.backoffice.caixa;

import java.time.LocalDate;

public interface ResumoPeriodoRepository {
    int contarIngressosPorPeriodo(LocalDate dataInicio, LocalDate dataFim);
    double somarValorIngressosPorPeriodo(LocalDate dataInicio, LocalDate dataFim);
    double somarVendasBombonierePorPeriodo(LocalDate dataInicio, LocalDate dataFim);
    double somarDescontosPontosPosPeriodo(LocalDate dataInicio, LocalDate dataFim);
    int corrigirDatasSessaoNula();
}