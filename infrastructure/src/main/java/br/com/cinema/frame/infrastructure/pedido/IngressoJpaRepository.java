package br.com.cinema.frame.infrastructure.pedido;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IngressoJpaRepository extends JpaRepository<IngressoJpa, UUID> {
    List<IngressoJpa> findByPedidoId(UUID pedidoId);
    List<IngressoJpa> findBySessaoId(UUID sessaoId);

    @Transactional
    void deleteByPedidoId(UUID pedidoId);
}
