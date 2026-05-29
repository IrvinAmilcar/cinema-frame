package br.com.cinema.frame.infrastructure.pedido;

import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IngressoJpaRepository extends JpaRepository<IngressoJpa, UUID> {
    List<IngressoJpa> findByPedidoId(UUID pedidoId);
    List<IngressoJpa> findBySessaoId(UUID sessaoId);

    @Transactional
    void deleteByPedidoId(UUID pedidoId);

    // Impede que duas leituras do mesmo ingresso ocorram no mesmo milissegundo
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM IngressoJpa i WHERE i.id = :id")
    Optional<IngressoJpa> findByIdWithLock(@Param("id") UUID id);
}

