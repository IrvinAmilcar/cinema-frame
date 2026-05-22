package br.com.cinema.frame.infrastructure.grade;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SessaoJpaRepository extends JpaRepository<SessaoJpa, UUID> {

    List<SessaoJpa> findByGradeId(UUID gradeId);
    List<SessaoJpa> findByFilmeId(UUID filmeId);
    List<SessaoJpa> findBySalaId(UUID salaId);
    void deleteByGradeId(UUID gradeId);
}
