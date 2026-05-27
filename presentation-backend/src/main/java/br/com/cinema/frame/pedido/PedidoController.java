package br.com.cinema.frame.pedido;

import br.com.cinema.frame.domain.backoffice.bomboniere.BombonieresService;
import br.com.cinema.frame.domain.portal.pedido.Pedido;
import br.com.cinema.frame.domain.portal.pedido.PedidoService;
import br.com.cinema.frame.domain.portal.pedido.ResultadoDoPedido;
import br.com.cinema.frame.domain.portal.pedido.StatusPagamento;
import br.com.cinema.frame.domain.portal.promocao.*;
import br.com.cinema.frame.domain.portal.reserva.ReservaDeAssento;
import br.com.cinema.frame.domain.portal.reserva.ReservaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@RestController
public class PedidoController {

    private final PedidoService pedidoService;
    private final ReservaService reservaService;
    private final CupomRepository cupomRepository;
    private final BombonieresService bombonieresService;

    // Strategy pattern: lista de estratégias de desconto disponíveis
    private final List<DescontoStrategy> estrategias = List.of(
            new DescontoLeve2Pague1(),
            new DescontoParceriaCartao(),
            new DescontoEstudante()
    );

    public PedidoController(PedidoService pedidoService,
                             ReservaService reservaService,
                             CupomRepository cupomRepository,
                             BombonieresService bombonieresService) {
        this.pedidoService = pedidoService;
        this.reservaService = reservaService;
        this.cupomRepository = cupomRepository;
        this.bombonieresService = bombonieresService;
    }

    @PostMapping("/api/reserva")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponse reservar(@RequestBody ReservaRequest req) {
        ReservaDeAssento reserva = reservaService.reservar(
                req.sessaoId(), req.numeroAssento(), LocalDateTime.now());
        return ReservaResponse.from(reserva);
    }

    @PostMapping("/api/pedido")
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoIniciadoResponse iniciar(@RequestBody IniciarPedidoRequest req) {
        Pedido pedido = pedidoService.iniciar(req.sessaoId(), req.clienteId());
        return new PedidoIniciadoResponse(pedido.getId());
    }

    @PostMapping("/api/pedido/{id}/ingresso")
    public void adicionarIngresso(@PathVariable UUID id,
                                  @RequestBody AdicionarIngressoRequest req) {
        LocalDate dataNasc = null;
        if (req.dataNascimento() != null && !req.dataNascimento().isBlank()) {
            try { dataNasc = LocalDate.parse(req.dataNascimento()); }
            catch (DateTimeParseException ignored) {}
        }
        int qtd = (req.quantidade() != null && req.quantidade() > 0) ? req.quantidade() : 1;
        for (int i = 0; i < qtd; i++) {
            // RN 3: passa elegibilidade real; RN 4: passa data de nascimento para validar classificação
            pedidoService.adicionarIngresso(id, req.tipo(), req.possuiElegibilidade(), dataNasc);
        }
    }

    @PostMapping("/api/pedido/{id}/cupom")
    public DescontoResponse aplicarCupom(@PathVariable UUID id,
                                          @RequestBody AplicarCupomRequest req) {
        Cupom cupom = cupomRepository.buscarPorCodigo(req.codigo())
                .orElseThrow(() -> new IllegalArgumentException("Cupom não encontrado: " + req.codigo()));

        // RN 9: valida prazo de validade do cupom
        if (!cupom.estaValido(LocalDate.now()))
            throw new IllegalArgumentException("Cupom expirado: " + req.codigo());

        // Strategy pattern: seleciona a estratégia correta pelo tipo do cupom
        DescontoStrategy estrategia = estrategias.stream()
                .filter(e -> e.getTipo() == cupom.getTipo())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de desconto não suportado"));

        AplicacaoDeDesconto desconto = estrategia.aplicar(req.valorTotal(), req.quantidadeIngressos());
        return DescontoResponse.from(desconto, cupom.isCumulativo());
    }

    @GetMapping("/api/sessao/{sessaoId}/assentos-ocupados")
    public List<Integer> listarAssentosOcupados(@PathVariable UUID sessaoId) {
        return reservaService.listarAssentosOcupados(sessaoId, LocalDateTime.now());
    }

    @PatchMapping("/api/pedido/{id}/reserva")
    public void vincularReserva(@PathVariable UUID id, @RequestBody VincularReservaRequest req) {
        pedidoService.vincularReserva(id, req.reservaId());
    }

    @PostMapping("/api/pedido/{id}/produto")
    public VoucherResponse adicionarProduto(@PathVariable UUID id,
                                             @RequestBody AdicionarProdutoRequest req) {
        // RN 7: vende imediatamente (verifica e debita estoque atomicamente)
        // Produtos não são persistidos no PedidoJpa, então o voucher é gerado aqui
        int qtd = Math.max(1, req.quantidade());
        for (int i = 0; i < qtd; i++) {
            bombonieresService.vender(req.produtoId());
        }
        // Código determinístico: VCH-{pedidoId} — mesmo algoritmo do domínio Voucher
        return new VoucherResponse("VCH-" + id.toString().toUpperCase());
    }

    public record VoucherResponse(String voucher) {}

    @PostMapping("/api/pedido/{id}/finalizar")
    public ResultadoPedidoResponse finalizar(@PathVariable UUID id) {
        ResultadoDoPedido resultado = pedidoService.finalizar(
                id, StatusPagamento.APROVADO, 0.0, LocalDateTime.now());
        return ResultadoPedidoResponse.from(resultado);
    }

    public record PedidoIniciadoResponse(UUID pedidoId) {}
}
