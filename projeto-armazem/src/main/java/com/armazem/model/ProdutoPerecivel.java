package com.armazem.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Produto com data de validade e temperatura de conservação (nível intermediário da hierarquia). */
public abstract class ProdutoPerecivel extends Produto {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private LocalDate dataValidade;
    private double temperaturaConservacao;

    protected ProdutoPerecivel() {
        super();
    }

    protected ProdutoPerecivel(String nome, String descricao, double precoUnitario, double peso,
                               int quantidadeEstoque, int estoqueMinimo, int tempoSeparacao,
                               LocalDate dataValidade, double temperaturaConservacao) {
        super(nome, descricao, precoUnitario, peso, quantidadeEstoque, estoqueMinimo, tempoSeparacao);
        setDataValidade(dataValidade);
        setTemperaturaConservacao(temperaturaConservacao);
    }

    public LocalDate getDataValidade() { return dataValidade; }

    /** RN04: perecíveis não podem ser cadastrados com validade no passado. */
    public void setDataValidade(LocalDate dataValidade) {
        this.dataValidade = validarValidade(dataValidade);
    }

    public double getTemperaturaConservacao() { return temperaturaConservacao; }

    public void setTemperaturaConservacao(double temperaturaConservacao) {
        if (Double.isNaN(temperaturaConservacao) || Double.isInfinite(temperaturaConservacao)) {
            throw new IllegalArgumentException("A temperatura de conservação é inválida.");
        }
        this.temperaturaConservacao = temperaturaConservacao;
    }

    /**
     * Sobrecarga de reabastecer(int): além da quantidade, recebe a validade do novo lote.
     * Modelo simplificado: o produto guarda uma única validade, que passa a ser a do lote novo.
     */
    public void reabastecer(int quantidade, LocalDate novaValidade) {
        validarValidade(novaValidade);       // valida antes de alterar qualquer coisa
        super.reabastecer(quantidade);       // reaproveita a regra da superclasse
        this.dataValidade = novaValidade;
    }

    @Override
    public boolean isVencido() {
        return dataValidade != null && dataValidade.isBefore(LocalDate.now());
    }

    @Override
    public String exibirDetalhes() {
        return detalhesComuns() + String.format(Validador.BR, " | Validade: %s | Conservação: %.1f °C",
                dataValidade == null ? "-" : FORMATO_DATA.format(dataValidade), temperaturaConservacao);
    }

    private static LocalDate validarValidade(LocalDate data) {
        if (data == null) {
            throw new IllegalArgumentException("A data de validade é obrigatória.");
        }
        if (data.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("A data de validade não pode estar no passado.");
        }
        return data;
    }
}
