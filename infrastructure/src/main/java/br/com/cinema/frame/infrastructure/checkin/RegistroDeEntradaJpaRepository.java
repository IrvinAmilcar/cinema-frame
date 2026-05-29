package br.com.cinema.frame.infrastructure.checkin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RegistroDeEntradaJpaRepository 
        extends JpaRepository<RegistroDeEntradaEntity, UUID> {

    List<RegistroDeEntradaEntity> findBySessaoId(UUID sessaoId);
}