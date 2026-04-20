package br.com.cinema.frame.domain.backoffice.grade;

import java.time.LocalDateTime;
import java.util.UUID;

public class CheckIn {

    private static final int MINUTOS_ANTES_PERMITIDOS = 30;

    public void realizar(Ingresso ingresso, UUID sessaoId, LocalDateTime agora) {
        if (ingresso == null)
            throw new IllegalArgumentException("Ingresso não pode ser nulo");

        if (!ingresso.getSessao().getId().equals(sessaoId))
            throw new IllegalStateException("Ingresso não pertence a esta sessão");

        if (ingresso.isUtilizado())
            throw new IllegalStateException("Ingresso já utilizado");

        LocalDateTime inicioSessao = ingresso.getSessao().getInicio();
        LocalDateTime aberturaPortas = inicioSessao.minusMinutes(MINUTOS_ANTES_PERMITIDOS);
        LocalDateTime fechamentoPortas = inicioSessao.plusMinutes(MINUTOS_ANTES_PERMITIDOS);

        if (agora.isBefore(aberturaPortas) || agora.isAfter(fechamentoPortas))
            throw new IllegalStateException("Fora do horário permitido de entrada");

        ingresso.marcarComoUtilizado();
    }
}