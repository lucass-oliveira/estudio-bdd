package br.edu.mackenzie.estudiobdd.model;

public enum TipoPasso {
    DADO("Dado"),
    QUANDO("Quando"),
    ENTAO("Então"),
    E("E"),
    MAS("Mas");

    private final String palavraChave;

    TipoPasso(String palavraChave) {
        this.palavraChave = palavraChave;
    }

    public String getPalavraChave() {
        return palavraChave;
    }
}
