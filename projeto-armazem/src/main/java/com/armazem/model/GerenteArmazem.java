package com.armazem.model;

/** Usuário que cadastra e administra um armazém (agregação: apenas referencia o Armazem). */
public class GerenteArmazem extends Usuario {

    private static final long serialVersionUID = 1L;

    private Armazem armazem;

    public GerenteArmazem() {
        super();
    }

    public GerenteArmazem(String nome, String email, String senha) {
        super(nome, email, senha);
    }

    public GerenteArmazem(String nome, String email, String senha, Armazem armazem) {
        this(nome, email, senha);
        setArmazem(armazem);
    }

    public Armazem getArmazem() { return armazem; }

    public void setArmazem(Armazem armazem) {
        if (armazem == null) {
            throw new IllegalArgumentException("O armazém não pode ser nulo.");
        }
        this.armazem = armazem;
    }

    @Override
    public String getPerfil() {
        return "Gerente do Armazém";
    }
}
