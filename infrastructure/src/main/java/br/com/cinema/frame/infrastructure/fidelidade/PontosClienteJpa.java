package br.com.cinema.frame.infrastructure.fidelidade;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.com.cinema.frame.domain.portal.fidelidade.LancamentoPontos;
import br.com.cinema.frame.domain.portal.fidelidade.PontosCliente;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "pontos_cliente")
public class PontosClienteJpa {

    @Id
    private UUID clienteId;

    private int saldoAtivo;

    @OneToMany(mappedBy = "pontosCliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<LancamentoJpa> lancamentos = new ArrayList<>();

    public PontosClienteJpa() {}

    /**
     * Atualiza este JPA a partir do domínio fazendo MERGE dos lançamentos —
     * reutiliza objetos já gerenciados pelo Hibernate para evitar conflito de contexto.
     */
    public void atualizarDe(PontosCliente domain) {
        this.clienteId = domain.getClienteId();
        this.saldoAtivo = domain.getSaldoAtivo();

        List<LancamentoPontos> domainLancamentos = domain.getLancamentos();
        List<LancamentoJpa> atualizados = new ArrayList<>();

        for (LancamentoPontos dl : domainLancamentos) {
            // Tenta encontrar o JPA correspondente já gerenciado
            LancamentoJpa existente = lancamentos.stream()
                    .filter(jpa -> jpa.correspondeA(dl))
                    .findFirst()
                    .orElse(null);

            if (existente != null) {
                existente.atualizarDe(dl);
                atualizados.add(existente);
            } else {
                atualizados.add(LancamentoJpa.fromDomain(dl, this));
            }
        }

        // Remove os que não estão mais no domínio e adiciona os novos
        lancamentos.retainAll(atualizados);
        for (LancamentoJpa l : atualizados) {
            if (!lancamentos.contains(l)) {
                lancamentos.add(l);
            }
        }
    }

    public PontosCliente toDomain() {
        List<LancamentoPontos> domainLancamentos = lancamentos.stream()
                .map(LancamentoJpa::toDomain)
                .toList();
        return PontosCliente.reconstituir(clienteId, saldoAtivo, domainLancamentos, List.of());
    }

    public UUID getClienteId() { return clienteId; }
    public int getSaldoAtivo() { return saldoAtivo; }
    public List<LancamentoJpa> getLancamentos() { return lancamentos; }
}