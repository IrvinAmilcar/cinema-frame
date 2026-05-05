package br.com.cinema.frame.domain.portal.fidelidade;

import java.util.List;
import java.util.UUID;

public interface RegistroResgateRepository {
    void salvar(UUID clienteId, RegistroResgate registro);
    List<RegistroResgate> buscarPorClienteEMes(UUID clienteId, int mes, int ano);
    List<RegistroResgate> buscarPorCliente(UUID clienteId);
}
