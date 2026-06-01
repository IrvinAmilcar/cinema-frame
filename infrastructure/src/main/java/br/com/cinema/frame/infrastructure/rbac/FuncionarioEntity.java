package br.com.cinema.frame.infrastructure.rbac;

import br.com.cinema.frame.domain.backoffice.rbac.Permissao;
import br.com.cinema.frame.domain.backoffice.rbac.RoleFuncionario;
import jakarta.persistence.*;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "funcionarios")
public class FuncionarioEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleFuncionario role;

    @Column(nullable = false)
    private boolean ativo;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "funcionario_permissoes",
        joinColumns = @JoinColumn(name = "funcionario_id")
    )
    @Column(name = "permissao")
    @Enumerated(EnumType.STRING)
    private Set<Permissao> permissoes;

    // Getters e setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public RoleFuncionario getRole() { return role; }
    public void setRole(RoleFuncionario role) { this.role = role; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public Set<Permissao> getPermissoes() { return permissoes; }
    public void setPermissoes(Set<Permissao> permissoes) { this.permissoes = permissoes; }
}