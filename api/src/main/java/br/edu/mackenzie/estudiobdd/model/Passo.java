package br.edu.mackenzie.estudiobdd.model;

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
@Table(name = "passos")
public class Passo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cenario_id", nullable = false, updatable = false)
    private Cenario cenario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private TipoPasso tipo;

    @Column(nullable = false, length = 800)
    private String texto;

    @Column(nullable = false)
    private int posicao;

    protected Passo() {
    }

    public Passo(Cenario cenario, TipoPasso tipo, String texto, int posicao) {
        this.cenario = cenario;
        atualizar(tipo, texto, posicao);
    }

    public void atualizar(TipoPasso tipo, String texto, int posicao) {
        this.tipo = tipo;
        this.texto = texto;
        this.posicao = posicao;
    }

    public Long getId() { return id; }
    public TipoPasso getTipo() { return tipo; }
    public String getTexto() { return texto; }
    public int getPosicao() { return posicao; }
}
