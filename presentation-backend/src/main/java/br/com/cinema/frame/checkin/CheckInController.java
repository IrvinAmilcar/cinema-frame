package br.com.cinema.frame.checkin;

import br.com.cinema.frame.domain.backoffice.checkin.CheckInService;
import br.com.cinema.frame.domain.backoffice.checkin.RegistroDeEntrada;
import br.com.cinema.frame.domain.backoffice.checkin.RegistroDeEntradaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    private final CheckInService checkInService;
    private final RegistroDeEntradaRepository registroRepository;
    private final CheckInFalhaService falhaService;

    public CheckInController(CheckInService checkInService,
                             RegistroDeEntradaRepository registroRepository,
                             CheckInFalhaService falhaService) {
        this.checkInService = checkInService;
        this.registroRepository = registroRepository;
        this.falhaService = falhaService;
    }

    @Transactional
    @PostMapping("/validar")
    public ResponseEntity<ResultadoCheckInResponse> validar(@RequestBody ValidarCheckInRequest req) {
        try {
            RegistroDeEntrada registro = checkInService.realizar(
                req.funcionarioId(),
                req.ingressoId(),
                req.sessaoId(),
                LocalDateTime.now()
            );
            return ResponseEntity.ok(
                new ResultadoCheckInResponse(true, "Acesso Autorizado", registro.getId())
            );
        } catch (IllegalArgumentException | IllegalStateException e) {
            falhaService.salvar(req.ingressoId(), req.sessaoId(), e.getMessage());
            return ResponseEntity.ok(
                new ResultadoCheckInResponse(false, e.getMessage(), null)
            );
        }
    }

    @GetMapping("/historico-hoje")
    public ResponseEntity<HistoricoCheckInResponse> historicoHoje(@RequestParam UUID sessaoId) {
        List<RegistroDeEntrada> registros = registroRepository.buscarPorSessao(sessaoId);

        long aprovados = registros.stream().filter(RegistroDeEntrada::isAutorizado).count();
        long negados = registros.size() - aprovados;

        return ResponseEntity.ok(
            new HistoricoCheckInResponse((long) registros.size(), aprovados, negados)
        );
    }
}