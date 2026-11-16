package br.edu.mackenzie.estudiobdd.service;

import br.edu.mackenzie.estudiobdd.model.DecisaoSugestao;
import br.edu.mackenzie.estudiobdd.model.MotivoRejeicao;
import org.springframework.http.HttpStatus;

public final class RegrasSugestao {
    private RegrasSugestao() {
    }

    public static void validarDecisao(DecisaoSugestao atual, DecisaoSugestao nova,
                                     MotivoRejeicao motivo, String justificativa) {
        if (atual != DecisaoSugestao.PENDENTE) {
            throw new RegraNegocioException(HttpStatus.CONFLICT, "A sugestão já foi decidida.");
        }
        if (nova == DecisaoSugestao.PENDENTE) {
            throw invalida("Selecione Aceita ou Rejeitada para decidir a sugestão.");
        }
        if (nova == DecisaoSugestao.REJEITADA && motivo == null) {
            throw invalida("Informe um motivo para rejeitar a sugestão.");
        }
        if (nova == DecisaoSugestao.ACEITA && motivo != null) {
            throw invalida("O motivo de rejeição é válido apenas para sugestões rejeitadas.");
        }
        if (justificativa == null || justificativa.isBlank()) {
            throw invalida("Informe uma justificativa para a decisão.");
        }
    }

    private static RegraNegocioException invalida(String mensagem) {
        return new RegraNegocioException(HttpStatus.UNPROCESSABLE_ENTITY, mensagem);
    }
}
