package br.edu.mackenzie.estudiobdd.controller;

import static br.edu.mackenzie.estudiobdd.service.UsuarioService.CABECALHO;

import java.net.URI;
import java.util.List;

import br.edu.mackenzie.estudiobdd.dto.Dtos.CenarioDados;
import br.edu.mackenzie.estudiobdd.dto.Dtos.CenarioResposta;
import br.edu.mackenzie.estudiobdd.dto.Dtos.PassoDados;
import br.edu.mackenzie.estudiobdd.dto.Dtos.PassoResposta;
import br.edu.mackenzie.estudiobdd.service.CenarioService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CenarioController {
    private final CenarioService cenarios;

    public CenarioController(CenarioService cenarios) {
        this.cenarios = cenarios;
    }

    @GetMapping("/historias/{historiaId}/cenarios")
    public List<CenarioResposta> listar(@RequestHeader(name = CABECALHO, required = false) Long usuarioId,
                                        @PathVariable Long historiaId) {
        return cenarios.listar(usuarioId, historiaId);
    }

    @PostMapping("/historias/{historiaId}/cenarios")
    public ResponseEntity<CenarioResposta> cadastrar(@RequestHeader(name = CABECALHO, required = false) Long usuarioId,
                                                       @PathVariable Long historiaId, @Valid @RequestBody CenarioDados dados) {
        CenarioResposta resposta = cenarios.cadastrar(usuarioId, historiaId, dados);
        return ResponseEntity.created(URI.create("/api/cenarios/" + resposta.id())).body(resposta);
    }

    @GetMapping("/cenarios/{cenarioId}")
    public CenarioResposta consultar(@RequestHeader(name = CABECALHO, required = false) Long usuarioId,
                                     @PathVariable Long cenarioId) {
        return cenarios.consultar(usuarioId, cenarioId);
    }

    @PutMapping("/cenarios/{cenarioId}")
    public CenarioResposta editar(@RequestHeader(name = CABECALHO, required = false) Long usuarioId,
                                  @PathVariable Long cenarioId, @Valid @RequestBody CenarioDados dados) {
        return cenarios.editar(usuarioId, cenarioId, dados);
    }

    @DeleteMapping("/cenarios/{cenarioId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@RequestHeader(name = CABECALHO, required = false) Long usuarioId, @PathVariable Long cenarioId) {
        cenarios.excluir(usuarioId, cenarioId);
    }

    @GetMapping("/cenarios/{cenarioId}/passos")
    public List<PassoResposta> listarPassos(@RequestHeader(name = CABECALHO, required = false) Long usuarioId,
                                            @PathVariable Long cenarioId) {
        return cenarios.listarPassos(usuarioId, cenarioId);
    }

    @GetMapping("/cenarios/{cenarioId}/passos/{passoId}")
    public PassoResposta consultarPasso(@RequestHeader(name = CABECALHO, required = false) Long usuarioId,
                                        @PathVariable Long cenarioId, @PathVariable Long passoId) {
        return cenarios.consultarPasso(usuarioId, cenarioId, passoId);
    }

    @PostMapping("/cenarios/{cenarioId}/passos")
    public ResponseEntity<PassoResposta> cadastrarPasso(@RequestHeader(name = CABECALHO, required = false) Long usuarioId,
                                                          @PathVariable Long cenarioId, @Valid @RequestBody PassoDados dados) {
        PassoResposta resposta = cenarios.cadastrarPasso(usuarioId, cenarioId, dados);
        URI local = URI.create("/api/cenarios/" + cenarioId + "/passos/" + resposta.id());
        return ResponseEntity.created(local).body(resposta);
    }

    @PutMapping("/cenarios/{cenarioId}/passos/{passoId}")
    public PassoResposta editarPasso(@RequestHeader(name = CABECALHO, required = false) Long usuarioId,
                                    @PathVariable Long cenarioId, @PathVariable Long passoId,
                                    @Valid @RequestBody PassoDados dados) {
        return cenarios.editarPasso(usuarioId, cenarioId, passoId, dados);
    }

    @DeleteMapping("/cenarios/{cenarioId}/passos/{passoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirPasso(@RequestHeader(name = CABECALHO, required = false) Long usuarioId,
                             @PathVariable Long cenarioId, @PathVariable Long passoId) {
        cenarios.excluirPasso(usuarioId, cenarioId, passoId);
    }
}
