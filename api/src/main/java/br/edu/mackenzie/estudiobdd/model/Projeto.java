package br.edu.mackenzie.estudiobdd.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "projetos")
public class Projeto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 160)
    private String nome;

    @Column(nullable = false, columnDefinition = "text")
    private String descricao;

    @Column(nullable = false, columnDefinition = "text")
    private String contextoDominio;

    @Column(nullable = false, updatable = false)
    private Instant criadoEm;

    @OneToMany(mappedBy = "projeto", cascade = CascadeType.REMOVE)
    @OrderBy("id ASC")
    private List<HistoriaUsuario> historias = new ArrayList<>();

    protected Projeto() {
    }

    public Projeto(String nome, String descricao, String contextoDominio) {
        atualizar(nome, descricao, contextoDominio);
        criadoEm = Instant.now();
    }

    public void atualizar(String nome, String descricao, String contextoDominio) {
        this.nome = nome;
        this.descricao = descricao;
        this.contextoDominio = contextoDominio;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public String getContextoDominio() { return contextoDominio; }
    public Instant getCriadoEm() { return criadoEm; }
    public List<HistoriaUsuario> getHistorias() { return historias; }
}
