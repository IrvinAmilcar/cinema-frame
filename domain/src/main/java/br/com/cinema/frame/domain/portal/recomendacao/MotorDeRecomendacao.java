package br.com.cinema.frame.domain.portal.recomendacao;
 
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import br.com.cinema.frame.domain.backoffice.grade.GeneroFilme;
 
public class MotorDeRecomendacao {
 
    public List<FilmeSugerido> recomendar(HistoricoDeCompras historico, List<FilmeSugerido> catalogo) {
        if (historico == null)
            throw new IllegalArgumentException("Histórico de compras não pode ser nulo");
        if (catalogo == null)
            throw new IllegalArgumentException("Catálogo de filmes não pode ser nulo");
 
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
