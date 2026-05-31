package br.com.cinema.frame.domain.portal.fidelidade;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import br.com.cinema.frame.domain.backoffice.bomboniere.BombonieresService;

/**
 * Contrato do serviço de fidelidade.
 * Permite o padrão Proxy: FidelidadeService implementa a lógica real,
 * FidelidadeServiceProxy intercepta as chamadas antes de delegar.
 */
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

    List<Beneficio> listarTodosBeneficios(UUID clienteId, LocalDate hoje);

    BombonieresService getBombonieresService();
}