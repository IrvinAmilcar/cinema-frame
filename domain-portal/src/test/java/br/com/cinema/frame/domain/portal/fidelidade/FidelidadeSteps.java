package br.com.cinema.frame.domain.portal.fidelidade;


import io.cucumber.java.pt.*;
import org.mockito.Mockito;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FidelidadeSteps {

    private final FidelidadeRepository fidelidadeRepository = mock(FidelidadeRepository.class);
    private final BeneficioRepository beneficioRepository = mock(BeneficioRepository.class);
    private final FidelidadeService service = new FidelidadeService(fidelidadeRepository, beneficioRepository);

    private final Map<String, PontosCliente> contasEmMemoria = new HashMap<>();
    private final Map<String, Beneficio> beneficiosPorNome = new HashMap<>();
    private final Map<UUID, Beneficio> beneficiosPorId = new HashMap<>();

    private List<Beneficio> beneficiosDisponiveis;
    private Exception excecaoCapturada;

    @Dado("que existe um cliente com id {string}")
    public void queExisteUmClienteComId(String clienteIdStr) {
        UUID clienteId = UUID.nameUUIDFromBytes(clienteIdStr.getBytes());
        PontosCliente pontos = new PontosCliente(clienteId);
        contasEmMemoria.put(clienteIdStr, pontos);

        when(fidelidadeRepository.buscarPorCliente(clienteId))
                .thenAnswer(inv -> Optional.ofNullable(contasEmMemoria.get(clienteIdStr)));
        doAnswer(inv -> {
            contasEmMemoria.put(clienteIdStr, inv.getArgument(0));
            return null;
        }).when(fidelidadeRepository).salvar(any());
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

    @Dado("que o cliente {string} possui {int} pontos acumulados em {string} com validade {string}")
    public void queClientePossuiPontos(String clienteIdStr, int pontos, String dataStr, String validadeStr) {
        UUID clienteId = UUID.nameUUIDFromBytes(clienteIdStr.getBytes());
        PontosCliente conta = contasEmMemoria.computeIfAbsent(clienteIdStr, k -> {
            PontosCliente p = new PontosCliente(clienteId);
            when(fidelidadeRepository.buscarPorCliente(clienteId))
                    .thenAnswer(inv -> Optional.ofNullable(contasEmMemoria.get(clienteIdStr)));
            doAnswer(inv -> { contasEmMemoria.put(clienteIdStr, inv.getArgument(0)); return null; })
                    .when(fidelidadeRepository).salvar(any());
            return p;
        });
        conta.acumularPontos(pontos, LocalDate.parse(validadeStr));
    }

    @Quando("o cliente {string} realiza uma compra de R$ {double} em {string}")
    public void clienteRealizaCompra(String clienteIdStr, double valor, String dataStr) {
        UUID clienteId = UUID.nameUUIDFromBytes(clienteIdStr.getBytes());
        service.acumularPontos(clienteId, valor, LocalDate.parse(dataStr));
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
}