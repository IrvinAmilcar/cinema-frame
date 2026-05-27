package br.com.cinema.frame.domain.portal.promocao;

public class DescontoLeve2Pague1 implements DescontoStrategy {

    @Override
    public AplicacaoDeDesconto aplicar(double valorTotal, int quantidadeIngressos) {
        if (quantidadeIngressos < 2)
            return new AplicacaoDeDesconto(valorTotal, 0);
        double precoUnitario = valorTotal / quantidadeIngressos;
        int pares = quantidadeIngressos / 2;
        return new AplicacaoDeDesconto(valorTotal, pares * precoUnitario);
    }

    @Override
    public TipoPromocao getTipo() { return TipoPromocao.LEVE2_PAGUE1; }
}
