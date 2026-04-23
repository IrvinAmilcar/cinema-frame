package br.com.cinema.frame.domain.portal.promocao;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PromocaoService {

    private final CupomRepository cupomRepository;
    private final MotorDePromocoes motorDePromocoes;

    public PromocaoService(CupomRepository cupomRepository) {
        if (cupomRepository == null)
            throw new IllegalArgumentException("CupomRepository não pode ser nulo");

        this.cupomRepository = cupomRepository;
        this.motorDePromocoes = new MotorDePromocoes();
    }

    public Cupom cadastrarCupom(String codigo, TipoPromocao tipo, boolean cumulativo, LocalDate validade) {
        if (codigo == null || codigo.isBlank())
            throw new IllegalArgumentException("Código do cupom não pode ser vazio");
        if (tipo == null)
            throw new IllegalArgumentException("Tipo da promoção não pode ser nulo");
        if (validade == null)
            throw new IllegalArgumentException("Validade do cupom não pode ser nula");

        Cupom cupom = new Cupom(codigo, tipo, cumulativo, validade);
        cupomRepository.salvar(cupom);
        return cupom;
    }

    public AplicacaoDeDesconto aplicarCupons(double valorTotal, int quantidadeIngressos,
                                             List<UUID> cupomIds, LocalDate hoje) {
        if (cupomIds == null)
            throw new IllegalArgumentException("Lista de IDs de cupons não pode ser nula");
        if (hoje == null)
            throw new IllegalArgumentException("Data atual não pode ser nula");

        List<Cupom> cupons = cupomIds.stream()
            .map(id -> cupomRepository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Cupom não encontrado: " + id)))
            .collect(Collectors.toList());

        return motorDePromocoes.aplicar(valorTotal, quantidadeIngressos, cupons, hoje);
    }
}