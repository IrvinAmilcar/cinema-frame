package br.com.cinema.frame.pedido;

import br.com.cinema.frame.domain.backoffice.bomboniere.BombonieresService;
import br.com.cinema.frame.domain.portal.pedido.Pedido;
import br.com.cinema.frame.domain.portal.pedido.PedidoService;
import br.com.cinema.frame.domain.portal.pedido.ResultadoDoPedido;
import br.com.cinema.frame.domain.portal.pedido.StatusPagamento;
import br.com.cinema.frame.domain.portal.promocao.*;
import br.com.cinema.frame.domain.portal.reserva.ReservaDeAssento;
import br.com.cinema.frame.domain.portal.reserva.ReservaService;
import br.com.cinema.frame.domain.portal.pedido.PedidoRepository;
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
    private final PedidoRepository pedidoRepository;
    private final MotorDePromocoes motorDePromocoes;

    public PedidoController(PedidoService pedidoService,
                             ReservaService reservaService,
                             CupomRepository cupomRepository,
                             BombonieresService bombonieresService,
                             PedidoRepository pedidoRepository,
                             MotorDePromocoes motorDePromocoes) {
        this.pedidoService = pedidoService;
        this.reservaService = reservaService;
        this.cupomRepository = cupomRepository;
        this.bombonieresService = bombonieresService;
        this.pedidoRepository = pedidoRepository;
        this.motorDePromocoes = motorDePromocoes;
    }

    @PostMapping("/api/reserva")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponse reservar(@RequestBody ReservaRequest req) {
        LocalDate dataOcorrencia = (req.data() != null && !req.data().isBlank())
                ? LocalDate.parse(req.data()) : LocalDate.now();
        ReservaDeAssento reserva = reservaService.reservar(
                req.sessaoId(), req.numeroAssento(), LocalDateTime.now(), dataOcorrencia);
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

        // Delega ao MotorDePromocoes que usa o padrão Strategy internamente
        AplicacaoDeDesconto desconto = motorDePromocoes.aplicar(
                cupom, req.valorTotal(), req.quantidadeIngressos(), LocalDate.now());
        return DescontoResponse.from(desconto, cupom.isCumulativo());
    }

    @GetMapping("/api/sessao/{sessaoId}/assentos-ocupados")
    public List<Integer> listarAssentosOcupados(@PathVariable UUID sessaoId,
                                                 @RequestParam(required = false) String data) {
        LocalDate dataOcorrencia = (data != null && !data.isBlank())
                ? LocalDate.parse(data) : LocalDate.now();
        return reservaService.listarAssentosOcupados(sessaoId, LocalDateTime.now(), dataOcorrencia);
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

    public record IngressoClienteResponse(
            UUID ingressoId, String qrCode, String filmeTitulo,
            String dataSessao, String horarioInicio, String horarioFim,
            int salaNumero, String tipoSala, String tipoIngresso
    ) {}

    @GetMapping("/api/cliente/{clienteId}/ingressos")
    public List<IngressoClienteResponse> listarIngressos(@PathVariable UUID clienteId) {
        LocalDate hoje = LocalDate.now();
        return pedidoRepository.buscarFinalizadosPorClienteAPartirDe(clienteId, hoje).stream()
                .flatMap(pedido -> pedido.getIngressos().stream().map(ingresso -> {
                    var sessao = ingresso.getSessao();
                    var filme  = sessao.getFilme();
                    var sala   = sessao.getSala();
                    return new IngressoClienteResponse(
                            ingresso.getId(),
                            ingresso.getId().toString(),
                            filme.getTitulo(),
                            hoje.toString(),
                            sessao.getInicio().toString(),
                            sessao.getFim().toString(),
                            sala.getNumero(),
                            sala.getTipo().name(),
                            ingresso.getTipo().name()
                    );
                }))
                .toList();
    }
}
