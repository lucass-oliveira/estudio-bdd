package br.edu.mackenzie.estudiobdd.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.edu.mackenzie.estudiobdd.dto.Dtos.CenarioDados;
import br.edu.mackenzie.estudiobdd.dto.Dtos.CenarioResposta;
import br.edu.mackenzie.estudiobdd.dto.Dtos.PassoDados;
import br.edu.mackenzie.estudiobdd.dto.Dtos.PassoResposta;
import br.edu.mackenzie.estudiobdd.dto.Respostas;
import br.edu.mackenzie.estudiobdd.model.Cenario;
import br.edu.mackenzie.estudiobdd.model.HistoriaUsuario;
import br.edu.mackenzie.estudiobdd.model.Passo;
import br.edu.mackenzie.estudiobdd.repository.CenarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CenarioService {
    private final CenarioRepository cenarios;
    private final HistoriaService historias;
    private final UsuarioService usuarios;

    public CenarioService(CenarioRepository cenarios, HistoriaService historias, UsuarioService usuarios) {
        this.cenarios = cenarios;
        this.historias = historias;
        this.usuarios = usuarios;
    }

    public List<CenarioResposta> listar(Long usuarioId, Long historiaId) {
        usuarios.exigirUsuario(usuarioId);
        return historias.buscar(historiaId).getCenarios().stream().map(Respostas::cenario).toList();
    }

    public CenarioResposta consultar(Long usuarioId, Long cenarioId) {
        usuarios.exigirUsuario(usuarioId);
        return Respostas.cenario(buscar(cenarioId));
    }

    @Transactional
    public CenarioResposta cadastrar(Long usuarioId, Long historiaId, CenarioDados dados) {
        HistoriaUsuario historia = historias.bloquearComoAutor(historiaId, usuarioId);
        String titulo = dados.titulo().strip();
        verificarTitulo(historia, titulo, null);
        int posicao = posicao(dados.posicao(), historia.getCenarios().size() + 1, historia.getCenarios().size() + 1);

        Cenario cenario = new Cenario(historia, titulo, dados.origem());
        aplicarPassos(cenario, dados.passos());
        ordenarCenario(historia, cenario, posicao);
        historia.registrarAtualizacao();
        return Respostas.cenario(cenarios.save(cenario));
    }

    @Transactional
    public CenarioResposta editar(Long usuarioId, Long cenarioId, CenarioDados dados) {
        Cenario cenario = bloquearComoAutor(cenarioId, usuarioId);
        HistoriaUsuario historia = cenario.getHistoria();
        String titulo = dados.titulo().strip();
        verificarTitulo(historia, titulo, cenarioId);
        int posicao = posicao(dados.posicao(), cenario.getPosicao(), historia.getCenarios().size());

        aplicarPassos(cenario, dados.passos());
        cenario.atualizar(titulo, dados.origem());
        ordenarCenario(historia, cenario, posicao);
        historia.registrarAtualizacao();
        cenarios.flush();
        return Respostas.cenario(cenario);
    }

    @Transactional
    public void excluir(Long usuarioId, Long cenarioId) {
        Cenario cenario = bloquearComoAutor(cenarioId, usuarioId);
        HistoriaUsuario historia = cenario.getHistoria();
        historia.getCenarios().remove(cenario);
        numerarCenarios(historia.getCenarios());
        historia.registrarAtualizacao();
    }

    public List<PassoResposta> listarPassos(Long usuarioId, Long cenarioId) {
        usuarios.exigirUsuario(usuarioId);
        return buscar(cenarioId).getPassos().stream().map(Respostas::passo).toList();
    }

    public PassoResposta consultarPasso(Long usuarioId, Long cenarioId, Long passoId) {
        usuarios.exigirUsuario(usuarioId);
        Cenario cenario = buscar(cenarioId);
        return Respostas.passo(cenario.getPassos().get(indicePasso(cenario, passoId)));
    }

    @Transactional
    public PassoResposta cadastrarPasso(Long usuarioId, Long cenarioId, PassoDados dados) {
        Cenario cenario = bloquearComoAutor(cenarioId, usuarioId);
        List<PassoDados> passos = detalharPassos(cenario);
        int posicao = posicao(dados.posicao(), passos.size() + 1, passos.size() + 1);
        passos.add(posicao - 1, new PassoDados(null, dados.tipo(), dados.texto(), null));
        salvarPassos(cenario, passos);
        return Respostas.passo(cenario.getPassos().get(posicao - 1));
    }

    @Transactional
    public PassoResposta editarPasso(Long usuarioId, Long cenarioId, Long passoId, PassoDados dados) {
        Cenario cenario = bloquearComoAutor(cenarioId, usuarioId);
        int indice = indicePasso(cenario, passoId);
        List<PassoDados> passos = detalharPassos(cenario);
        int posicao = posicao(dados.posicao(), indice + 1, passos.size());
        passos.remove(indice);
        passos.add(posicao - 1, new PassoDados(passoId, dados.tipo(), dados.texto(), null));
        salvarPassos(cenario, passos);
        return Respostas.passo(cenario.getPassos().get(posicao - 1));
    }

    @Transactional
    public void excluirPasso(Long usuarioId, Long cenarioId, Long passoId) {
        Cenario cenario = bloquearComoAutor(cenarioId, usuarioId);
        List<PassoDados> passos = detalharPassos(cenario);
        passos.remove(indicePasso(cenario, passoId));
        salvarPassos(cenario, passos);
    }

    private void salvarPassos(Cenario cenario, List<PassoDados> passos) {
        aplicarPassos(cenario, passos);
        cenario.getHistoria().registrarAtualizacao();
        cenarios.flush();
    }

    private Cenario bloquearComoAutor(Long cenarioId, Long usuarioId) {
        Long historiaId = cenarios.buscarHistoriaId(cenarioId).orElseThrow(CenarioService::ausente);
        HistoriaUsuario historia = historias.bloquearComoAutor(historiaId, usuarioId);
        return historia.getCenarios().stream()
                .filter(cenario -> cenario.getId().equals(cenarioId))
                .findFirst()
                .orElseThrow(CenarioService::ausente);
    }

    private Cenario buscar(Long cenarioId) {
        return cenarios.findById(cenarioId).orElseThrow(CenarioService::ausente);
    }

    private static List<PassoDados> detalharPassos(Cenario cenario) {
        List<PassoDados> dados = new ArrayList<>();
        for (Passo passo : cenario.getPassos()) {
            dados.add(new PassoDados(passo.getId(), passo.getTipo(), passo.getTexto(), null));
        }
        return dados;
    }

    private static void aplicarPassos(Cenario cenario, List<PassoDados> dados) {
        if (dados.size() > 100) {
            throw invalido("Um cenário pode ter até 100 passos.");
        }
        RegrasCenario.validar(dados.stream().map(PassoDados::tipo).toList());

        List<Passo> passos = cenario.getPassos();
        Map<Long, Passo> aindaNaoUsados = new HashMap<>();
        passos.forEach(passo -> aindaNaoUsados.put(passo.getId(), passo));

        for (int indice = 0; indice < dados.size(); indice++) {
            PassoDados item = dados.get(indice);
            int posicao = indice + 1;
            String texto = item.texto().strip();

            if (item.id() == null) {
                passos.add(new Passo(cenario, item.tipo(), texto, posicao));
            } else {
                Passo existente = aindaNaoUsados.remove(item.id());
                if (existente == null) {
                    throw invalido("Cada passo existente deve pertencer a este cenário e aparecer uma só vez.");
                }
                existente.atualizar(item.tipo(), texto, posicao);
            }
        }

        passos.removeAll(aindaNaoUsados.values());
        passos.sort(Comparator.comparingInt(Passo::getPosicao));
    }

    private static void verificarTitulo(HistoriaUsuario historia, String titulo, Long cenarioId) {
        boolean repetido = historia.getCenarios().stream()
                .anyMatch(cenario -> cenario.getTitulo().equals(titulo) && !cenario.getId().equals(cenarioId));
        if (repetido) {
            throw new RegraNegocioException(HttpStatus.CONFLICT, "Já existe um cenário com este título na história.");
        }
    }

    private static void ordenarCenario(HistoriaUsuario historia, Cenario cenario, int posicao) {
        List<Cenario> ordenados = historia.getCenarios();
        ordenados.remove(cenario);
        ordenados.add(posicao - 1, cenario);
        numerarCenarios(ordenados);
    }

    private static void numerarCenarios(List<Cenario> cenarios) {
        for (int indice = 0; indice < cenarios.size(); indice++) {
            cenarios.get(indice).setPosicao(indice + 1);
        }
    }

    private static int indicePasso(Cenario cenario, Long passoId) {
        for (int indice = 0; indice < cenario.getPassos().size(); indice++) {
            if (cenario.getPassos().get(indice).getId().equals(passoId)) {
                return indice;
            }
        }
        throw new RegraNegocioException(HttpStatus.NOT_FOUND, "Passo não encontrado neste cenário.");
    }

    private static int posicao(Integer solicitada, int padrao, int maxima) {
        if (solicitada == null) {
            return padrao;
        }
        if (solicitada < 1 || solicitada > maxima) {
            throw invalido("A posição precisa estar entre 1 e " + maxima + ".");
        }
        return solicitada;
    }

    private static RegraNegocioException ausente() {
        return new RegraNegocioException(HttpStatus.NOT_FOUND, "Cenário não encontrado.");
    }

    private static RegraNegocioException invalido(String mensagem) {
        return new RegraNegocioException(HttpStatus.UNPROCESSABLE_ENTITY, mensagem);
    }
}
