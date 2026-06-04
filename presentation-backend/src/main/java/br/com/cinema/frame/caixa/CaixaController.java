package br.com.cinema.frame.caixa;

import br.com.cinema.frame.domain.backoffice.caixa.CaixaService;
import br.com.cinema.frame.domain.backoffice.caixa.FechamentoCaixa;
import br.com.cinema.frame.domain.backoffice.caixa.ResumoPeriodoRepository;
import br.com.cinema.frame.domain.backoffice.caixa.VendaDia;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/caixa")
@CrossOrigin(origins = "*")
public class CaixaController {

    private final CaixaService caixaService;
    private final ResumoPeriodoRepository resumoRepository;

    public CaixaController(CaixaService caixaService, ResumoPeriodoRepository resumoRepository) {
        this.caixaService = caixaService;
        this.resumoRepository = resumoRepository;
    }

    @PostMapping("/fechar")
    public ResponseEntity<FechamentoCaixaResponse> fecharCaixa(@RequestBody FecharCaixaRequest request) {
        List<VendaDia> vendas = request.vendas().stream()
                .map(v -> new VendaDia(
                        v.sessaoId(),
                        v.capacidadeSala(),
                        v.ingressosVendidos(),
                        v.valorArrecadado()
                ))
                .toList();

        FechamentoCaixa fechamento = caixaService.fecharCaixa(
                request.data(),
                vendas,
                request.momentoFechamento()
        );

        return ResponseEntity.ok(toResponse(fechamento));
    }

    @GetMapping("/relatorio")
    public ResponseEntity<FechamentoCaixaResponse> consultarRelatorio(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        FechamentoCaixa fechamento = caixaService.consultarRelatorio(data);
        return ResponseEntity.ok(toResponse(fechamento));
    }

    @GetMapping("/resumo")
    public ResponseEntity<ResumoPeriodoResponse> resumoPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        int totalIngressos = resumoRepository.contarIngressosPorPeriodo(dataInicio, dataFim);
        double totalBomboniere = resumoRepository.somarVendasBombonierePorPeriodo(dataInicio, dataFim);
        double totalDescontoPontos = resumoRepository.somarDescontosPontosPosPeriodo(dataInicio, dataFim);
        double totalIngressosValor = 0.0;

        return ResponseEntity.ok(new ResumoPeriodoResponse(
                totalIngressos,
                totalIngressosValor,
                totalBomboniere,
                totalDescontoPontos,
                totalIngressosValor + totalBomboniere
        ));
    }

    private FechamentoCaixaResponse toResponse(FechamentoCaixa f) {
        return new FechamentoCaixaResponse(
                f.getId(),
                f.getData(),
                f.getTotalVendas(),
                f.getTotalIngressos(),
                f.getTotalSessoes(),
                f.getTaxaOcupacaoMedia(),
                f.getMomentoFechamento()
        );
    }
}