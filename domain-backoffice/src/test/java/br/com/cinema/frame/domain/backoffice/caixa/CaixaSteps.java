package br.com.cinema.frame.domain.backoffice.caixa;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

public class CaixaSteps {

    private final FechamentoCaixaRepository fechamentoRepository = mock(FechamentoCaixaRepository.class);
    private final CaixaService service = new CaixaService(fechamentoRepository);

    private final Map<LocalDate, FechamentoCaixa> fechamentosEmMemoria = new HashMap<>();
    private final Map<LocalDate, List<VendaDia>> vendasPorDia = new HashMap<>();

    private FechamentoCaixa ultimoFechamento;
    private FechamentoCaixa relatorioConsultado;
    private Exception excecaoCapturada;

    @Dado("que existem as seguintes vendas para o dia {string}:")
    public void queExistemVendasParaODia(String dataStr, DataTable tabela) {
        LocalDate data = LocalDate.parse(dataStr);
        List<VendaDia> vendas = new ArrayList<>();
        for (Map<String, String> row : tabela.asMaps()) {
            UUID sessaoId = UUID.nameUUIDFromBytes(row.get("sessaoId").getBytes());
            int capacidade = Integer.parseInt(row.get("capacidade").trim());
            int ingressos = Integer.parseInt(row.get("ingressos").trim());
            double valor = Double.parseDouble(row.get("valor").trim());
            vendas.add(new VendaDia(sessaoId, capacidade, ingressos, valor));
        }
        vendasPorDia.put(data, vendas);
        when(fechamentoRepository.existeFechamentoParaData(data)).thenReturn(false);
        doAnswer(inv -> { fechamentosEmMemoria.put(data, inv.getArgument(0)); return null; })
                .when(fechamentoRepository).salvar(any());
    }

    @Dado("que já existe um fechamento para o dia {string}")
    public void queJaExisteFechamento(String dataStr) {
        LocalDate data = LocalDate.parse(dataStr);
        when(fechamentoRepository.existeFechamentoParaData(data)).thenReturn(true);
    }

    @Dado("que já existe um fechamento para o dia {string} com total de vendas {int}")
    public void queJaExisteFechamentoComTotal(String dataStr, int total) {
        LocalDate data = LocalDate.parse(dataStr);
        FechamentoCaixa f = new FechamentoCaixa(UUID.randomUUID(), data, (double) total, 50, 1, 80.0, LocalDateTime.now());
        when(fechamentoRepository.buscarPorData(data)).thenReturn(Optional.of(f));
    }

    @Quando("o caixa é fechado para o dia {string} às {string}")
    public void caixaEFechado(String dataStr, String momentoStr) {
        LocalDate data = LocalDate.parse(dataStr);
        LocalDateTime momento = LocalDateTime.parse(momentoStr);
        List<VendaDia> vendas = vendasPorDia.getOrDefault(data, List.of());
        ultimoFechamento = service.fecharCaixa(data, vendas, momento);
    }

    @Quando("o caixa tenta ser fechado novamente para o dia {string} às {string}")
    public void caixaTentaSerFechadoNovamente(String dataStr, String momentoStr) {
        LocalDate data = LocalDate.parse(dataStr);
        LocalDateTime momento = LocalDateTime.parse(momentoStr);
        try {
            service.fecharCaixa(data, List.of(new VendaDia(UUID.randomUUID(), 100, 50, 1000.0)), momento);
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Quando("o relatório do dia {string} é consultado")
    public void relatorioEConsultado(String dataStr) {
        relatorioConsultado = service.consultarRelatorio(LocalDate.parse(dataStr));
    }

    @Quando("o relatório do dia {string} é consultado sem fechamento registrado")
    public void relatorioEConsultadoSemFechamento(String dataStr) {
        LocalDate data = LocalDate.parse(dataStr);
        when(fechamentoRepository.buscarPorData(data)).thenReturn(Optional.empty());
        try {
            relatorioConsultado = service.consultarRelatorio(data);
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Então("o total de vendas do fechamento deve ser {int}")
    public void totalVendasDeve(int esperado) {
        assertThat(ultimoFechamento.getTotalVendas()).isEqualTo((double) esperado);
    }

    @Então("o total de ingressos do fechamento deve ser {int}")
    public void totalIngressosDeve(int esperado) {
        assertThat(ultimoFechamento.getTotalIngressos()).isEqualTo(esperado);
    }

    @Então("o número de sessões do fechamento deve ser {int}")
    public void totalSessoesDeve(int esperado) {
        assertThat(ultimoFechamento.getTotalSessoes()).isEqualTo(esperado);
    }

    @Então("a taxa de ocupação média do fechamento deve ser {int}")
    public void taxaOcupacaoDeve(int esperada) {
        assertThat(ultimoFechamento.getTaxaOcupacaoMedia()).isEqualTo((double) esperada);
    }

    @Então("o total de vendas do relatório deve ser {int}")
    public void totalVendasRelatorio(int esperado) {
        assertThat(relatorioConsultado.getTotalVendas()).isEqualTo((double) esperado);
    }

    @Então("deve ocorrer o erro {string}")
    public void deveOcorrerErro(String mensagem) {
        assertThat(excecaoCapturada).isNotNull()
                .hasMessageContaining(mensagem);
    }
}