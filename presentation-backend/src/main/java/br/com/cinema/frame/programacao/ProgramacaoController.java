package br.com.cinema.frame.programacao;

import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.precificacao.PrecificacaoService;
import br.com.cinema.frame.domain.portal.programacao.ProgramacaoComRecomendacaoDecorator;
import br.com.cinema.frame.domain.portal.programacao.ProgramacaoService;
import br.com.cinema.frame.domain.portal.recomendacao.MotorDeRecomendacao;
import br.com.cinema.frame.domain.shared.classificacao.ClassificacaoIndicativa;
import br.com.cinema.frame.domain.shared.filme.GeneroFilme;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/programacao")
public class ProgramacaoController {

    private final ProgramacaoService programacaoService;
    private final ProgramacaoComRecomendacaoDecorator programacaoDecorator;
    private final MotorDeRecomendacao motorDeRecomendacao;
    private final PrecificacaoService precificacaoService;

    public ProgramacaoController(ProgramacaoService programacaoService,
                                  ProgramacaoComRecomendacaoDecorator programacaoDecorator,
                                  MotorDeRecomendacao motorDeRecomendacao,
                                  PrecificacaoService precificacaoService) {
        this.programacaoService = programacaoService;
        this.programacaoDecorator = programacaoDecorator;
        this.motorDeRecomendacao = motorDeRecomendacao;
        this.precificacaoService = precificacaoService;
    }

    @GetMapping("/sessoes")
    public List<SessaoDisponivelResponse> listarSessoes(
            @RequestParam(required = false) String data) {
        LocalDate dataFiltro = data != null ? LocalDate.parse(data) : LocalDate.now();
        List<Sessao> sessoes = data != null
                ? programacaoService.listarSessoesPorData(dataFiltro)
                : programacaoService.listarSessoesDisponiveis(LocalDateTime.now());
        return sessoes.stream()
                .map(s -> SessaoDisponivelResponse.from(s, precificacaoService.calcularPreco(s, dataFiltro)))
                .toList();
    }

    /**
     * Lista filmes em cartaz com filtros opcionais.
     * - data (YYYY-MM-DD): dia da sessão, padrão = hoje
     * - genero / classificacao: filtros adicionais
     * - clienteId: ativa o Decorator de recomendação (só para hoje)
     */
    @GetMapping("/filmes")
    public List<FilmeCardResponse> listarFilmes(
            @RequestParam(required = false) UUID clienteId,
            @RequestParam(required = false) String genero,
            @RequestParam(required = false) String classificacao,
            @RequestParam(required = false) String data,
            @RequestParam(required = false) String ordenar) {

        LocalDate dataFiltro = (data != null) ? LocalDate.parse(data) : LocalDate.now();
        LocalDateTime agora = LocalDateTime.now();
        boolean ehHoje = dataFiltro.equals(LocalDate.now());
        boolean porPopularidade = "popularidade".equalsIgnoreCase(ordenar);

        List<Sessao> sessoes;
        if (porPopularidade && ehHoje) {
            sessoes = programacaoService.ordenarPorPopularidade(agora);
        } else if (ehHoje && clienteId != null) {
            sessoes = programacaoDecorator.listarSessoesRecomendadas(agora, clienteId);
        } else if (ehHoje) {
            sessoes = programacaoService.listarSessoesDisponiveis(agora);
        } else {
            sessoes = programacaoService.listarSessoesPorData(dataFiltro);
        }

        if (genero != null && !genero.isBlank()) {
            GeneroFilme generoEnum = GeneroFilme.valueOf(genero.toUpperCase());
            sessoes = sessoes.stream()
                    .filter(s -> s.getFilme().getGenero() == generoEnum)
                    .collect(Collectors.toList());
        }

        if (classificacao != null && !classificacao.isBlank()) {
            ClassificacaoIndicativa classEnum = ClassificacaoIndicativa.valueOf(classificacao.toUpperCase());
            sessoes = sessoes.stream()
                    .filter(s -> s.getFilme().getClassificacaoIndicativa().getIdadeMinima()
                            <= classEnum.getIdadeMinima())
                    .collect(Collectors.toList());
        }

        LinkedHashMap<UUID, List<Sessao>> porFilme = new LinkedHashMap<>();
        for (Sessao s : sessoes) {
            porFilme.computeIfAbsent(s.getFilme().getId(), k -> new ArrayList<>()).add(s);
        }

        List<UUID> filmesOrdenados = new ArrayList<>(porFilme.keySet());

        // RN 9: recomendados vêm do MotorDeRecomendacao (histórico de compras)
        // clientes sem histórico não recebem sugestões
        final Set<UUID> recomendados;
        if (clienteId != null) {
            Set<UUID> sugestoes = motorDeRecomendacao.recomendar(clienteId)
                    .stream().map(f -> f.getId()).collect(Collectors.toSet());
            recomendados = filmesOrdenados.stream()
                    .filter(sugestoes::contains)
                    .collect(Collectors.toSet());
        } else {
            recomendados = Collections.emptySet();
        }

        return filmesOrdenados.stream().map(filmeId -> {
            var ref = porFilme.get(filmeId).get(0).getFilme();
            return FilmeCardResponse.from(
                    filmeId,
                    ref.getTitulo(),
                    ref.getGenero().name(),
                    ref.getClassificacaoIndicativa().name(),
                    (int) ref.getDuracao().toMinutes(),
                    ref.getTrailerURL(),
                    ref.getSinopse(),
                    ref.getNota(),
                    recomendados.contains(filmeId),
                    porFilme.get(filmeId)
            );
        }).toList();
    }
}
