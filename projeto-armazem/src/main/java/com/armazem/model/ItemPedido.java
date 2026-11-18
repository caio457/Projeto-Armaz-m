package com.armazem.model;

import java.io.Serializable;

/**
 * Item de um pedido: referencia um Produto (agregação) e guarda a quantidade pedida.
 * É criado pelo próprio Pedido, que o contém (composição).
 */
public class ItemPedido implements Serializable {

    private static final long serialVersionUID = 1L;

    private Produto produto;
    private int quantidade;

    public ItemPedido() { }

    public ItemPedido(Produto produto, int quantidade) {
        setProduto(produto);
        setQuantidade(quantidade);
    }

    public Produto getProduto() { return produto; }

    public void setProduto(Produto produto) {
        if (produto == null) {
            throw new IllegalArgumentException("O produto do item não pode ser nulo.");
        }
        this.produto = produto;
    }

    public int getQuantidade() { return quantidade; }

    /** RN07: a quantidade deve ser maior que zero e no máximo o estoque disponível. */
    public void setQuantidade(int quantidade) {
        Validador.maiorQueZero(quantidade, "A quantidade do item");
        if (produto != null && quantidade > produto.getQuantidadeEstoque()) {
            throw new RegraDeNegocioException("Quantidade indisponível de \"" + produto.getNome()
                    + "\": solicitado " + quantidade + ", em estoque " + produto.getQuantidadeEstoque() + ".");
        }
        this.quantidade = quantidade;
    }

    /** Soma quantidade a um item já existente (RN07), revalidando o estoque. */
    public void somarQuantidade(int extra) {
        Validador.maiorQueZero(extra, "A quantidade");
        setQuantidade(quantidade + extra);
    }

    /** RN07: preço + taxa de manuseio + embalagem especial (se o produto for Fragil), por unidade. */
    public static double valorUnitarioComTaxas(Produto produto) {
        double unitario = produto.getPrecoUnitario() + produto.calcularTaxaManuseio();
        if (produto instanceof Fragil fragil) {
            unitario += fragil.getTaxaEmbalagemEspecial();
        }
        return unitario;
    }

    public double getValorUnitarioComTaxas() {
        return produto == null ? 0 : valorUnitarioComTaxas(produto);
    }

    /** Valor do item = (preço + manuseio + embalagem) × quantidade. */
    public double getValor() {
        return getValorUnitarioComTaxas() * quantidade;
    }

    /** Tempo de separação do item (RN08): minutos por unidade × quantidade. */
    public int getTempoSeparacao() {
        return produto == null ? 0 : produto.getTempoSeparacao() * quantidade;
    }
}
