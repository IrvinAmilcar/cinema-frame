package br.com.cinema.frame.programacao;

import br.com.cinema.frame.domain.portal.notificacao.NotificacaoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notificacao")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @PostMapping("/favoritar")
    @ResponseStatus(HttpStatus.CREATED)
    public void favoritar(@RequestBody FavoritarRequest req) {
        notificacaoService.favoritarFilme(req.usuarioId(), req.filmeId());
    }

    @GetMapping("/favoritos/{clienteId}")
    public List<UUID> listarFavoritos(@PathVariable UUID clienteId) {
        return notificacaoService.listarFilmesFavoritados(clienteId);
    }

    @DeleteMapping("/favoritos/{clienteId}/{filmeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desfavoritar(@PathVariable UUID clienteId, @PathVariable UUID filmeId) {
        notificacaoService.desfavoritarFilme(clienteId, filmeId);
    }
}
