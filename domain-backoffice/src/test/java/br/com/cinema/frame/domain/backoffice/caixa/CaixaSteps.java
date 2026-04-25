package br.com.cinema.frame.domain.backoffice.caixa;

import br.com.cinema.frame.domain.shared.classificacao.ClassificacaoIndicativa;
import br.com.cinema.frame.domain.shared.filme.Filme;
import br.com.cinema.frame.domain.shared.filme.GeneroFilme;
import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicao;
import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicaoRepository;
import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.ingresso.Ingresso;
import br.com.cinema.frame.domain.backoffice.ingresso.IngressoRepository;
import br.com.cinema.frame.domain.backoffice.ingresso.TipoIngresso;
import br.com.cinema.frame.domain.backoffice.sala.Sala;
import br.com.cinema.frame.domain.backoffice.sala.TipoSala;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class CaixaSteps {

    private IngressoRepository ingressoRepository = mock(IngressoRepository.class);
    private GradeDeExibicaoRepository gradeRepository = mock(GradeDeExibicaoRepository.class);
    private CaixaService caixaService = new CaixaService(ingressoRepository, gradeRepository);

    private Sessao sessao;
    private GradeDeExibicao grade;
    private final List<Ingresso> ingressos = new ArrayList<>();
    private Bordero bordero;
    private Exception excecaoCapturada;

    @Dado("que existe uma sessão cadastrada numa sala padrão numa sexta-feira às {int}:{int}")
    public void existeSessaoCadastradaSextaFeira(int hora, int minuto) {
        Filme filme = new Filme("Filme Teste", Duration.ofMinutes(120),
            ClassificacaoIndicativa.LIVRE, GeneroFilme.ACAO);
        Sala sala = new Sala(1, 100, TipoSala.PADRAO);
        LocalDateTime inicio = LocalDate.now()
            .with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
            .atTime(hora, minuto);
        sessao = new Sessao(filme, sala, inicio);

        grade = new GradeDeExibicao(LocalDate.now(), LocalDate.now().plusDays(7));
        grade.adicionarSessao(sessao);

        when(gradeRepository.buscarPorData(any(LocalDate.class))).thenReturn(Optional.of(grade));
    }

    @Dado("foram vendidos {int} ingressos inteiros cadastrados para essa sessão")
    public void venderInteirasCadastradas(int quantidade) {
        for (int i = 0; i < quantidade; i++)
            ingressos.add(new Ingresso(sessao, TipoIngresso.INTEIRA));
        when(ingressoRepository.buscarPorSessao(sessao)).thenReturn(ingressos);
    }

    @Dado("foram vendidos {int} ingressos meia cadastrados para essa sessão")
    public void venderMeiasCadastradas(int quantidade) {
        for (int i = 0; i < quantidade; i++)
            ingressos.add(new Ingresso(sessao, TipoIngresso.MEIA));
        when(ingressoRepository.buscarPorSessao(sessao)).thenReturn(ingressos);
    }

    @Dado("foram vendidos {int} ingressos convite cadastrados para essa sessão")
    public void venderConvitesCadastrados(int quantidade) {
        for (int i = 0; i < quantidade; i++)
            ingressos.add(new Ingresso(sessao, TipoIngresso.CONVITE));
        when(ingressoRepository.buscarPorSessao(sessao)).thenReturn(ingressos);
    }

    @Quando("o sistema gerar o borderô da sessão")
    public void gerarBordero() {
        bordero = caixaService.gerarBordero(sessao.getId());
    }

    @Quando("o sistema tentar gerar o borderô sem ingressos cadastrados")
    public void tentarGerarBorderoVazio() {
        when(ingressoRepository.buscarPorSessao(sessao)).thenReturn(new ArrayList<>());
        try {
            bordero = caixaService.gerarBordero(sessao.getId());
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
        assertTrue(excecaoCapturada.getMessage().contains("ingresso"));
    }
}