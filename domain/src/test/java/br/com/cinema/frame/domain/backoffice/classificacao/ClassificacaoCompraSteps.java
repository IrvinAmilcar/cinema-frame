package br.com.cinema.frame.domain.backoffice.classificacao;

import br.com.cinema.frame.domain.backoffice.grade.Filme;
import br.com.cinema.frame.domain.backoffice.grade.FilmeRepository;
import br.com.cinema.frame.domain.backoffice.grade.GeneroFilme;
import br.com.cinema.frame.domain.portal.cliente.Cliente;
import br.com.cinema.frame.domain.portal.cliente.ClienteRepository;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class ClassificacaoCompraSteps {

    private ClienteRepository clienteRepository = mock(ClienteRepository.class);
    private FilmeRepository filmeRepository = mock(FilmeRepository.class);
    private ClassificacaoDeCompraService validacao = new ClassificacaoDeCompraService(clienteRepository, filmeRepository);

    private Cliente cliente;
    private Filme filme;
    private Exception excecaoCapturada;

    @Dado("que existe um cliente cadastrado nascido em {string}")
    public void clienteCadastradoNascidoEm(String data) {
        cliente = new Cliente("Cliente Teste", "teste@email.com", LocalDate.parse(data));
        when(clienteRepository.buscarPorId(cliente.getId())).thenReturn(Optional.of(cliente));
    }

    @Dado("que existe um cliente cadastrado com exatamente {int} anos")
    public void clienteCadastradoComExatamenteAnos(int anos) {
        cliente = new Cliente("Cliente Teste", "teste@email.com", LocalDate.now().minusYears(anos));
        when(clienteRepository.buscarPorId(cliente.getId())).thenReturn(Optional.of(cliente));
    }

    @Dado("existe um filme cadastrado com classificação {string}")
    public void filmeCadastradoComClassificacao(String classificacao) {
        filme = new Filme("Filme Teste", Duration.ofMinutes(120),
            ClassificacaoIndicativa.valueOf(classificacao), GeneroFilme.ACAO);
        when(filmeRepository.buscarPorId(filme.getId())).thenReturn(Optional.of(filme));
    }

    @Quando("o sistema validar a compra do cliente")
    public void sistemaValidarCompraDoCliente() {
        validacao.validarCompra(cliente.getId(), filme.getId());
    }

    @Quando("o sistema tentar validar a compra do cliente")
    public void sistemaTentarValidarCompraDoCliente() {
        try {
            validacao.validarCompra(cliente.getId(), filme.getId());
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Então("a compra deve ser permitida")
    public void compraDeveSerPermitida() {
        assertNull(excecaoCapturada);
    }

    @Então("a compra deve ser bloqueada com mensagem de idade insuficiente")
    public void compraDeveSerBloqueada() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalStateException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("não tem idade permitida"));
    }
}