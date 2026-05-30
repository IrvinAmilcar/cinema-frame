package br.com.cinema.frame.infrastructure.fidelidade;

import java.time.LocalDate;
import java.util.UUID;

import br.com.cinema.frame.domain.portal.fidelidade.LancamentoPontos;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "lancamentos_pontos")
public class LancamentoJpa {

    @Id
    @GeneratedValue
    private UUID id;

    private int saldo;
    private int pontosOriginais;
    private LocalDate validade;
    private LocalDate dataCriacao;
    private boolean expirado;
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pontos_cliente_id")
    private PontosClienteJpa pontosCliente;

    public LancamentoJpa() {}

    public static LancamentoJpa fromDomain(LancamentoPontos domain, PontosClienteJpa pontos) {
        LancamentoJpa jpa = new LancamentoJpa();
        jpa.saldo = domain.getSaldo();
        jpa.pontosOriginais = domain.getPontosOriginais();
        jpa.validade = domain.getValidade();
        jpa.dataCriacao = domain.getDataCriacao();
        jpa.expirado = domain.isExpirado();
        jpa.descricao = domain.getDescricao();
        jpa.pontosCliente = pontos;
        return jpa;
    }

    public LancamentoPontos toDomain() {
        LancamentoPontos l = new LancamentoPontos(pontosOriginais, validade, dataCriacao,
                descricao != null ? descricao : "Compra de ingresso");
        int diferenca = pontosOriginais - saldo;
        if (diferenca > 0) l.debitar(diferenca);
        if (expirado) l.expirar();
        return l;
    }

    public int getSaldo() { return saldo; }
    public int getPontosOriginais() { return pontosOriginais; }
    public LocalDate getValidade() { return validade; }
    public LocalDate getDataCriacao() { return dataCriacao; }
    public boolean isExpirado() { return expirado; }
    public String getDescricao() { return descricao; }
}
