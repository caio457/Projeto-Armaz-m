package com.armazem.model;

import java.time.LocalDate;

/** Produto perecível alimentício. Taxa de manuseio: 2% do preço (RN05). */
public class Alimento extends ProdutoPerecivel {

    private static final long serialVersionUID = 1L;
    private static final double PERCENTUAL_MANUSEIO = 0.02;

    private String categoria = "";
    private boolean organico;

    public Alimento() {
        super();
    }

    public Alimento(String nome, String descricao, double precoUnitario, double peso,
                    int quantidadeEstoque, int estoqueMinimo, int tempoSeparacao,
                    LocalDate dataValidade, double temperaturaConservacao,
                    String categoria, boolean organico) {
        super(nome, descricao, precoUnitario, peso, quantidadeEstoque, estoqueMinimo, tempoSeparacao,
                dataValidade, temperaturaConservacao);
        setCategoria(categoria);
        setOrganico(organico);
    }

    public String getCategoria() { return categoria; }

    public void setCategoria(String categoria) { this.categoria = Validador.texto(categoria, 1, "A categoria"); }

    public boolean isOrganico() { return organico; }

    public void setOrganico(boolean organico) { this.organico = organico; }

    @Override
    public double calcularTaxaManuseio() {
        return arredondar(getPrecoUnitario() * PERCENTUAL_MANUSEIO);
    }

    @Override
    public String exibirDetalhes() {
        return super.exibirDetalhes() + " | Categoria: " + categoria + " | " + (organico ? "Orgânico" : "Convencional");
    }

    @Override
    public String getTipo() {
        return "Alimento";
    }
}
