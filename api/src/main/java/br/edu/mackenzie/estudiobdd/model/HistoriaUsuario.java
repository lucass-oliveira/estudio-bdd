package br.edu.mackenzie.estudiobdd.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "historias", uniqueConstraints = @UniqueConstraint(
        name = "uk_historia_projeto_titulo", columnNames = {"projeto_id", "titulo"}))
public class HistoriaUsuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "projeto_id", nullable = false, updatable = false)
    private Projeto projeto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id", nullable = false, updatable = false)
    private Usuario autor;

    @Column(nullable = false, length = 160)
    private String titulo;

    @Column(nullable = false, length = 160)
    private String papel;

    @Column(nullable = false, length = 800)
    private String objetivo;

    @Column(nullable = false, length = 800)
    private String beneficio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Prioridade prioridade;

    @Column(nullable = false, updatable = false)
    private Instant criadaEm;

    @Column(nullable = false)
    private Instant atualizadaEm;

    @OneToMany(mappedBy = "historia", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("posicao ASC")
    private List<Cenario> cenarios = new ArrayList<>();

    @OneToMany(mappedBy = "historia", cascade = CascadeType.REMOVE)
    @OrderBy("id ASC")
    private List<Sugestao> sugestoes = new ArrayList<>();

    protected HistoriaUsuario() {
    }

    public HistoriaUsuario(Projeto projeto, Usuario autor, String titulo, String papel,
                           String objetivo, String beneficio, Prioridade prioridade) {
        this.projeto = projeto;
        this.autor = autor;
        this.criadaEm = Instant.now();
        atualizar(titulo, papel, objetivo, beneficio, prioridade);
    }

    public void atualizar(String titulo, String papel, String objetivo, String beneficio, Prioridade prioridade) {
        this.titulo = titulo;
        this.papel = papel;
        this.objetivo = objetivo;
        this.beneficio = beneficio;
        this.prioridade = prioridade;
        registrarAtualizacao();
    }

    public void registrarAtualizacao() { atualizadaEm = Instant.now(); }

    public Long getId() { return id; }
    public Projeto getProjeto() { return projeto; }
    public Usuario getAutor() { return autor; }
    public String getTitulo() { return titulo; }
    public String getPapel() { return papel; }
    public String getObjetivo() { return objetivo; }
    public String getBeneficio() { return beneficio; }
    public Prioridade getPrioridade() { return prioridade; }
    public Instant getCriadaEm() { return criadaEm; }
    public Instant getAtualizadaEm() { return atualizadaEm; }
    public List<Cenario> getCenarios() { return cenarios; }
    public List<Sugestao> getSugestoes() { return sugestoes; }
}
