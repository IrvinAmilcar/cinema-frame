package br.com.cinema.frame.infrastructure.fidelidade;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.com.cinema.frame.domain.portal.fidelidade.LancamentoPontos;
import br.com.cinema.frame.domain.portal.fidelidade.PontosCliente;
import br.com.cinema.frame.domain.portal.fidelidade.RegistroResgate;
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

    @OneToMany(mappedBy = "pontosCliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ResgateJpa> historicoResgates = new ArrayList<>();

    public PontosClienteJpa() {}

    public static PontosClienteJpa fromDomain(PontosCliente domain) {
        PontosClienteJpa jpa = new PontosClienteJpa();
        jpa.clienteId = domain.getClienteId();
        jpa.saldoAtivo = domain.getSaldoAtivo();

        for (LancamentoPontos l : domain.getLancamentos()) {
            jpa.lancamentos.add(LancamentoJpa.fromDomain(l, jpa));
        }
        for (RegistroResgate r : domain.getHistoricoResgates()) {
            jpa.historicoResgates.add(ResgateJpa.fromDomainComPontos(r, jpa));
        }

        return jpa;
    }

    public PontosCliente toDomain() {
        List<LancamentoPontos> domainLancamentos = lancamentos.stream()
                .map(LancamentoJpa::toDomain)
                .toList();

        List<RegistroResgate> domainResgates = historicoResgates.stream()
                .map(ResgateJpa::toDomain)
                .toList();

        return PontosCliente.reconstituir(clienteId, saldoAtivo, domainLancamentos, domainResgates);
    }

    public UUID getClienteId() { return clienteId; }
    public int getSaldoAtivo() { return saldoAtivo; }
    public List<LancamentoJpa> getLancamentos() { return lancamentos; }
    public List<ResgateJpa> getHistoricoResgates() { return historicoResgates; }
}
