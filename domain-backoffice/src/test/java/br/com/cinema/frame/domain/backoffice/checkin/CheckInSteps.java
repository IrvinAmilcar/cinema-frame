package br.com.cinema.frame.domain.backoffice.checkin;

import br.com.cinema.frame.domain.shared.classificacao.ClassificacaoIndicativa;
import br.com.cinema.frame.domain.backoffice.grade.Filme;
import br.com.cinema.frame.domain.shared.filme.GeneroFilme;
import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.ingresso.Ingresso;
import br.com.cinema.frame.domain.backoffice.ingresso.IngressoRepository;
import br.com.cinema.frame.domain.backoffice.ingresso.TipoIngresso;
import br.com.cinema.frame.domain.backoffice.rbac.FuncionarioRepository;
import br.com.cinema.frame.domain.backoffice.rbac.Permissao;
import br.com.cinema.frame.domain.backoffice.rbac.RbacService;
import br.com.cinema.frame.domain.backoffice.sala.Sala;
import br.com.cinema.frame.domain.backoffice.sala.TipoSala;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class CheckInSteps {

    private IngressoRepository ingressoRepository = mock(IngressoRepository.class);
    private RegistroDeEntradaRepository registroRepository = mock(RegistroDeEntradaRepository.class);
    private RbacService rbacService = mock(RbacService.class);
    private CheckInService checkInService = new CheckInService(ingressoRepository, registroRepository, rbacService);

    private final UUID funcionarioAutorizadoId = UUID.randomUUID();
    private final UUID funcionarioNaoAutorizadoId = UUID.randomUUID();

    private Ingresso ingresso;
    private Sessao sessao;
    private RegistroDeEntrada registroResultado;
    private Exception excecaoCapturada;

    @Dado("que existe um ingresso cadastrado válido para a sessão de hoje às {int}:{int}")
    public void existeIngressoCadastradoValido(int hora, int minuto) {
        Filme filme = new Filme("Filme Teste", Duration.ofMinutes(120),
            ClassificacaoIndicativa.LIVRE, GeneroFilme.ACAO);
        Sala sala = new Sala(1, 100, TipoSala.PADRAO);
        sessao = new Sessao(filme, sala, LocalDate.now().atTime(hora, minuto));
        ingresso = new Ingresso(sessao, TipoIngresso.INTEIRA);
        when(ingressoRepository.buscarPorId(ingresso.getId())).thenReturn(Optional.of(ingresso));
        // funcionário autorizado não lança exceção (comportamento padrão do mock)
        // funcionário não autorizado lança exceção
        doThrow(new IllegalStateException("Acesso negado: sem permissão para REALIZAR_CHECKIN"))
            .when(rbacService).verificar(funcionarioNaoAutorizadoId, Permissao.REALIZAR_CHECKIN);
    }

    @Dado("o ingresso já foi utilizado")
    public void ingressoJaUtilizado() {
        ingresso.marcarComoUtilizado();
        when(ingressoRepository.buscarPorId(ingresso.getId())).thenReturn(Optional.of(ingresso));
    }

    @Quando("o funcionário escaneia o ingresso cadastrado às {int}:{int}")
    public void funcionarioEscaneia(int hora, int minuto) {
        LocalDateTime agora = LocalDate.now().atTime(hora, minuto);
        try {
            registroResultado = checkInService.realizar(funcionarioAutorizadoId, ingresso.getId(), sessao.getId(), agora);
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Quando("o funcionário escaneia o ingresso cadastrado numa sessão diferente às {int}:{int}")
    public void funcionarioEscaneiaOutraSessao(int hora, int minuto) {
        LocalDateTime agora = LocalDate.now().atTime(hora, minuto);
        try {
            registroResultado = checkInService.realizar(funcionarioAutorizadoId, ingresso.getId(), UUID.randomUUID(), agora);
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Quando("o funcionário escaneia um QR Code inexistente às {int}:{int}")
    public void funcionarioEscaneiaQrCodeInexistente(int hora, int minuto) {
        LocalDateTime agora = LocalDate.now().atTime(hora, minuto);
        UUID idInexistente = UUID.randomUUID();
        when(ingressoRepository.buscarPorId(idInexistente)).thenReturn(Optional.empty());
        try {
            registroResultado = checkInService.realizar(funcionarioAutorizadoId, idInexistente, UUID.randomUUID(), agora);
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Quando("um funcionário sem permissão tenta fazer check-in às {int}:{int}")
    public void funcionarioSemPermissaoTentaCheckin(int hora, int minuto) {
        LocalDateTime agora = LocalDate.now().atTime(hora, minuto);
        try {
            registroResultado = checkInService.realizar(funcionarioNaoAutorizadoId, ingresso.getId(), sessao.getId(), agora);
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Então("o check-in deve ser realizado com sucesso")
    public void checkInSucesso() {
        assertNull(excecaoCapturada);
        verify(ingressoRepository).salvar(ingresso);
        assertTrue(ingresso.isUtilizado());
    }

    @Então("um registro de entrada deve ser salvo")
    public void registroDeEntradaSalvo() {
        verify(registroRepository).salvar(any(RegistroDeEntrada.class));
        assertNotNull(registroResultado);
    }

    @Então("o sistema deve rejeitar informando QR Code inválido")
    public void rejeitarQrCodeInvalido() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalArgumentException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("QR Code inválido"));
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

    @Então("o sistema deve rejeitar informando acesso negado")
    public void rejeitarAcessoNegado() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalStateException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("Acesso negado"));
    }
}