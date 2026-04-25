package br.com.cinema.frame.domain.backoffice.sala;

import java.util.List;
import java.util.UUID;

public class SalaService {

    private final SalaRepository salaRepository;

    public SalaService(SalaRepository salaRepository) {
        if (salaRepository == null)
            throw new IllegalArgumentException("SalaRepository não pode ser nulo");
        this.salaRepository = salaRepository;
    }

    public void cadastrar(Sala sala) {
        if (sala == null)
            throw new IllegalArgumentException("Sala não pode ser nula");
        salaRepository.salvar(sala);
    }

    public Sala buscarPorId(UUID id) {
        return salaRepository.buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("Sala não encontrada: " + id));
    }

    public Sala buscarPorNumero(int numero) {
        return salaRepository.buscarPorNumero(numero)
            .orElseThrow(() -> new IllegalArgumentException("Sala não encontrada: " + numero));
    }

    public List<Sala> listarTodas() {
        return salaRepository.listarTodas();
    }

    public void remover(UUID id) {
        salaRepository.buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("Sala não encontrada: " + id));
        salaRepository.remover(id);
    }
}