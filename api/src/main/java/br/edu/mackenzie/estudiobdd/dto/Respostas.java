package br.edu.mackenzie.estudiobdd.dto;

import java.util.List;

import br.edu.mackenzie.estudiobdd.model.Cenario;
import br.edu.mackenzie.estudiobdd.model.DecisaoSugestao;
import br.edu.mackenzie.estudiobdd.model.HistoriaUsuario;
import br.edu.mackenzie.estudiobdd.model.Passo;
import br.edu.mackenzie.estudiobdd.model.Projeto;
import br.edu.mackenzie.estudiobdd.model.Sugestao;
import br.edu.mackenzie.estudiobdd.model.Usuario;

public final class Respostas {
    private Respostas() {
    }

    public static Dtos.UsuarioResposta usuario(Usuario usuario) {
        return new Dtos.UsuarioResposta(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }

    public static Dtos.ProjetoResposta projeto(Projeto projeto) {
        return new Dtos.ProjetoResposta(projeto.getId(), projeto.getNome(), projeto.getDescricao(),
                projeto.getContextoDominio(), projeto.getCriadoEm());
    }

    public static Dtos.ProjetoDetalhe projeto(Projeto projeto, List<Dtos.HistoriaResumo> historias) {
        return new Dtos.ProjetoDetalhe(projeto.getId(), projeto.getNome(), projeto.getDescricao(),
                projeto.getContextoDominio(), projeto.getCriadoEm(), historias);
    }

    public static Dtos.HistoriaResumo historiaResumo(HistoriaUsuario historia) {
        long pendentes = historia.getSugestoes().stream()
                .filter(sugestao -> sugestao.getDecisao() == DecisaoSugestao.PENDENTE)
                .count();

        return new Dtos.HistoriaResumo(historia.getId(), historia.getTitulo(), historia.getPapel(),
                historia.getObjetivo(), historia.getBeneficio(), historia.getPrioridade(), usuario(historia.getAutor()),
                historia.getCriadaEm(), historia.getAtualizadaEm(), historia.getCenarios().size(), pendentes);
    }

    public static Dtos.HistoriaDetalhe historiaDetalhe(HistoriaUsuario historia) {
        Dtos.HistoriaResumo resumo = historiaResumo(historia);
        return new Dtos.HistoriaDetalhe(resumo.id(), resumo.titulo(), resumo.papel(), resumo.objetivo(),
                resumo.beneficio(), resumo.prioridade(), resumo.autor(), resumo.criadaEm(), resumo.atualizadaEm(),
                resumo.quantidadeCenarios(), resumo.quantidadeSugestoesPendentes(),
                historia.getCenarios().stream().map(Respostas::cenario).toList());
    }

    public static Dtos.CenarioResposta cenario(Cenario cenario) {
        return new Dtos.CenarioResposta(cenario.getId(), cenario.getTitulo(), cenario.getOrigem(),
                cenario.getPosicao(), cenario.getPassos().stream().map(Respostas::passo).toList());
    }

    public static Dtos.PassoResposta passo(Passo passo) {
        return new Dtos.PassoResposta(passo.getId(), passo.getTipo(), passo.getTexto(), passo.getPosicao());
    }

    public static Dtos.SugestaoResposta sugestao(Sugestao sugestao) {
        Dtos.UsuarioResposta decisor = sugestao.getDecididaPor() == null ? null : usuario(sugestao.getDecididaPor());
        return new Dtos.SugestaoResposta(sugestao.getId(), sugestao.getFerramenta(), sugestao.getPrompt(),
                sugestao.getConteudo(), sugestao.getDecisao(), sugestao.getMotivoRejeicao(), sugestao.getJustificativa(),
                usuario(sugestao.getRegistradaPor()), decisor, sugestao.getRegistradaEm(), sugestao.getDecididaEm());
    }
}
