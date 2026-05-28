package br.com.cinema.frame.infrastructure.programacao;

import br.com.cinema.frame.domain.portal.recomendacao.HistoricoDeCompras;
import br.com.cinema.frame.domain.shared.filme.GeneroFilme;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "historico_compras")
public class HistoricoJpa {

    @Id
    private UUID clienteId;

    @ElementCollection
    @CollectionTable(name = "historico_generos", joinColumns = @JoinColumn(name = "cliente_id"))
    @Column(name = "genero")
    private List<String> generosAssistidos = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "historico_filmes", joinColumns = @JoinColumn(name = "cliente_id"))
    @Column(name = "filme_id")
    private List<String> filmesAssistidos = new ArrayList<>();

    protected HistoricoJpa() {}

    public static HistoricoJpa fromDomain(HistoricoDeCompras h) {
        HistoricoJpa e = new HistoricoJpa();
        e.clienteId = h.getClienteId();
        e.generosAssistidos = h.getGenerosAssistidos().stream()
                .map(GeneroFilme::name)
                .collect(Collectors.toList());
        e.filmesAssistidos = h.getFilmesAssistidos().stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
        return e;
    }

    public HistoricoDeCompras toDomain() {
        List<GeneroFilme> generos = generosAssistidos.stream()
                .map(GeneroFilme::valueOf)
                .collect(Collectors.toList());
        List<UUID> filmes = filmesAssistidos.stream()
                .map(UUID::fromString)
                .collect(Collectors.toList());
        return HistoricoDeCompras.reconstituir(clienteId, filmes, generos);
    }

    public UUID getClienteId() { return clienteId; }
}
