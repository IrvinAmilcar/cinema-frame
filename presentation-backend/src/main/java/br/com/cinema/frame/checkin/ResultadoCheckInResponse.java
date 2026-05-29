package br.com.cinema.frame.checkin;

import java.util.UUID;

public record ResultadoCheckInResponse(
    boolean sucesso,
    String mensagem,
    UUID registroId
) {}