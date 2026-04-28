package br.com.cinema.frame.domain.backoffice.bomboniere;

import java.time.LocalDateTime;
import java.util.UUID;

public class MovimentacaoEstoque {

    private final UUID id;
    private final UUID insumoId;
    private final TipoMovimentacao tipo;
    private final double quantidade;
    private final String motivo;
    private final LocalDateTime momento;

    public MovimentacaoEstoque(UUID insumoId, TipoMovimentacao tipo,
                                double quantidade, String motivo, LocalDateTime momento) {
        if (insumoId == null)
            throw new IllegalArgumentException("ID do insumo não pode ser nulo");
        if (tipo == null)
            throw new IllegalArgumentException("Tipo de movimentação não pode ser nulo");
        if (quantidade <= 0)
            throw new IllegalArgumentException("Quantidade deve ser positiva");
        if (motivo == null || motivo.isBlank())
            throw new IllegalArgumentException("Motivo não pode ser vazio");
        if (momento == null)
            throw new IllegalArgumentException("Momento não pode ser nulo");

        this.id = UUID.randomUUID();
        this.insumoId = insumoId;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.motivo = motivo;
        this.momento = momento;
    }

    public UUID getId() { return id; }
    public UUID getInsumoId() { return insumoId; }
    public TipoMovimentacao getTipo() { return tipo; }
    public double getQuantidade() { return quantidade; }
    public String getMotivo() { return motivo; }
    public LocalDateTime getMomento() { return momento; }
}