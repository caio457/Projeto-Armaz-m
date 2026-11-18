package com.armazem.model;

import java.io.Serializable;

/** Endereço de um armazém. Existe apenas dentro do Armazem que o contém (composição). */
public class Endereco implements Serializable {

    private static final long serialVersionUID = 1L;

    private String rua = "";
    private String numero = "";
    private String cidade = "";

    public Endereco() { }

    public Endereco(String rua, String numero, String cidade) {
        setRua(rua);
        setNumero(numero);
        setCidade(cidade);
    }

    public String getRua() { return rua; }

    public void setRua(String rua) { this.rua = Validador.texto(rua, 3, "A rua"); }

    public String getNumero() { return numero; }

    public void setNumero(String numero) { this.numero = Validador.texto(numero, 1, "O número"); }

    public String getCidade() { return cidade; }

    public void setCidade(String cidade) { this.cidade = Validador.texto(cidade, 3, "A cidade"); }

    @Override
    public String toString() {
        return rua + ", " + numero + " - " + cidade;
    }
}
