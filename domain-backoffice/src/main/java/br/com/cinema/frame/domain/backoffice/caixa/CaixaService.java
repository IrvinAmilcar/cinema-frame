package br.com.cinema.frame.domain.backoffice.caixa;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CaixaService {

    private final FechamentoCaixaRepository fechamentoRepository;

    public CaixaService(FechamentoCaixaRepository fechamentoRepository) {
        if (fechamentoRepository == null) throw new IllegalArgumentException("FechamentoCaixaRepository é obrigatório");
        this.fechamentoRepository = fechamentoRepository;
    }

    public FechamentoCaixa fecharCaixa(LocalDate data, List<VendaDia> vendas, LocalDateTime momentoFechamento) {
        if (data == null) throw new IllegalArgumentException("Data é obrigatória");
        if (vendas == null || vendas.isEmpty()) throw new IllegalArgumentException("Vendas não podem ser vazias");
        if (momentoFechamento == null) throw new IllegalArgumentException("Momento do fechamento é obrigatório");

        if (fechamentoRepository.existeFechamentoParaData(data)) {
            throw new IllegalStateException("Já existe um fechamento de caixa para a data " + data);
        }

        FechamentoCaixa fechamento = new RelatorioDiario().gerar(data, vendas, momentoFechamento);
        fechamentoRepository.salvar(fechamento);
        return fechamento;
    }

    public FechamentoCaixa consultarRelatorio(LocalDate data) {
        if (data == null) throw new IllegalArgumentException("Data é obrigatória");
        return fechamentoRepository.buscarPorData(data)
                .orElseThrow(() -> new IllegalStateException("Não há fechamento registrado para a data " + data));
    }
}
