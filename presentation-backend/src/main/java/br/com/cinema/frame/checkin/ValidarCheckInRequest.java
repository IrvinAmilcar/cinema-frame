package br.com.cinema.frame.checkin;

import java.util.UUID;

public record ValidarCheckInRequest(
    UUID ingressoId, 
    UUID sessaoId, 
    UUID funcionarioId
) {}