package br.com.cinema.frame.caixa;

import br.com.cinema.frame.domain.backoffice.caixa.CaixaService;
import br.com.cinema.frame.domain.backoffice.caixa.FechamentoCaixa;
import br.com.cinema.frame.domain.backoffice.caixa.BombonierePorSessaoItem;
import br.com.cinema.frame.domain.backoffice.caixa.OcupacaoPorSessaoItem;
import br.com.cinema.frame.domain.backoffice.caixa.IngressosPorSessaoItem;
import br.com.cinema.frame.domain.backoffice.caixa.ResumoPeriodoRepository;
import br.com.cinema.frame.domain.backoffice.caixa.VendaDia;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/ocupacao-por-sessao")
    public ResponseEntity<List<OcupacaoPorSessaoResponse>> ocupacaoPorSessao(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        List<OcupacaoPorSessaoResponse> result = resumoRepository.ocupacaoPorSessao(dataInicio, dataFim)
                .stream()
                .map(i -> new OcupacaoPorSessaoResponse(
                        i.sessaoId(), i.filme(), i.horario(),
                        i.salaNumero(), i.salaTipo(),
                        i.capacidade(), i.ingressosVendidos(), i.diasVigencia()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/bomboniere-por-sessao")
    public ResponseEntity<List<BombonierePorSessaoResponse>> bombonierePorSessao(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        List<BombonierePorSessaoResponse> result = resumoRepository.bombonierePorSessao(dataInicio, dataFim)
                .stream()
                .map(i -> new BombonierePorSessaoResponse(
                        i.sessaoId(), i.filme(), i.horario(),
                        i.salaNumero(), i.salaTipo(), i.produto(),
                        i.quantidade(), i.valorTotal()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/ingressos-por-sessao")
    public ResponseEntity<List<IngressosPorSessaoResponse>> ingressosPorSessao(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        List<IngressosPorSessaoResponse> result = resumoRepository.ingressosPorSessao(dataInicio, dataFim)
                .stream()
                .map(i -> new IngressosPorSessaoResponse(
                        i.sessaoId(), i.filme(), i.horario(),
                        i.salaNumero(), i.salaTipo(),
                        i.totalIngressos(), i.totalInteira(), i.valorTotal()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
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