package com.armazem.app;

import com.armazem.controller.Alertas;
import com.armazem.controller.ClienteController;
import com.armazem.controller.GerenteController;
import com.armazem.controller.LoginController;
import com.armazem.controller.Navegador;
import com.armazem.model.Cliente;
import com.armazem.model.GerenteArmazem;
import com.armazem.model.SistemaArmazem;
import java.io.IOException;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Ponto de entrada JavaFX: carrega os dados, mostra o login e controla a navegação entre telas. */
public class MainApp extends Application implements Navegador {

    private SistemaArmazem sistema;
    private Stage janela;

    @Override
    public void start(Stage stage) {
        this.janela = stage;
        this.sistema = new SistemaArmazem();
        try {
            sistema.carregar();   // RN10: recarrega os dados gravados na última execução
        } catch (IOException | ClassNotFoundException e) {
            Alertas.erro("Erro ao carregar os dados",
                    "Não foi possível ler o arquivo armazem.dat. O sistema iniciará vazio.\n" + e.getMessage());
        }
        janela.setTitle("Sistema de Gestão de Armazém");
        irParaLogin();
        janela.show();
    }

    @Override
    public void stop() {
        try {
            sistema.salvar();     // grava tudo ao fechar a janela
        } catch (IOException e) {
            System.err.println("Não foi possível salvar os dados: " + e.getMessage());
        }
    }

    @Override
    public void irParaLogin() {
        mostrar(new LoginController(sistema, this).getView(), 560, 460);
    }

    @Override
    public void irParaGerente(GerenteArmazem gerente) {
        mostrar(new GerenteController(sistema, gerente, this).getView(), 1200, 700);
    }

    @Override
    public void irParaCliente(Cliente cliente) {
        mostrar(new ClienteController(sistema, cliente, this).getView(), 1200, 700);
    }

    private void mostrar(Parent raiz, double largura, double altura) {
        janela.setScene(new Scene(raiz, largura, altura));
        janela.sizeToScene();
        janela.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
