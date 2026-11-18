package com.armazem.controller;

import com.armazem.model.Cliente;
import com.armazem.model.GerenteArmazem;

/** Permite aos controladores trocar de tela sem conhecer a classe principal da aplicação. */
public interface Navegador {

    void irParaLogin();

    void irParaGerente(GerenteArmazem gerente);

    void irParaCliente(Cliente cliente);
}
