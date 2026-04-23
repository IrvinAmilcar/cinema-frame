package br.com.cinema.frame.domain.portal.reserva;

import br.com.cinema.frame.domain.backoffice.classificacao.ClassificacaoIndicativa;
import br.com.cinema.frame.domain.backoffice.grade.Filme;
import br.com.cinema.frame.domain.backoffice.grade.GeneroFilme;
import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicao;
import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicaoRepository;
import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.sala.Sala;
import br.com.cinema.frame.domain.backoffice.sala.TipoSala;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class ReservaSteps {

    private ReservaRepository reservaRepository = mock(ReservaRepository.class);
    private GradeDeExibicaoRepository gradeRepository = mock(GradeDeExibicaoRepository.class);
    private ReservaService reservaService = new ReservaService(reservaRepository, gradeRepository);

    private Sessao sessao;
    private GradeDeExibicao grade;
    private ReservaDeAssento reservaCriada;
    private Exception excecaoCapturada;
    private LocalDateTime agora;

    // Lista mutável de reservas em memória para simular o repositório
    private final List<ReservaDeAssento> reservasEmMemoria = new ArrayList<>();

    @Dado("que existe uma sessão cadastrada disponível")
    public void existeSessaoCadastradaDisponivel() {
        agora = LocalDate.now().atTime(14, 0);

        Filme filme = new Filme("Filme Teste", Duration.ofMinutes(120),
            ClassificacaoIndicativa.LIVRE, GeneroFilme.COMEDIA);
        Sala sala = new Sala(1, 100, TipoSala.PADRAO);
        sessao = new Sessao(filme, sala, agora.plusHours(2));

        grade = new GradeDeExibicao(LocalDate.now(), LocalDate.now().plusDays(7));
        grade.adicionarSessao(sessao);

        when(gradeRepository.listarTodas()).thenReturn(List.of(grade));

        // Configura o repositório de reservas para usar a lista em memória
        when(reservaRepository.buscarPorSessaoId(sessao.getId()))
            .thenAnswer(inv -> new ArrayList<>(reservasEmMemoria));
        doAnswer(inv -> {
            ReservaDeAssento r = inv.getArgument(0);
            reservasEmMemoria.removeIf(existing -> existing.getId().equals(r.getId()));
            reservasEmMemoria.add(r);
            return null;
        }).when(reservaRepository).salvar(any(ReservaDeAssento.class));
        when(reservaRepository.buscarPorId(any(UUID.class)))
            .thenAnswer(inv -> reservasEmMemoria.stream()
                .filter(r -> r.getId().equals(inv.getArgument(0)))
                .findFirst());
    }

    @Dado("o assento {int} já foi reservado para a sessão cadastrada")
    public void assentoJaFoiReservadoParaSessaoCadastrada(int numeroAssento) {
        ReservaDeAssento reserva = new ReservaDeAssento(sessao, numeroAssento, agora);
        reservasEmMemoria.add(reserva);
    }

    @Dado("o assento {int} foi reservado há {int} minutos para a sessão cadastrada")
    public void assentoReservadoHaMinutosParaSessaoCadastrada(int numeroAssento, int minutos) {
        LocalDateTime momentoReserva = agora.minusMinutes(minutos);
        ReservaDeAssento reserva = new ReservaDeAssento(sessao, numeroAssento, momentoReserva);
        reservasEmMemoria.add(reserva);
        reservaCriada = reserva;
    }

    @Quando("o cliente reservar o assento {int} na sessão cadastrada")
    public void clienteReservarAssento(int numeroAssento) {
        reservaCriada = reservaService.reservar(sessao.getId(), numeroAssento, agora);
    }

    @Quando("outro cliente tentar reservar o assento {int} na sessão cadastrada")
    public void outroClienteTentarReservar(int numeroAssento) {
        try {
            reservaCriada = reservaService.reservar(sessao.getId(), numeroAssento, agora);
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Quando("o cliente confirmar a reserva cadastrada")
    public void clienteConfirmarReservaCadastrada() {
        reservaService.confirmar(reservaCriada.getId(), agora);
    }

    @Quando("o cliente tentar confirmar a reserva cadastrada expirada")
    public void clienteTentarConfirmarReservaCadastradaExpirada() {
        try {
            reservaService.confirmar(reservaCriada.getId(), agora);
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