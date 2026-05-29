package br.com.cinema.frame.checkin;

public record HistoricoCheckInResponse(
    long total,
    long aprovados,
    long negados
) {}