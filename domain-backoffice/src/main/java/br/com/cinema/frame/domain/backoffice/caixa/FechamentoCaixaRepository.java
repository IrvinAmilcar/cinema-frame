package br.com.cinema.frame.domain.backoffice.caixa;

import java.time.LocalDate;
import java.util.Optional;

public interface FechamentoCaixaRepository {
    void salvar(FechamentoCaixa fechamento);
    Optional<FechamentoCaixa> buscarPorData(LocalDate data);
    boolean existeFechamentoParaData(LocalDate data);
}