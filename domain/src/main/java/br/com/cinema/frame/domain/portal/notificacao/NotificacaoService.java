package br.com.cinema.frame.domain.portal.notificacao;

import br.com.cinema.frame.domain.backoffice.grade.Filme;
import br.com.cinema.frame.domain.backoffice.grade.FilmeRepository;
import br.com.cinema.frame.domain.backoffice.grade.SessaoRepository;
import br.com.cinema.frame.domain.backoffice.grade.Sessao;

import java.util.List;
import java.util.UUID;

public class NotificacaoService {

    private final FilmeFavoritadoRepository filmeFavoritadoRepository;
    private final FilmeRepository filmeRepository;
    private final SessaoRepository sessaoRepository;

    public NotificacaoService(FilmeFavoritadoRepository filmeFavoritadoRepository,
                               FilmeRepository filmeRepository,
                               SessaoRepository sessaoRepository) {
        if (filmeFavoritadoRepository == null)
            throw new IllegalArgumentException("FilmeFavoritadoRepository não pode ser nulo");
        if (filmeRepository == null)
            throw new IllegalArgumentException("FilmeRepository não pode ser nulo");
        if (sessaoRepository == null)
            throw new IllegalArgumentException("SessaoRepository não pode ser nulo");

        this.filmeFavoritadoRepository = filmeFavoritadoRepository;
        this.filmeRepository = filmeRepository;
        this.sessaoRepository = sessaoRepository;
    }

    public void favoritarFilme(UUID usuarioId, UUID filmeId) {
        if (usuarioId == null)
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        if (filmeId == null)
            throw new IllegalArgumentException("ID do filme não pode ser nulo");

        filmeRepository.buscarPorId(filmeId)
            .orElseThrow(() -> new IllegalArgumentException("Filme não encontrado: " + filmeId));

        FilmeFavoritado favoritado = new FilmeFavoritado(usuarioId, filmeId);
        filmeFavoritadoRepository.salvar(favoritado);
    }

    public List<FilmeFavoritado> notificarFavoritosParaSessao(UUID sessaoId) {
        if (sessaoId == null)
            throw new IllegalArgumentException("ID da sessão não pode ser nulo");

        Sessao sessao = sessaoRepository.buscarPorId(sessaoId)
            .orElseThrow(() -> new IllegalArgumentException("Sessão não encontrada: " + sessaoId));

        UUID filmeId = sessao.getFilme().getId();

        List<FilmeFavoritado> naoNotificados =
            filmeFavoritadoRepository.buscarNaoNotificadosPorFilme(filmeId);

        for (FilmeFavoritado favorito : naoNotificados) {
            favorito.marcarComoNotificado();
            filmeFavoritadoRepository.salvar(favorito);
        }

        return naoNotificados;
    }
}