package br.com.cinema.frame.domain.portal.promocao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PromocaoService {

    private final CupomRepository cupomRepository;
    private final MotorDePromocoes motorDePromocoes;

    public PromocaoService(CupomRepository cupomRepository) {
        this(cupomRepository, new MotorDePromocoes(List.of(
                new DescontoLeve2Pague1(),
                new DescontoParceriaCartao(),
                new DescontoEstudante()
        )));
    }

    public PromocaoService(CupomRepository cupomRepository, MotorDePromocoes motorDePromocoes) {
        if (cupomRepository == null)
            throw new IllegalArgumentException("CupomRepository não pode ser nulo");
        if (motorDePromocoes == null)
            throw new IllegalArgumentException("MotorDePromocoes não pode ser nulo");

        this.cupomRepository = cupomRepository;
        this.motorDePromocoes = motorDePromocoes;
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

    public List<Cupom> gerarCuponsDeReembolso(List<UUID> ingressoIds, LocalDate validade) {
        if (ingressoIds == null)
            throw new IllegalArgumentException("Lista de IDs de ingressos não pode ser nula");
        if (validade == null)
            throw new IllegalArgumentException("Validade do cupom não pode ser nula");

        List<Cupom> cupons = new ArrayList<>();
        for (UUID ingressoId : ingressoIds) {
            String codigo = "REEMBOLSO-" + ingressoId.toString().substring(0, 8).toUpperCase();
            Cupom cupom = new Cupom(codigo, TipoPromocao.REEMBOLSO, false, validade);
            cupomRepository.salvar(cupom);
            cupons.add(cupom);
        }
        return cupons;
    }

    public AplicacaoDeDesconto aplicarCupom(double valorTotal, int quantidadeIngressos,
                                            UUID cupomId, LocalDate hoje) {
        if (cupomId == null)
            throw new IllegalArgumentException("ID do cupom não pode ser nulo");
        if (hoje == null)
            throw new IllegalArgumentException("Data atual não pode ser nula");

        Cupom cupom = cupomRepository.buscarPorId(cupomId)
                .orElseThrow(() -> new IllegalArgumentException("Cupom não encontrado: " + cupomId));

        return motorDePromocoes.aplicar(cupom, valorTotal, quantidadeIngressos, hoje);
    }

    // Mantido para compatibilidade com os testes BDD da 1.ª entrega
    public AplicacaoDeDesconto aplicarCupons(double valorTotal, int quantidadeIngressos,
                                             List<UUID> cupomIds, LocalDate hoje) {
        if (cupomIds == null || cupomIds.isEmpty())
            throw new IllegalArgumentException("Lista de cupons não pode ser vazia");

        boolean temNaoCumulativo = cupomIds.stream()
            .map(id -> cupomRepository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Cupom não encontrado: " + id)))
            .anyMatch(c -> !c.isCumulativo());
        if (temNaoCumulativo && cupomIds.size() > 1)
            throw new IllegalStateException("Cupons não cumulativos não podem ser combinados");

        double totalDesconto = 0;
        for (UUID id : cupomIds) {
            Cupom cupom = cupomRepository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Cupom não encontrado: " + id));
            totalDesconto += motorDePromocoes.aplicar(cupom, valorTotal, quantidadeIngressos, hoje).getValorDesconto();
        }
        double final_ = Math.min(totalDesconto, valorTotal);
        return new AplicacaoDeDesconto(valorTotal, final_);
    }
}