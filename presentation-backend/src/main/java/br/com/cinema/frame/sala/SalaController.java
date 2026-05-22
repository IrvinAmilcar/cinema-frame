package br.com.cinema.frame.sala;

import br.com.cinema.frame.domain.backoffice.sala.Sala;
import br.com.cinema.frame.domain.backoffice.sala.SalaService;
import br.com.cinema.frame.domain.backoffice.sala.TipoSala;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/salas")
public class SalaController {

    private final SalaService salaService;

    public SalaController(SalaService salaService) {
        this.salaService = salaService;
    }

    @GetMapping
    public List<SalaResponse> listarTodas() {
        return salaService.listarTodas().stream().map(SalaResponse::from).toList();
    }

    @GetMapping("/{id}")
    public SalaResponse buscarPorId(@PathVariable UUID id) {
        return SalaResponse.from(salaService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SalaResponse cadastrar(@RequestBody SalaRequest req) {
        var sala = new Sala(req.numero(), req.capacidade(), TipoSala.valueOf(req.tipo()));
        salaService.cadastrar(sala);
        return SalaResponse.from(sala);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable UUID id) {
        salaService.remover(id);
    }
}
