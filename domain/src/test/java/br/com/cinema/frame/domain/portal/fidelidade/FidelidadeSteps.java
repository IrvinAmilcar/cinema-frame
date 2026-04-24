package br.com.cinema.frame.domain.portal.fidelidade;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class FidelidadeSteps {

    private ContaDeFidelidadeRepository contaRepository = mock(ContaDeFidelidadeRepository.class);
    private ProgramaDeFidelidade programa = new ProgramaDeFidelidade(contaRepository);

    private ContaDeFidelidade conta;
    private Exception excecaoCapturada;

    @Dado("que o cliente possui uma conta de fidelidade cadastrada com {int} pontos")
    public void clientePossuiContaCadastradaComPontos(int pontosIniciais) {
        conta = new ContaDeFidelidade(UUID.randomUUID());
        if (pontosIniciais > 0)
            conta.adicionarPontos(pontosIniciais);
        when(contaRepository.buscarPorId(conta.getId())).thenReturn(Optional.of(conta));
    }

    @Quando("o cliente realizar uma compra no valor de R$ {double} na conta cadastrada")
    public void clienteRealizarCompraNaContaCadastrada(double valor) {
        programa.creditar(conta.getId(), valor);
    }

    @Quando("o cliente resgatar {int} pontos da conta cadastrada")
    public void clienteResgatarPontosDaContaCadastrada(int pontos) {
        programa.resgatar(conta.getId(), pontos);
    }

    @Quando("o cliente tentar resgatar {int} pontos da conta cadastrada")
    public void clienteTentarResgatarPontosDaContaCadastrada(int pontos) {
        try {
            programa.resgatar(conta.getId(), pontos);
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Então("o saldo de pontos deve ser {int}")
    public void saldoDePontosDeveSer(int esperado) {
        assertEquals(esperado, conta.getSaldoDePontos());
    }

    @Então("o sistema deve rejeitar informando saldo insuficiente")
    public void rejeitarSaldoInsuficiente() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalStateException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("insuficiente"));
    }

    @Então("o sistema deve rejeitar informando quantidade inválida de pontos")
    public void rejeitarQuantidadeInvalida() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalArgumentException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("positiva"));
    }
}