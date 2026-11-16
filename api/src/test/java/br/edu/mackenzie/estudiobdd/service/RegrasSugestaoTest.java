package br.edu.mackenzie.estudiobdd.service;

import static br.edu.mackenzie.estudiobdd.model.DecisaoSugestao.ACEITA;
import static br.edu.mackenzie.estudiobdd.model.DecisaoSugestao.PENDENTE;
import static br.edu.mackenzie.estudiobdd.model.DecisaoSugestao.REJEITADA;
import static br.edu.mackenzie.estudiobdd.model.MotivoRejeicao.DUPLICADA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import br.edu.mackenzie.estudiobdd.model.DecisaoSugestao;

class RegrasSugestaoTest {

    @Test
    void rejeicaoSemMotivoERecusada() {
        assertThatThrownBy(() -> RegrasSugestao.validarDecisao(
                PENDENTE, REJEITADA, null, "O caso já está coberto."))
                .isInstanceOfSatisfying(RegraNegocioException.class,
                        erro -> assertThat(erro.getStatus()).isEqualTo(UNPROCESSABLE_ENTITY));
    }

    @Test
    void permiteAceitarSugestaoPendente() {
        assertThatCode(() -> RegrasSugestao.validarDecisao(
                PENDENTE, ACEITA, null, "Cobre um caso de borda."))
                .doesNotThrowAnyException();
    }

    @Test
    void permiteRejeitarSugestaoPendenteComMotivo() {
        assertThatCode(() -> RegrasSugestao.validarDecisao(
                PENDENTE, REJEITADA, DUPLICADA, "O caso já está coberto."))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @EnumSource(value = DecisaoSugestao.class, names = {"ACEITA", "REJEITADA"})
    void bloqueiaUmaNovaDecisaoSobreSugestaoJaDecidida(DecisaoSugestao atual) {
        assertThatThrownBy(() -> RegrasSugestao.validarDecisao(
                atual, ACEITA, null, "Revisão do mesmo caso."))
                .isInstanceOf(RegraNegocioException.class);
    }
}
