package br.com.cinema.frame.domain.backoffice.grade;

import br.com.cinema.frame.domain.shared.classificacao.ClassificacaoIndicativa;
import br.com.cinema.frame.domain.shared.filme.GeneroFilme;
import br.com.cinema.frame.domain.backoffice.ingresso.Ingresso;
import br.com.cinema.frame.domain.backoffice.ingresso.IngressoRepository;
import br.com.cinema.frame.domain.backoffice.ingresso.IngressoService;
import br.com.cinema.frame.domain.backoffice.ingresso.TipoIngresso;
import br.com.cinema.frame.domain.backoffice.sala.Sala;
import br.com.cinema.frame.domain.backoffice.sala.SalaRepository;
import br.com.cinema.frame.domain.backoffice.sala.TipoSala;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class SessaoSteps {

    private GradeDeExibicaoRepository gradeRepository = mock(GradeDeExibicaoRepository.class);
    private FilmeRepository filmeRepository = mock(FilmeRepository.class);
    private SalaRepository salaRepository = mock(SalaRepository.class);
    private IngressoRepository ingressoRepository = mock(IngressoRepository.class);
    private GradeService gradeService = new GradeService(gradeRepository, filmeRepository, salaRepository);
    private IngressoService ingressoService = new IngressoService(ingressoRepository);

    private Filme filme;
    private Sala sala;
    private GradeDeExibicao grade;
    private Sessao sessaoAdicionada;
    private Sessao sessaoRemovida;
    private List<Ingresso> ingressosAfetados;
    private Exception excecaoCapturada;

    @Dado("que existe um filme cadastrado {string} com duração de {int} minutos e classificação {string} e gênero {string}")
    public void existeUmFilmeCadastrado(String titulo, int minutos, String classificacao, String genero) {
        filme = new Filme(titulo, Duration.ofMinutes(minutos),
            ClassificacaoIndicativa.valueOf(classificacao),
            GeneroFilme.valueOf(genero));
        when(filmeRepository.buscarPorId(filme.getId())).thenReturn(Optional.of(filme));
    }

    @Dado("existe uma sala cadastrada de número {int} com capacidade para {int} pessoas")
    public void existeUmaSalaCadastrada(int numero, int capacidade) {
        sala = new Sala(numero, capacidade, TipoSala.PADRAO);
        when(salaRepository.buscarPorId(sala.getId())).thenReturn(Optional.of(sala));
    }

    @Dado("existe uma grade de exibição cadastrada para a semana")
    public void existeUmaGradeCadastrada() {
        grade = new GradeDeExibicao(LocalDate.now(), LocalDate.now().plusDays(7));
        when(gradeRepository.buscarPorId(grade.getId())).thenReturn(Optional.of(grade));
        when(gradeRepository.buscarPorData(any(LocalDate.class))).thenReturn(Optional.of(grade));
    }

    @Dado("a gerente já cadastrou uma sessão na grade às {int}:{int}")
    public void gerenteJaCadastrouSessao(int hora, int minuto) {
        LocalDateTime inicio = LocalDate.now().atTime(hora, minuto);
        gradeService.adicionarSessao(grade.getId(), filme.getId(), sala.getId(), inicio);
        sessaoAdicionada = grade.getSessoes().get(grade.getSessoes().size() - 1);
    }

    @Dado("a gerente já cadastrou uma sessão para amanhã às {int}:{int}")
    public void gerenteJaCadastrouSessaoParaAmanha(int hora, int minuto) {
        LocalDateTime inicio = LocalDate.now().plusDays(1).atTime(hora, minuto);
        gradeService.adicionarSessao(grade.getId(), filme.getId(), sala.getId(), inicio);
        sessaoAdicionada = grade.getSessoes().get(grade.getSessoes().size() - 1);
    }

    @Dado("já existem {int} ingressos comprados para essa sessão")
    public void jaExistemIngressosComprados(int quantidade) {
        List<Ingresso> ingressos = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            ingressos.add(new Ingresso(sessaoAdicionada, TipoIngresso.INTEIRA));
        }
        when(ingressoRepository.buscarPorSessao(sessaoAdicionada)).thenReturn(ingressos);
    }

    @Dado("a gerente já cadastrou uma sessão que já foi iniciada")
    public void gerenteJaCadastrouSessaoJaIniciada() {
        LocalDateTime inicio = LocalDateTime.now().minusHours(2);
        gradeService.adicionarSessao(grade.getId(), filme.getId(), sala.getId(), inicio);
        sessaoAdicionada = grade.getSessoes().get(grade.getSessoes().size() - 1);
    }

    @Quando("a gerente adiciona a sessão na grade às {int}:{int}")
    public void gerenteAdicionaSessao(int hora, int minuto) {
        LocalDateTime inicio = LocalDate.now().atTime(hora, minuto);
        gradeService.adicionarSessao(grade.getId(), filme.getId(), sala.getId(), inicio);
    }

    @Quando("a gerente tenta cadastrar uma sessão de {string} com duração de {int} minutos e gênero {string} às {int}:{int}")
    public void gerenteTentaCadastrarSessaoComConflito(String titulo, int minutos, String genero, int hora, int minuto) {
        try {
            Filme novoFilme = new Filme(titulo, Duration.ofMinutes(minutos),
                ClassificacaoIndicativa.QUATORZE, GeneroFilme.valueOf(genero));
            when(filmeRepository.buscarPorId(novoFilme.getId())).thenReturn(Optional.of(novoFilme));
            LocalDateTime inicio = LocalDate.now().atTime(hora, minuto);
            gradeService.adicionarSessao(grade.getId(), novoFilme.getId(), sala.getId(), inicio);
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Quando("a gerente remove a sessão")
    public void gerenteRemoveSessao() {
        sessaoRemovida = gradeService.removerSessao(grade.getId(), sessaoAdicionada.getId(), LocalDateTime.now());
        ingressosAfetados = ingressoService.buscarPorSessao(sessaoRemovida);
    }

    @Quando("a gerente tenta remover a sessão já iniciada")
    public void gerenteTentaRemoverSessaoJaIniciada() {
        try {
            gradeService.removerSessao(grade.getId(), sessaoAdicionada.getId(), LocalDateTime.now());
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Então("a sessão deve ser salva na grade")
    public void sessaoDeveSerSalvaNaGrade() {
        verify(gradeRepository, atLeastOnce()).salvar(any(GradeDeExibicao.class));
        assertFalse(grade.getSessoes().isEmpty());
    }

    @Então("a sessão deve ser removida da grade")
    public void sessaoDeveSerRemovidaDaGrade() {
        verify(gradeRepository, atLeastOnce()).salvar(any(GradeDeExibicao.class));
        assertTrue(grade.getSessoes().isEmpty());
    }

    @Então("o sistema identifica {int} ingressos que precisam de reembolso")
    public void sistemaIdentificaIngressosParaReembolso(int quantidade) {
        assertNotNull(ingressosAfetados);
        assertEquals(quantidade, ingressosAfetados.size());
    }

    @Então("o sistema deve rejeitar informando conflito de horário")
    public void sistemaDeveRejeitarConflito() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalStateException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("Conflito de horário"));
    }

    @Então("o sistema deve impedir informando que a sessão já foi iniciada")
    public void sistemaDeveImpedirRemocaoSessaoIniciada() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalStateException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("já foi iniciada"));
    }
}
