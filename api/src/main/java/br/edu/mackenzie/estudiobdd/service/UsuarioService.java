package br.edu.mackenzie.estudiobdd.service;

import java.util.List;
import java.util.Locale;

import br.edu.mackenzie.estudiobdd.dto.Dtos.UsuarioDados;
import br.edu.mackenzie.estudiobdd.dto.Dtos.UsuarioResposta;
import br.edu.mackenzie.estudiobdd.dto.Respostas;
import br.edu.mackenzie.estudiobdd.model.Usuario;
import br.edu.mackenzie.estudiobdd.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UsuarioService {
    public static final String CABECALHO = "X-Usuario-Id";

    private final UsuarioRepository usuarios;

    public UsuarioService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public List<UsuarioResposta> listar() {
        return usuarios.findAllByOrderByNomeAsc().stream().map(Respostas::usuario).toList();
    }

    @Transactional
    public UsuarioResposta cadastrar(UsuarioDados dados) {
        String email = dados.email().strip().toLowerCase(Locale.ROOT);
        if (usuarios.findByEmail(email).isPresent()) {
            throw new RegraNegocioException(HttpStatus.CONFLICT, "Já existe um usuário com este e-mail.");
        }
        return Respostas.usuario(usuarios.save(new Usuario(dados.nome().strip(), email)));
    }

    Usuario exigirUsuario(Long id) {
        if (id == null || id <= 0) {
            throw new RegraNegocioException(HttpStatus.UNAUTHORIZED, "Selecione um usuário de demonstração para entrar.");
        }
        return usuarios.findById(id)
                .orElseThrow(() -> new RegraNegocioException(HttpStatus.UNAUTHORIZED, "Usuário de demonstração inválido."));
    }
}
