package com.armazem.controller;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/** Mensagens de erro, validação e confirmação exibidas com Alert (nunca só no console). */
public final class Alertas {

    private Alertas() { }

    public static void info(String titulo, String mensagem) {
        mostrar(Alert.AlertType.INFORMATION, titulo, mensagem);
    }

    public static void aviso(String titulo, String mensagem) {
        mostrar(Alert.AlertType.WARNING, titulo, mensagem);
    }

    public static void erro(String titulo, String mensagem) {
        mostrar(Alert.AlertType.ERROR, titulo, mensagem);
    }

    public static boolean confirmar(String titulo, String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensagem, ButtonType.YES, ButtonType.NO);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        return alerta.showAndWait().filter(botao -> botao == ButtonType.YES).isPresent();
    }

    private static void mostrar(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}
