package com.armazem.model;

/** Produto durável e frágil. Taxa de manuseio: 5% do preço (RN05). */
public class Eletronico extends Produto implements Fragil {

    private static final long serialVersionUID = 1L;
    private static final double PERCENTUAL_MANUSEIO = 0.05;

    private int voltagem = 110;
    private int garantiaMeses;

    public Eletronico() {
        super();
    }

    public Eletronico(String nome, String descricao, double precoUnitario, double peso,
                      int quantidadeEstoque, int estoqueMinimo, int tempoSeparacao,
                      int voltagem, int garantiaMeses) {
        super(nome, descricao, precoUnitario, peso, quantidadeEstoque, estoqueMinimo, tempoSeparacao);
        setVoltagem(voltagem);
        setGarantiaMeses(garantiaMeses);
    }

    public int getVoltagem() { return voltagem; }

    /** RN01: a voltagem deve ser 110 ou 220. */
    public void setVoltagem(int voltagem) {
        if (voltagem != 110 && voltagem != 220) {
            throw new IllegalArgumentException("A voltagem deve ser 110 ou 220.");
        }
        this.voltagem = voltagem;
    }

    public int getGarantiaMeses() { return garantiaMeses; }

    /** RN01: garantia entre 0 e 60 meses. */
    public void setGarantiaMeses(int garantiaMeses) {
        this.garantiaMeses = Validador.entre(garantiaMeses, 0, 60, "A garantia (meses)");
    }

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
        return "Não empilhar, proteger de impactos e de umidade.";
    }

    @Override
    public String exibirDetalhes() {
        return detalhesComuns() + " | Voltagem: " + voltagem + " V | Garantia: " + garantiaMeses + " meses";
    }

    @Override
    public String getTipo() {
        return "Eletrônico";
    }
}
