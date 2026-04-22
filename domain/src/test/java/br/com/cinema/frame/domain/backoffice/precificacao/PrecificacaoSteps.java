package br.com.cinema.frame.domain.backoffice.precificacao;

import br.com.cinema.frame.domain.backoffice.grade.Filme;
import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.sala.Sala;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

import br.com.cinema.frame.domain.backoffice.classificacao.ClassificacaoIndicativa;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

import static org.junit.jupiter.api.Assertions.*;

public class PrecificacaoSteps {

    private Sessao sessao;
    private double precoCalculado;
    private final Precificacao precificacao = new Precificacao();

    @Dado("que existe uma sessão numa sala padrão numa sexta-feira")
    public void sessaoSalaPadraoSexta() { sessao = criarSessao(TipoSala.PADRAO, DayOfWeek.FRIDAY); }

    @Dado("que existe uma sessão numa sala 3D numa sexta-feira")
    public void sessaoSala3DSexta() { sessao = criarSessao(TipoSala.TRES_D, DayOfWeek.FRIDAY); }

    @Dado("que existe uma sessão numa sala IMAX numa sexta-feira")
    public void sessaoSalaIMAXSexta() { sessao = criarSessao(TipoSala.IMAX, DayOfWeek.FRIDAY); }

    @Dado("que existe uma sessão numa sala VIP numa sexta-feira")
    public void sessaoSalaVIPSexta() { sessao = criarSessao(TipoSala.VIP, DayOfWeek.FRIDAY); }

    @Dado("que existe uma sessão numa sala padrão numa segunda-feira")
    public void sessaoSalaPadraoSegunda() { sessao = criarSessao(TipoSala.PADRAO, DayOfWeek.MONDAY); }

    @Dado("que existe uma sessão numa sala padrão numa terça-feira")
    public void sessaoSalaPadraoTerca() { sessao = criarSessao(TipoSala.PADRAO, DayOfWeek.TUESDAY); }

    @Dado("que existe uma sessão numa sala padrão numa quarta-feira")
    public void sessaoSalaPadraoQuarta() { sessao = criarSessao(TipoSala.PADRAO, DayOfWeek.WEDNESDAY); }

    @Dado("que existe uma sessão numa sala padrão numa quinta-feira")
    public void sessaoSalaPadraoQuinta() { sessao = criarSessao(TipoSala.PADRAO, DayOfWeek.THURSDAY); }

    @Dado("que existe uma sessão numa sala padrão num sábado")
    public void sessaoSalaPadraoSabado() { sessao = criarSessao(TipoSala.PADRAO, DayOfWeek.SATURDAY); }

    @Dado("que existe uma sessão numa sala padrão num domingo")
    public void sessaoSalaPadraoDomingo() { sessao = criarSessao(TipoSala.PADRAO, DayOfWeek.SUNDAY); }

    @Dado("que existe uma sessão numa sala 3D numa terça-feira")
    public void sessaoSala3DTerca() { sessao = criarSessao(TipoSala.TRES_D, DayOfWeek.TUESDAY); }

    @Dado("que existe uma sessão numa sala IMAX numa terça-feira")
    public void sessaoSalaIMAXTerca() { sessao = criarSessao(TipoSala.IMAX, DayOfWeek.TUESDAY); }

    @Dado("que existe uma sessão numa sala VIP numa terça-feira")
    public void sessaoSalaVIPTerca() { sessao = criarSessao(TipoSala.VIP, DayOfWeek.TUESDAY); }

    @Dado("que existe uma sessão numa sala 3D numa segunda-feira")
    public void sessaoSala3DSegunda() { sessao = criarSessao(TipoSala.TRES_D, DayOfWeek.MONDAY); }

    @Dado("que existe uma sessão numa sala IMAX numa segunda-feira")
    public void sessaoSalaIMAXSegunda() { sessao = criarSessao(TipoSala.IMAX, DayOfWeek.MONDAY); }

    @Dado("que existe uma sessão numa sala VIP numa segunda-feira")
    public void sessaoSalaVIPSegunda() { sessao = criarSessao(TipoSala.VIP, DayOfWeek.MONDAY); }

    @Quando("o sistema calcular o preço")
    public void calcularPreco() {
        precoCalculado = precificacao.calcularPreco(sessao);
    }

    @Então("o preço deve ser R$ {double}")
    public void precioDeveSer(double precoEsperado) {
        assertEquals(precoEsperado, precoCalculado, 0.01);
    }

    private Sessao criarSessao(TipoSala tipoSala, DayOfWeek dia) {
    Filme filme = new Filme("Filme Teste", Duration.ofMinutes(120), ClassificacaoIndicativa.QUATORZE);
    Sala sala = new Sala(1, 100, tipoSala);
    LocalDateTime inicio = LocalDateTime.now()
        .with(TemporalAdjusters.nextOrSame(dia))
        .withHour(20)
        .withMinute(0);
    return new Sessao(filme, sala, inicio);
}
}