package br.com.cinema.frame.infrastructure.grade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface GradeJpaRepository extends JpaRepository<GradeJpa, UUID> {

    @Query("SELECT g FROM GradeJpa g WHERE :data BETWEEN g.inicio AND g.fim")
    Optional<GradeJpa> findByData(@Param("data") LocalDate data);
}
