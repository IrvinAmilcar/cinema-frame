package br.com.cinema.frame.application.compra;

import br.com.cinema.frame.domain.portal.promocao.AplicacaoDeDesconto;
import br.com.cinema.frame.domain.portal.promocao.Cupom;
import br.com.cinema.frame.domain.portal.promocao.CupomRepository;
import br.com.cinema.frame.domain.portal.promocao.MotorDePromocoes;

import java.time.LocalDate;

public class AplicarCupomUseCase {

    private final CupomRepository cupomRepository;
    private final MotorDePromocoes motorDePromocoes;

    public AplicarCupomUseCase(CupomRepository cupomRepository, MotorDePromocoes motorDePromocoes) {
        this.cupomRepository = cupomRepository;
        this.motorDePromocoes = motorDePromocoes;
    }

    public AplicacaoDeDesconto executar(String codigo, double valorTotal, int quantidadeIngressos) {
        Cupom cupom = cupomRepository.buscarPorCodigo(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Cupom não encontrado: " + codigo));

        // Delega ao MotorDePromocoes que usa o padrão Strategy para selecionar o algoritmo de desconto
        return motorDePromocoes.aplicar(cupom, valorTotal, quantidadeIngressos, LocalDate.now());
    }
}
