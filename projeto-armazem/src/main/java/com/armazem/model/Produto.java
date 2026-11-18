package com.armazem.model;

import java.io.Serializable;

/**
 * Superclasse abstrata de todos os produtos do armazém.
 * Cada tipo concreto define sua taxa de manuseio (RN05), seus detalhes e seu tipo.
 */
public abstract class Produto implements Serializable {

    private static final long serialVersionUID = 1L;

    private int codigo;
    private String nome = "";
    private String descricao = "";
    private double precoUnitario;
    private double peso;
    private int quantidadeEstoque;
    private int estoqueMinimo;
    private int tempoSeparacao;

    protected Produto() { }

    protected Produto(String nome, String descricao, double precoUnitario, double peso,
                      int quantidadeEstoque, int estoqueMinimo, int tempoSeparacao) {
        setNome(nome);
        setDescricao(descricao);
        setPrecoUnitario(precoUnitario);
        setPeso(peso);
        setQuantidadeEstoque(quantidadeEstoque);
        setEstoqueMinimo(estoqueMinimo);
        setTempoSeparacao(tempoSeparacao);
    }

    public int getCodigo() { return codigo; }

    /** RN02: o código é atribuído somente pelo Armazem (mesmo pacote). */
    void setCodigo(int codigo) { this.codigo = codigo; }

    public String getNome() { return nome; }

    public void setNome(String nome) { this.nome = Validador.texto(nome, 3, "O nome do produto"); }

    public String getDescricao() { return descricao; }

    public void setDescricao(String descricao) { this.descricao = descricao == null ? "" : descricao.trim(); }

    public double getPrecoUnitario() { return precoUnitario; }

    public void setPrecoUnitario(double precoUnitario) {
        this.precoUnitario = Validador.maiorQueZero(precoUnitario, "O preço unitário");
    }

    public double getPeso() { return peso; }

    public void setPeso(double peso) { this.peso = Validador.maiorQueZero(peso, "O peso"); }

    public int getQuantidadeEstoque() { return quantidadeEstoque; }

    public void setQuantidadeEstoque(int quantidadeEstoque) {
        this.quantidadeEstoque = Validador.naoNegativo(quantidadeEstoque, "A quantidade em estoque");
    }

    public int getEstoqueMinimo() { return estoqueMinimo; }

    public void setEstoqueMinimo(int estoqueMinimo) {
        this.estoqueMinimo = Validador.naoNegativo(estoqueMinimo, "O estoque mínimo");
    }

    /** Tempo de separação em minutos, por unidade. */
    public int getTempoSeparacao() { return tempoSeparacao; }

    public void setTempoSeparacao(int tempoSeparacao) {
        this.tempoSeparacao = Validador.maiorQueZero(tempoSeparacao, "O tempo de separação");
    }

    /** Registra a entrada de novas unidades no estoque. */
    public void reabastecer(int quantidade) {
        Validador.maiorQueZero(quantidade, "A quantidade de reabastecimento");
        quantidadeEstoque += quantidade;
    }

    /** Dá baixa no estoque (usado ao finalizar um pedido). */
    public void retirarDoEstoque(int quantidade) {
        Validador.maiorQueZero(quantidade, "A quantidade retirada");
        if (quantidade > quantidadeEstoque) {
            throw new RegraDeNegocioException("Estoque insuficiente de \"" + nome + "\": solicitado "
                    + quantidade + ", disponível " + quantidadeEstoque + ".");
        }
        quantidadeEstoque -= quantidade;
    }

    /** RN04: estoque baixo quando a quantidade é menor ou igual ao estoque mínimo. */
    public boolean isEstoqueBaixo() { return quantidadeEstoque <= estoqueMinimo; }

    /** Produtos duráveis nunca vencem; os perecíveis sobrescrevem este método. */
    public boolean isVencido() { return false; }

    public double getPesoTotalEmEstoque() { return peso * quantidadeEstoque; }

    /** RN05: taxa de manuseio por unidade, calculada por cada tipo de produto. */
    public abstract double calcularTaxaManuseio();

    /** Texto com os detalhes do produto, complementado em cada nível da hierarquia. */
    public abstract String exibirDetalhes();

    /** Nome do tipo do produto, para exibição. */
    public abstract String getTipo();

    /** Detalhes comuns a todos os produtos, reaproveitados pelas subclasses. */
    protected String detalhesComuns() {
        return String.format(Validador.BR,
                "[%d] %s - %s | Preço: R$ %.2f | Peso: %.2f kg | Estoque: %d (mín. %d) | Separação: %d min/un",
                codigo, nome, descricao, precoUnitario, peso, quantidadeEstoque, estoqueMinimo, tempoSeparacao);
    }

    /** Arredonda para centavos. */
    protected static double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    @Override
    public String toString() {
        return nome;
    }
}
