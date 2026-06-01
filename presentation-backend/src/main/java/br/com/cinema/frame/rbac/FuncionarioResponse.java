package br.com.cinema.frame.rbac;

import java.util.List;
import java.util.UUID;

public record FuncionarioResponse(
    UUID id,
    String nome,
    String email,
    String role,
    boolean ativo,
    List<String> permissoes
) {}