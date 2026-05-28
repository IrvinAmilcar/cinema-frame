package br.com.cinema.frame.infrastructure.programacao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface FavoritoJpaRepository extends JpaRepository<FavoritoJpa, UUID> {
    List<FavoritoJpa> findByFilmeIdAndNotificadoFalse(UUID filmeId);
    List<FavoritoJpa> findByUsuarioId(UUID usuarioId);
    @Transactional
    void deleteByUsuarioIdAndFilmeId(UUID usuarioId, UUID filmeId);
}
