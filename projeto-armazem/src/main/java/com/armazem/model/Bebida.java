package com.armazem.model;

import java.time.LocalDate;

/** Produto perecível líquido e frágil. Taxa de manuseio: 3% do preço (RN05). */
public class Bebida extends ProdutoPerecivel implements Fragil {

    private static final long serialVersionUID = 1L;
    private static final double PERCENTUAL_MANUSEIO = 0.03;

    private int volumeMl;
    private boolean alcoolica;

    public Bebida() {
        super();
    }

    public Bebida(String nome, String descricao, double precoUnitario, double peso,
                  int quantidadeEstoque, int estoqueMinimo, int tempoSeparacao,
                  LocalDate dataValidade, double temperaturaConservacao,
                  int volumeMl, boolean alcoolica) {
        super(nome, descricao, precoUnitario, peso, quantidadeEstoque, estoqueMinimo, tempoSeparacao,
                dataValidade, temperaturaConservacao);
        setVolumeMl(volumeMl);
        setAlcoolica(alcoolica);
    }

    public int getVolumeMl() { return volumeMl; }

    public void setVolumeMl(int volumeMl) { this.volumeMl = Validador.maiorQueZero(volumeMl, "O volume"); }

    public boolean isAlcoolica() { return alcoolica; }

    public void setAlcoolica(boolean alcoolica) { this.alcoolica = alcoolica; }

    @Override
    public double calcularTaxaManuseio() {
        return arredondar(getPrecoUnitario() * PERCENTUAL_MANUSEIO);
    }

    @Override
    public double getTaxaEmbalagemEspecial() {
        return TAXA_EMBALAGEM_ESPECIAL;
    }

    @Override
    public String getInstrucoesManuseio() {
        return "Transportar na vertical, evitar impactos e proteger do calor.";
    }

    @Override
    public String exibirDetalhes() {
        return super.exibirDetalhes() + " | Volume: " + volumeMl + " ml | " + (alcoolica ? "Alcoólica" : "Não alcoólica");
    }

    @Override
    public String getTipo() {
        return "Bebida";
    }
}
