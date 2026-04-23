package br.com.cinema.frame.domain.backoffice.caixa;

import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicaoRepository;
import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.ingresso.Ingresso;
import br.com.cinema.frame.domain.backoffice.ingresso.IngressoRepository;
import br.com.cinema.frame.domain.backoffice.ingresso.TipoIngresso;
import br.com.cinema.frame.domain.backoffice.precificacao.PrecificacaoService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class CaixaService {

    private final IngressoRepository ingressoRepository;
    private final GradeDeExibicaoRepository gradeRepository;
    private final PrecificacaoService precificacaoService;

    public CaixaService(IngressoRepository ingressoRepository,
                        GradeDeExibicaoRepository gradeRepository) {
        if (ingressoRepository == null)
            throw new IllegalArgumentException("IngressoRepository não pode ser nulo");
        if (gradeRepository == null)
            throw new IllegalArgumentException("GradeRepository não pode ser nulo");

        this.ingressoRepository = ingressoRepository;
        this.gradeRepository = gradeRepository;
        this.precificacaoService = new PrecificacaoService();
    }

    public Bordero gerarBordero(UUID sessaoId) {
        if (sessaoId == null)
            throw new IllegalArgumentException("ID da sessão não pode ser nulo");

        Sessao sessao = gradeRepository.buscarPorData(LocalDate.now())
            .orElseThrow(() -> new IllegalArgumentException("Grade não encontrada"))
            .getSessoes()
            .stream()
            .filter(s -> s.getId().equals(sessaoId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Sessão não encontrada: " + sessaoId));

        List<Ingresso> ingressos = ingressoRepository.buscarPorSessao(sessao);

        if (ingressos.isEmpty())
            throw new IllegalArgumentException("Nenhum ingresso encontrado para esta sessão");

        double precoBase = precificacaoService.calcularPreco(sessao);

        int inteiras = 0, meias = 0, convites = 0;
        double total = 0.0;

        for (Ingresso ingresso : ingressos) {
            TipoIngresso tipo = ingresso.getTipo();
            total += precoBase * tipo.getFatorPreco();

            switch (tipo) {
                case INTEIRA -> inteiras++;
                case MEIA -> meias++;
                case CONVITE -> convites++;
            }
        }

        return new Bordero(sessao, inteiras, meias, convites, total);
    }
}