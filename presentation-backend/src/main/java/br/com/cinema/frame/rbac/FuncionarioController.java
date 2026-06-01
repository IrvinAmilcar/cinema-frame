package br.com.cinema.frame.rbac;

import br.com.cinema.frame.domain.backoffice.rbac.Funcionario;
import br.com.cinema.frame.domain.backoffice.rbac.FuncionarioRepository;
import br.com.cinema.frame.domain.backoffice.rbac.Permissao;
import br.com.cinema.frame.domain.backoffice.rbac.RoleFuncionario;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/funcionarios")
@CrossOrigin(origins = "*")
public class FuncionarioController {

    private final FuncionarioRepository funcionarioRepository;

    public FuncionarioController(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }

    @GetMapping
    public ResponseEntity<List<FuncionarioResponse>> listarTodos() {
        List<FuncionarioResponse> lista = funcionarioRepository.listarTodos()
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(lista); // Retorna 200 OK
    }

    @GetMapping("/{id}")
    public ResponseEntity<FuncionarioResponse> buscarPorId(@PathVariable UUID id) {
        return funcionarioRepository.buscarPorId(id)
                .map(f -> ResponseEntity.ok(toResponse(f))) // Retorna 200 OK se achar
                .orElseGet(() -> ResponseEntity.notFound().build()); // Retorna 404 Not Found se não achar
    }

    @PostMapping
    public ResponseEntity<FuncionarioResponse> cadastrar(@RequestBody FuncionarioRequest request) {
        Funcionario funcionario = new Funcionario(
                request.nome(),
                request.email(),
                RoleFuncionario.valueOf(request.role())
        );
        Set<Permissao> permissoes = parsePermissoes(request.permissoes());
        funcionario.atualizar(
                funcionario.getNome(),
                funcionario.getEmail(),
                funcionario.getRole(),
                funcionario.isAtivo(),
                permissoes
        );
        funcionarioRepository.salvar(funcionario);
        
        // Retorna 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(funcionario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FuncionarioResponse> atualizar(@PathVariable UUID id,
                                          @RequestBody FuncionarioRequest request) {
        return funcionarioRepository.buscarPorId(id).map(funcionario -> {
            funcionario.atualizar(
                    request.nome(),
                    request.email(),
                    RoleFuncionario.valueOf(request.role()),
                    request.ativo(),
                    parsePermissoes(request.permissoes())
            );
            funcionarioRepository.salvar(funcionario);
            return ResponseEntity.ok(toResponse(funcionario)); // Retorna 200 OK
        }).orElseGet(() -> ResponseEntity.notFound().build()); // Retorna 404 Not Found
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        if (funcionarioRepository.buscarPorId(id).isPresent()) {
            funcionarioRepository.deletar(id);
            return ResponseEntity.noContent().build(); // Retorna 204 No Content (sucesso sem corpo de resposta)
        }
        return ResponseEntity.notFound().build(); // Retorna 404 Not Found
    }

    // -------------------------------------------------------

    private Set<Permissao> parsePermissoes(List<String> permissoes) {
        if (permissoes == null || permissoes.isEmpty())
            return EnumSet.noneOf(Permissao.class);
        return permissoes.stream()
                .map(Permissao::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Permissao.class)));
    }

    private FuncionarioResponse toResponse(Funcionario f) {
        return new FuncionarioResponse(
                f.getId(),
                f.getNome(),
                f.getEmail(),
                f.getRole().name(),
                f.isAtivo(),
                f.getPermissoesEspecificas()
                        .stream()
                        .map(Permissao::name)
                        .toList()
        );
    }
}