package com.armazem.controller;

import java.util.function.Function;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/** Atalho para criar colunas de texto em TableView sem repetir código. */
public final class Tabelas {

    private Tabelas() { }

    public static <T> void adicionarColuna(TableView<T> tabela, String titulo, double largura,
                                           Function<T, String> extrator) {
        TableColumn<T, String> coluna = new TableColumn<>(titulo);
        coluna.setPrefWidth(largura);
        coluna.setCellValueFactory(celula -> new SimpleStringProperty(extrator.apply(celula.getValue())));
        tabela.getColumns().add(coluna);
    }
}
