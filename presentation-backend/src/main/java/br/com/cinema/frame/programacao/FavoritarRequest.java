package br.com.cinema.frame.programacao;

import java.util.UUID;

public record FavoritarRequest(UUID usuarioId, UUID filmeId) {}
