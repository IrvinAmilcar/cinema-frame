package br.com.cinema.frame.auth;

import br.com.cinema.frame.domain.portal.cliente.Cliente;
import br.com.cinema.frame.domain.portal.cliente.ClienteAuthPort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ClienteAuthPort clienteAuth;

    public AuthController(ClienteAuthPort clienteAuth) {
        this.clienteAuth = clienteAuth;
    }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse cadastrar(@RequestBody CadastroRequest req) {
        if (clienteAuth.emailJaCadastrado(req.email()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");

        Cliente cliente = clienteAuth.cadastrar(req.nome(), req.email(), req.senha(),
                LocalDate.parse(req.dataNascimento()));
        return ClienteResponse.from(cliente);
    }

    @PostMapping("/login")
    public ClienteResponse login(@RequestBody LoginRequest req) {
        return clienteAuth.autenticar(req.email(), req.senha())
                .map(ClienteResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Email ou senha inválidos"));
    }

    @GetMapping("/perfil/{clienteId}")
    public ClienteResponse perfil(@PathVariable UUID clienteId) {
        return clienteAuth.buscarPorId(clienteId)
                .map(ClienteResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Cliente não encontrado"));
    }

    public record CadastroRequest(String nome, String email, String senha, String dataNascimento) {}
    public record LoginRequest(String email, String senha) {}
}
