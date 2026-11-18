package com.armazem.controller;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Formatação de valores para exibição e leitura de números digitados nos campos de texto. */
public final class Formatos {

    private static final Locale BR = Locale.forLanguageTag("pt-BR");
    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(BR);
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Formatos() { }

    public static String moeda(double valor) {
        return MOEDA.format(valor);
    }

    public static String numero(double valor) {
        return String.format(BR, "%.2f", valor);
    }

    public static String data(LocalDate data) {
        return data == null ? "-" : DATA.format(data);
    }

    public static String dataHora(LocalDateTime dataHora) {
        return dataHora == null ? "-" : DATA_HORA.format(dataHora);
    }

    /** Número sem perda de precisão e sem zeros desnecessários, para preencher campos de edição. */
    public static String editavel(double valor) {
        return BigDecimal.valueOf(valor).stripTrailingZeros().toPlainString();
    }

    /** Lê um decimal digitado (aceita vírgula ou ponto). */
    public static double lerDouble(String texto, String campo) {
        try {
            double valor = Double.parseDouble(texto.trim().replace(',', '.'));
            if (!Double.isFinite(valor)) {
                throw new NumberFormatException();
            }
            return valor;
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException(campo + " inválido: informe um número.");
        }
    }

    /** Lê um inteiro digitado. */
    public static int lerInt(String texto, String campo) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException(campo + " inválido: informe um número inteiro.");
        }
    }
}
