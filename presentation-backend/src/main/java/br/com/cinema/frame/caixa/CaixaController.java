package br.com.cinema.frame.caixa;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.cinema.frame.domain.backoffice.caixa.CaixaService;
import br.com.cinema.frame.domain.backoffice.caixa.FechamentoCaixa;
import br.com.cinema.frame.domain.backoffice.caixa.ResumoPeriodoRepository;
import br.com.cinema.frame.domain.backoffice.caixa.VendaDia;

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
        double totalIngressosValor = resumoRepository.somarValorIngressosPorPeriodo(dataInicio, dataFim);
        double totalBomboniere = resumoRepository.somarVendasBombonierePorPeriodo(dataInicio, dataFim);
        double totalDescontoPontos = resumoRepository.somarDescontosPontosPosPeriodo(dataInicio, dataFim);
        double receitaTotal = totalIngressosValor + totalBomboniere - totalDescontoPontos;

        return ResponseEntity.ok(new ResumoPeriodoResponse(
                totalIngressos,
                totalIngressosValor,
                totalBomboniere,
                totalDescontoPontos,
                receitaTotal
        ));
    }

    @PostMapping("/corrigir-datas")
    public ResponseEntity<String> corrigirDatas() {
        int atualizados = resumoRepository.corrigirDatasSessaoNula();
        return ResponseEntity.ok("Pedidos corrigidos: " + atualizados);
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