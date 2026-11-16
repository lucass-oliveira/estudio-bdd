package br.edu.mackenzie.estudiobdd.controller;

import static br.edu.mackenzie.estudiobdd.service.UsuarioService.CABECALHO;

import java.util.List;

import br.edu.mackenzie.estudiobdd.dto.Dtos.DecisaoDados;
import br.edu.mackenzie.estudiobdd.dto.Dtos.SugestaoDados;
import br.edu.mackenzie.estudiobdd.dto.Dtos.SugestaoResposta;
import br.edu.mackenzie.estudiobdd.model.DecisaoSugestao;
import br.edu.mackenzie.estudiobdd.service.SugestaoService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
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
@RequestMapping("/api")
public class SugestaoController {
    private final SugestaoService sugestoes;

    public SugestaoController(SugestaoService sugestoes) {
        this.sugestoes = sugestoes;
    }

    @GetMapping("/historias/{historiaId}/sugestoes")
    public List<SugestaoResposta> listar(@RequestHeader(name = CABECALHO, required = false) Long usuarioId,
                                          @PathVariable Long historiaId, @RequestParam(required = false) DecisaoSugestao decisao) {
        return sugestoes.listar(usuarioId, historiaId, decisao);
    }

    @PostMapping("/historias/{historiaId}/sugestoes")
    @ResponseStatus(HttpStatus.CREATED)
    public SugestaoResposta cadastrar(@RequestHeader(name = CABECALHO, required = false) Long usuarioId,
                                      @PathVariable Long historiaId, @Valid @RequestBody SugestaoDados dados) {
        return sugestoes.cadastrar(usuarioId, historiaId, dados);
    }

    @PutMapping("/sugestoes/{sugestaoId}/decisao")
    public SugestaoResposta decidir(@RequestHeader(name = CABECALHO, required = false) Long usuarioId,
                                    @PathVariable Long sugestaoId, @Valid @RequestBody DecisaoDados dados) {
        return sugestoes.decidir(usuarioId, sugestaoId, dados);
    }
}
