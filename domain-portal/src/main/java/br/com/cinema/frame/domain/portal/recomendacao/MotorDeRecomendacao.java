package br.com.cinema.frame.domain.portal.recomendacao;

import br.com.cinema.frame.domain.portal.notificacao.FilmeFavoritado;
import br.com.cinema.frame.domain.portal.notificacao.FilmeFavoritadoRepository;
import br.com.cinema.frame.domain.shared.filme.GeneroFilme;

import java.util.*;
import java.util.stream.Collectors;

public class MotorDeRecomendacao {

    private static final int PONTOS_ASSISTIDO = 3;
    private static final int PONTOS_FAVORITADO = 1;

    private final HistoricoDeComprasRepository historicoRepository;
    private final FilmeSugeridoRepository filmeSugeridoRepository;
    private final FilmeFavoritadoRepository filmeFavoritadoRepository;

    public MotorDeRecomendacao(HistoricoDeComprasRepository historicoRepository,
                                FilmeSugeridoRepository filmeSugeridoRepository,
                                FilmeFavoritadoRepository filmeFavoritadoRepository) {
        if (historicoRepository == null)
            throw new IllegalArgumentException("HistoricoDeComprasRepository não pode ser nulo");
        if (filmeSugeridoRepository == null)
            throw new IllegalArgumentException("FilmeSugeridoRepository não pode ser nulo");
        if (filmeFavoritadoRepository == null)
            throw new IllegalArgumentException("FilmeFavoritadoRepository não pode ser nulo");

        this.historicoRepository = historicoRepository;
        this.filmeSugeridoRepository = filmeSugeridoRepository;
        this.filmeFavoritadoRepository = filmeFavoritadoRepository;
    }

    public List<FilmeSugerido> recomendar(UUID clienteId) {
        if (clienteId == null)
            throw new IllegalArgumentException("ID do cliente não pode ser nulo");

        List<FilmeSugerido> catalogo = filmeSugeridoRepository.listarTodos();
        Map<UUID, GeneroFilme> generoPorFilmeId = catalogo.stream()
            .collect(Collectors.toMap(FilmeSugerido::getId, FilmeSugerido::getGenero));

        Map<GeneroFilme, Integer> scorePorGenero = new HashMap<>();
        Set<UUID> filmesJaAssistidos = new HashSet<>();

        // +3 pontos por cada gênero de filme assistido (histórico de compras)
        historicoRepository.buscarPorClienteId(clienteId).ifPresent(historico -> {
            filmesJaAssistidos.addAll(historico.getFilmesAssistidos());
            for (GeneroFilme g : historico.getGenerosAssistidos()) {
                scorePorGenero.merge(g, PONTOS_ASSISTIDO, Integer::sum);
            }
        });

        // +1 ponto por cada gênero de filme favoritado
        for (FilmeFavoritado fav : filmeFavoritadoRepository.buscarPorUsuarioId(clienteId)) {
            GeneroFilme g = generoPorFilmeId.get(fav.getFilmeId());
            if (g != null) {
                scorePorGenero.merge(g, PONTOS_FAVORITADO, Integer::sum);
            }
        }

        // Sem sinais → sem recomendações (RN 9)
        if (scorePorGenero.isEmpty()) return Collections.emptyList();

        // Gêneros ordenados por pontuação decrescente
        List<GeneroFilme> generosPorScore = scorePorGenero.entrySet().stream()
            .sorted(Map.Entry.<GeneroFilme, Integer>comparingByValue().reversed())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        Map<GeneroFilme, Integer> rankPorGenero = new HashMap<>();
        for (int i = 0; i < generosPorScore.size(); i++) {
            rankPorGenero.put(generosPorScore.get(i), i);
        }

        return catalogo.stream()
            .filter(f -> scorePorGenero.containsKey(f.getGenero()))
            .filter(f -> !filmesJaAssistidos.contains(f.getId()))
            .sorted(Comparator.comparingInt((FilmeSugerido f) -> rankPorGenero.getOrDefault(f.getGenero(), Integer.MAX_VALUE)))
            .collect(Collectors.toList());
    }
}
