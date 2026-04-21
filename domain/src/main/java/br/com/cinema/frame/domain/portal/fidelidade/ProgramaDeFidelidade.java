package br.com.cinema.frame.domain.portal.fidelidade;

public class ProgramaDeFidelidade {

    private static final double PONTOS_POR_REAL = 1.0;
    private static final int PONTOS_POR_INGRESSO = 100;
    private static final int PONTOS_POR_PRODUTO = 50;

    public ContaDeFidelidade creditar(ContaDeFidelidade conta, double valorGasto) {
        if (conta == null)
            throw new IllegalArgumentException("Conta de fidelidade não pode ser nula");
        if (valorGasto < 0)
            throw new IllegalArgumentException("Valor gasto não pode ser negativo");

        int pontos = (int) (valorGasto * PONTOS_POR_REAL);
        conta.adicionarPontos(pontos);
        return conta;
    }

    public void resgatar(ContaDeFidelidade conta, int pontosParaResgatar) {
        if (conta == null)
            throw new IllegalArgumentException("Conta de fidelidade não pode ser nula");
        if (pontosParaResgatar <= 0)
            throw new IllegalArgumentException("Quantidade de pontos para resgate deve ser positiva");

        conta.removerPontos(pontosParaResgatar);
    }
}

