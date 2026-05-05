package br.com.cinema.frame.domain.portal.fidelidade;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.com.cinema.frame.domain.portal.cliente.ClienteRepository;
import br.com.cinema.frame.domain.shared.cliente.ClienteId;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

public class FidelidadeSteps {

    private final FidelidadeRepository fidelidadeRepository = mock(FidelidadeRepository.class);
    private final BeneficioRepository beneficioRepository = mock(BeneficioRepository.class);
    private final ClienteRepository clienteRepository = mock(ClienteRepository.class);
    private final RegistroResgateRepository resgateRepository = mock(RegistroResgateRepository.class);

    private final FidelidadeService service = new FidelidadeService(
            fidelidadeRepository, beneficioRepository, clienteRepository, resgateRepository
    );

    private final Map<String, PontosCliente> contasEmMemoria = new HashMap<>();
    private final Map<String, Beneficio> beneficiosPorNome = new HashMap<>();
    private final Map<UUID, Beneficio> beneficiosPorId = new HashMap<>();
    private final Map<UUID, List<RegistroResgate>> resgatesEmMemoria = new HashMap<>();

    private List<Beneficio> beneficiosDisponiveis;
    private Exception excecaoCapturada;

    private void configurarClienteMock(String clienteIdStr, UUID clienteId) {
        when(fidelidadeRepository.buscarPorCliente(clienteId))
                .thenAnswer(inv -> Optional.ofNullable(contasEmMemoria.get(clienteIdStr)));
        doAnswer(inv -> {
            contasEmMemoria.put(clienteIdStr, inv.getArgument(0));
            return null;
        }).when(fidelidadeRepository).salvar(any());
    }

    private void configurarResgateMock(UUID clienteId) {
        when(resgateRepository.buscarPorCliente(clienteId))
                .thenAnswer(inv -> resgatesEmMemoria.getOrDefault(clienteId, new ArrayList<>()));
        when(resgateRepository.buscarPorClienteEMes(eq(clienteId), anyInt(), anyInt()))
                .thenAnswer(inv -> {
                    int mes = inv.getArgument(1);
                    int ano = inv.getArgument(2);
                    return resgatesEmMemoria.getOrDefault(clienteId, new ArrayList<>()).stream()
                            .filter(r -> r.getData().getMonthValue() == mes && r.getData().getYear() == ano)
                            .toList();
                });
        doAnswer(inv -> {
            RegistroResgate registro = inv.getArgument(1);
            resgatesEmMemoria.computeIfAbsent(clienteId, k -> new ArrayList<>()).add(registro);
            return null;
        }).when(resgateRepository).salvar(eq(clienteId), any());
    }

    @Dado("que existe um cliente com id {string}")
    public void queExisteUmClienteComId(String clienteIdStr) {
        UUID clienteId = UUID.nameUUIDFromBytes(clienteIdStr.getBytes());
        contasEmMemoria.put(clienteIdStr, new PontosCliente(clienteId));
        configurarClienteMock(clienteIdStr, clienteId);
        configurarResgateMock(clienteId);
        when(clienteRepository.buscarAniversarioPorCliente(new ClienteId(clienteId))).thenReturn(Optional.empty());
    }

    @Dado("que existe um benefício {string} do tipo INGRESSO_GRATIS exigindo {int} pontos disponível todos os dias")
    public void queExisteBeneficioIngressoGratis(String nome, int pontos) {
        UUID id = UUID.nameUUIDFromBytes(nome.getBytes());
        Beneficio b = new Beneficio(id, nome, TipoBeneficio.INGRESSO_GRATIS, pontos, true, Set.of(), Set.of());
        beneficiosPorNome.put(nome, b);
        beneficiosPorId.put(id, b);
        when(beneficioRepository.buscarPorId(id)).thenReturn(Optional.of(b));
        when(beneficioRepository.listarTodos()).thenAnswer(inv -> new ArrayList<>(beneficiosPorId.values()));
    }

    @Dado("que existe um benefício {string} do tipo DESCONTO_PERCENTUAL exigindo {int} pontos disponível apenas às segundas-feiras")
    public void queExisteBeneficioSegunda(String nome, int pontos) {
        UUID id = UUID.nameUUIDFromBytes(nome.getBytes());
        Beneficio b = new Beneficio(id, nome, TipoBeneficio.DESCONTO_PERCENTUAL, pontos, true, Set.of(), Set.of(DayOfWeek.MONDAY));
        beneficiosPorNome.put(nome, b);
        beneficiosPorId.put(id, b);
        when(beneficioRepository.buscarPorId(id)).thenReturn(Optional.of(b));
        when(beneficioRepository.listarTodos()).thenAnswer(inv -> new ArrayList<>(beneficiosPorId.values()));
    }

    @Dado("que existe um benefício {string} do tipo PIPOCA_GRATIS exigindo {int} pontos não combinável com INGRESSO_GRATIS disponível todos os dias")
    public void queExisteBeneficioPipocaNaoCombinavel(String nome, int pontos) {
        UUID id = UUID.nameUUIDFromBytes(nome.getBytes());
        Beneficio b = new Beneficio(id, nome, TipoBeneficio.PIPOCA_GRATIS, pontos, false,
                Set.of(TipoBeneficio.INGRESSO_GRATIS), Set.of());
        beneficiosPorNome.put(nome, b);
        beneficiosPorId.put(id, b);
        when(beneficioRepository.buscarPorId(id)).thenReturn(Optional.of(b));
        when(beneficioRepository.listarTodos()).thenAnswer(inv -> new ArrayList<>(beneficiosPorId.values()));
    }

    @Dado("que existe um benefício {string} do tipo UPGRADE_ASSENTO exigindo {int} pontos combinável disponível todos os dias")
    public void queExisteBeneficioUpgradeCombinavel(String nome, int pontos) {
        UUID id = UUID.nameUUIDFromBytes(nome.getBytes());
        Beneficio b = new Beneficio(id, nome, TipoBeneficio.UPGRADE_ASSENTO, pontos, true, Set.of(), Set.of());
        beneficiosPorNome.put(nome, b);
        beneficiosPorId.put(id, b);
        when(beneficioRepository.buscarPorId(id)).thenReturn(Optional.of(b));
        when(beneficioRepository.listarTodos()).thenAnswer(inv -> new ArrayList<>(beneficiosPorId.values()));
    }

    @Dado("que o cliente {string} possui {int} pontos acumulados em {string} com validade {string}")
    public void queClientePossuiPontos(String clienteIdStr, int pontos, String dataStr, String validadeStr) {
        UUID clienteId = UUID.nameUUIDFromBytes(clienteIdStr.getBytes());
        PontosCliente conta = contasEmMemoria.computeIfAbsent(clienteIdStr, k -> {
            PontosCliente p = new PontosCliente(clienteId);
            configurarClienteMock(clienteIdStr, clienteId);
            configurarResgateMock(clienteId);
            return p;
        });
        conta.acumularPontos(pontos, LocalDate.parse(validadeStr), LocalDate.parse(dataStr));
    }

    @Dado("que o cliente {string} já acumulou {int} pontos no mês {string}")
    public void queClienteJaAcumulouPontosNoMes(String clienteIdStr, int pontos, String mesAnoStr) {
        UUID clienteId = UUID.nameUUIDFromBytes(clienteIdStr.getBytes());
        String[] partes = mesAnoStr.split("-");
        int ano = Integer.parseInt(partes[0]);
        int mes = Integer.parseInt(partes[1]);
        LocalDate data = LocalDate.of(ano, mes, 1);
        LocalDate validade = data.plusMonths(12);

        PontosCliente conta = contasEmMemoria.computeIfAbsent(clienteIdStr, k -> {
            PontosCliente p = new PontosCliente(clienteId);
            configurarClienteMock(clienteIdStr, clienteId);
            configurarResgateMock(clienteId);
            return p;
        });
        conta.acumularPontos(pontos, validade, data);
    }

    @Dado("que o aniversário do cliente {string} é em {string}")
    public void queAniversarioDoClienteEEm(String clienteIdStr, String diaStr) {
        UUID clienteId = UUID.nameUUIDFromBytes(clienteIdStr.getBytes());
        MonthDay aniversario = MonthDay.parse("--" + diaStr);
        when(clienteRepository.buscarAniversarioPorCliente(new ClienteId(clienteId))).thenReturn(Optional.of(aniversario));
    }

    @Dado("que o cliente {string} já resgatou o benefício {string} {int} vezes em {string}")
    public void queClienteJaResgatouNVezes(String clienteIdStr, String nomeBeneficio, int vezes, String mesAnoStr) {
        UUID clienteId = UUID.nameUUIDFromBytes(clienteIdStr.getBytes());
        UUID beneficioId = UUID.nameUUIDFromBytes(nomeBeneficio.getBytes());
        String[] partes = mesAnoStr.split("-");
        int ano = Integer.parseInt(partes[0]);
        int mes = Integer.parseInt(partes[1]);

        List<RegistroResgate> lista = resgatesEmMemoria.computeIfAbsent(clienteId, k -> new ArrayList<>());
        for (int i = 1; i <= vezes; i++) {
            lista.add(new RegistroResgate(beneficioId, 100, LocalDate.of(ano, mes, i)));
        }

        PontosCliente conta = contasEmMemoria.get(clienteIdStr);
        if (conta != null) {
            for (int i = 1; i <= vezes; i++) {
                conta.registrarResgateNoHistorico(new RegistroResgate(beneficioId, 100, LocalDate.of(ano, mes, i)));
            }
        }
    }

    @Dado("que o cliente {string} já resgatou o benefício {string} em {string}")
    public void queClienteJaResgatouBeneficioEm(String clienteIdStr, String nomeBeneficio, String dataStr) {
        UUID clienteId = UUID.nameUUIDFromBytes(clienteIdStr.getBytes());
        UUID beneficioId = UUID.nameUUIDFromBytes(nomeBeneficio.getBytes());
        LocalDate data = LocalDate.parse(dataStr);

        Beneficio beneficio = beneficiosPorId.get(beneficioId);
        int pontosDoResgate = beneficio != null ? beneficio.getPontosNecessarios() : 100;

        resgatesEmMemoria.computeIfAbsent(clienteId, k -> new ArrayList<>())
                .add(new RegistroResgate(beneficioId, pontosDoResgate, data));

        PontosCliente conta = contasEmMemoria.get(clienteIdStr);
        if (conta != null) {
            conta.debitarPontos(pontosDoResgate, beneficioId, data);
        }
    }

    @Quando("o cliente {string} realiza uma compra de R$ {double} em {string}")
    public void clienteRealizaCompra(String clienteIdStr, double valor, String dataStr) {
        UUID clienteId = UUID.nameUUIDFromBytes(clienteIdStr.getBytes());
        service.acumularPontos(clienteId, valor, LocalDate.parse(dataStr));
    }

    @Quando("o cliente {string} tenta realizar uma compra de R$ {double} em {string}")
    public void clienteTentaRealizarCompra(String clienteIdStr, double valor, String dataStr) {
        UUID clienteId = UUID.nameUUIDFromBytes(clienteIdStr.getBytes());
        try {
            service.acumularPontos(clienteId, valor, LocalDate.parse(dataStr));
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Quando("o cliente {string} consulta os benefícios disponíveis em {string}")
    public void clienteConsultaBeneficios(String clienteIdStr, String dataStr) {
        UUID clienteId = UUID.nameUUIDFromBytes(clienteIdStr.getBytes());
        beneficiosDisponiveis = service.verificarBeneficios(clienteId, LocalDate.parse(dataStr));
    }

    @Quando("o cliente {string} resgata o benefício {string} em {string}")
    public void clienteResgata(String clienteIdStr, String nomeBeneficio, String dataStr) {
        UUID clienteId = UUID.nameUUIDFromBytes(clienteIdStr.getBytes());
        UUID beneficioId = UUID.nameUUIDFromBytes(nomeBeneficio.getBytes());
        service.resgatarBeneficio(clienteId, beneficioId, LocalDate.parse(dataStr));
    }

    @Quando("o cliente {string} tenta resgatar o benefício {string} em {string}")
    public void clienteTentaResgatar(String clienteIdStr, String nomeBeneficio, String dataStr) {
        UUID clienteId = UUID.nameUUIDFromBytes(clienteIdStr.getBytes());
        UUID beneficioId = UUID.nameUUIDFromBytes(nomeBeneficio.getBytes());
        try {
            service.resgatarBeneficio(clienteId, beneficioId, LocalDate.parse(dataStr));
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Então("o saldo de pontos do cliente {string} em {string} deve ser {int}")
    public void saldoDeveSerX(String clienteIdStr, String dataStr, int esperado) {
        UUID clienteId = UUID.nameUUIDFromBytes(clienteIdStr.getBytes());
        int saldo = service.consultarSaldo(clienteId, LocalDate.parse(dataStr));
        assertThat(saldo).isEqualTo(esperado);
    }

    @Então("o benefício {string} deve estar na lista de disponíveis")
    public void beneficioDeveEstarNaLista(String nome) {
        assertThat(beneficiosDisponiveis).anyMatch(b -> b.getNome().equals(nome));
    }

    @Então("o benefício {string} não deve estar na lista de disponíveis")
    public void beneficioNaoDeveEstarNaLista(String nome) {
        assertThat(beneficiosDisponiveis).noneMatch(b -> b.getNome().equals(nome));
    }

    @Então("deve ocorrer o erro {string}")
    public void deveOcorrerErro(String mensagem) {
        assertThat(excecaoCapturada).isNotNull()
                .hasMessageContaining(mensagem);
    }

    @Então("o histórico do cliente {string} deve conter {int} resgate(s)")
    public void historicoDeveConterNResgates(String clienteIdStr, int quantidade) {
        UUID clienteId = UUID.nameUUIDFromBytes(clienteIdStr.getBytes());
        List<RegistroResgate> historico = service.consultarHistoricoResgates(clienteId);
        assertThat(historico).hasSize(quantidade);
    }
}