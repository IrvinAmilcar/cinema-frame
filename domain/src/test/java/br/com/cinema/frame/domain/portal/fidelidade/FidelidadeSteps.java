package br.com.cinema.frame.domain.portal.fidelidade;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

public class FidelidadeSteps {

    private ContaDeFidelidade conta;
    private Exception excecaoCapturada;
    private final ProgramaDeFidelidade programa = new ProgramaDeFidelidade();

    @Dado("que o cliente possui uma conta de fidelidade com {int} pontos")
    public void clientePossuiContaComPontos(int pontosIniciais) {
        conta = new ContaDeFidelidade(UUID.randomUUID());
        if (pontosIniciais > 0)
            conta.adicionarPontos(pontosIniciais);
    }

    @Quando("o cliente realizar uma compra no valor de R$ {double}")
    public void clienteRealizarCompra(double valor) {
        programa.creditar(conta, valor);
    }

    @Quando("o cliente resgatar {int} pontos")
    public void clienteResgatarPontos(int pontos) {
        programa.resgatar(conta, pontos);
    }

    @Quando("o cliente tentar resgatar {int} pontos")
    public void clienteTentarResgatarPontos(int pontos) {
        try {
            programa.resgatar(conta, pontos);
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
