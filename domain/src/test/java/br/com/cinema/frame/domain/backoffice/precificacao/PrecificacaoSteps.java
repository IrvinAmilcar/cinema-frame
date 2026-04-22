package br.com.cinema.frame.domain.backoffice.precificacao;

import br.com.cinema.frame.domain.backoffice.classificacao.ClassificacaoIndicativa;
import br.com.cinema.frame.domain.backoffice.grade.Filme;
import br.com.cinema.frame.domain.backoffice.grade.FilmeRepository;
import br.com.cinema.frame.domain.backoffice.grade.GeneroFilme;
import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicao;
import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicaoRepository;
import br.com.cinema.frame.domain.backoffice.grade.GradeService;
import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.sala.Sala;
import br.com.cinema.frame.domain.backoffice.sala.SalaRepository;
import br.com.cinema.frame.domain.backoffice.sala.TipoSala;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class PrecificacaoSteps {

    private GradeDeExibicaoRepository gradeRepository = mock(GradeDeExibicaoRepository.class);
    private FilmeRepository filmeRepository = mock(FilmeRepository.class);
    private SalaRepository salaRepository = mock(SalaRepository.class);
    private GradeService gradeService = new GradeService(gradeRepository, filmeRepository, salaRepository);
    private PrecificacaoService precificacaoService = new PrecificacaoService();

    private Sessao sessao;
    private GradeDeExibicao grade;
    private double precoCalculado;

    @Dado("que existe uma sessão cadastrada numa sala padrão numa sexta-feira")
    public void sessaoCadastradaSalaPadraoSexta() { sessao = criarSessaoMockada(TipoSala.PADRAO, DayOfWeek.FRIDAY); }

    @Dado("que existe uma sessão cadastrada numa sala 3D numa sexta-feira")
    public void sessaoCadastradaSala3DSexta() { sessao = criarSessaoMockada(TipoSala.TRES_D, DayOfWeek.FRIDAY); }

    @Dado("que existe uma sessão cadastrada numa sala IMAX numa sexta-feira")
    public void sessaoCadastradaSalaIMAXSexta() { sessao = criarSessaoMockada(TipoSala.IMAX, DayOfWeek.FRIDAY); }

    @Dado("que existe uma sessão cadastrada numa sala VIP numa sexta-feira")
    public void sessaoCadastradaSalaVIPSexta() { sessao = criarSessaoMockada(TipoSala.VIP, DayOfWeek.FRIDAY); }

    @Dado("que existe uma sessão cadastrada numa sala padrão numa segunda-feira")
    public void sessaoCadastradaSalaPadraoSegunda() { sessao = criarSessaoMockada(TipoSala.PADRAO, DayOfWeek.MONDAY); }

    @Dado("que existe uma sessão cadastrada numa sala padrão numa terça-feira")
    public void sessaoCadastradaSalaPadraoTerca() { sessao = criarSessaoMockada(TipoSala.PADRAO, DayOfWeek.TUESDAY); }

    @Dado("que existe uma sessão cadastrada numa sala padrão numa quarta-feira")
    public void sessaoCadastradaSalaPadraoQuarta() { sessao = criarSessaoMockada(TipoSala.PADRAO, DayOfWeek.WEDNESDAY); }

    @Dado("que existe uma sessão cadastrada numa sala padrão numa quinta-feira")
    public void sessaoCadastradaSalaPadraoQuinta() { sessao = criarSessaoMockada(TipoSala.PADRAO, DayOfWeek.THURSDAY); }

    @Dado("que existe uma sessão cadastrada numa sala padrão num sábado")
    public void sessaoCadastradaSalaPadraoSabado() { sessao = criarSessaoMockada(TipoSala.PADRAO, DayOfWeek.SATURDAY); }

    @Dado("que existe uma sessão cadastrada numa sala padrão num domingo")
    public void sessaoCadastradaSalaPadraoDomingo() { sessao = criarSessaoMockada(TipoSala.PADRAO, DayOfWeek.SUNDAY); }

    @Dado("que existe uma sessão cadastrada numa sala 3D numa terça-feira")
    public void sessaoCadastradaSala3DTerca() { sessao = criarSessaoMockada(TipoSala.TRES_D, DayOfWeek.TUESDAY); }

    @Dado("que existe uma sessão cadastrada numa sala IMAX numa terça-feira")
    public void sessaoCadastradaSalaIMAXTerca() { sessao = criarSessaoMockada(TipoSala.IMAX, DayOfWeek.TUESDAY); }

    @Dado("que existe uma sessão cadastrada numa sala VIP numa terça-feira")
    public void sessaoCadastradaSalaVIPTerca() { sessao = criarSessaoMockada(TipoSala.VIP, DayOfWeek.TUESDAY); }

    @Dado("que existe uma sessão cadastrada numa sala 3D numa segunda-feira")
    public void sessaoCadastradaSala3DSegunda() { sessao = criarSessaoMockada(TipoSala.TRES_D, DayOfWeek.MONDAY); }

    @Dado("que existe uma sessão cadastrada numa sala IMAX numa segunda-feira")
    public void sessaoCadastradaSalaIMAXSegunda() { sessao = criarSessaoMockada(TipoSala.IMAX, DayOfWeek.MONDAY); }

    @Dado("que existe uma sessão cadastrada numa sala VIP numa segunda-feira")
    public void sessaoCadastradaSalaVIPSegunda() { sessao = criarSessaoMockada(TipoSala.VIP, DayOfWeek.MONDAY); }

    @Quando("o sistema calcular o preço da sessão")
    public void calcularPreco() {
        Sessao sessaoDoBanco = gradeRepository.buscarPorData(LocalDate.now())
            .get()
            .getSessoes()
            .get(0);
        precoCalculado = precificacaoService.calcularPreco(sessaoDoBanco);
    }

    @Então("o preço deve ser R$ {double}")
    public void precoDeveSer(double precoEsperado) {
        assertEquals(precoEsperado, precoCalculado, 0.01);
    }

    private Sessao criarSessaoMockada(TipoSala tipoSala, DayOfWeek dia) {
        Filme filme = new Filme("Filme Teste", Duration.ofMinutes(120),
            ClassificacaoIndicativa.QUATORZE, GeneroFilme.ACAO);
        Sala sala = new Sala(1, 100, tipoSala);
        LocalDateTime inicio = LocalDateTime.now()
            .with(TemporalAdjusters.nextOrSame(dia))
            .withHour(20)
            .withMinute(0);

        Sessao s = new Sessao(filme, sala, inicio);

        grade = new GradeDeExibicao(LocalDate.now(), LocalDate.now().plusDays(7));
        grade.adicionarSessao(s);

        when(filmeRepository.buscarPorId(filme.getId())).thenReturn(Optional.of(filme));
        when(salaRepository.buscarPorId(sala.getId())).thenReturn(Optional.of(sala));
        when(gradeRepository.buscarPorId(grade.getId())).thenReturn(Optional.of(grade));
        when(gradeRepository.buscarPorData(any(LocalDate.class))).thenReturn(Optional.of(grade));

        return s;
    }
}