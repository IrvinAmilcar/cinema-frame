package br.com.cinema.frame.grade;

import java.time.LocalDate;

public record GradeRequest(
    LocalDate inicio,
    LocalDate fim
) {}
