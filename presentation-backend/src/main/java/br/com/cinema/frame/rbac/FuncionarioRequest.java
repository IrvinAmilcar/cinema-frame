package br.com.cinema.frame.rbac;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record FuncionarioRequest(
    @JsonProperty("nome") String nome,
    @JsonProperty("email") String email,
    @JsonProperty("role") String role,
    @JsonProperty("ativo") boolean ativo,
    @JsonProperty("permissoes") List<String> permissoes
) {}