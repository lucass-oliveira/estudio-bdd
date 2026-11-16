package br.edu.mackenzie.estudiobdd.service;

import java.util.List;

import br.edu.mackenzie.estudiobdd.model.TipoPasso;
import org.springframework.http.HttpStatus;

public final class RegrasCenario {
    private RegrasCenario() {
    }

    public static void validar(List<TipoPasso> tipos) {
        TipoPasso anterior = null;
        boolean temQuando = false;
        boolean temEntao = false;

        for (TipoPasso tipo : tipos) {
            if (tipo == TipoPasso.E || tipo == TipoPasso.MAS) {
                if (anterior == null) {
                    throw invalido("E e Mas não podem iniciar um cenário.");
                }
                continue;
            }

            if (anterior != null && tipo.ordinal() < anterior.ordinal()) {
                throw invalido("Use os passos na ordem Dado, Quando e Então.");
            }
            temQuando |= tipo == TipoPasso.QUANDO;
            temEntao |= tipo == TipoPasso.ENTAO;
            anterior = tipo;
        }

        if (!temQuando) {
            throw invalido("O cenário precisa de pelo menos um passo Quando.");
        }
        if (!temEntao) {
            throw invalido("O cenário precisa de pelo menos um passo Então.");
        }
    }

    private static RegraNegocioException invalido(String mensagem) {
        return new RegraNegocioException(HttpStatus.UNPROCESSABLE_ENTITY, mensagem);
    }
}
