package br.com.cinema.frame.domain.portal.reserva;

import br.com.cinema.frame.domain.backoffice.classificacao.ClassificacaoIndicativa;
import br.com.cinema.frame.domain.backoffice.grade.Filme;
import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.precificacao.TipoSala;
import br.com.cinema.frame.domain.backoffice.sala.Sala;
import br.com.cinema.frame.domain.portal.reserva.GestaoDeReservas;
import br.com.cinema.frame.domain.portal.reserva.ReservaDeAssento;
import br.com.cinema.frame.domain.portal.reserva.StatusReserva;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ReservaSteps {

    private Sessao sessao;
    private ReservaDeAssento reservaCriada;
    private Exception excecaoCapturada;
    private LocalDateTime agora;
    private final GestaoDeReservas gestao = new GestaoDeReservas();

    @Dado("que existe uma sessão disponível")
    public void existeSessaoDisponivel() {
        agora = LocalDate.now().atTime(14, 0);
        Filme filme = new Filme("Filme Teste", Duration.ofMinutes(120), ClassificacaoIndicativa.LIVRE);
        Sala sala = new Sala(1, 100, TipoSala.PADRAO);
        sessao = new Sessao(filme, sala, agora.plusHours(2));
    }

    @Dado("o assento {int} já foi reservado")
    public void assentoJaFoiReservado(int numeroAssento) {
        reservaCriada = gestao.reservar(sessao, numeroAssento, agora);
    }

    @Dado("o assento {int} foi reservado há {int} minutos")
    public void assentoReservadoHaMinutos(int numeroAssento, int minutos) {
        LocalDateTime momentoReserva = agora.minusMinutes(minutos);
        reservaCriada = gestao.reservar(sessao, numeroAssento, momentoReserva);
    }

    @Quando("o cliente reservar o assento {int}")
    public void clienteReservarAssento(int numeroAssento) {
        reservaCriada = gestao.reservar(sessao, numeroAssento, agora);
    }

    @Quando("outro cliente tentar reservar o assento {int}")
    public void outroClienteTentarReservar(int numeroAssento) {
        try {
            reservaCriada = gestao.reservar(sessao, numeroAssento, agora);
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Quando("o cliente confirmar a reserva")
    public void clienteConfirmarReserva() {
        gestao.confirmar(reservaCriada.getId(), agora);
    }

    @Quando("o cliente tentar confirmar a reserva expirada")
    public void clienteTentarConfirmarReservaExpirada() {
        try {
            gestao.confirmar(reservaCriada.getId(), agora);
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Então("a reserva deve estar ativa com status {string}")
    public void reservaDeveEstarAtivaComStatus(String statusEsperado) {
        assertNotNull(reservaCriada);
        assertEquals(StatusReserva.valueOf(statusEsperado), reservaCriada.getStatus());
    }

    @Então("a reserva deve expirar em {int} minutos")
    public void reservaDeveExpirarEmMinutos(int minutos) {
        LocalDateTime expiracaoEsperada = agora.plusMinutes(minutos);
        assertEquals(expiracaoEsperada, reservaCriada.getExpiracaoEm());
    }

    @Então("a reserva deve ter status {string}")
    public void reservaDeveTerStatus(String statusEsperado) {
        assertEquals(StatusReserva.valueOf(statusEsperado), reservaCriada.getStatus());
    }

    @Então("o sistema deve rejeitar informando assento já reservado")
    public void rejeitarAssentoJaReservado() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalStateException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("já está reservado"));
    }

    @Então("o sistema deve rejeitar informando reserva expirada")
    public void rejeitarReservaExpirada() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalStateException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("expirada"));
    }
}