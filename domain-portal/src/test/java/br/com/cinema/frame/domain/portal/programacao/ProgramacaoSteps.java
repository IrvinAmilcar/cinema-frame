package br.com.cinema.frame.domain.portal.programacao;

import br.com.cinema.frame.domain.backoffice.grade.*;
import br.com.cinema.frame.domain.backoffice.sala.*;
import br.com.cinema.frame.domain.shared.classificacao.ClassificacaoIndicativa;
import br.com.cinema.frame.domain.shared.filme.GeneroFilme;
import io.cucumber.java.pt.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class ProgramacaoSteps {

    private GradeDeExibicaoRepository gradeRepository = mock(GradeDeExibicaoRepository.class);
    private ProgramacaoService programacaoService = new ProgramacaoService(gradeRepository);

    private final List<GradeDeExibicao> grades = new ArrayList<>();
    private final LocalDateTime agora = LocalDate.now().atTime(10, 0);
    private List<Sessao> resultado;
    private int contadorSala = 1;

    private void atualizarMock() {
        when(gradeRepository.listarTodas()).thenReturn(new ArrayList<>(grades));
    }

    private Sessao criarSessaoFutura(String tituloFilme, GeneroFilme genero, ClassificacaoIndicativa classificacao) {
        Filme filme = new Filme(tituloFilme, Duration.ofMinutes(120), classificacao, genero);
        Sala sala = new Sala(contadorSala++, 100, TipoSala.PADRAO);
        return new Sessao(filme, sala, agora.plusDays(1));
    }

    private Sessao criarSessaoPassada(String tituloFilme, GeneroFilme genero, ClassificacaoIndicativa classificacao) {
        Filme filme = new Filme(tituloFilme, Duration.ofMinutes(120), classificacao, genero);
        Sala sala = new Sala(contadorSala++, 100, TipoSala.PADRAO);
        return new Sessao(filme, sala, agora.minusDays(1));
    }

    private void adicionarSessaoNaGrade(Sessao sessao) {
        GradeDeExibicao grade = new GradeDeExibicao(LocalDate.now(), LocalDate.now().plusDays(7));
        grade.adicionarSessao(sessao);
        grades.add(grade);
        atualizarMock();
    }

    @Dado("que existe uma sessão futura cadastrada para o filme {string}")
    public void sessaoFuturaCadastrada(String titulo) {
        adicionarSessaoNaGrade(criarSessaoFutura(titulo, GeneroFilme.DRAMA, ClassificacaoIndicativa.LIVRE));
    }

    @Dado("existe uma sessão passada cadastrada para o filme {string}")
    public void sessaoPassadaCadastrada(String titulo) {
        adicionarSessaoNaGrade(criarSessaoPassada(titulo, GeneroFilme.COMEDIA, ClassificacaoIndicativa.LIVRE));
    }

    @Dado("que existe uma sessão futura cadastrada para o filme {string} do gênero {string}")
    public void sessaoFuturaComGenero(String titulo, String genero) {
        adicionarSessaoNaGrade(criarSessaoFutura(titulo, GeneroFilme.valueOf(genero), ClassificacaoIndicativa.LIVRE));
    }

    @Dado("existe uma sessão futura cadastrada para o filme {string} do gênero {string}")
    public void sessaoFuturaComGeneroSemQue(String titulo, String genero) {
        adicionarSessaoNaGrade(criarSessaoFutura(titulo, GeneroFilme.valueOf(genero), ClassificacaoIndicativa.LIVRE));
    }

    @Dado("que existe uma sessão futura cadastrada para o filme {string} com classificação {string}")
    public void sessaoFuturaComClassificacao(String titulo, String classificacao) {
        adicionarSessaoNaGrade(criarSessaoFutura(titulo, GeneroFilme.DRAMA, ClassificacaoIndicativa.valueOf(classificacao)));
    }

    @Dado("existe uma sessão futura cadastrada para o filme {string} com classificação {string}")
    public void sessaoFuturaComClassificacaoSemQue(String titulo, String classificacao) {
        adicionarSessaoNaGrade(criarSessaoFutura(titulo, GeneroFilme.DRAMA, ClassificacaoIndicativa.valueOf(classificacao)));
    }

    @Dado("que existe uma sessão passada cadastrada para o filme {string}")
    public void que_existe_uma_sessao_passada_cadastrada_para_o_filme(String nomeFilme) {
    Filme filme = new Filme(
        nomeFilme,
        Duration.ofMinutes(120),
        ClassificacaoIndicativa.LIVRE,
        GeneroFilme.COMEDIA
    );

    Sala sala = new Sala(1, 100, TipoSala.PADRAO);

    Sessao sessaoPassada = new Sessao(
        filme,
        sala,
        LocalDate.now().minusDays(1).atTime(20, 0)
    );

    GradeDeExibicao grade = new GradeDeExibicao(
        LocalDate.now().minusDays(7),
        LocalDate.now()
    );

    grade.adicionarSessao(sessaoPassada);

    when(gradeRepository.listarTodas()).thenReturn(List.of(grade));
}

    @Quando("o cliente consultar a programação disponível")
    public void consultarProgramacao() {
        resultado = programacaoService.listarSessoesDisponiveis(agora);
    }

    @Quando("o cliente filtrar a programação pelo gênero {string}")
    public void filtrarPorGenero(String genero) {
        resultado = programacaoService.filtrarPorGenero(agora, GeneroFilme.valueOf(genero));
    }

    @Quando("o cliente filtrar a programação pela classificação máxima {string}")
    public void filtrarPorClassificacao(String classificacao) {
        resultado = programacaoService.filtrarPorClassificacao(agora, ClassificacaoIndicativa.valueOf(classificacao));
    }

    @Então("o filme {string} deve aparecer na listagem")
    public void filmeDeveAparecerNaListagem(String titulo) {
        assertNotNull(resultado);
        assertTrue(resultado.stream().anyMatch(s -> s.getFilme().getTitulo().equals(titulo)),
            "Esperava encontrar o filme: " + titulo);
    }

    @Então("o filme {string} não deve aparecer na listagem")
    public void filmeNaoDeveAparecerNaListagem(String titulo) {
        assertNotNull(resultado);
        assertTrue(resultado.stream().noneMatch(s -> s.getFilme().getTitulo().equals(titulo)),
            "Não esperava encontrar o filme: " + titulo);
    }

    @Então("a listagem de sessões deve estar vazia")
    public void listagemDeveEstarVazia() {
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }
}