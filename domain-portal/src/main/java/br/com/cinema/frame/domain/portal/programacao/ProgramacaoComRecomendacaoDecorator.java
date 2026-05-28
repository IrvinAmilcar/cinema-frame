package br.com.cinema.frame.domain.portal.programacao;

import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.portal.notificacao.FilmeFavoritadoRepository;
import br.com.cinema.frame.domain.portal.recomendacao.HistoricoDeComprasRepository;
import br.com.cinema.frame.domain.shared.filme.GeneroFilme;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ProgramacaoComRecomendacaoDecorator {

    private final ProgramacaoService base;
    private final HistoricoDeComprasRepository historicoRepository;
    private final FilmeFavoritadoRepository favoritoRepository;

    public ProgramacaoComRecomendacaoDecorator(ProgramacaoService base,
                                                HistoricoDeComprasRepository historicoRepository,
                                                FilmeFavoritadoRepository favoritoRepository) {
        if (base == null) throw new IllegalArgumentException("ProgramacaoService base não pode ser nulo");
        if (historicoRepository == null) throw new IllegalArgumentException("HistoricoRepository não pode ser nulo");
        if (favoritoRepository == null) throw new IllegalArgumentException("FavoritoRepository não pode ser nulo");
        this.base = base;
        this.historicoRepository = historicoRepository;
        this.favoritoRepository = favoritoRepository;
    }

    public List<Sessao> listarSessoesDisponiveis(LocalDateTime agora) {
        return base.listarSessoesDisponiveis(agora);
    }

    /**
     * Reordena sessões por afinidade de gênero usando favoritos + histórico de compras.
     */
    public List<Sessao> listarSessoesRecomendadas(LocalDateTime agora, UUID clienteId) {
        List<Sessao> sessoes = base.listarSessoesDisponiveis(agora);

        // Mapeia filmeId -> gênero a partir das sessões disponíveis
        Map<UUID, GeneroFilme> generoPorFilme = sessoes.stream()
                .collect(Collectors.toMap(
                        s -> s.getFilme().getId(),
                        s -> s.getFilme().getGenero(),
                        (a, b) -> a));

        // Gêneros de filmes favoritados
        List<GeneroFilme> generos = new ArrayList<>(
                favoritoRepository.buscarPorUsuarioId(clienteId).stream()
                        .map(f -> generoPorFilme.get(f.getFilmeId()))
                        .filter(Objects::nonNull)
                        .toList());

        // Adiciona gêneros do histórico de compras
        historicoRepository.buscarPorClienteId(clienteId)
                .ifPresent(h -> generos.addAll(h.getGenerosAssistidos()));

        if (generos.isEmpty()) return sessoes;

        Map<GeneroFilme, Long> frequencia = generos.stream()
                .collect(Collectors.groupingBy(g -> g, Collectors.counting()));

        return sessoes.stream()
                .sorted(Comparator.comparingLong(
                        (Sessao s) -> frequencia.getOrDefault(s.getFilme().getGenero(), 0L)
                ).reversed())
                .collect(Collectors.toList());
    }
}
