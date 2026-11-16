package br.edu.mackenzie.estudiobdd.service;

import java.text.Normalizer;
import java.util.Locale;

import br.edu.mackenzie.estudiobdd.dto.Dtos.ArquivoFeature;
import br.edu.mackenzie.estudiobdd.model.Cenario;
import br.edu.mackenzie.estudiobdd.model.DecisaoSugestao;
import br.edu.mackenzie.estudiobdd.model.HistoriaUsuario;
import br.edu.mackenzie.estudiobdd.model.Passo;

import org.springframework.http.HttpStatus;

public final class ExportacaoGherkin {
    private ExportacaoGherkin() {
    }

    public static ArquivoFeature gerar(HistoriaUsuario historia) {
        if (historia.getCenarios().isEmpty()) {
            throw new RegraNegocioException(HttpStatus.CONFLICT, "Cadastre um cenário antes de exportar a história.");
        }
        if (historia.getSugestoes().stream().anyMatch(s -> s.getDecisao() == DecisaoSugestao.PENDENTE)) {
            throw new RegraNegocioException(HttpStatus.CONFLICT, "Decida todas as sugestões pendentes antes de exportar.");
        }

        StringBuilder texto = new StringBuilder("# language: pt\n")
                .append("Funcionalidade: ").append(historia.getTitulo()).append('\n')
                .append("  Como ").append(historia.getPapel()).append('\n')
                .append("  Quero ").append(historia.getObjetivo()).append('\n')
                .append("  Para ").append(historia.getBeneficio()).append('\n');

        for (Cenario cenario : historia.getCenarios()) {
            texto.append("\n  Cenário: ").append(cenario.getTitulo()).append('\n');
            for (Passo passo : cenario.getPassos()) {
                texto.append("    ").append(passo.getTipo().getPalavraChave()).append(' ')
                        .append(passo.getTexto()).append('\n');
            }
        }

        String nome = Normalizer.normalize(historia.getTitulo(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        if (nome.isBlank()) {
            nome = "historia-" + historia.getId();
        }
        return new ArquivoFeature(nome + ".feature", texto.toString());
    }
}
