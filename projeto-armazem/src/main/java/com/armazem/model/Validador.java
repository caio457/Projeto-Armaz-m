package com.armazem.model;

import java.util.Locale;

/**
 * Regras de validação de dados (RN01), reutilizadas pelos métodos set e pelos
 * construtores parametrizados. Um valor inválido lança IllegalArgumentException
 * antes de qualquer atributo ser alterado.
 */
final class Validador {

    /** Locale usado nas mensagens e nos detalhes dos produtos. */
    static final Locale BR = Locale.forLanguageTag("pt-BR");

    private Validador() { }

    static String texto(String valor, int minimo, String campo) {
        if (valor == null || valor.trim().length() < minimo) {
            throw new IllegalArgumentException(campo + " deve ter no mínimo " + minimo + " caractere(s).");
        }
        return valor.trim();
    }

    static double maiorQueZero(double valor, String campo) {
        if (Double.isNaN(valor) || Double.isInfinite(valor) || valor <= 0) {
            throw new IllegalArgumentException(campo + " deve ser maior que zero.");
        }
        return valor;
    }

    static int maiorQueZero(int valor, String campo) {
        if (valor <= 0) {
            throw new IllegalArgumentException(campo + " deve ser maior que zero.");
        }
        return valor;
    }

    static int naoNegativo(int valor, String campo) {
        if (valor < 0) {
            throw new IllegalArgumentException(campo + " deve ser maior ou igual a zero.");
        }
        return valor;
    }

    static int entre(int valor, int minimo, int maximo, String campo) {
        if (valor < minimo || valor > maximo) {
            throw new IllegalArgumentException(campo + " deve estar entre " + minimo + " e " + maximo + ".");
        }
        return valor;
    }

    static String email(String valor) {
        if (valor == null || !valor.contains("@")) {
            throw new IllegalArgumentException("O e-mail deve conter \"@\".");
        }
        return valor.trim();
    }

    static String senha(String valor) {
        if (valor == null || valor.length() < 6) {
            throw new IllegalArgumentException("A senha deve ter no mínimo 6 caracteres.");
        }
        return valor;
    }
}
