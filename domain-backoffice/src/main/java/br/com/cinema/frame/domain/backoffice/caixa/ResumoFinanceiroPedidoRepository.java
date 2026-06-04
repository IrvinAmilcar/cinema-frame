package br.com.cinema.frame.domain.backoffice.caixa;

import java.time.LocalDate;
import java.util.UUID;

public interface ResumoFinanceiroPedidoRepository {
    void salvar(UUID pedidoId, double valorIngressos, double valorBomboniere,
                double descontoCupom, double descontoPontos, double valorTotal, LocalDate dataPedido);
}