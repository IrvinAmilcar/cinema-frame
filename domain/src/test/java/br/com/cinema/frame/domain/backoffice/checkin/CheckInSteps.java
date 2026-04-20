package br.com.cinema.frame.domain.backoffice.checkin;

import br.com.cinema.frame.domain.backoffice.checkin.CheckIn;
import br.com.cinema.frame.domain.backoffice.classificacao.ClassificacaoIndicativa;
import br.com.cinema.frame.domain.backoffice.grade.Filme;
import br.com.cinema.frame.domain.backoffice.grade.Sala;
import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.ingresso.Ingresso;
import br.com.cinema.frame.domain.backoffice.ingresso.TipoIngresso;
import br.com.cinema.frame.domain.backoffice.precificacao.TipoSala;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CheckInSteps {

    private Ingresso ingresso;
    private Sessao sessao;
    private Exception excecaoCapturada;
    private final CheckIn checkIn = new CheckIn();

    @Dado("que existe um ingresso válido para a sessão de hoje às {int}:{int}")
    public void existeIngressoValido(int hora, int minuto) {
        Filme filme = new Filme("Filme Teste", Duration.ofMinutes(120), ClassificacaoIndicativa.LIVRE);
        Sala sala = new Sala(1, 100, TipoSala.PADRAO);
        sessao = new Sessao(filme, sala, LocalDate.now().atTime(hora, minuto));
        ingresso = new Ingresso(sessao, TipoIngresso.INTEIRA);
    }

    @Dado("o ingresso já foi utilizado")
    public void ingressoJaUtilizado() {
        ingresso.marcarComoUtilizado();
    }

    @Quando("o funcionário escaneia o ingresso às {int}:{int}")
    public void funcionarioEscaneia(int hora, int minuto) {
        LocalDateTime agora = LocalDate.now().atTime(hora, minuto);
        try {
            checkIn.realizar(ingresso, sessao.getId(), agora);
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Quando("o funcionário escaneia o ingresso numa sessão diferente às {int}:{int}")
    public void funcionarioEscaneiaOutraSessao(int hora, int minuto) {
        LocalDateTime agora = LocalDate.now().atTime(hora, minuto);
        try {
            checkIn.realizar(ingresso, UUID.randomUUID(), agora);
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Então("o check-in deve ser realizado com sucesso")
    public void checkInSucesso() {
        assertNull(excecaoCapturada);
        assertTrue(ingresso.isUtilizado());
    }

    @Então("o sistema deve rejeitar informando ingresso já utilizado")
    public void rejeitarIngressoJaUtilizado() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalStateException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("já utilizado"));
    }

    @Então("o sistema deve rejeitar informando sessão incorreta")
    public void rejeitarSessaoIncorreta() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalStateException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("não pertence"));
    }

    @Então("o sistema deve rejeitar informando fora do horário de entrada")
    public void rejeitarForaDoHorario() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalStateException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("horário permitido"));
    }
}