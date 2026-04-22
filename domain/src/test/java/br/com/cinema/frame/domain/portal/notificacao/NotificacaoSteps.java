package br.com.cinema.frame.domain.portal.notificacao;

import br.com.cinema.frame.domain.backoffice.classificacao.ClassificacaoIndicativa;
import br.com.cinema.frame.domain.backoffice.grade.Filme;
import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.precificacao.TipoSala;
import br.com.cinema.frame.domain.backoffice.sala.Sala;
import br.com.cinema.frame.domain.portal.cliente.Cliente;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NotificacaoSteps {

    private final List<Cliente> clientes = new ArrayList<>();
    private final List<NotificacaoDePrevenda> notificacoesEnviadas = new ArrayList<>();
    private Sessao sessao;

    private final ServicoDeNotificacao servicoFake = notificacao ->
        notificacoesEnviadas.add(notificacao);

    private final GerenciadorDePrevenda gerenciador =
        new GerenciadorDePrevenda(servicoFake);

    @Dado("que existe um cliente {string} que favoritou o filme {string}")
    public void clienteFavoritouFilme(String nomeCliente, String nomeFilme) {
        Cliente cliente = new Cliente(nomeCliente, nomeCliente.toLowerCase() + "@email.com",
            LocalDate.of(1990, 1, 1));
        Filme filme = new Filme(nomeFilme, Duration.ofMinutes(120),
            ClassificacaoIndicativa.QUATORZE);
        cliente.favoritarFilme(filme);
        clientes.add(cliente);
    }

    @Dado("que existe um cliente {string} que não favoritou nenhum filme")
    public void clienteSemFavoritos(String nomeCliente) {
        Cliente cliente = new Cliente(nomeCliente, nomeCliente.toLowerCase() + "@email.com",
            LocalDate.of(1990, 1, 1));
        clientes.add(cliente);
    }

    @Dado("existe um cliente {string} que não favoritou nenhum filme")
    public void outroClienteSemFavoritos(String nomeCliente) {
        clienteSemFavoritos(nomeCliente);
    }

    @Dado("existe um cliente {string} que favoritou o filme {string}")
    public void outroClienteFavoritouFilme(String nomeCliente, String nomeFilme) {
        clienteFavoritouFilme(nomeCliente, nomeFilme);
    }

    @Dado("existe uma sessão do filme {string}")
    public void existeSessaoDoFilme(String nomeFilme) {
        Filme filme = new Filme(nomeFilme, Duration.ofMinutes(120),
            ClassificacaoIndicativa.QUATORZE);
        Sala sala = new Sala(1, 100, TipoSala.PADRAO);
        sessao = new Sessao(filme, sala, LocalDateTime.now().plusDays(1));
    }

    @Quando("o sistema processar as notificações de pré-venda")
    public void processarNotificacoes() {
        gerenciador.notificarClientesInteressados(sessao, clientes);
    }

    @Então("o cliente {string} deve ser notificado")
    public void clienteDeveSerNotificado(String nomeCliente) {
        boolean notificado = notificacoesEnviadas.stream()
            .anyMatch(n -> n.getCliente().getNome().equals(nomeCliente));
        assertTrue(notificado, "Cliente " + nomeCliente + " deveria ter sido notificado");
    }

    @Então("o cliente {string} não deve ser notificado")
    public void clienteNaoDeveSerNotificado(String nomeCliente) {
        boolean notificado = notificacoesEnviadas.stream()
            .anyMatch(n -> n.getCliente().getNome().equals(nomeCliente));
        assertFalse(notificado, "Cliente " + nomeCliente + " não deveria ter sido notificado");
    }

    @Então("apenas {int} cliente deve ser notificado")
    public void apenasNClientesDevemSerNotificados(int quantidade) {
        assertEquals(quantidade, notificacoesEnviadas.size());
    }
}