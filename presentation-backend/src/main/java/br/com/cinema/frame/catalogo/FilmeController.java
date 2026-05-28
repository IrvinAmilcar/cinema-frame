package br.com.cinema.frame.catalogo;

import br.com.cinema.frame.domain.backoffice.grade.FilmeService;
import br.com.cinema.frame.domain.shared.classificacao.ClassificacaoIndicativa;
import br.com.cinema.frame.domain.shared.filme.GeneroFilme;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/filmes")
public class FilmeController {

    private final FilmeService filmeService;

    public FilmeController(FilmeService filmeService) {
        this.filmeService = filmeService;
    }

    @GetMapping
    public List<FilmeResponse> listarTodos() {
        return filmeService.listarTodos().stream().map(FilmeResponse::from).toList();
    }

    @GetMapping("/{id}")
    public FilmeResponse buscarPorId(@PathVariable UUID id) {
        return FilmeResponse.from(filmeService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmeResponse cadastrar(@RequestBody FilmeRequest req) {
        var filme = new br.com.cinema.frame.domain.backoffice.grade.Filme(
            req.titulo(),
            Duration.ofMinutes(req.duracaoMinutos()),
            ClassificacaoIndicativa.valueOf(req.classificacaoIndicativa()),
            GeneroFilme.valueOf(req.genero())
        );
        if (req.trailerURL() != null)
            filme.atualizar(null, null, null, null, req.trailerURL());
        if (req.sinopse() != null || req.nota() != null)
            filme.definirDetalhes(req.sinopse(), req.nota());
        filmeService.cadastrar(filme);
        return FilmeResponse.from(filme);
    }

    @PutMapping("/{id}")
    public FilmeResponse atualizar(@PathVariable UUID id, @RequestBody FilmeRequest req) {
        var filme = filmeService.atualizar(
            id,
            req.titulo(),
            req.duracaoMinutos() > 0 ? Duration.ofMinutes(req.duracaoMinutos()) : null,
            req.classificacaoIndicativa() != null ? ClassificacaoIndicativa.valueOf(req.classificacaoIndicativa()) : null,
            req.genero() != null ? GeneroFilme.valueOf(req.genero()) : null,
            req.trailerURL(),
            req.sinopse(),
            req.nota()
        );
        return FilmeResponse.from(filme);
    }

    @PatchMapping("/{id}/ativar")
    public void ativar(@PathVariable UUID id) {
        filmeService.ativar(id);
    }

    @PatchMapping("/{id}/desativar")
    public void desativar(@PathVariable UUID id) {
        filmeService.desativar(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable UUID id) {
        filmeService.remover(id);
    }
}
