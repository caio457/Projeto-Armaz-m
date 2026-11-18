package com.armazem.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pedido de um Cliente em um Armazem (agregação: ambos existem independentemente).
 * Os itens são criados e pertencem ao próprio pedido (composição).
 */
public class Pedido implements Serializable {

    private static final long serialVersionUID = 1L;

    private int numero;
    private Cliente cliente;
    private Armazem armazem;
    private final List<ItemPedido> itens = new ArrayList<>();
    private StatusPedido status = StatusPedido.ABERTO;
    private final LocalDateTime dataCriacao = LocalDateTime.now();
    private LocalDateTime dataFinalizacao;

    public Pedido() { }

    public Pedido(Cliente cliente, Armazem armazem) {
        setCliente(cliente);
        setArmazem(armazem);
    }

    public int getNumero() { return numero; }

    /** O número é atribuído pelo SistemaArmazem ao registrar o pedido (mesmo pacote). */
    void setNumero(int numero) { this.numero = numero; }

    public Cliente getCliente() { return cliente; }

    public void setCliente(Cliente cliente) {
        exigirAberto();
        if (cliente == null) {
            throw new IllegalArgumentException("O cliente do pedido não pode ser nulo.");
        }
        this.cliente = cliente;
    }

    public Armazem getArmazem() { return armazem; }

    public void setArmazem(Armazem armazem) {
        exigirAberto();
        if (armazem == null) {
            throw new IllegalArgumentException("O armazém do pedido não pode ser nulo.");
        }
        this.armazem = armazem;
    }

    public StatusPedido getStatus() { return status; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }

    public LocalDateTime getDataFinalizacao() { return dataFinalizacao; }

    public List<ItemPedido> getItens() { return Collections.unmodifiableList(itens); }

    /** RN07: adiciona o produto ao pedido; se já existir, soma a quantidade ao item. */
    public void adicionarItem(Produto produto, int quantidade) {
        exigirAberto();
        if (armazem == null) {
            throw new RegraDeNegocioException("O pedido ainda não tem armazém.");
        }
        if (produto == null) {
            throw new IllegalArgumentException("Selecione um produto.");
        }
        if (armazem.buscarProduto(produto.getCodigo()) != produto) {
            throw new RegraDeNegocioException("O produto não pertence ao armazém deste pedido.");
        }
        if (produto.isVencido()) {
            throw new RegraDeNegocioException("O produto \"" + produto.getNome() + "\" está vencido.");
        }
        ItemPedido existente = buscarItem(produto);
        if (existente != null) {
            existente.somarQuantidade(quantidade);
        } else {
            itens.add(new ItemPedido(produto, quantidade));
        }
    }

    public void removerItem(ItemPedido item) {
        exigirAberto();
        itens.remove(item);
    }

    /** RN08: total do pedido = soma dos valores dos itens. */
    public double calcularTotal() {
        double total = 0;
        for (ItemPedido item : itens) {
            total += item.getValor();
        }
        return total;
    }

    /** RN08: tempo estimado de espera, em minutos. */
    public int calcularTempoEspera() {
        int minutos = 0;
        for (ItemPedido item : itens) {
            minutos += item.getTempoSeparacao();
        }
        return minutos;
    }

    /** Instruções de manuseio dos itens frágeis, obtidas pela interface Fragil. */
    public List<String> getInstrucoesManuseio() {
        List<String> instrucoes = new ArrayList<>();
        for (ItemPedido item : itens) {
            if (item.getProduto() instanceof Fragil fragil) {
                instrucoes.add(item.getProduto().getNome() + ": " + fragil.getInstrucoesManuseio());
            }
        }
        return instrucoes;
    }

    /**
     * RN09: finaliza o pedido. Primeiro confere tudo; só depois dá baixa no estoque,
     * de modo que uma falha não deixe o estoque pela metade.
     */
    public void finalizar() {
        exigirAberto();
        if (itens.isEmpty()) {
            throw new RegraDeNegocioException("O pedido precisa ter pelo menos um item.");
        }
        for (ItemPedido item : itens) {
            Produto produto = item.getProduto();
            if (armazem.buscarProduto(produto.getCodigo()) != produto) {
                throw new RegraDeNegocioException("O produto \"" + produto.getNome() + "\" não está mais disponível.");
            }
            if (produto.isVencido()) {
                throw new RegraDeNegocioException("O produto \"" + produto.getNome() + "\" está vencido.");
            }
            if (item.getQuantidade() > produto.getQuantidadeEstoque()) {
                throw new RegraDeNegocioException("Estoque insuficiente para \"" + produto.getNome()
                        + "\": pedido " + item.getQuantidade() + ", disponível " + produto.getQuantidadeEstoque() + ".");
            }
        }
        for (ItemPedido item : itens) {
            item.getProduto().retirarDoEstoque(item.getQuantidade());
        }
        status = StatusPedido.FINALIZADO;
        dataFinalizacao = LocalDateTime.now();
    }

    private ItemPedido buscarItem(Produto produto) {
        for (ItemPedido item : itens) {
            if (item.getProduto() == produto) {
                return item;
            }
        }
        return null;
    }

    private void exigirAberto() {
        if (status != StatusPedido.ABERTO) {
            throw new RegraDeNegocioException("Um pedido finalizado não pode mais ser alterado.");
        }
    }
}
