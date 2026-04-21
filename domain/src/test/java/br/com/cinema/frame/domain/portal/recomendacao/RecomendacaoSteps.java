package br.com.cinema.frame.domain.portal.recomendacao;
 
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.cinema.frame.domain.backoffice.grade.GeneroFilme;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
 
public class RecomendacaoSteps {
 
    private HistoricoDeCompras historico;
    private final List<FilmeSugerido> catalogo = new ArrayList<>();
    private List<FilmeSugerido> sugestoes;
    private final MotorDeRecomendacao motor = new MotorDeRecomendacao();
 
    @Dado("que o cliente assistiu filmes dos gêneros {string}")
    public void clienteAssistiuUmGenero(String genero) {
        historico = new HistoricoDeCompras(UUID.randomUUID());
        historico.registrarGenero(GeneroFilme.valueOf(genero));
    }
 
    @Dado("que o cliente assistiu filmes dos gêneros {string} e {string} e {string}")
    public void clienteAssistiuTresGeneros(String g1, String g2, String g3) {
        historico = new HistoricoDeCompras(UUID.randomUUID());
        historico.registrarGenero(GeneroFilme.valueOf(g1));
        historico.registrarGenero(GeneroFilme.valueOf(g2));
        historico.registrarGenero(GeneroFilme.valueOf(g3));
    }
 
    @Dado("que o cliente assistiu filmes dos gêneros {string} e {string}")
    public void clienteAssistiuDoisGeneros(String g1, String g2) {
        historico = new HistoricoDeCompras(UUID.randomUUID());
        historico.registrarGenero(GeneroFilme.valueOf(g1));
        historico.registrarGenero(GeneroFilme.valueOf(g2));
    }
 
    @Dado("que o cliente não possui histórico de compras")
    public void clienteSemHistorico() {
        historico = new HistoricoDeCompras(UUID.randomUUID());
    }
 
    @Dado("o catálogo possui um filme {string} do gênero {string}")
    public void catalogoPossuiFilme(String titulo, String genero) {
        catalogo.add(new FilmeSugerido(titulo, GeneroFilme.valueOf(genero)));
    }
 
    @Dado("o catálogo está vazio")
    public void catalogoVazio() {
        catalogo.clear();
    }
 
    @Quando("o sistema gerar as recomendações")
    public void sistemaGerarRecomendacoes() {
        sugestoes = motor.recomendar(historico, catalogo);
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