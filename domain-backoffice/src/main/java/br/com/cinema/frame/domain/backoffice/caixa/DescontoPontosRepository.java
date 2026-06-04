package br.com.cinema.frame.domain.backoffice.caixa;

import java.time.LocalDate;
import java.util.UUID;

public interface DescontoPontosRepository {
    void salvar(UUID pedidoId, double valorDesconto, LocalDate dataPedido);
}
