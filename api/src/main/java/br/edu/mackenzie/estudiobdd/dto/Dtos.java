package br.edu.mackenzie.estudiobdd.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import br.edu.mackenzie.estudiobdd.model.DecisaoSugestao;
import br.edu.mackenzie.estudiobdd.model.MotivoRejeicao;
import br.edu.mackenzie.estudiobdd.model.OrigemCenario;
import br.edu.mackenzie.estudiobdd.model.Prioridade;
import br.edu.mackenzie.estudiobdd.model.TipoPasso;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public final class Dtos {
    private static final String LINHA = "[^\\p{Cntrl}\\p{Zl}\\p{Zp}]*";

    private Dtos() {
    }

    public record UsuarioDados(
            @NotBlank @Size(max = 120) String nome,
            @NotBlank @Email @Size(max = 254) String email) {}

    public record UsuarioResposta(Long id, String nome, String email) {}

    public record ProjetoDados(
            @NotBlank @Size(max = 160) String nome,
            @Size(max = 4000) String descricao,
            @Size(max = 8000) String contextoDominio) {}

    public record ProjetoResposta(Long id, String nome, String descricao, String contextoDominio, Instant criadoEm) {}

    public record ProjetoDetalhe(Long id, String nome, String descricao, String contextoDominio, Instant criadoEm,
                                  List<HistoriaResumo> historias) {}

    public record HistoriaDados(
            @NotBlank @Size(max = 160) @Pattern(regexp = LINHA) String titulo,
            @NotBlank @Size(max = 160) @Pattern(regexp = LINHA) String papel,
            @NotBlank @Size(max = 800) @Pattern(regexp = LINHA) String objetivo,
            @NotBlank @Size(max = 800) @Pattern(regexp = LINHA) String beneficio,
            @NotNull Prioridade prioridade) {}

    public record HistoriaResumo(Long id, String titulo, String papel, String objetivo, String beneficio,
                                  Prioridade prioridade, UsuarioResposta autor, Instant criadaEm,
                                  Instant atualizadaEm, int quantidadeCenarios, long quantidadeSugestoesPendentes) {}

    public record HistoriaDetalhe(Long id, String titulo, String papel, String objetivo, String beneficio,
                                   Prioridade prioridade, UsuarioResposta autor, Instant criadaEm,
                                   Instant atualizadaEm, int quantidadeCenarios, long quantidadeSugestoesPendentes,
                                   List<CenarioResposta> cenarios) {}

    public record CenarioDados(
            @NotBlank @Size(max = 160) @Pattern(regexp = LINHA) String titulo,
            @NotNull OrigemCenario origem,
            @Positive Integer posicao,
            @NotEmpty @Size(max = 100) List<@NotNull @Valid PassoDados> passos) {}

    public record CenarioResposta(Long id, String titulo, OrigemCenario origem, int posicao, List<PassoResposta> passos) {}

    public record PassoDados(
            @Positive Long id,
            @NotNull TipoPasso tipo,
            @NotBlank @Size(max = 800) @Pattern(regexp = LINHA) String texto,
            @Positive Integer posicao) {}

    public record PassoResposta(Long id, TipoPasso tipo, String texto, int posicao) {}

    public record SugestaoDados(
            @NotBlank @Size(max = 120) String ferramenta,
            @NotBlank @Size(max = 12000) String prompt,
            @NotBlank @Size(max = 20000) String conteudo) {}

    public record DecisaoDados(
            @NotNull DecisaoSugestao decisao,
            MotivoRejeicao motivoRejeicao,
            @NotBlank @Size(max = 1500) String justificativa) {}

    public record SugestaoResposta(Long id, String ferramenta, String prompt, String conteudo, DecisaoSugestao decisao,
                                    MotivoRejeicao motivoRejeicao, String justificativa, UsuarioResposta registradaPor,
                                    UsuarioResposta decididaPor, Instant registradaEm, Instant decididaEm) {}

    public record ResumoProjeto(int quantidadeHistorias, Map<OrigemCenario, Long> cenariosPorOrigem,
                                 Map<DecisaoSugestao, Long> sugestoesPorDecisao, List<MotivoQuantidade> motivosRejeicao) {}

    public record MotivoQuantidade(MotivoRejeicao motivo, long quantidade) {}

    public record ArquivoFeature(String nome, String conteudo) {}
}
