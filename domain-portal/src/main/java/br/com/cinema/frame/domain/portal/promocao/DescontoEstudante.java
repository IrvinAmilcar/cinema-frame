package br.com.cinema.frame.domain.portal.promocao;

public class DescontoEstudante implements DescontoStrategy {

    @Override
    public AplicacaoDeDesconto aplicar(double valorTotal, int quantidadeIngressos) {
        return new AplicacaoDeDesconto(valorTotal, valorTotal * 0.20);
    }

    @Override
    public TipoPromocao getTipo() { return TipoPromocao.DESCONTO_ESTUDANTE; }
}
