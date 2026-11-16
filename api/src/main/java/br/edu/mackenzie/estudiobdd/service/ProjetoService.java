package br.edu.mackenzie.estudiobdd.service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import br.edu.mackenzie.estudiobdd.dto.Dtos.HistoriaResumo;
import br.edu.mackenzie.estudiobdd.dto.Dtos.MotivoQuantidade;
import br.edu.mackenzie.estudiobdd.dto.Dtos.ProjetoDados;
import br.edu.mackenzie.estudiobdd.dto.Dtos.ProjetoDetalhe;
import br.edu.mackenzie.estudiobdd.dto.Dtos.ProjetoResposta;
import br.edu.mackenzie.estudiobdd.dto.Dtos.ResumoProjeto;
import br.edu.mackenzie.estudiobdd.dto.Respostas;
import br.edu.mackenzie.estudiobdd.model.DecisaoSugestao;
import br.edu.mackenzie.estudiobdd.model.MotivoRejeicao;
import br.edu.mackenzie.estudiobdd.model.OrigemCenario;
import br.edu.mackenzie.estudiobdd.model.Prioridade;
import br.edu.mackenzie.estudiobdd.model.Projeto;
import br.edu.mackenzie.estudiobdd.model.Usuario;
import br.edu.mackenzie.estudiobdd.repository.ProjetoRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjetoService {
    private final ProjetoRepository projetos;
    private final UsuarioService usuarios;

    public ProjetoService(ProjetoRepository projetos, UsuarioService usuarios) {
        this.projetos = projetos;
        this.usuarios = usuarios;
    }

    public List<ProjetoResposta> listar(Long usuarioId) {
        usuarios.exigirUsuario(usuarioId);
        return projetos.findAllByOrderByNomeAsc().stream().map(Respostas::projeto).toList();
    }

    public ProjetoDetalhe consultar(Long usuarioId, Long projetoId, Prioridade prioridade) {
        usuarios.exigirUsuario(usuarioId);
        Projeto projeto = buscar(projetoId);
        List<HistoriaResumo> historias = projeto.getHistorias().stream()
                .filter(historia -> prioridade == null || historia.getPrioridade() == prioridade)
                .map(Respostas::historiaResumo)
                .toList();
        return Respostas.projeto(projeto, historias);
    }

    @Transactional
    public ProjetoResposta cadastrar(Long usuarioId, ProjetoDados dados) {
        usuarios.exigirUsuario(usuarioId);
        String nome = dados.nome().strip();
        if (projetos.existsByNome(nome)) {
            throw nomeRepetido();
        }
        Projeto projeto = new Projeto(nome, descricao(dados.descricao()), descricao(dados.contextoDominio()));
        return Respostas.projeto(projetos.save(projeto));
    }

    @Transactional
    public ProjetoResposta editar(Long usuarioId, Long projetoId, ProjetoDados dados) {
        usuarios.exigirUsuario(usuarioId);
        Projeto projeto = bloquear(projetoId);
        String nome = dados.nome().strip();
        if (projetos.existsByNomeAndIdNot(nome, projetoId)) {
            throw nomeRepetido();
        }
        projeto.atualizar(nome, descricao(dados.descricao()), descricao(dados.contextoDominio()));
        return Respostas.projeto(projeto);
    }

    @Transactional
    public void excluir(Long usuarioId, Long projetoId) {
        Usuario atual = usuarios.exigirUsuario(usuarioId);
        Projeto projeto = bloquear(projetoId);
        boolean temOutroAutor = projeto.getHistorias().stream()
                .anyMatch(historia -> !historia.getAutor().getId().equals(atual.getId()));
        if (temOutroAutor) {
            throw new RegraNegocioException(HttpStatus.FORBIDDEN,
                    "O projeto contém histórias de outro autor e não pode ser excluído por este usuário.");
        }
        projetos.delete(projeto);
    }

    public ResumoProjeto resumir(Long usuarioId, Long projetoId) {
        usuarios.exigirUsuario(usuarioId);
        Projeto projeto = buscar(projetoId);

        Map<OrigemCenario, Long> origens = contadores(OrigemCenario.class);
        Map<DecisaoSugestao, Long> decisoes = contadores(DecisaoSugestao.class);
        Map<MotivoRejeicao, Long> motivos = new EnumMap<>(MotivoRejeicao.class);

        for (var historia : projeto.getHistorias()) {
            historia.getCenarios().forEach(cenario -> origens.merge(cenario.getOrigem(), 1L, Long::sum));
            historia.getSugestoes().forEach(sugestao -> {
                decisoes.merge(sugestao.getDecisao(), 1L, Long::sum);
                if (sugestao.getDecisao() == DecisaoSugestao.REJEITADA) {
                    motivos.merge(sugestao.getMotivoRejeicao(), 1L, Long::sum);
                }
            });
        }

        List<MotivoQuantidade> motivosOrdenados = motivos.entrySet().stream()
                .map(entrada -> new MotivoQuantidade(entrada.getKey(), entrada.getValue()))
                .sorted(Comparator.comparingLong(MotivoQuantidade::quantidade).reversed()
                        .thenComparing(MotivoQuantidade::motivo))
                .toList();

        return new ResumoProjeto(projeto.getHistorias().size(), origens, decisoes, motivosOrdenados);
    }

    Projeto bloquear(Long projetoId) {
        return projetos.buscarParaAlterar(projetoId).orElseThrow(ProjetoService::ausente);
    }

    private Projeto buscar(Long projetoId) {
        return projetos.findById(projetoId).orElseThrow(ProjetoService::ausente);
    }

    private static <E extends Enum<E>> Map<E, Long> contadores(Class<E> tipo) {
        EnumMap<E, Long> resultado = new EnumMap<>(tipo);
        Arrays.stream(tipo.getEnumConstants()).forEach(valor -> resultado.put(valor, 0L));
        return resultado;
    }

    private static String descricao(String texto) {
        return texto == null ? "" : texto.strip();
    }

    private static RegraNegocioException nomeRepetido() {
        return new RegraNegocioException(HttpStatus.CONFLICT, "Já existe um projeto com este nome.");
    }

    private static RegraNegocioException ausente() {
        return new RegraNegocioException(HttpStatus.NOT_FOUND, "Projeto não encontrado.");
    }
}
