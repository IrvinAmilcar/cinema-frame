package br.com.cinema.frame.application.programacao;

import br.com.cinema.frame.domain.portal.notificacao.NotificacaoService;

import java.util.UUID;

public class FavoritarFilmeUseCase {

    private final NotificacaoService notificacaoService;

    public FavoritarFilmeUseCase(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    public void executar(UUID clienteId, UUID filmeId) {
        notificacaoService.favoritarFilme(clienteId, filmeId);
    }
}
