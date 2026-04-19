package br.com.cinema.frame.domain.backoffice.grade;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class SessaoSteps {

    private Filme filme;
    private Sala sala;
    private GradeDeExibicao grade;
    private Exception excecaoCapturada;

    @Dado("que existe um filme {string} com duração de {int} minutos e classificação {string}")
    public void existeUmFilme(String titulo, int minutos, String classificacao) {
        filme = new Filme(titulo, Duration.ofMinutes(minutos), classificacao);
    }

    @Dado("existe uma sala de número {int} com capacidade para {int} pessoas")
    public void existeUmaSala(int numero, int capacidade) {
        sala = new Sala(numero, capacidade);
    }

    @Dado("existe uma grade de exibição para a semana")
    public void existeUmaGrade() {
        grade = new GradeDeExibicao(
            LocalDate.now(),
            LocalDate.now().plusDays(7)
        );
    }

    @Dado("a gerente já adicionou a sessão na sala {int} às {int}:{int}")
    public void dadoGerenteAdicionaSessao(int numeroSala, int hora, int minuto) {
        LocalDateTime inicio = LocalDate.now().atTime(hora, minuto);
        Sessao sessao = new Sessao(filme, sala, inicio);
        grade.adicionarSessao(sessao);
    }

    @Quando("a gerente adiciona a sessão na sala {int} às {int}:{int}")
    public void quandoGerenteAdicionaSessao(int numeroSala, int hora, int minuto) {
        LocalDateTime inicio = LocalDate.now().atTime(hora, minuto);
        Sessao sessao = new Sessao(filme, sala, inicio);
        grade.adicionarSessao(sessao);
    }

    @Quando("a gerente tenta adicionar uma sessão de {string} com duração de {int} minutos às {int}:{int} na sala {int}")
    public void gerenteTentaAdicionarSessaoComConflito(String titulo, int minutos, int hora, int minuto, int numeroSala) {
        try {
            Filme novoFilme = new Filme(titulo, Duration.ofMinutes(minutos), "14");
            LocalDateTime inicio = LocalDate.now().atTime(hora, minuto);
            Sessao novaSessao = new Sessao(novoFilme, sala, inicio);
            grade.adicionarSessao(novaSessao);
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Então("a sessão deve estar na grade")
    public void sessaoDeveEstarNaGrade() {
        assertFalse(grade.getSessoes().isEmpty());
    }

    @Então("o sistema deve rejeitar informando conflito de horário")
    public void sistemadeveRejeitarConflito() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalStateException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("Conflito de horário"));
    }
}