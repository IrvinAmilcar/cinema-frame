package br.com.cinema.frame.checkin;

import br.com.cinema.frame.domain.backoffice.checkin.RegistroDeEntrada;
import br.com.cinema.frame.domain.backoffice.checkin.RegistroDeEntradaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CheckInFalhaService {

    private final RegistroDeEntradaRepository registroRepository;

    public CheckInFalhaService(RegistroDeEntradaRepository registroRepository) {
        this.registroRepository = registroRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void salvar(UUID ingressoId, UUID sessaoId, String motivo) {
        RegistroDeEntrada falha = new RegistroDeEntrada(
            ingressoId, sessaoId, LocalDateTime.now(), false, motivo
        );
        registroRepository.salvar(falha);
    }
}