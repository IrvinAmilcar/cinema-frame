package br.com.cinema.frame.grade;

import java.time.LocalDateTime;
import java.util.UUID;

public record SessaoRequest(
    UUID filmeId,
    UUID salaId,
    LocalDateTime inicio
) {}
