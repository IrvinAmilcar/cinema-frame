package br.com.cinema.frame.domain.portal.reserva;

import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicaoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final GradeDeExibicaoRepository gradeRepository;

    public ReservaService(ReservaRepository reservaRepository,
                          GradeDeExibicaoRepository gradeRepository) {
        if (reservaRepository == null)
            throw new IllegalArgumentException("ReservaRepository não pode ser nulo");
        if (gradeRepository == null)
            throw new IllegalArgumentException("GradeDeExibicaoRepository não pode ser nulo");

        this.reservaRepository = reservaRepository;
        this.gradeRepository = gradeRepository;
    }

    public ReservaDeAssento reservar(UUID sessaoId, int numeroAssento, LocalDateTime agora) {
        return reservar(sessaoId, numeroAssento, agora, agora.toLocalDate());
    }

    public ReservaDeAssento reservar(UUID sessaoId, int numeroAssento, LocalDateTime agora, LocalDate dataOcorrencia) {
        if (sessaoId == null)
            throw new IllegalArgumentException("ID da sessão não pode ser nulo");
        if (agora == null)
            throw new IllegalArgumentException("Horário não pode ser nulo");

        Sessao sessao = buscarSessaoPorId(sessaoId);

        liberarExpiradas(sessaoId, agora);

        boolean assentoOcupado = reservaRepository.buscarPorSessaoId(sessaoId).stream()
            .anyMatch(r -> r.getNumeroAssento() == numeroAssento
                    && r.estaOcupado()
                    && r.getDataOcorrencia().equals(dataOcorrencia));

        if (assentoOcupado)
            throw new IllegalStateException("Assento " + numeroAssento + " já está reservado para esta sessão");

        ReservaDeAssento reserva = new ReservaDeAssento(sessao, numeroAssento, agora, dataOcorrencia);
        reservaRepository.salvar(reserva);
        return reserva;
    }

    public void confirmar(UUID reservaId, LocalDateTime agora) {
        if (reservaId == null)
            throw new IllegalArgumentException("ID da reserva não pode ser nulo");
        if (agora == null)
            throw new IllegalArgumentException("Horário não pode ser nulo");

        ReservaDeAssento reserva = reservaRepository.buscarPorId(reservaId)
            .orElseThrow(() -> new IllegalArgumentException("Reserva não encontrada: " + reservaId));

        reserva.confirmar(agora);
        reservaRepository.salvar(reserva);
    }

    public List<Integer> listarAssentosOcupados(UUID sessaoId, LocalDateTime agora) {
        return listarAssentosOcupados(sessaoId, agora, agora.toLocalDate());
    }

    public List<Integer> listarAssentosOcupados(UUID sessaoId, LocalDateTime agora, LocalDate dataOcorrencia) {
        liberarExpiradas(sessaoId, agora);
        return reservaRepository.buscarPorSessaoId(sessaoId).stream()
                .filter(ReservaDeAssento::estaOcupado)
                .filter(r -> r.getDataOcorrencia().equals(dataOcorrencia))
                .map(ReservaDeAssento::getNumeroAssento)
                .toList();
    }

    public void liberarExpiradas(UUID sessaoId, LocalDateTime agora) {
        reservaRepository.buscarPorSessaoId(sessaoId).stream()
            .filter(r -> r.estaAtiva() && r.estaExpirada(agora))
            .forEach(r -> {
                r.expirar();
                reservaRepository.salvar(r);
            });
    }

    private Sessao buscarSessaoPorId(UUID sessaoId) {
        return gradeRepository.listarTodas().stream()
            .flatMap(g -> g.getSessoes().stream())
            .filter(s -> s.getId().equals(sessaoId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Sessão não encontrada: " + sessaoId));
    }
}