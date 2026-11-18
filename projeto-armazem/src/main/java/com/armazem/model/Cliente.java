package com.armazem.model;

/** Usuário que consulta armazéns e faz pedidos. */
public class Cliente extends Usuario {

    private static final long serialVersionUID = 1L;

    public Cliente() {
        super();
    }

    public Cliente(String nome, String email, String senha) {
        super(nome, email, senha);
    }

    @Override
    public String getPerfil() {
        return "Cliente";
    }
}
