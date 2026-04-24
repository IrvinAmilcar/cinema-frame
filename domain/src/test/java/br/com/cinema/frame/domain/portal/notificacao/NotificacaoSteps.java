package br.com.cinema.frame.domain.portal.notificacao;

import br.com.cinema.frame.domain.backoffice.classificacao.ClassificacaoIndicativa;
import br.com.cinema.frame.domain.backoffice.grade.Filme;
import br.com.cinema.frame.domain.backoffice.grade.FilmeRepository;
import br.com.cinema.frame.domain.backoffice.grade.GeneroFilme;
import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.grade.SessaoRepository;
import br.com.cinema.frame.domain.backoffice.sala.Sala;
import br.com.cinema.frame.domain.backoffice.sala.TipoSala;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class NotificacaoSteps {

    private FilmeFavoritadoRepository filmeFavoritadoRepository = mock(FilmeFavoritadoRepository.class);
    private FilmeRepository filmeRepository = mock(FilmeRepository.class);
    private SessaoRepository sessaoRepository = mock(SessaoRepository.class);

    private NotificacaoService notificacaoService = new NotificacaoService(
        filmeFavoritadoRepository, filmeRepository, sessaoRepository
    );

    private Filme filme;
    private Sessao sessao;
    private UUID filmeIdNaoCadastrado;
    private final List<UUID> usuarioIds = new ArrayList<>();
    private final List<FilmeFavoritado> favoritosNaoNotificados = new ArrayList<>();
    private List<FilmeFavoritado> resultado;
    private Exception excecaoCapturada;

    @Dado("que existe um filme cadastrado {string}")
    public void existeFilmeCadastrado(String titulo) {
        filme = new Filme(titulo, Duration.ofMinutes(120),
            ClassificacaoIndicativa.QUATORZE, GeneroFilme.ACAO);
        when(filmeRepository.buscarPorId(filme.getId())).thenReturn(Optional.of(filme));
    }

    @Dado("que o filme {string} não está cadastrado no sistema")
    public void filmeNaoCadastrado(String titulo) {
        filmeIdNaoCadastrado = UUID.randomUUID();
        when(filmeRepository.buscarPorId(filmeIdNaoCadastrado)).thenReturn(Optional.empty());
    }

    @Dado("o usuário {string} favoritou o filme cadastrado {string}")
    public void usuarioFavoritouFilme(String usuarioKey, String titulo) {
        UUID usuarioId = UUID.randomUUID();
        usuarioIds.add(usuarioId);
        FilmeFavoritado favorito = new FilmeFavoritado(usuarioId, filme.getId());
        favoritosNaoNotificados.add(favorito);
    }

    @Dado("nenhum usuário favoritou o filme cadastrado {string}")
    public void nenhumUsuarioFavoritou(String titulo) {
        // lista permanece vazia
    }

    @Dado("existe uma sessão cadastrada para o filme {string}")
    public void existeSessaoCadastrada(String titulo) {
        Sala sala = new Sala(1, 100, TipoSala.PADRAO);
        sessao = new Sessao(filme, sala, LocalDate.now().plusDays(7).atTime(20, 0));
        when(sessaoRepository.buscarPorId(sessao.getId())).thenReturn(Optional.of(sessao));
        when(filmeFavoritadoRepository.buscarNaoNotificadosPorFilme(filme.getId()))
            .thenReturn(new ArrayList<>(favoritosNaoNotificados));
    }

    @Quando("o sistema processar as notificações para a sessão cadastrada")
    public void processarNotificacoes() {
        resultado = notificacaoService.notificarFavoritosParaSessao(sessao.getId());
    }

    @Quando("o usuário {string} favoritar o filme cadastrado {string}")
    public void usuarioFavoritar(String usuarioKey, String titulo) {
        UUID usuarioId = UUID.randomUUID();
        notificacaoService.favoritarFilme(usuarioId, filme.getId());
    }

    @Quando("o usuário {string} tentar favoritar o filme não cadastrado")
    public void usuarioTentarFavoritarNaoCadastrado(String usuarioKey) {
        try {
            notificacaoService.favoritarFilme(UUID.randomUUID(), filmeIdNaoCadastrado);
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Então("{int} usuário deve ser notificado")
    public void umUsuarioDeveSerNotificado(int quantidade) {
        assertEquals(quantidade, resultado.size());
    }

    @Então("{int} usuários devem ser notificados")
    public void usuariosDevemSerNotificados(int quantidade) {
        assertEquals(quantidade, resultado.size());
    }

    @Então("o favorito deve ser marcado como notificado")
    public void favoritoMarcadoComoNotificado() {
        assertTrue(resultado.get(0).isNotificado());
        verify(filmeFavoritadoRepository, atLeastOnce()).salvar(any(FilmeFavoritado.class));
    }

    @Então("o favorito deve ser salvo no sistema")
    public void favoritoSalvoNoSistema() {
        assertNull(excecaoCapturada);
        verify(filmeFavoritadoRepository).salvar(any(FilmeFavoritado.class));
    }

    @Então("o sistema deve rejeitar informando filme não encontrado")
    public void rejeitarFilmeNaoEncontrado() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalArgumentException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("Filme não encontrado"));
    }
}