package br.com.cinema.frame.domain.portal.recomendacao;

import br.com.cinema.frame.domain.portal.notificacao.FilmeFavoritado;
import br.com.cinema.frame.domain.portal.notificacao.FilmeFavoritadoRepository;
import br.com.cinema.frame.domain.shared.filme.GeneroFilme;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class RecomendacaoSteps {

    private HistoricoDeComprasRepository historicoRepository = mock(HistoricoDeComprasRepository.class);
    private FilmeSugeridoRepository filmeSugeridoRepository = mock(FilmeSugeridoRepository.class);
    private FilmeFavoritadoRepository filmeFavoritadoRepository = mock(FilmeFavoritadoRepository.class);
    private MotorDeRecomendacao motor =
        new MotorDeRecomendacao(historicoRepository, filmeSugeridoRepository, filmeFavoritadoRepository);

    private HistoricoDeCompras historico;
    private final List<FilmeSugerido> catalogo = new ArrayList<>();
    private final List<FilmeFavoritado> favoritosDoCliente = new ArrayList<>();
    private List<FilmeSugerido> sugestoes;
    private UUID clienteId;

    @Dado("que existe um histórico cadastrado com os gêneros {string}")
    public void historicoComUmGenero(String genero) {
        clienteId = UUID.randomUUID();
        historico = new HistoricoDeCompras(clienteId);
        historico.registrarGenero(GeneroFilme.valueOf(genero));
        when(historicoRepository.buscarPorClienteId(clienteId)).thenReturn(Optional.of(historico));
    }

    @Dado("que existe um histórico cadastrado com os gêneros {string} e {string} e {string}")
    public void historicoComTresGeneros(String g1, String g2, String g3) {
        clienteId = UUID.randomUUID();
        historico = new HistoricoDeCompras(clienteId);
        historico.registrarGenero(GeneroFilme.valueOf(g1));
        historico.registrarGenero(GeneroFilme.valueOf(g2));
        historico.registrarGenero(GeneroFilme.valueOf(g3));
        when(historicoRepository.buscarPorClienteId(clienteId)).thenReturn(Optional.of(historico));
    }

    @Dado("que existe um histórico cadastrado com os gêneros {string} e {string}")
    public void historicoComDoisGeneros(String g1, String g2) {
        clienteId = UUID.randomUUID();
        historico = new HistoricoDeCompras(clienteId);
        historico.registrarGenero(GeneroFilme.valueOf(g1));
        historico.registrarGenero(GeneroFilme.valueOf(g2));
        when(historicoRepository.buscarPorClienteId(clienteId)).thenReturn(Optional.of(historico));
    }

    @Dado("que existe um histórico cadastrado vazio para o cliente")
    public void historicoCadastradoVazio() {
        clienteId = UUID.randomUUID();
        historico = new HistoricoDeCompras(clienteId);
        when(historicoRepository.buscarPorClienteId(clienteId)).thenReturn(Optional.of(historico));
    }

    @Dado("que o cliente não possui histórico de compras")
    public void clienteSemHistorico() {
        clienteId = UUID.randomUUID();
        historico = null;
        when(historicoRepository.buscarPorClienteId(clienteId)).thenReturn(Optional.empty());
    }

    @Dado("o catálogo cadastrado possui um filme {string} do gênero {string}")
    public void catalogoCadastradoPossuiFilme(String titulo, String genero) {
        catalogo.add(new FilmeSugerido(titulo, GeneroFilme.valueOf(genero)));
        when(filmeSugeridoRepository.listarTodos()).thenReturn(new ArrayList<>(catalogo));
    }

    @Dado("o catálogo cadastrado está vazio")
    public void catalogoCadastradoVazio() {
        catalogo.clear();
        when(filmeSugeridoRepository.listarTodos()).thenReturn(new ArrayList<>());
    }

    @Dado("o cliente favoritou o filme {string}")
    public void clienteFavoritouFilme(String titulo) {
        FilmeSugerido filme = catalogo.stream()
            .filter(f -> f.getTitulo().equals(titulo))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Filme não encontrado no catálogo: " + titulo));
        favoritosDoCliente.add(new FilmeFavoritado(clienteId, filme.getId()));
        when(filmeFavoritadoRepository.buscarPorUsuarioId(clienteId))
            .thenReturn(new ArrayList<>(favoritosDoCliente));
    }

    @Dado("o cliente já assistiu o filme {string}")
    public void clienteJaAssistiuFilme(String titulo) {
        FilmeSugerido filme = catalogo.stream()
            .filter(f -> f.getTitulo().equals(titulo))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Filme não encontrado no catálogo: " + titulo));
        if (historico == null) {
            historico = new HistoricoDeCompras(clienteId);
        }
        historico.registrarFilme(filme.getId(), filme.getGenero());
        when(historicoRepository.buscarPorClienteId(clienteId)).thenReturn(Optional.of(historico));
    }

    @Quando("o sistema gerar as recomendações para o cliente cadastrado")
    public void sistemaGerarRecomendacoesParaClienteCadastrado() {
        sugestoes = motor.recomendar(clienteId);
    }

    @Então("a lista de sugestões não deve estar vazia")
    public void listaNaoDeveEstarVazia() {
        assertNotNull(sugestoes);
        assertFalse(sugestoes.isEmpty());
    }

    @Então("a lista de sugestões deve estar vazia")
    public void listaDeveEstarVazia() {
        assertNotNull(sugestoes);
        assertTrue(sugestoes.isEmpty());
    }

    @Então("o primeiro filme sugerido deve ser do gênero {string}")
    public void primeiroFilmeDeveSerDoGenero(String genero) {
        assertFalse(sugestoes.isEmpty());
        assertEquals(GeneroFilme.valueOf(genero), sugestoes.get(0).getGenero());
    }

    @Então("a lista de sugestões deve conter o filme {string}")
    public void listaDeveConterFilme(String titulo) {
        assertTrue(sugestoes.stream().anyMatch(f -> f.getTitulo().equals(titulo)),
            "Esperava encontrar o filme: " + titulo);
    }

    @Então("a lista de sugestões não deve conter o filme {string}")
    public void listaNaoDeveConterFilme(String titulo) {
        assertTrue(sugestoes.stream().noneMatch(f -> f.getTitulo().equals(titulo)),
            "Não esperava encontrar o filme: " + titulo);
    }
}
