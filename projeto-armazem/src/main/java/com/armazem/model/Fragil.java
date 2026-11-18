package com.armazem.model;

/**
 * Contrato dos produtos frágeis (RN06). É implementada por Eletronico e Bebida,
 * que pertencem a ramos diferentes da hierarquia de Produto.
 */
public interface Fragil {

    /** Taxa de embalagem especial, por unidade (RN06). */
    double TAXA_EMBALAGEM_ESPECIAL = 4.00;

    double getTaxaEmbalagemEspecial();

    String getInstrucoesManuseio();
}
