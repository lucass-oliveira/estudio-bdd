package br.edu.mackenzie.estudiobdd.service;

import br.edu.mackenzie.estudiobdd.dto.Dtos.ArquivoFeature;
import br.edu.mackenzie.estudiobdd.dto.Dtos.HistoriaDados;
import br.edu.mackenzie.estudiobdd.dto.Dtos.HistoriaDetalhe;
import br.edu.mackenzie.estudiobdd.dto.Respostas;
import br.edu.mackenzie.estudiobdd.model.HistoriaUsuario;
import br.edu.mackenzie.estudiobdd.model.Projeto;
import br.edu.mackenzie.estudiobdd.model.Usuario;
import br.edu.mackenzie.estudiobdd.repository.HistoriaRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class HistoriaService {
    private final HistoriaRepository historias;
    private final ProjetoService projetos;
    private final UsuarioService usuarios;

    public HistoriaService(HistoriaRepository historias, ProjetoService projetos, UsuarioService usuarios) {
        this.historias = historias;
        this.projetos = projetos;
        this.usuarios = usuarios;
    }

    public HistoriaDetalhe consultar(Long usuarioId, Long historiaId) {
        usuarios.exigirUsuario(usuarioId);
        return Respostas.historiaDetalhe(buscar(historiaId));
    }

    @Transactional
    public HistoriaDetalhe cadastrar(Long usuarioId, Long projetoId, HistoriaDados dados) {
        Usuario autor = usuarios.exigirUsuario(usuarioId);
        Projeto projeto = projetos.bloquear(projetoId);
        String titulo = dados.titulo().strip();
        if (historias.existsByProjetoIdAndTitulo(projetoId, titulo)) {
            throw tituloRepetido();
        }
        HistoriaUsuario historia = new HistoriaUsuario(projeto, autor, titulo, dados.papel().strip(),
                dados.objetivo().strip(), dados.beneficio().strip(), dados.prioridade());
        return Respostas.historiaDetalhe(historias.save(historia));
    }

    @Transactional
    public HistoriaDetalhe editar(Long usuarioId, Long historiaId, HistoriaDados dados) {
        HistoriaUsuario historia = bloquearComoAutor(historiaId, usuarioId);
        String titulo = dados.titulo().strip();
        if (historias.existsByProjetoIdAndTituloAndIdNot(historia.getProjeto().getId(), titulo, historiaId)) {
            throw tituloRepetido();
        }
        historia.atualizar(titulo, dados.papel().strip(), dados.objetivo().strip(), dados.beneficio().strip(),
                dados.prioridade());
        return Respostas.historiaDetalhe(historia);
    }

    @Transactional
    public void excluir(Long usuarioId, Long historiaId) {
        historias.delete(bloquearComoAutor(historiaId, usuarioId));
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public ArquivoFeature exportar(Long usuarioId, Long historiaId) {
        usuarios.exigirUsuario(usuarioId);
        return ExportacaoGherkin.gerar(buscar(historiaId));
    }

    HistoriaUsuario buscar(Long historiaId) {
        return historias.findById(historiaId).orElseThrow(HistoriaService::ausente);
    }

    HistoriaUsuario bloquear(Long historiaId) {
        // Escritas em histórias, cenários, passos e sugestões são serializadas no projeto.
        Long projetoId = historias.buscarProjetoId(historiaId).orElseThrow(HistoriaService::ausente);
        projetos.bloquear(projetoId);
        return buscar(historiaId);
    }

    HistoriaUsuario bloquearComoAutor(Long historiaId, Long usuarioId) {
        Usuario atual = usuarios.exigirUsuario(usuarioId);
        HistoriaUsuario historia = bloquear(historiaId);
        if (!historia.getAutor().getId().equals(atual.getId())) {
            throw new RegraNegocioException(HttpStatus.FORBIDDEN,
                    "Somente o autor pode alterar ou excluir a história, os cenários e os passos.");
        }
        return historia;
    }

    private static RegraNegocioException tituloRepetido() {
        return new RegraNegocioException(HttpStatus.CONFLICT, "Já existe uma história com este título no projeto.");
    }

    private static RegraNegocioException ausente() {
        return new RegraNegocioException(HttpStatus.NOT_FOUND, "História não encontrada.");
    }
}
