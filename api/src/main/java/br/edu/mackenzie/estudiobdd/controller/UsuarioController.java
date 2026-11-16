package br.edu.mackenzie.estudiobdd.controller;

import java.util.List;

import br.edu.mackenzie.estudiobdd.dto.Dtos.UsuarioDados;
import br.edu.mackenzie.estudiobdd.dto.Dtos.UsuarioResposta;
import br.edu.mackenzie.estudiobdd.service.UsuarioService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService usuarios;

    public UsuarioController(UsuarioService usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping
    public List<UsuarioResposta> listar() {
        return usuarios.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResposta cadastrar(@Valid @RequestBody UsuarioDados dados) {
        return usuarios.cadastrar(dados);
    }
}
