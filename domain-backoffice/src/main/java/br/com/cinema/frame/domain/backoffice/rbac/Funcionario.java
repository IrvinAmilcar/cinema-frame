package br.com.cinema.frame.domain.backoffice.rbac;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

public class Funcionario {

    private final UUID id;
    private String nome;
    private String email;
    private RoleFuncionario role;
    private boolean ativo;
    private Set<Permissao> permissoesEspecificas;

  
    public Funcionario(String nome, String email, RoleFuncionario role) {
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome não pode ser vazio");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email não pode ser vazio");
        if (role == null) throw new IllegalArgumentException("Role não pode ser nula");

        this.id = UUID.randomUUID();
        this.nome = nome;
        this.email = email;
        this.role = role;
        this.ativo = true;
        this.permissoesEspecificas = EnumSet.noneOf(Permissao.class);
    }

    public Funcionario(String nome, String email, RoleFuncionario role, Set<Permissao> permissoesEspecificas) {
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome não pode ser vazio");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email não pode ser vazio");
        if (role == null) throw new IllegalArgumentException("Role não pode ser nula");

        this.id = UUID.randomUUID();
        this.nome = nome;
        this.email = email;
        this.role = role;
        this.ativo = true;
        this.permissoesEspecificas = permissoesEspecificas != null 
                ? permissoesEspecificas 
                : EnumSet.noneOf(Permissao.class);
    }

    public Funcionario(UUID id, String nome, String email, RoleFuncionario role,
                       boolean ativo, Set<Permissao> permissoesEspecificas) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.role = role;
        this.ativo = ativo;
        this.permissoesEspecificas = permissoesEspecificas != null
                ? permissoesEspecificas
                : EnumSet.noneOf(Permissao.class);
    }

    public void atualizar(String nome, String email, RoleFuncionario role,
                          boolean ativo, Set<Permissao> permissoesEspecificas) {
        this.nome = nome;
        this.email = email;
        this.role = role;
        this.ativo = ativo;
        this.permissoesEspecificas = permissoesEspecificas != null
                ? permissoesEspecificas
                : EnumSet.noneOf(Permissao.class);
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public RoleFuncionario getRole() { return role; }
    public boolean isAtivo() { return ativo; }
    public Set<Permissao> getPermissoesEspecificas() { return permissoesEspecificas; }
}