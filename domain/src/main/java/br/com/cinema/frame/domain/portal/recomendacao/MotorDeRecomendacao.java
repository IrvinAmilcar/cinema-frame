package br.com.cinema.frame.domain.portal.recomendacao;

import br.com.cinema.frame.domain.backoffice.grade.GeneroFilme;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class MotorDeRecomendacao {

    private final HistoricoDeComprasRepository historicoRepository;
    private final FilmeSugeridoRepository filmeSugeridoRepository;

    public MotorDeRecomendacao(HistoricoDeComprasRepository historicoRepository,
                                FilmeSugeridoRepository filmeSugeridoRepository) {
        if (historicoRepository == null)
            throw new IllegalArgumentException("HistoricoDeComprasRepository não pode ser nulo");
        if (filmeSugeridoRepository == null)
            throw new IllegalArgumentException("FilmeSugeridoRepository não pode ser nulo");
        this.historicoRepository = historicoRepository;
        this.filmeSugeridoRepository = filmeSugeridoRepository;
    }

    public List<FilmeSugerido> recomendar(UUID clienteId) {
        if (clienteId == null)
            throw new IllegalArgumentException("ID do cliente não pode ser nulo");

        HistoricoDeCompras historico = historicoRepository.buscarPorClienteId(clienteId)
            .orElseThrow(() -> new IllegalArgumentException("Histórico não encontrado para o cliente: " + clienteId));

        List<FilmeSugerido> catalogo = filmeSugeridoRepository.listarTodos();

        if (historico.getGenerosAssistidos().isEmpty())
            return Collections.emptyList();

        Map<GeneroFilme, Long> frequenciaPorGenero = historico.getGenerosAssistidos().stream()
            .collect(Collectors.groupingBy(g -> g, Collectors.counting()));

        return catalogo.stream()
            .filter(f -> frequenciaPorGenero.containsKey(f.getGenero()))
            .sorted(Comparator.comparingLong(
                (FilmeSugerido f) -> frequenciaPorGenero.getOrDefault(f.getGenero(), 0L)
            ).reversed())
            .collect(Collectors.toList());
    }
}