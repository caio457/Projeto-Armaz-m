package com.armazem.model;

import java.io.IOException;

/** Contrato de quem sabe gravar e recarregar seus dados (RN10). */
public interface Persistivel {

    void salvar() throws IOException;

    void carregar() throws IOException, ClassNotFoundException;
}
