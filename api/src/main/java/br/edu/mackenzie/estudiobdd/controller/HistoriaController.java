package br.edu.mackenzie.estudiobdd.controller;

import static br.edu.mackenzie.estudiobdd.service.UsuarioService.CABECALHO;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import br.edu.mackenzie.estudiobdd.dto.Dtos.ArquivoFeature;
import br.edu.mackenzie.estudiobdd.dto.Dtos.HistoriaDados;
import br.edu.mackenzie.estudiobdd.dto.Dtos.HistoriaDetalhe;
import br.edu.mackenzie.estudiobdd.service.HistoriaService;
import jakarta.validation.Valid;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
public class HistoriaController {
    private final HistoriaService historias;

    public HistoriaController(HistoriaService historias) {
        this.historias = historias;
    }

    @PostMapping("/projetos/{projetoId}/historias")
    public ResponseEntity<HistoriaDetalhe> cadastrar(@RequestHeader(name = CABECALHO, required = false) Long usuarioId,
                                                      @PathVariable Long projetoId, @Valid @RequestBody HistoriaDados dados) {
        HistoriaDetalhe resposta = historias.cadastrar(usuarioId, projetoId, dados);
        return ResponseEntity.created(URI.create("/api/historias/" + resposta.id())).body(resposta);
    }

    @GetMapping("/historias/{historiaId}")
    public HistoriaDetalhe consultar(@RequestHeader(name = CABECALHO, required = false) Long usuarioId,
                                     @PathVariable Long historiaId) {
        return historias.consultar(usuarioId, historiaId);
    }

    @PutMapping("/historias/{historiaId}")
    public HistoriaDetalhe editar(@RequestHeader(name = CABECALHO, required = false) Long usuarioId,
                                  @PathVariable Long historiaId, @Valid @RequestBody HistoriaDados dados) {
        return historias.editar(usuarioId, historiaId, dados);
    }

    @DeleteMapping("/historias/{historiaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@RequestHeader(name = CABECALHO, required = false) Long usuarioId, @PathVariable Long historiaId) {
        historias.excluir(usuarioId, historiaId);
    }

    @GetMapping("/historias/{historiaId}/feature")
    public ResponseEntity<String> exportar(@RequestHeader(name = CABECALHO, required = false) Long usuarioId,
                                           @PathVariable Long historiaId) {
        ArquivoFeature arquivo = historias.exportar(usuarioId, historiaId);
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(arquivo.nome()).build().toString())
                .body(arquivo.conteudo());
    }
}
