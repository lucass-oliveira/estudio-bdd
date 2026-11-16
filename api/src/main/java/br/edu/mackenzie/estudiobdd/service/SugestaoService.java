package br.edu.mackenzie.estudiobdd.service;

import java.util.List;

import br.edu.mackenzie.estudiobdd.dto.Dtos.DecisaoDados;
import br.edu.mackenzie.estudiobdd.dto.Dtos.SugestaoDados;
import br.edu.mackenzie.estudiobdd.dto.Dtos.SugestaoResposta;
import br.edu.mackenzie.estudiobdd.dto.Respostas;
import br.edu.mackenzie.estudiobdd.model.DecisaoSugestao;
import br.edu.mackenzie.estudiobdd.model.HistoriaUsuario;
import br.edu.mackenzie.estudiobdd.model.Sugestao;
import br.edu.mackenzie.estudiobdd.model.Usuario;
import br.edu.mackenzie.estudiobdd.repository.SugestaoRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SugestaoService {
    private final SugestaoRepository sugestoes;
    private final HistoriaService historias;
    private final UsuarioService usuarios;

    public SugestaoService(SugestaoRepository sugestoes, HistoriaService historias, UsuarioService usuarios) {
        this.sugestoes = sugestoes;
        this.historias = historias;
        this.usuarios = usuarios;
    }

    public List<SugestaoResposta> listar(Long usuarioId, Long historiaId, DecisaoSugestao decisao) {
        usuarios.exigirUsuario(usuarioId);
        return historias.buscar(historiaId).getSugestoes().stream()
                .filter(sugestao -> decisao == null || sugestao.getDecisao() == decisao)
                .map(Respostas::sugestao)
                .toList();
    }

    @Transactional
    public SugestaoResposta cadastrar(Long usuarioId, Long historiaId, SugestaoDados dados) {
        Usuario usuario = usuarios.exigirUsuario(usuarioId);
        HistoriaUsuario historia = historias.bloquear(historiaId);
        Sugestao sugestao = new Sugestao(historia, usuario, dados.ferramenta().strip(), dados.prompt().strip(),
                dados.conteudo().strip());
        historia.registrarAtualizacao();
        return Respostas.sugestao(sugestoes.save(sugestao));
    }

    @Transactional
    public SugestaoResposta decidir(Long usuarioId, Long sugestaoId, DecisaoDados dados) {
        Usuario usuario = usuarios.exigirUsuario(usuarioId);
        Long historiaId = sugestoes.buscarHistoriaId(sugestaoId).orElseThrow(SugestaoService::ausente);
        HistoriaUsuario historia = historias.bloquear(historiaId);
        Sugestao sugestao = sugestoes.findById(sugestaoId).orElseThrow(SugestaoService::ausente);
        RegrasSugestao.validarDecisao(sugestao.getDecisao(), dados.decisao(), dados.motivoRejeicao(), dados.justificativa());
        sugestao.decidir(usuario, dados.decisao(), dados.motivoRejeicao(), dados.justificativa().strip());
        historia.registrarAtualizacao();
        return Respostas.sugestao(sugestao);
    }

    private static RegraNegocioException ausente() {
        return new RegraNegocioException(HttpStatus.NOT_FOUND, "Sugestão não encontrada.");
    }
}
