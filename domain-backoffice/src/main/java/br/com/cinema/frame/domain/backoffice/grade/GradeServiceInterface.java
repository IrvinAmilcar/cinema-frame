package br.com.cinema.frame.domain.backoffice.grade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface GradeServiceInterface {
    GradeDeExibicao criarGrade(LocalDate inicio, LocalDate fim);
    UUID adicionarSessao(UUID gradeId, UUID filmeId, UUID salaId, LocalTime inicio);
    GradeDeExibicao buscarPorId(UUID id);
    GradeDeExibicao buscarPorData(LocalDate data);
    ResultadoRemocaoSessao removerSessao(UUID gradeId, UUID sessaoId, LocalDateTime agora);
    void removerGrade(UUID id);
    void atualizarSessao(UUID gradeId, UUID sessaoId, UUID filmeId, UUID salaId, LocalTime novoInicio);
    GradeDeExibicao atualizarGrade(UUID id, LocalDate novoInicio, LocalDate novoFim);
    List<GradeDeExibicao> listarTodas();
}
