package br.com.cinema.frame.domain.portal.recomendacao;

import java.util.Optional;
import java.util.UUID;

public interface HistoricoDeComprasRepository {

    void salvar(HistoricoDeCompras historico);
    Optional<HistoricoDeCompras> buscarPorClienteId(UUID clienteId);
}