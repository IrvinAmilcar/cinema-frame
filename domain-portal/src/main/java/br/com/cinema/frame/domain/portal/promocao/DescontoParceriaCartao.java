package br.com.cinema.frame.domain.portal.promocao;

public class DescontoParceriaCartao implements DescontoStrategy {

    @Override
    public AplicacaoDeDesconto aplicar(double valorTotal, int quantidadeIngressos) {
        return new AplicacaoDeDesconto(valorTotal, valorTotal * 0.15);
    }

    @Override
    public TipoPromocao getTipo() { return TipoPromocao.PARCERIA_CARTAO; }
}
