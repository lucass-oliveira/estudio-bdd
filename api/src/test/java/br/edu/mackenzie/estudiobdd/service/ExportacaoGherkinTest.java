package br.edu.mackenzie.estudiobdd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpStatus.CONFLICT;

import org.junit.jupiter.api.Test;

import br.edu.mackenzie.estudiobdd.dto.Dtos.ArquivoFeature;
import br.edu.mackenzie.estudiobdd.model.Cenario;
import br.edu.mackenzie.estudiobdd.model.HistoriaUsuario;
import br.edu.mackenzie.estudiobdd.model.OrigemCenario;
import br.edu.mackenzie.estudiobdd.model.Passo;
import br.edu.mackenzie.estudiobdd.model.Prioridade;
import br.edu.mackenzie.estudiobdd.model.Projeto;
import br.edu.mackenzie.estudiobdd.model.Sugestao;
import br.edu.mackenzie.estudiobdd.model.TipoPasso;
import br.edu.mackenzie.estudiobdd.model.Usuario;

class ExportacaoGherkinTest {

    @Test
    void bloqueiaExportacaoComSugestaoPendente() {
        HistoriaUsuario historia = novaHistoria();
        adicionarCenario(historia);
        historia.getSugestoes().add(new Sugestao(historia, historia.getAutor(), "Codex",
                "Proponha um caso de borda.", "Tentar exportar sem cenários."));

        assertThatThrownBy(() -> ExportacaoGherkin.gerar(historia))
                .isInstanceOfSatisfying(RegraNegocioException.class,
                        erro -> assertThat(erro.getStatus()).isEqualTo(CONFLICT));
    }

    @Test
    void bloqueiaExportacaoSemCenarios() {
        HistoriaUsuario historia = novaHistoria();

        assertThatThrownBy(() -> ExportacaoGherkin.gerar(historia))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void exportaHistoriaEScenarioComPalavrasChaveEmPortugues() {
        HistoriaUsuario historia = novaHistoria();
        adicionarCenario(historia);

        ArquivoFeature arquivo = ExportacaoGherkin.gerar(historia);

        assertThat(arquivo.nome()).isEqualTo("exportar-criterios.feature");
        assertThat(arquivo.conteudo())
                .startsWith("# language: pt")
                .containsSubsequence("Funcionalidade: Exportar critérios", "Como analista",
                        "Quero exportar a história", "Para compartilhar os critérios de aceite",
                        "Cenário: Exportação concluída", "Quando Ana solicita o arquivo",
                        "Então a história é exportada");
    }

    private HistoriaUsuario novaHistoria() {
        Projeto projeto = new Projeto("Portal acadêmico", "Portal para estudantes", "Matrículas");
        Usuario autor = new Usuario("Ana", "ana@example.com");
        return new HistoriaUsuario(projeto, autor, "Exportar critérios", "analista",
                "exportar a história", "compartilhar os critérios de aceite", Prioridade.ALTA);
    }

    private void adicionarCenario(HistoriaUsuario historia) {
        Cenario cenario = new Cenario(historia, "Exportação concluída", OrigemCenario.HUMANO);
        cenario.setPosicao(1);
        cenario.getPassos().add(new Passo(cenario, TipoPasso.QUANDO, "Ana solicita o arquivo", 1));
        cenario.getPassos().add(new Passo(cenario, TipoPasso.ENTAO, "a história é exportada", 2));
        historia.getCenarios().add(cenario);
    }
}
