package br.com.cinema.frame.domain.portal.recomendacao;
 
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import br.com.cinema.frame.domain.backoffice.grade.GeneroFilme;
 
public class HistoricoDeCompras {
 
    private final UUID clienteId;
    private final List<GeneroFilme> generosAssistidos;
 
    public HistoricoDeCompras(UUID clienteId) {
        if (clienteId == null)
            throw new IllegalArgumentException("ID do cliente não pode ser nulo");
 
        this.clienteId = clienteId;
        this.generosAssistidos = new ArrayList<>();
    }
 
    public void registrarGenero(GeneroFilme genero) {
        if (genero == null)
            throw new IllegalArgumentException("Gênero não pode ser nulo");
 
        generosAssistidos.add(genero);
    }
 
    public UUID getClienteId() { return clienteId; }
    public List<GeneroFilme> getGenerosAssistidos() { return Collections.unmodifiableList(generosAssistidos); }
}