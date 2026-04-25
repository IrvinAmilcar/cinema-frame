package br.com.cinema.frame.domain.portal.fidelidade;

import java.util.UUID;

public class ProgramaDeFidelidade {

    private static final double PONTOS_POR_REAL = 1.0;

    private final ContaDeFidelidadeRepository contaRepository;

    public ProgramaDeFidelidade(ContaDeFidelidadeRepository contaRepository) {
        if (contaRepository == null)
            throw new IllegalArgumentException("ContaDeFidelidadeRepository não pode ser nulo");
        this.contaRepository = contaRepository;
    }

    public ContaDeFidelidade creditar(UUID contaId, double valorGasto) {
        if (valorGasto < 0)
            throw new IllegalArgumentException("Valor gasto não pode ser negativo");

        ContaDeFidelidade conta = contaRepository.buscarPorId(contaId)
            .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada: " + contaId));

        int pontos = (int) (valorGasto * PONTOS_POR_REAL);
        conta.adicionarPontos(pontos);
        contaRepository.salvar(conta);
        return conta;
    }

    public void resgatar(UUID contaId, int pontosParaResgatar) {
        if (pontosParaResgatar <= 0)
            throw new IllegalArgumentException("Quantidade de pontos para resgate deve ser positiva");

        ContaDeFidelidade conta = contaRepository.buscarPorId(contaId)
            .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada: " + contaId));

        conta.removerPontos(pontosParaResgatar);
        contaRepository.salvar(conta);
    }
}