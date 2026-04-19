package br.com.cinema.frame.domain.backoffice.grade;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalaRepository {

    void salvar(Sala sala);
    Optional<Sala> buscarPorId(UUID id);
    Optional<Sala> buscarPorNumero(int numero);
    List<Sala> listarTodas();
    void remover(UUID id);
}