package br.edu.mackenzie.estudiobdd.service;

import static br.edu.mackenzie.estudiobdd.model.TipoPasso.DADO;
import static br.edu.mackenzie.estudiobdd.model.TipoPasso.E;
import static br.edu.mackenzie.estudiobdd.model.TipoPasso.ENTAO;
import static br.edu.mackenzie.estudiobdd.model.TipoPasso.MAS;
import static br.edu.mackenzie.estudiobdd.model.TipoPasso.QUANDO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import br.edu.mackenzie.estudiobdd.model.TipoPasso;

class RegrasCenarioTest {

    @Test
    void rejeitaCenarioSemEntao() {
        assertThatThrownBy(() -> RegrasCenario.validar(List.of(DADO, QUANDO)))
                .isInstanceOfSatisfying(RegraNegocioException.class,
                        erro -> assertThat(erro.getStatus()).isEqualTo(UNPROCESSABLE_ENTITY));
    }

    @Test
    void rejeitaCenarioSemQuando() {
        assertThatThrownBy(() -> RegrasCenario.validar(List.of(DADO, ENTAO)))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void rejeitaPassosForaDeOrdem() {
        assertThatThrownBy(() -> RegrasCenario.validar(List.of(DADO, QUANDO, DADO, ENTAO)))
                .isInstanceOf(RegraNegocioException.class);
    }

    @ParameterizedTest
    @EnumSource(value = TipoPasso.class, names = {"E", "MAS"})
    void rejeitaConjuncaoNoInicio(TipoPasso conjuncao) {
        assertThatThrownBy(() -> RegrasCenario.validar(List.of(conjuncao, QUANDO, ENTAO)))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void aceitaQuandoEEntaoSemDado() {
        assertThatCode(() -> RegrasCenario.validar(List.of(QUANDO, ENTAO)))
                .doesNotThrowAnyException();
    }

    @Test
    void aceitaConjuncoesAposOsTiposQueElasContinuam() {
        assertThatCode(() -> RegrasCenario.validar(List.of(DADO, E, QUANDO, MAS, ENTAO, E)))
                .doesNotThrowAnyException();
    }
}
