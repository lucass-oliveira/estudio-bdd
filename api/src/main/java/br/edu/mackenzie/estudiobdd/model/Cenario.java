package br.edu.mackenzie.estudiobdd.model;

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
@Table(name = "cenarios", uniqueConstraints = @UniqueConstraint(
        name = "uk_cenario_historia_titulo", columnNames = {"historia_id", "titulo"}))
public class Cenario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "historia_id", nullable = false, updatable = false)
    private HistoriaUsuario historia;

    @Column(nullable = false, length = 160)
    private String titulo;

    @Column(nullable = false)
    private int posicao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OrigemCenario origem;

    @OneToMany(mappedBy = "cenario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("posicao ASC")
    private List<Passo> passos = new ArrayList<>();

    protected Cenario() {
    }

    public Cenario(HistoriaUsuario historia, String titulo, OrigemCenario origem) {
        this.historia = historia;
        atualizar(titulo, origem);
    }

    public void atualizar(String titulo, OrigemCenario origem) {
        this.titulo = titulo;
        this.origem = origem;
    }

    public void setPosicao(int posicao) { this.posicao = posicao; }

    public Long getId() { return id; }
    public HistoriaUsuario getHistoria() { return historia; }
    public String getTitulo() { return titulo; }
    public int getPosicao() { return posicao; }
    public OrigemCenario getOrigem() { return origem; }
    public List<Passo> getPassos() { return passos; }
}
