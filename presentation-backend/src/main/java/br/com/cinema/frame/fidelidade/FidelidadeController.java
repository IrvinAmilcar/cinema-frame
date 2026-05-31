package br.com.cinema.frame.fidelidade;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.cinema.frame.domain.backoffice.bomboniere.ProdutoDaBomboniere;
import br.com.cinema.frame.domain.portal.fidelidade.Beneficio;
import br.com.cinema.frame.domain.portal.fidelidade.FidelidadeService;
import br.com.cinema.frame.domain.portal.fidelidade.LancamentoPontos;
import br.com.cinema.frame.domain.portal.fidelidade.RegistroResgate;

@RestController
@RequestMapping("/api/fidelidade")
public class FidelidadeController {

    private static final UUID MARCADOR_COMPRA = new UUID(0L, 0L);

    private final FidelidadeService fidelidadeService;

    public FidelidadeController(FidelidadeService fidelidadeService) {
        this.fidelidadeService = fidelidadeService;
    }

    @PostMapping("/{clienteId}/acumular")
    public ResponseEntity<Map<String, Object>> acumularPontos(
            @PathVariable UUID clienteId,
            @RequestParam double valor,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        LocalDate hoje = data != null ? data : LocalDate.now();
        fidelidadeService.acumularPontos(clienteId, valor, hoje);
        int saldo = fidelidadeService.consultarSaldo(clienteId, hoje);
        return ResponseEntity.ok(Map.of("clienteId", clienteId, "saldoAtivo", saldo));
    }

    @GetMapping("/{clienteId}/saldo")
    public ResponseEntity<Map<String, Object>> consultarSaldo(
            @PathVariable UUID clienteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        LocalDate hoje = data != null ? data : LocalDate.now();
        int saldo = fidelidadeService.consultarSaldo(clienteId, hoje);
        return ResponseEntity.ok(Map.of("clienteId", clienteId, "saldoAtivo", saldo));
    }

    @GetMapping("/{clienteId}/extrato")
    public ResponseEntity<List<LancamentoResponse>> consultarExtrato(@PathVariable UUID clienteId) {
        List<LancamentoPontos> lancamentos = fidelidadeService.consultarExtrato(clienteId);
        List<LancamentoResponse> response = lancamentos.stream()
                .map(l -> new LancamentoResponse(
                        l.getPontosOriginais(), l.getSaldo(),
                        l.getValidade().toString(), l.getDataCriacao().toString(),
                        l.isExpirado() ? "EXPIRADO" : "ATIVO",
                        l.getDescricao()))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{clienteId}/beneficios")
    public ResponseEntity<List<BeneficioResponse>> consultarBeneficios(
            @PathVariable UUID clienteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        LocalDate hoje = data != null ? data : LocalDate.now();
        List<Beneficio> beneficios = fidelidadeService.verificarBeneficios(clienteId, hoje);
        List<BeneficioResponse> response = beneficios.stream()
                .map(b -> new BeneficioResponse(b.getId(), b.getNome(), b.getTipo().name(),
                        b.getPontosNecessarios(), null, null))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{clienteId}/recompensas")
    public ResponseEntity<List<RecompensaResponse>> listarRecompensas(
            @PathVariable UUID clienteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {

        LocalDate hoje = data != null ? data : LocalDate.now();
        int saldo = fidelidadeService.consultarSaldo(clienteId, hoje);

        List<RecompensaResponse> recompensas = new ArrayList<>();

        List<ProdutoDaBomboniere> produtos = fidelidadeService.getBombonieresService()
                .listarProdutos().stream()
                .filter(ProdutoDaBomboniere::isAtivo)
                .toList();

        for (ProdutoDaBomboniere p : produtos) {
            int pontosNecessarios = (int) Math.ceil(p.getPreco() / 2.0 * 100);
            recompensas.add(new RecompensaResponse(
                    p.getId(),
                    p.getNome(),
                    "PRODUTO_BOMBONIERE",
                    pontosNecessarios,
                    p.getCategoria().name(),
                    p.getPreco(),
                    saldo >= pontosNecessarios
            ));
        }

        List<Beneficio> beneficios = fidelidadeService.verificarBeneficios(clienteId, hoje);
        for (Beneficio b : beneficios) {
            recompensas.add(new RecompensaResponse(
                    b.getId(),
                    b.getNome(),
                    b.getTipo().name(),
                    b.getPontosNecessarios(),
                    null,
                    null,
                    saldo >= b.getPontosNecessarios()
            ));
        }

        return ResponseEntity.ok(recompensas);
    }

    @PostMapping("/{clienteId}/resgatar/{beneficioId}")
    public ResponseEntity<Map<String, String>> resgatarBeneficio(
            @PathVariable UUID clienteId,
            @PathVariable UUID beneficioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        LocalDate hoje = data != null ? data : LocalDate.now();
        fidelidadeService.resgatarBeneficio(clienteId, beneficioId, hoje);
        return ResponseEntity.ok(Map.of("status", "Benefício resgatado com sucesso"));
    }

    @PostMapping("/{clienteId}/resgatar-produto/{produtoId}")
    public ResponseEntity<Map<String, String>> resgatarProdutoBomboniere(
            @PathVariable UUID clienteId,
            @PathVariable UUID produtoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        LocalDate hoje = data != null ? data : LocalDate.now();
        String voucher = fidelidadeService.resgatarProdutoBomboniere(clienteId, produtoId, hoje);
        return ResponseEntity.ok(Map.of(
                "status", "Produto resgatado com sucesso!",
                "voucher", voucher
        ));
    }

    @GetMapping("/{clienteId}/historico")
    public ResponseEntity<List<ResgateResponse>> consultarHistorico(@PathVariable UUID clienteId) {
        List<RegistroResgate> resgates = fidelidadeService.consultarHistoricoResgates(clienteId);
        List<ResgateResponse> response = resgates.stream()
                .map(r -> new ResgateResponse(
                        r.getBeneficioId(),
                        r.getPontosDebitados(),
                        r.getData().toString(),
                        MARCADOR_COMPRA.equals(r.getBeneficioId())
                                ? "Pontos usados na compra"
                                : "Resgate de recompensa"))
                .toList();
        return ResponseEntity.ok(response);
    }

    record LancamentoResponse(int pontosOriginais, int saldoAtual, String validade,
                               String dataCriacao, String status, String descricao) {}
    record BeneficioResponse(UUID id, String nome, String tipo, int pontosNecessarios,
                              String categoria, Double preco) {}
    record RecompensaResponse(UUID id, String nome, String tipo, int pontosNecessarios,
                               String categoria, Double preco, boolean disponivel) {}
    record ResgateResponse(UUID beneficioId, int pontosDebitados, String data, String descricao) {}
}