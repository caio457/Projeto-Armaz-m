package com.armazem.model;

import java.util.List;

/** Contrato de gerenciamento dos produtos de um estoque. Implementada por Armazem. */
public interface GerenciavelEstoque {

    void adicionarProduto(Produto produto);

    void atualizarProduto(int codigo, Produto novoProduto);

    boolean removerProduto(int codigo);

    Produto buscarProduto(int codigo);

    List<Produto> listarProdutos();
}
