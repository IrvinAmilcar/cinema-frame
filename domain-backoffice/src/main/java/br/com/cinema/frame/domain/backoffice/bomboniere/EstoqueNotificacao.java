package br.com.cinema.frame.domain.backoffice.bomboniere;

public class EstoqueNotificacao {

    private final Insumo insumo;

    public EstoqueNotificacao(Insumo insumo) {
        this.insumo = insumo;
    }

    public String getMensagem() {
        return "Estoque crítico: " + insumo.getNome() +
               " com apenas " + insumo.getQuantidadeEmEstoque() +
               " " + insumo.getUnidade() + " restantes";
    }

    public Insumo getInsumo() { return insumo; }
}