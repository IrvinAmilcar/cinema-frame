package br.com.cinema.frame.grade;

import java.time.LocalTime;
import java.util.UUID;

public record SessaoRequest(
    UUID filmeId,
    UUID salaId,
    LocalTime inicio
) {}
