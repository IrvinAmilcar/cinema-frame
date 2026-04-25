package br.com.cinema.frame.domain.backoffice.grade;

import br.com.cinema.frame.domain.shared.classificacao.ClassificacaoIndicativa;
import br.com.cinema.frame.domain.shared.filme.GeneroFilme;
import br.com.cinema.frame.domain.backoffice.sala.Sala;
import br.com.cinema.frame.domain.backoffice.sala.SalaRepository;
import br.com.cinema.frame.domain.backoffice.sala.TipoSala;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class SessaoSteps {

    private GradeDeExibicaoRepository gradeRepository = mock(GradeDeExibicaoRepository.class);
    private FilmeRepository filmeRepository = mock(FilmeRepository.class);
    private SalaRepository salaRepository = mock(SalaRepository.class);
    private GradeService gradeService = new GradeService(gradeRepository, filmeRepository, salaRepository);

    private Filme filme;
    private Sala sala;
    private GradeDeExibicao grade;
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

    @Então("a sessão deve ser salva na grade")
    public void sessaoDeveSerSalvaNaGrade() {
        verify(gradeRepository, atLeastOnce()).salvar(any(GradeDeExibicao.class));
        assertFalse(grade.getSessoes().isEmpty());
    }

    @Então("o sistema deve rejeitar informando conflito de horário")
    public void sistemaDeveRejeitarConflito() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalStateException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("Conflito de horário"));
    }
}