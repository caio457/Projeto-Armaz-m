package com.armazem.model;

/** Violação de uma regra de negócio (capacidade, estoque, pedido vazio etc.). */
public class RegraDeNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
