package br.com.cinema.frame.domain.backoffice.dashboard;

import br.com.cinema.frame.domain.backoffice.classificacao.ClassificacaoIndicativa;
import br.com.cinema.frame.domain.backoffice.grade.Filme;
import br.com.cinema.frame.domain.backoffice.grade.GeneroFilme;
import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.ingresso.Ingresso;
import br.com.cinema.frame.domain.backoffice.ingresso.IngressoRepository;
import br.com.cinema.frame.domain.backoffice.ingresso.TipoIngresso;
import br.com.cinema.frame.domain.backoffice.precificacao.PrecificacaoService;
import br.com.cinema.frame.domain.backoffice.sala.Sala;
import br.com.cinema.frame.domain.backoffice.sala.TipoSala;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DashboardSteps {

    private Sessao sessao;
    private Sessao outraSessao;
    private OcupacaoDaSessao ocupacao;
    private List<OcupacaoDaSessao> ocupacoes;
    private final List<Ingresso> ingressos = new ArrayList<>();
    private final List<Ingresso> outrosIngressos = new ArrayList<>();

    private final IngressoRepository ingressoRepositoryFake = new IngressoRepository() {
        @Override
        public void salvar(Ingresso ingresso) {}

        @Override
        public Optional<Ingresso> buscarPorId(UUID id) { return Optional.empty(); }

        @Override
        public List<Ingresso> buscarPorSessao(Sessao s) {
            if (outraSessao != null && s.getId().equals(outraSessao.getId()))
                return outrosIngressos;
            return ingressos;
        }

        @Override
        public void remover(UUID id) {}
    };

    private final DashboardDeOcupacao dashboard =
        new DashboardDeOcupacao(ingressoRepositoryFake, new PrecificacaoService());

    @Dado("que existe uma sessão na sala padrão com capacidade para {int} pessoas")
    public void existeSessaoNaSala(int capacidade) {
        Filme filme = new Filme("Filme Teste", Duration.ofMinutes(120),
            ClassificacaoIndicativa.LIVRE, GeneroFilme.COMEDIA);
        Sala sala = new Sala(1, capacidade, TipoSala.PADRAO);
        sessao = new Sessao(filme, sala, LocalDateTime.now().plusDays(1)
            .with(java.time.DayOfWeek.FRIDAY)
            .withHour(20).withMinute(0));
    }

    @Dado("foram vendidos {int} ingressos para essa sessão")
    public void foramVendidosIngressos(int quantidade) {
        for (int i = 0; i < quantidade; i++)
            ingressos.add(new Ingresso(sessao, TipoIngresso.INTEIRA));
    }

    @Dado("existe outra sessão na sala padrão com capacidade para {int} pessoas")
    public void existeOutraSessao(int capacidade) {
        Filme filme = new Filme("Outro Filme", Duration.ofMinutes(120),
            ClassificacaoIndicativa.LIVRE, GeneroFilme.COMEDIA);
        Sala sala = new Sala(2, capacidade, TipoSala.PADRAO);
        outraSessao = new Sessao(filme, sala, LocalDateTime.now().plusDays(2)
            .with(java.time.DayOfWeek.FRIDAY)
            .withHour(20).withMinute(0));
    }

    @Dado("foram vendidos {int} ingressos para essa outra sessão")
    public void foramVendidosIngressosOutraSessao(int quantidade) {
        for (int i = 0; i < quantidade; i++)
            outrosIngressos.add(new Ingresso(outraSessao, TipoIngresso.INTEIRA));
    }

    @Quando("o dashboard calcular a ocupação da sessão")
    public void calcularOcupacao() {
        ocupacao = dashboard.calcularOcupacao(sessao);
    }

    @Quando("o dashboard calcular a ocupação da semana")
    public void calcularOcupacaoDaSemana() {
        ocupacoes = dashboard.calcularOcupacaoDaSemana(List.of(sessao, outraSessao));
    }

    @Então("a taxa de ocupação deve ser {double}%")
    public void taxaDeOcupacaoDeveSer(double taxa) {
        assertEquals(taxa, ocupacao.getTaxaDeOcupacao(), 0.01);
    }

    @Então("o faturamento realizado deve ser igual ao projetado")
    public void faturamentoRealizadoIgualProjetado() {
        assertEquals(ocupacao.getFaturamentoProjetado(),
            ocupacao.getFaturamentoRealizado(), 0.01);
    }

    @Então("o faturamento realizado deve ser {double}")
    public void faturamentoRealizadoDeveSer(double valor) {
        assertEquals(valor, ocupacao.getFaturamentoRealizado(), 0.01);
    }

    @Então("o faturamento projetado deve ser {double}")
    public void faturamentoProjetadoDeveSer(double valor) {
        assertEquals(valor, ocupacao.getFaturamentoProjetado(), 0.01);
    }

    @Então("o resultado deve conter {int} sessões")
    public void resultadoDeveConterSessoes(int quantidade) {
        assertEquals(quantidade, ocupacoes.size());
    }
}