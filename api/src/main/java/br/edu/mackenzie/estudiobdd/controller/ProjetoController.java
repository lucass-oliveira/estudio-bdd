package br.edu.mackenzie.estudiobdd.controller;

import static br.edu.mackenzie.estudiobdd.service.UsuarioService.CABECALHO;

import java.net.URI;
import java.util.List;

import br.edu.mackenzie.estudiobdd.dto.Dtos.ProjetoDados;
import br.edu.mackenzie.estudiobdd.dto.Dtos.ProjetoDetalhe;
import br.edu.mackenzie.estudiobdd.dto.Dtos.ProjetoResposta;
import br.edu.mackenzie.estudiobdd.dto.Dtos.ResumoProjeto;
import br.edu.mackenzie.estudiobdd.model.Prioridade;
import br.edu.mackenzie.estudiobdd.service.ProjetoService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projetos")
public class ProjetoController {
    private final ProjetoService projetos;

    public ProjetoController(ProjetoService projetos) {
        this.projetos = projetos;
    }

    @GetMapping
    public List<ProjetoResposta> listar(@RequestHeader(name = CABECALHO, required = false) Long usuarioId) {
        return projetos.listar(usuarioId);
    }

    @PostMapping
    public ResponseEntity<ProjetoResposta> cadastrar(@RequestHeader(name = CABECALHO, required = false) Long usuarioId,
                                                     @Valid @RequestBody ProjetoDados dados) {
        ProjetoResposta resposta = projetos.cadastrar(usuarioId, dados);
        return ResponseEntity.created(URI.create("/api/projetos/" + resposta.id())).body(resposta);
    }

    @GetMapping("/{projetoId}")
    public ProjetoDetalhe consultar(@RequestHeader(name = CABECALHO, required = false) Long usuarioId,
                                    @PathVariable Long projetoId,
                                    @RequestParam(required = false) Prioridade prioridade) {
        return projetos.consultar(usuarioId, projetoId, prioridade);
    }

    @PutMapping("/{projetoId}")
    public ProjetoResposta editar(@RequestHeader(name = CABECALHO, required = false) Long usuarioId,
                                   @PathVariable Long projetoId, @Valid @RequestBody ProjetoDados dados) {
        return projetos.editar(usuarioId, projetoId, dados);
    }

    @DeleteMapping("/{projetoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@RequestHeader(name = CABECALHO, required = false) Long usuarioId, @PathVariable Long projetoId) {
        projetos.excluir(usuarioId, projetoId);
    }

    @GetMapping("/{projetoId}/resumo")
    public ResumoProjeto resumir(@RequestHeader(name = CABECALHO, required = false) Long usuarioId,
                                 @PathVariable Long projetoId) {
        return projetos.resumir(usuarioId, projetoId);
    }
}
