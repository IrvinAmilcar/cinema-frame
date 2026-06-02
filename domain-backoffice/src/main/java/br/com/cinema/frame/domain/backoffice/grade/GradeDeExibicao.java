package br.com.cinema.frame.domain.backoffice.grade;

import br.com.cinema.frame.domain.backoffice.sala.Sala;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class GradeDeExibicao {

    private UUID id;
    private LocalDate inicio;
    private LocalDate fim;
    private List<Sessao> sessoes;

    public GradeDeExibicao(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null)
            throw new IllegalArgumentException("Período da grade não pode ser nulo");
        if (inicio.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Data de início não pode ser no passado");
        if (fim.isBefore(inicio))
            throw new IllegalArgumentException("Data de fim não pode ser anterior ao início");

        this.id = UUID.randomUUID();
        this.inicio = inicio;
        this.fim = fim;
        this.sessoes = new ArrayList<>();
    }

    public void adicionarSessao(Sessao novaSessao) {
        if (novaSessao == null)
            throw new IllegalArgumentException("Sessão não pode ser nula");

        for (Sessao sessaoExistente : sessoes) {
            if (sessaoExistente.conflitaCom(novaSessao))
                throw new IllegalStateException(
                    "Conflito de horário: a sala já possui uma sessão nesse período"
                );
        }

        sessoes.add(novaSessao);
    }

    public Sessao removerSessao(UUID sessaoId, LocalDateTime agora) {
        if (sessaoId == null)
            throw new IllegalArgumentException("ID da sessão não pode ser nulo");
        if (agora == null)
            throw new IllegalArgumentException("Horário atual não pode ser nulo");

        Sessao sessao = sessoes.stream()
            .filter(s -> s.getId().equals(sessaoId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Sessão não encontrada: " + sessaoId));

        if (!sessao.getInicio().isAfter(agora.toLocalTime()))
            throw new IllegalStateException("Não é possível remover sessão que já foi iniciada ou encerrada");

        sessoes.remove(sessao);
        return sessao;
    }

    public void atualizarSessao(UUID sessaoId, Filme novoFilme, Sala novaSala, LocalTime novoInicio) {
        Sessao sessao = sessoes.stream()
            .filter(s -> s.getId().equals(sessaoId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Sessão não encontrada: " + sessaoId));

        Filme filmeAntigo = sessao.getFilme();
        Sala salaAntiga = sessao.getSala();
        LocalTime inicioAntigo = sessao.getInicio();

        sessao.atualizar(novoFilme, novaSala, novoInicio);

        for (Sessao outra : sessoes) {
            if (outra.getId().equals(sessaoId)) continue;
            if (sessao.conflitaCom(outra)) {
                sessao.atualizar(filmeAntigo, salaAntiga, inicioAntigo);
                throw new IllegalStateException("Conflito de horário: a sala já possui uma sessão nesse período");
            }
        }
    }

    public void atualizarPeriodo(LocalDate novoInicio, LocalDate novoFim) {
        if (novoInicio == null || novoFim == null)
            throw new IllegalArgumentException("Período da grade não pode ser nulo");
        if (novoFim.isBefore(novoInicio))
            throw new IllegalArgumentException("Data de fim não pode ser anterior ao início");
        this.inicio = novoInicio;
        this.fim = novoFim;
    }

    public static GradeDeExibicao reconstituir(UUID id, LocalDate inicio, LocalDate fim, List<Sessao> sessoes) {
        GradeDeExibicao g = new GradeDeExibicao();
        g.id = id;
        g.inicio = inicio;
        g.fim = fim;
        g.sessoes = new ArrayList<>(sessoes);
        return g;
    }

    private GradeDeExibicao() {}

    public UUID getId() { return id; }
    public LocalDate getInicio() { return inicio; }
    public LocalDate getFim() { return fim; }
    public List<Sessao> getSessoes() { return Collections.unmodifiableList(sessoes); }
}