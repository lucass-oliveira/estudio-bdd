package br.edu.mackenzie.estudiobdd.config;

import java.util.List;

import br.edu.mackenzie.estudiobdd.dto.Dtos.CenarioDados;
import br.edu.mackenzie.estudiobdd.dto.Dtos.DecisaoDados;
import br.edu.mackenzie.estudiobdd.dto.Dtos.HistoriaDados;
import br.edu.mackenzie.estudiobdd.dto.Dtos.PassoDados;
import br.edu.mackenzie.estudiobdd.dto.Dtos.ProjetoDados;
import br.edu.mackenzie.estudiobdd.dto.Dtos.SugestaoDados;
import br.edu.mackenzie.estudiobdd.dto.Dtos.UsuarioDados;
import br.edu.mackenzie.estudiobdd.model.DecisaoSugestao;
import br.edu.mackenzie.estudiobdd.model.MotivoRejeicao;
import br.edu.mackenzie.estudiobdd.model.OrigemCenario;
import br.edu.mackenzie.estudiobdd.model.Prioridade;
import br.edu.mackenzie.estudiobdd.model.TipoPasso;
import br.edu.mackenzie.estudiobdd.service.CenarioService;
import br.edu.mackenzie.estudiobdd.service.HistoriaService;
import br.edu.mackenzie.estudiobdd.service.ProjetoService;
import br.edu.mackenzie.estudiobdd.service.SugestaoService;
import br.edu.mackenzie.estudiobdd.service.UsuarioService;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.demo-data", havingValue = "true")
public class DadosDemonstracao implements ApplicationRunner {
    private final UsuarioService usuarios;
    private final ProjetoService projetos;
    private final HistoriaService historias;
    private final CenarioService cenarios;
    private final SugestaoService sugestoes;
    private final JdbcTemplate jdbc;

    public DadosDemonstracao(UsuarioService usuarios, ProjetoService projetos, HistoriaService historias,
                             CenarioService cenarios, SugestaoService sugestoes, JdbcTemplate jdbc) {
        this.usuarios = usuarios;
        this.projetos = projetos;
        this.historias = historias;
        this.cenarios = cenarios;
        this.sugestoes = sugestoes;
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Somente uma instância pode preencher um banco vazio durante a inicialização.
        jdbc.execute("select pg_advisory_xact_lock(737201)");
        if (!usuarios.listar().isEmpty()) {
            return;
        }

        Long ana = usuarios.cadastrar(new UsuarioDados("Ana Martins", "ana@exemplo.test")).id();
        Long bruno = usuarios.cadastrar(new UsuarioDados("Bruno Lima", "bruno@exemplo.test")).id();
        Long projeto = projetos.cadastrar(ana, new ProjetoDados("Portal da Biblioteca",
                "Acompanhar reservas e empréstimos de uma biblioteca universitária fictícia.",
                "Leitor é quem possui cadastro ativo. Exemplar é uma cópia física de um título. "
                        + "Reserva garante a retirada do exemplar por 48 horas.")).id();
        Long reserva = historias.cadastrar(ana, projeto, new HistoriaDados("Reservar um livro disponível", "leitora",
                "reservar um exemplar disponível", "retirar o livro na biblioteca dentro do prazo", Prioridade.ALTA)).id();
        Long consulta = historias.cadastrar(bruno, projeto, new HistoriaDados("Consultar empréstimos ativos", "leitor",
                "consultar meus empréstimos ativos", "planejar as devoluções", Prioridade.MEDIA)).id();

        cenarios.cadastrar(ana, reserva, new CenarioDados("Reserva confirmada para exemplar disponível",
                OrigemCenario.HUMANO, null, List.of(
                passo(TipoPasso.DADO, "que Ana possui cadastro ativo"),
                passo(TipoPasso.E, "um exemplar de \"Dom Casmurro\" está disponível"),
                passo(TipoPasso.QUANDO, "Ana solicita a reserva do exemplar"),
                passo(TipoPasso.ENTAO, "a reserva de Ana é confirmada"),
                passo(TipoPasso.E, "o prazo de retirada é de 48 horas"))));
        cenarios.cadastrar(ana, reserva, new CenarioDados("Exemplar já reservado por outro leitor",
                OrigemCenario.SUGESTAO_IA, null, List.of(
                passo(TipoPasso.DADO, "que Bruno tem uma reserva ativa do único exemplar de \"Dom Casmurro\""),
                passo(TipoPasso.QUANDO, "Ana solicita a reserva do mesmo exemplar"),
                passo(TipoPasso.ENTAO, "o pedido de Ana é recusado"))));
        cenarios.cadastrar(bruno, consulta, new CenarioDados("Empréstimo aberto aparece na consulta",
                OrigemCenario.HUMANO, null, List.of(
                passo(TipoPasso.DADO, "que Bruno está com um exemplar de \"Memórias Póstumas de Brás Cubas\""),
                passo(TipoPasso.QUANDO, "Bruno consulta seus empréstimos ativos"),
                passo(TipoPasso.ENTAO, "a consulta mostra o título e a data de devolução"))));

        Long aceita = sugestoes.cadastrar(bruno, reserva, new SugestaoDados("Assistente de IA fictício",
                "Sugira um caso de borda para a reserva de exemplar.",
                "Cobrir o caso em que outro leitor já reservou o único exemplar.")).id();
        sugestoes.decidir(ana, aceita, new DecisaoDados(DecisaoSugestao.ACEITA, null,
                "O exemplo cobre a disputa pelo único exemplar, uma condição do domínio."));
        Long rejeitada = sugestoes.cadastrar(ana, reserva, new SugestaoDados("Assistente de IA fictício",
                "Sugira um comportamento complementar ao confirmar a reserva.",
                "Enviar um e-mail promocional a cada reserva.")).id();
        sugestoes.decidir(ana, rejeitada, new DecisaoDados(DecisaoSugestao.REJEITADA, MotivoRejeicao.FORA_DO_ESCOPO,
                "A história trata somente da confirmação da reserva e do prazo de retirada."));
        sugestoes.cadastrar(ana, consulta, new SugestaoDados("Assistente de IA fictício",
                "Identifique um caso de borda na consulta de empréstimos.",
                "Acrescentar um cenário em que o leitor não tem nenhum empréstimo ativo."));
    }

    private static PassoDados passo(TipoPasso tipo, String texto) {
        return new PassoDados(null, tipo, texto, null);
    }
}
