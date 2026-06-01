package br.com.cinema.frame.infrastructure.rbac;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface FuncionarioJpaRepository extends JpaRepository<FuncionarioEntity, UUID> {
}