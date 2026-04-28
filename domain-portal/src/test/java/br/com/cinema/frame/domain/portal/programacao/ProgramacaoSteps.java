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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class ProgramacaoSteps {

    private GradeDeExibicaoRepository gradeRepository = mock(GradeDeExibicaoRepository.class);
    private ProgramacaoService programacaoService = new ProgramacaoService(gradeRepository);

    private final List<GradeDeExibicao> grades = new ArrayList<>();
    private LocalDateTime momentoConsulta = LocalDate.now().atTime(10, 0);
    private List<Sessao> resultado;
    private int contadorSala = 1;

    private final Map<UUID, Integer> ingressosPorSessao = new HashMap<>();

    private void atualizarMock() {
        when(gradeRepository.listarTodas()).thenReturn(new ArrayList<>(grades));
    }

    private Sessao criarSessao(String tituloFilme, GeneroFilme genero,
                                ClassificacaoIndicativa classificacao,
                                LocalDateTime inicio, boolean ativo) {
        Filme filme = new Filme(tituloFilme, Duration.ofMinutes(120), classificacao, genero);
        if (!ativo) filme.desativar();
        Sala sala = new Sala(contadorSala++, 100, TipoSala.PADRAO);
        return new Sessao(filme, sala, inicio);
    }

    private Sessao criarSessaoFutura(String titulo, GeneroFilme genero, ClassificacaoIndicativa classificacao) {
        return criarSessao(titulo, genero, classificacao, momentoConsulta.plusDays(1), true);
    }

    private Sessao criarSessaoPassada(String titulo, GeneroFilme genero, ClassificacaoIndicativa classificacao) {
        return criarSessao(titulo, genero, classificacao, momentoConsulta.minusDays(1), true);
    }

    private void adicionarSessaoNaGrade(Sessao sessao) {
        LocalDate dataInicio = sessao.getInicio().toLocalDate().minusDays(1);
        GradeDeExibicao grade = new GradeDeExibicao(dataInicio, dataInicio.plusDays(14));
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

    @Dado("que existe uma sessão passada cadastrada para o filme {string}")
    public void que_existe_uma_sessao_passada_cadastrada_para_o_filme(String titulo) {
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

    @Dado("que existe uma sessão futura cadastrada para o filme {string} com {int} ingressos vendidos")
    public void sessaoFuturaComIngressosVendidos(String titulo, int ingressos) {
        Sessao sessao = criarSessaoFutura(titulo, GeneroFilme.DRAMA, ClassificacaoIndicativa.LIVRE);
        adicionarSessaoNaGrade(sessao);
        ingressosPorSessao.put(sessao.getId(), ingressos);
    }

    @Dado("que existe uma sessão cadastrada para o filme {string} iniciando hoje às {int}:{int}")
    public void sessaoCadastradaIniciandoHojeAs(String titulo, int hora, int minuto) {
        LocalDateTime inicio = LocalDate.now().atTime(hora, minuto);
        Sessao sessao = criarSessao(titulo, GeneroFilme.DRAMA, ClassificacaoIndicativa.LIVRE, inicio, true);
        adicionarSessaoNaGrade(sessao);
    }

    @Dado("que existe um filme inativo {string} com sessão futura cadastrada")
    public void filmeInativoComSessaoFutura(String titulo) {
        LocalDateTime inicio = LocalDate.now().atTime(10, 0).plusDays(1);
        Sessao sessao = criarSessao(titulo, GeneroFilme.COMEDIA, ClassificacaoIndicativa.LIVRE, inicio, false);
        adicionarSessaoNaGrade(sessao);
    }

    @Quando("o cliente consultar a programação disponível")
    public void consultarProgramacao() {
        resultado = programacaoService.listarSessoesDisponiveis(momentoConsulta);
    }

    @Quando("o cliente consultar a programação disponível às {int}:{int}")
    public void consultarProgramacaoAs(int hora, int minuto) {
        LocalDateTime consulta = LocalDate.now().atTime(hora, minuto);
        resultado = programacaoService.listarSessoesDisponiveis(consulta);
    }

    @Quando("o cliente com {int} anos consultar a programação disponível")
    public void consultarProgramacaoPorIdade(int idade) {
        resultado = programacaoService.listarSessoesDisponiveisPorIdade(momentoConsulta, idade);
    }

    @Quando("o cliente filtrar a programação pelo gênero {string}")
    public void filtrarPorGenero(String genero) {
        resultado = programacaoService.filtrarPorGenero(momentoConsulta, GeneroFilme.valueOf(genero));
    }

    @Quando("o cliente filtrar a programação pela classificação máxima {string}")
    public void filtrarPorClassificacao(String classificacao) {
        resultado = programacaoService.filtrarPorClassificacao(momentoConsulta, ClassificacaoIndicativa.valueOf(classificacao));
    }

    @Quando("o cliente ordenar a programação por popularidade")
    public void ordenarPorPopularidade() {
        resultado = programacaoService.ordenarPorPopularidade(momentoConsulta, ingressosPorSessao);
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

    @Então("o primeiro filme da listagem deve ser {string}")
    public void primeiroFilmeDaListagemDeveSer(String titulo) {
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty(), "A listagem não deveria estar vazia");
        assertEquals(titulo, resultado.get(0).getFilme().getTitulo());
    }

    @Então("o último filme da listagem deve ser {string}")
    public void ultimoFilmeDaListagemDeveSer(String titulo) {
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty(), "A listagem não deveria estar vazia");
        assertEquals(titulo, resultado.get(resultado.size() - 1).getFilme().getTitulo());
    }
}