package br.com.cinema.frame.domain.backoffice.classificacao;

import br.com.cinema.frame.domain.backoffice.grade.Filme;
import br.com.cinema.frame.domain.backoffice.grade.GeneroFilme;
import br.com.cinema.frame.domain.portal.cliente.Cliente;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class ClassificacaoCompraSteps {

    private Cliente cliente;
    private Filme filme;
    private Exception excecaoCapturada;
    private final ValidacaoClassificacaoDeCompra validacao = new ValidacaoClassificacaoDeCompra();

    @Dado("que existe um cliente nascido em {string}")
    public void clienteNascidoEm(String data) {
        cliente = new Cliente("Cliente Teste", "teste@email.com", LocalDate.parse(data));
    }

    @Dado("que existe um cliente que tem exatamente {int} anos")
    public void clienteComExatamenteAnos(int anos) {
        cliente = new Cliente("Cliente Teste", "teste@email.com", LocalDate.now().minusYears(anos));
    }

    @Dado("existe um filme com classificação {string}")
    public void filmeComClassificacao(String classificacao) {
        filme = new Filme("Filme Teste", Duration.ofMinutes(120),
            ClassificacaoIndicativa.valueOf(classificacao), GeneroFilme.ACAO);
    }

    @Quando("o sistema validar a compra")
    public void sistemaValidarCompra() {
        validacao.validarCompra(cliente, filme);
    }

    @Quando("o sistema tentar validar a compra")
    public void sistemaTentarValidarCompra() {
        try {
            validacao.validarCompra(cliente, filme);
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Então("a compra deve ser permitida")
    public void compradeveSerPermitida() {
        assertNull(excecaoCapturada);
    }

    @Então("a compra deve ser bloqueada com mensagem de idade insuficiente")
    public void compraDeveSerBloqueada() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalStateException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("não tem idade permitida"));
    }
}