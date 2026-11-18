package com.armazem.app;

/**
 * Classe de partida alternativa, que não estende Application. Útil para rodar a partir
 * de IDEs ou de um JAR sem precisar configurar o módulo do JavaFX na linha de comando.
 */
public class Launcher {

    public static void main(String[] args) {
        MainApp.main(args);
    }
}
