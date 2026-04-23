package br.com.cinema.frame.domain.portal.promocao;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class PromocaoSteps {

    private CupomRepository cupomRepository = mock(CupomRepository.class);
    private PromocaoService promocaoService = new PromocaoService(cupomRepository);

    private double valorTotal;
    private int quantidadeIngressos;
    private final List<UUID> cupomIds = new ArrayList<>();
    private AplicacaoDeDesconto resultado;
    private Exception excecaoCapturada;
    private final LocalDate hoje = LocalDate.now();

    @Dado("que o valor total do pedido é R$ {double} com {int} ingressos")
    public void valorTotalDoPedido(double valor, int quantidade) {
        this.valorTotal = valor;
        this.quantidadeIngressos = quantidade;
    }

    @Dado("existe um cupom cadastrado {string} do tipo {string} não cumulativo e válido")
    public void cupomCadastradoNaoCumulativoValido(String codigo, String tipo) {
        Cupom cupom = new Cupom(codigo, TipoPromocao.valueOf(tipo), false, hoje.plusDays(30));
        when(cupomRepository.buscarPorId(cupom.getId())).thenReturn(Optional.of(cupom));
        cupomIds.add(cupom.getId());
    }

    @Dado("existe um cupom cadastrado {string} do tipo {string} cumulativo e válido")
    public void cupomCadastradoCumulativoValido(String codigo, String tipo) {
        Cupom cupom = new Cupom(codigo, TipoPromocao.valueOf(tipo), true, hoje.plusDays(30));
        when(cupomRepository.buscarPorId(cupom.getId())).thenReturn(Optional.of(cupom));
        cupomIds.add(cupom.getId());
    }

    @Dado("existe um cupom cadastrado {string} do tipo {string} não cumulativo e expirado")
    public void cupomCadastradoNaoCumulativoExpirado(String codigo, String tipo) {
        Cupom cupom = new Cupom(codigo, TipoPromocao.valueOf(tipo), false, hoje.minusDays(1));
        when(cupomRepository.buscarPorId(cupom.getId())).thenReturn(Optional.of(cupom));
        cupomIds.add(cupom.getId());
    }

    @Quando("o motor de promoções aplicar os cupons cadastrados")
    public void aplicarCuponsCadastrados() {
        resultado = promocaoService.aplicarCupons(valorTotal, quantidadeIngressos, cupomIds, hoje);
    }

    @Quando("o motor de promoções tentar aplicar os cupons cadastrados")
    public void tentarAplicarCuponsCadastrados() {
        try {
            resultado = promocaoService.aplicarCupons(valorTotal, quantidadeIngressos, cupomIds, hoje);
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