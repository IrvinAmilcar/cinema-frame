package br.com.cinema.frame.domain.portal.promocao;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import br.com.cinema.frame.domain.portal.promocao.AplicacaoDeDesconto;
import br.com.cinema.frame.domain.portal.promocao.Cupom;
import br.com.cinema.frame.domain.portal.promocao.MotorDePromocoes;
import br.com.cinema.frame.domain.portal.promocao.TipoPromocao;

import static org.junit.jupiter.api.Assertions.*;

public class PromocaoSteps {

    private double valorTotal;
    private int quantidadeIngressos;
    private final List<Cupom> cupons = new ArrayList<>();
    private AplicacaoDeDesconto resultado;
    private Exception excecaoCapturada;
    private final MotorDePromocoes motor = new MotorDePromocoes();
    private final LocalDate hoje = LocalDate.now();

    @Dado("que o valor total do pedido é R$ {double} com {int} ingressos")
    public void valorTotalDoPedido(double valor, int quantidade) {
        this.valorTotal = valor;
        this.quantidadeIngressos = quantidade;
    }

    @Dado("existe um cupom {string} do tipo {string} não cumulativo e válido")
    public void cupomNaoCumulativoValido(String codigo, String tipo) {
        cupons.add(new Cupom(codigo, TipoPromocao.valueOf(tipo), false, hoje.plusDays(30)));
    }

    @Dado("existe um cupom {string} do tipo {string} cumulativo e válido")
    public void cupomCumulativoValido(String codigo, String tipo) {
        cupons.add(new Cupom(codigo, TipoPromocao.valueOf(tipo), true, hoje.plusDays(30)));
    }

    @Dado("existe um cupom {string} do tipo {string} não cumulativo e expirado")
    public void cupomNaoCumulativoExpirado(String codigo, String tipo) {
        cupons.add(new Cupom(codigo, TipoPromocao.valueOf(tipo), false, hoje.minusDays(1)));
    }

    @Quando("o motor de promoções aplicar os cupons")
    public void aplicarCupons() {
        resultado = motor.aplicar(valorTotal, quantidadeIngressos, cupons, hoje);
    }

    @Quando("o motor de promoções tentar aplicar os cupons")
    public void tentarAplicarCupons() {
        try {
            resultado = motor.aplicar(valorTotal, quantidadeIngressos, cupons, hoje);
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Então("o valor de desconto deve ser R$ {double}")
    public void valorDescontoDeveSer(double esperado) {
        assertEquals(esperado, resultado.getValorDesconto(), 0.01);
    }

    @Então("o valor final deve ser R$ {double}")
    public void valorFinalDeveSer(double esperado) {
        assertEquals(esperado, resultado.getValorFinal(), 0.01);
    }

    @Então("o sistema deve rejeitar informando que cupons não cumulativos não podem ser combinados")
    public void rejeitarCuponsNaoCumulativos() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalStateException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("não cumulativos"));
    }

    @Então("o sistema deve rejeitar informando que o cupom está expirado")
    public void rejeitarCupomExpirado() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalStateException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("expirado"));
    }
}