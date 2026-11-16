package br.edu.mackenzie.estudiobdd.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "sugestoes")
public class Sugestao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "historia_id", nullable = false, updatable = false)
    private HistoriaUsuario historia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrada_por_id", nullable = false, updatable = false)
    private Usuario registradaPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decidida_por_id")
    private Usuario decididaPor;

    @Column(nullable = false, length = 120)
    private String ferramenta;

    @Column(nullable = false, columnDefinition = "text")
    private String prompt;

    @Column(nullable = false, columnDefinition = "text")
    private String conteudo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DecisaoSugestao decisao;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private MotivoRejeicao motivoRejeicao;

    @Column(length = 1500)
    private String justificativa;

    @Column(nullable = false, updatable = false)
    private Instant registradaEm;

    private Instant decididaEm;

    protected Sugestao() {
    }

    public Sugestao(HistoriaUsuario historia, Usuario registradaPor, String ferramenta, String prompt, String conteudo) {
        this.historia = historia;
        this.registradaPor = registradaPor;
        this.ferramenta = ferramenta;
        this.prompt = prompt;
        this.conteudo = conteudo;
        this.decisao = DecisaoSugestao.PENDENTE;
        this.registradaEm = Instant.now();
    }

    public void decidir(Usuario decididaPor, DecisaoSugestao decisao, MotivoRejeicao motivoRejeicao,
                        String justificativa) {
        this.decididaPor = decididaPor;
        this.decisao = decisao;
        this.motivoRejeicao = motivoRejeicao;
        this.justificativa = justificativa;
        this.decididaEm = Instant.now();
    }

    public Long getId() { return id; }
    public HistoriaUsuario getHistoria() { return historia; }
    public Usuario getRegistradaPor() { return registradaPor; }
    public Usuario getDecididaPor() { return decididaPor; }
    public String getFerramenta() { return ferramenta; }
    public String getPrompt() { return prompt; }
    public String getConteudo() { return conteudo; }
    public DecisaoSugestao getDecisao() { return decisao; }
    public MotivoRejeicao getMotivoRejeicao() { return motivoRejeicao; }
    public String getJustificativa() { return justificativa; }
    public Instant getRegistradaEm() { return registradaEm; }
    public Instant getDecididaEm() { return decididaEm; }
}
