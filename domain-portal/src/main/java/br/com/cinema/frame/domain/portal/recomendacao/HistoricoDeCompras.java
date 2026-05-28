package br.com.cinema.frame.domain.portal.recomendacao;

import br.com.cinema.frame.domain.shared.filme.GeneroFilme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class HistoricoDeCompras {

    private final UUID clienteId;
    private final List<UUID> filmesAssistidos;
    private final List<GeneroFilme> generosAssistidos;

    public HistoricoDeCompras(UUID clienteId) {
        if (clienteId == null)
            throw new IllegalArgumentException("ID do cliente não pode ser nulo");

        this.clienteId = clienteId;
        this.filmesAssistidos = new ArrayList<>();
        this.generosAssistidos = new ArrayList<>();
    }

    public void registrarFilme(UUID filmeId, GeneroFilme genero) {
        if (filmeId == null)
            throw new IllegalArgumentException("ID do filme não pode ser nulo");
        if (genero == null)
            throw new IllegalArgumentException("Gênero não pode ser nulo");

        filmesAssistidos.add(filmeId);
        generosAssistidos.add(genero);
    }

    public void registrarGenero(GeneroFilme genero) {
        if (genero == null)
            throw new IllegalArgumentException("Gênero não pode ser nulo");

        generosAssistidos.add(genero);
    }

    public static HistoricoDeCompras reconstituir(UUID clienteId, List<UUID> filmes, List<GeneroFilme> generos) {
        HistoricoDeCompras h = new HistoricoDeCompras(clienteId);
        filmes.forEach(h.filmesAssistidos::add);
        generos.forEach(h::registrarGenero);
        return h;
    }

    public static HistoricoDeCompras reconstituir(UUID clienteId, List<GeneroFilme> generos) {
        return reconstituir(clienteId, Collections.emptyList(), generos);
    }

    public UUID getClienteId() { return clienteId; }
    public List<GeneroFilme> getGenerosAssistidos() { return Collections.unmodifiableList(generosAssistidos); }
    public List<UUID> getFilmesAssistidos() { return Collections.unmodifiableList(filmesAssistidos); }
}
