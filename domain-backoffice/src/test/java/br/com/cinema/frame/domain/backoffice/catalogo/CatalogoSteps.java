package br.com.cinema.frame.domain.backoffice.catalogo;

import br.com.cinema.frame.domain.shared.classificacao.ClassificacaoIndicativa;
import br.com.cinema.frame.domain.backoffice.grade.Filme;
import br.com.cinema.frame.domain.backoffice.grade.FilmeRepository;
import br.com.cinema.frame.domain.backoffice.grade.FilmeService;
import br.com.cinema.frame.domain.shared.filme.GeneroFilme;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class CatalogoSteps {

    private FilmeRepository filmeRepository = mock(FilmeRepository.class);
    private FilmeService filmeService = new FilmeService(filmeRepository);

    private Filme filme;
    private Filme filmeAtualizado;
    private Exception excecaoCapturada;

    @Dado("que a gerente tem um novo filme {string} com duração de {int} minutos classificação {string} e gênero {string}")
    public void gerente_tem_novo_filme(String titulo, int minutos, String classificacao, String genero) {
        filme = new Filme(titulo, Duration.ofMinutes(minutos),
            ClassificacaoIndicativa.valueOf(classificacao),
            GeneroFilme.valueOf(genero));
    }

    @Dado("que existe um filme {string} com duração de {int} minutos e classificação {string} e gênero {string} no catálogo")
    public void existe_filme_no_catalogo(String titulo, int minutos, String classificacao, String genero) {
        filme = new Filme(titulo, Duration.ofMinutes(minutos),
            ClassificacaoIndicativa.valueOf(classificacao),
            GeneroFilme.valueOf(genero));
        when(filmeRepository.buscarPorId(filme.getId())).thenReturn(Optional.of(filme));
        when(filmeRepository.listarTodos()).thenReturn(List.of(filme));
    }

    @Quando("a gerente cadastra o filme no catálogo")
    public void gerente_cadastra_filme() {
        filmeService.cadastrar(filme);
    }

    @Quando("a gerente atualiza o título do filme para {string}")
    public void gerente_atualiza_titulo(String novoTitulo) {
        filmeAtualizado = filmeService.atualizar(filme.getId(), novoTitulo, null, null, null);
    }

    @Quando("a gerente remove o filme do catálogo")
    public void gerente_remove_filme() {
        filmeService.remover(filme.getId());
    }

    @Quando("a gerente tenta cadastrar um filme com título vazio")
    public void gerente_tenta_cadastrar_titulo_vazio() {
        try {
            new Filme("", Duration.ofMinutes(90), ClassificacaoIndicativa.LIVRE, GeneroFilme.COMEDIA);
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Então("o filme deve ser salvo no repositório")
    public void filme_deve_ser_salvo() {
        verify(filmeRepository).salvar(filme);
    }

    @Então("o título do filme deve ser {string}")
    public void titulo_do_filme_deve_ser(String esperado) {
        assertEquals(esperado, filmeAtualizado.getTitulo());
        verify(filmeRepository, atLeastOnce()).salvar(any(Filme.class));
    }

    @Então("o filme deve ser removido do repositório")
    public void filme_deve_ser_removido() {
        verify(filmeRepository).remover(filme.getId());
    }

    @Então("o sistema deve rejeitar o cadastro informando título inválido")
    public void sistema_rejeita_titulo_invalido() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalArgumentException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("Título"));
    }
}
