package br.com.cinema.frame.domain.backoffice.caixa;

import br.com.cinema.frame.domain.backoffice.classificacao.ClassificacaoIndicativa;
import br.com.cinema.frame.domain.backoffice.grade.*;
import br.com.cinema.frame.domain.backoffice.ingresso.Ingresso;
import br.com.cinema.frame.domain.backoffice.ingresso.TipoIngresso;
import br.com.cinema.frame.domain.backoffice.precificacao.TipoSala;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CaixaSteps {

    private Sessao sessao;
    private final List<Ingresso> ingressos = new ArrayList<>();
    private Bordero bordero;
    private Exception excecaoCapturada;
    private final FechamentoDeCaixa fechamento = new FechamentoDeCaixa();

    @Dado("que existe uma sessão numa sala padrão numa sexta-feira às {int}:{int}")
    public void existeSessaoSextaFeira(int hora, int minuto) {
        var filme = new Filme("Filme Teste", Duration.ofMinutes(120), ClassificacaoIndicativa.LIVRE);
        var sala = new Sala(1, 100, TipoSala.PADRAO);
        LocalDateTime inicio = LocalDate.now()
            .with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
            .atTime(hora, minuto);
        sessao = new Sessao(filme, sala, inicio);
    }

    @Dado("foram vendidos {int} ingressos inteiros para essa sessão")
    public void venderInteiras(int quantidade) {
        for (int i = 0; i < quantidade; i++)
            ingressos.add(new Ingresso(sessao, TipoIngresso.INTEIRA));
    }

    @Dado("foram vendidos {int} ingressos meia para essa sessão")
    public void venderMeias(int quantidade) {
        for (int i = 0; i < quantidade; i++)
            ingressos.add(new Ingresso(sessao, TipoIngresso.MEIA));
    }

    @Dado("foram vendidos {int} ingressos convite para essa sessão")
    public void venderConvites(int quantidade) {
        for (int i = 0; i < quantidade; i++)
            ingressos.add(new Ingresso(sessao, TipoIngresso.CONVITE));
    }

    @Quando("o sistema gerar o borderô")
    public void gerarBordero() {
        bordero = fechamento.gerarBordero(sessao, ingressos);
    }

    @Quando("o sistema tentar gerar o borderô sem ingressos")
    public void tentarGerarBorderoVazio() {
        try {
            bordero = fechamento.gerarBordero(sessao, new ArrayList<>());
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Então("o total arrecadado deve ser R$ {double}")
    public void verificarTotal(double esperado) {
        assertEquals(esperado, bordero.getTotalArrecadado(), 0.01);
    }

    @Então("o repasse para a distribuidora deve ser R$ {double}")
    public void verificarRepasse(double esperado) {
        assertEquals(esperado, bordero.getRepasseDistribuidora(), 0.01);
    }

    @Então("a receita do cinema deve ser R$ {double}")
    public void verificarReceitaCinema(double esperado) {
        assertEquals(esperado, bordero.getReceitaCinema(), 0.01);
    }

    @Então("o sistema deve rejeitar informando lista de ingressos inválida")
    public void rejeitarListaVazia() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalArgumentException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("ingressos"));
    }
}