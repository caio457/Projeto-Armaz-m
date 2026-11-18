package com.armazem.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Armazém: contém um Endereco (composição) e agrega uma lista de Produto.
 * Gera os códigos dos produtos (RN02) e controla a capacidade em kg (RN03).
 */
public class Armazem implements GerenciavelEstoque, Serializable {

    private static final long serialVersionUID = 1L;
    private static final double TOLERANCIA = 1e-9;

    private String nome = "";
    private final Endereco endereco;                          // composição: criado aqui dentro
    private double capacidadeMaxima;                          // em kg
    private final List<Produto> produtos = new ArrayList<>(); // agregação: produtos vêm de fora
    private int proximoCodigo = 1;

    public Armazem() {
        this.endereco = new Endereco();
    }

    public Armazem(String nome, String rua, String numero, String cidade, double capacidadeMaxima) {
        this.endereco = new Endereco(rua, numero, cidade);
        setNome(nome);
        setCapacidadeMaxima(capacidadeMaxima);
    }

    public String getNome() { return nome; }

    public void setNome(String nome) { this.nome = Validador.texto(nome, 3, "O nome do armazém"); }

    public Endereco getEndereco() { return endereco; }

    public double getCapacidadeMaxima() { return capacidadeMaxima; }

    /** RN03: a capacidade não pode ficar abaixo do espaço já ocupado. */
    public void setCapacidadeMaxima(double capacidadeMaxima) {
        Validador.maiorQueZero(capacidadeMaxima, "A capacidade máxima");
        if (capacidadeMaxima + TOLERANCIA < getCapacidadeOcupada()) {
            throw new RegraDeNegocioException(String.format(Validador.BR,
                    "A capacidade (%.2f kg) não pode ser menor que o espaço já ocupado (%.2f kg).",
                    capacidadeMaxima, getCapacidadeOcupada()));
        }
        this.capacidadeMaxima = capacidadeMaxima;
    }

    /** Atualiza todos os dados de uma vez: valida tudo antes de alterar qualquer atributo. */
    public void atualizarDados(String nome, String rua, String numero, String cidade, double capacidadeMaxima) {
        new Armazem(nome, rua, numero, cidade, capacidadeMaxima);  // objeto descartável, só para validar
        setCapacidadeMaxima(capacidadeMaxima);                      // pode ser recusada pela RN03
        setNome(nome);
        endereco.setRua(rua);
        endereco.setNumero(numero);
        endereco.setCidade(cidade);
    }

    /** Soma de (peso × quantidade em estoque) de todos os produtos. */
    public double getCapacidadeOcupada() {
        double total = 0;
        for (Produto produto : produtos) {
            total += produto.getPesoTotalEmEstoque();
        }
        return total;
    }

    public double getEspacoDisponivel() {
        return capacidadeMaxima - getCapacidadeOcupada();
    }

    @Override
    public void adicionarProduto(Produto produto) {
        if (produto == null) {
            throw new IllegalArgumentException("O produto não pode ser nulo.");
        }
        if (produtos.contains(produto)) {
            throw new RegraDeNegocioException("Este produto já foi adicionado ao armazém.");
        }
        verificarCapacidade(produto.getPesoTotalEmEstoque());
        produto.setCodigo(proximoCodigo++);   // RN02: sequencial a partir de 1
        produtos.add(produto);
    }

    /** Substitui o produto de código informado, preservando o código (RN02) e respeitando a RN03. */
    @Override
    public void atualizarProduto(int codigo, Produto novoProduto) {
        if (novoProduto == null) {
            throw new IllegalArgumentException("O produto não pode ser nulo.");
        }
        int indice = indiceDe(codigo);
        if (indice < 0) {
            throw new RegraDeNegocioException("Produto de código " + codigo + " não encontrado.");
        }
        double aumento = novoProduto.getPesoTotalEmEstoque() - produtos.get(indice).getPesoTotalEmEstoque();
        if (aumento > 0) {
            verificarCapacidade(aumento);
        }
        novoProduto.setCodigo(codigo);
        produtos.set(indice, novoProduto);
    }

    @Override
    public boolean removerProduto(int codigo) {
        return produtos.removeIf(produto -> produto.getCodigo() == codigo);
    }

    @Override
    public Produto buscarProduto(int codigo) {
        int indice = indiceDe(codigo);
        return indice < 0 ? null : produtos.get(indice);
    }

    @Override
    public List<Produto> listarProdutos() {
        return Collections.unmodifiableList(new ArrayList<>(produtos));
    }

    /** Reabastecimento simples. Produtos vencidos exigem a validade do novo lote. */
    public void reabastecerProduto(int codigo, int quantidade) {
        Produto produto = obterProduto(codigo);
        if (produto.isVencido()) {
            throw new RegraDeNegocioException("O produto está vencido: informe a validade do novo lote.");
        }
        Validador.maiorQueZero(quantidade, "A quantidade de reabastecimento");
        verificarCapacidade(produto.getPeso() * quantidade);
        produto.reabastecer(quantidade);
    }

    /** Sobrecarga para perecíveis: reabastece informando a validade do novo lote. */
    public void reabastecerProduto(int codigo, int quantidade, LocalDate novaValidade) {
        Produto produto = obterProduto(codigo);
        if (!(produto instanceof ProdutoPerecivel perecivel)) {
            throw new RegraDeNegocioException("Somente produtos perecíveis têm validade de lote.");
        }
        Validador.maiorQueZero(quantidade, "A quantidade de reabastecimento");
        verificarCapacidade(produto.getPeso() * quantidade);
        perecivel.reabastecer(quantidade, novaValidade);
    }

    /** RN04: produtos com quantidade menor ou igual ao estoque mínimo. */
    public List<Produto> getProdutosComEstoqueBaixo() {
        return produtos.stream().filter(Produto::isEstoqueBaixo).toList();
    }

    /** RN04: perecíveis vencidos. */
    public List<Produto> getProdutosVencidos() {
        return produtos.stream().filter(Produto::isVencido).toList();
    }

    /** RN04: produtos que o Cliente pode ver (vencidos ficam de fora). */
    public List<Produto> getProdutosDisponiveis() {
        return produtos.stream().filter(produto -> !produto.isVencido()).toList();
    }

    private Produto obterProduto(int codigo) {
        Produto produto = buscarProduto(codigo);
        if (produto == null) {
            throw new RegraDeNegocioException("Produto de código " + codigo + " não encontrado.");
        }
        return produto;
    }

    private int indiceDe(int codigo) {
        for (int i = 0; i < produtos.size(); i++) {
            if (produtos.get(i).getCodigo() == codigo) {
                return i;
            }
        }
        return -1;
    }

    /** RN03: recusa a operação se o peso adicional não couber, informando o espaço livre. */
    private void verificarCapacidade(double pesoAdicional) {
        if (pesoAdicional > getEspacoDisponivel() + TOLERANCIA) {
            throw new RegraDeNegocioException(String.format(Validador.BR,
                    "Capacidade do armazém excedida. Espaço ainda disponível: %.2f kg.", getEspacoDisponivel()));
        }
    }

    @Override
    public String toString() {
        return nome + " (" + endereco.getCidade() + ")";
    }
}
