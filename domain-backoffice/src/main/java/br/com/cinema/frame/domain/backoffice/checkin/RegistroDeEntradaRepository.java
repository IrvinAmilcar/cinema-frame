package br.com.cinema.frame.domain.backoffice.checkin;

import java.util.List;
import java.util.UUID;

public interface RegistroDeEntradaRepository {

    void salvar(RegistroDeEntrada registro);
    List<RegistroDeEntrada> buscarPorSessao(UUID sessaoId);
}