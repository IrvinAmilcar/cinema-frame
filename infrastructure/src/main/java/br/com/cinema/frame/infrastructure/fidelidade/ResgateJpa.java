package br.com.cinema.frame.infrastructure.fidelidade;

import java.time.LocalDate;
import java.util.UUID;

import br.com.cinema.frame.domain.portal.fidelidade.RegistroResgate;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "resgates_pontos")
public class ResgateJpa {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID clienteId;
    private UUID beneficioId;
    private int pontosDebitados;
    private LocalDate data;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pontos_cliente_id")
    private PontosClienteJpa pontosCliente;

    public ResgateJpa() {}

    public static ResgateJpa fromDomainComPontos(RegistroResgate domain, PontosClienteJpa pontos) {
        ResgateJpa jpa = new ResgateJpa();
        jpa.clienteId = pontos.getClienteId();
        jpa.beneficioId = domain.getBeneficioId();
        jpa.pontosDebitados = domain.getPontosDebitados();
        jpa.data = domain.getData();
        jpa.pontosCliente = pontos;
        return jpa;
    }

    public static ResgateJpa fromDomain(UUID clienteId, RegistroResgate domain) {
        ResgateJpa jpa = new ResgateJpa();
        jpa.clienteId = clienteId;
        jpa.beneficioId = domain.getBeneficioId();
        jpa.pontosDebitados = domain.getPontosDebitados();
        jpa.data = domain.getData();
        return jpa;
    }

    public RegistroResgate toDomain() {
        return new RegistroResgate(beneficioId, pontosDebitados, data);
    }

    public UUID getClienteId() { return clienteId; }
    public UUID getBeneficioId() { return beneficioId; }
    public int getPontosDebitados() { return pontosDebitados; }
    public LocalDate getData() { return data; }
}
