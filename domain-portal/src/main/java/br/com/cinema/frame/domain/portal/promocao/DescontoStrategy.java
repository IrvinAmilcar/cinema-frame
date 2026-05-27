package br.com.cinema.frame.domain.portal.promocao;

public interface DescontoStrategy {
    AplicacaoDeDesconto aplicar(double valorTotal, int quantidadeIngressos);
    TipoPromocao getTipo();
}
