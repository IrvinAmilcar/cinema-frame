package br.com.cinema.frame.grade;

import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicao;
import br.com.cinema.frame.domain.backoffice.grade.GradeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/grades")
public class GradeController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @GetMapping
    public List<GradeResponse> listarTodas() {
        return gradeService.listarTodas().stream().map(GradeResponse::from).toList();
    }

    @GetMapping("/data")
    public GradeResponse buscarPorData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return GradeResponse.from(gradeService.buscarPorData(data));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GradeResponse criar(@RequestBody GradeRequest req) {
        return GradeResponse.from(gradeService.criarGrade(req.inicio(), req.fim()));
    }

    @PostMapping("/{gradeId}/sessoes")
    @ResponseStatus(HttpStatus.CREATED)
    public GradeResponse adicionarSessao(@PathVariable UUID gradeId,
                                         @RequestBody SessaoRequest req) {
        gradeService.adicionarSessao(gradeId, req.filmeId(), req.salaId(), req.inicio());
        return GradeResponse.from(gradeService.buscarPorId(gradeId));
    }

    @DeleteMapping("/{gradeId}/sessoes/{sessaoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerSessao(@PathVariable UUID gradeId, @PathVariable UUID sessaoId) {
        gradeService.removerSessao(gradeId, sessaoId, LocalDateTime.now());
    }

    @DeleteMapping("/{gradeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerGrade(@PathVariable UUID gradeId) {
        gradeService.removerGrade(gradeId);
    }
}
