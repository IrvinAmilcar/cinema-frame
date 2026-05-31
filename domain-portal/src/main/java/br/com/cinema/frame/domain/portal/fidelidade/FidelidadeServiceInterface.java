package br.com.cinema.frame.domain.portal.fidelidade;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import br.com.cinema.frame.domain.backoffice.bomboniere.BombonieresService;

public interface FidelidadeServiceInterface {

    boolean acumularPontos(UUID clienteId, double valorGasto, LocalDate hoje);

    boolean acumularPontos(UUID clienteId, double valorGasto, LocalDate hoje, String tituloFilme);

    boolean acumularPontos(UUID clienteId, double valorGasto, LocalDate hoje,
                           String tituloFilme, String horario, int salaNumero);

    void usarPontosNaCompra(UUID clienteId, LocalDate hoje);

    int consultarSaldo(UUID clienteId, LocalDate hoje);

    List<LancamentoPontos> consultarExtrato(UUID clienteId);

    List<Beneficio> verificarBeneficios(UUID clienteId, LocalDate hoje);

    void resgatarBeneficio(UUID clienteId, UUID beneficioId, LocalDate hoje);

    String resgatarProdutoBomboniere(UUID clienteId, UUID produtoId, LocalDate hoje);

    List<RegistroResgate> consultarHistoricoResgates(UUID clienteId);

    BombonieresService getBombonieresService();
}