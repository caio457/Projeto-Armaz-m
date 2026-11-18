package com.armazem.controller;

import com.armazem.model.Cliente;
import com.armazem.model.GerenteArmazem;
import com.armazem.model.RegraDeNegocioException;
import com.armazem.model.SistemaArmazem;
import com.armazem.model.Usuario;
import java.io.IOException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/** Tela de login e de cadastro de usuários (Cliente ou Gerente do Armazém). */
public class LoginController {

    private static final String PERFIL_CLIENTE = "Cliente";
    private static final String PERFIL_GERENTE = "Gerente do Armazém";

    private final SistemaArmazem sistema;
    private final Navegador navegador;
    private final VBox raiz = new VBox(16);
    private TabPane abas;

    public LoginController(SistemaArmazem sistema, Navegador navegador) {
        this.sistema = sistema;
        this.navegador = navegador;
        montar();
    }

    public Parent getView() {
        return raiz;
    }

    private void montar() {
        Label titulo = new Label("Sistema de Gestão de Armazém");
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        abas = new TabPane();
        abas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        abas.getTabs().add(new Tab("Entrar", criarAbaLogin()));
        abas.getTabs().add(new Tab("Cadastrar", criarAbaCadastro()));

        raiz.setPadding(new Insets(24));
        raiz.setAlignment(Pos.TOP_CENTER);
        raiz.getChildren().addAll(titulo, abas);
    }

    private Node criarAbaLogin() {
        TextField tfEmail = new TextField();
        tfEmail.setPromptText("E-mail");
        PasswordField pfSenha = new PasswordField();
        pfSenha.setPromptText("Senha");
        pfSenha.setOnAction(e -> entrar(tfEmail.getText(), pfSenha.getText()));

        Button btnEntrar = new Button("Entrar");
        btnEntrar.setOnAction(e -> entrar(tfEmail.getText(), pfSenha.getText()));

        GridPane form = criarGrade();
        form.addRow(0, new Label("E-mail:"), tfEmail);
        form.addRow(1, new Label("Senha:"), pfSenha);

        VBox caixa = new VBox(16, form, btnEntrar);
        caixa.setPadding(new Insets(16));
        return caixa;
    }

    private Node criarAbaCadastro() {
        ComboBox<String> cbPerfil = new ComboBox<>();
        cbPerfil.getItems().addAll(PERFIL_CLIENTE, PERFIL_GERENTE);
        cbPerfil.getSelectionModel().selectFirst();
        TextField tfNome = new TextField();
        TextField tfEmail = new TextField();
        PasswordField pfSenha = new PasswordField();
        PasswordField pfConfirmacao = new PasswordField();

        Button btnCadastrar = new Button("Cadastrar");
        btnCadastrar.setOnAction(e -> {
            if (cadastrar(cbPerfil.getValue(), tfNome.getText(), tfEmail.getText(),
                    pfSenha.getText(), pfConfirmacao.getText())) {
                tfNome.clear();
                tfEmail.clear();
                pfSenha.clear();
                pfConfirmacao.clear();
                abas.getSelectionModel().selectFirst();
            }
        });

        GridPane form = criarGrade();
        form.addRow(0, new Label("Perfil:"), cbPerfil);
        form.addRow(1, new Label("Nome:"), tfNome);
        form.addRow(2, new Label("E-mail:"), tfEmail);
        form.addRow(3, new Label("Senha (mín. 6):"), pfSenha);
        form.addRow(4, new Label("Confirmar senha:"), pfConfirmacao);

        VBox caixa = new VBox(16, form, btnCadastrar);
        caixa.setPadding(new Insets(16));
        return caixa;
    }

    private void entrar(String email, String senha) {
        Usuario usuario = sistema.autenticar(email, senha);
        if (usuario == null) {
            Alertas.erro("Falha no login", "E-mail ou senha incorretos.");
            return;
        }
        // instanceof + downcast apenas para abrir a tela exclusiva de cada perfil
        if (usuario instanceof GerenteArmazem gerente) {
            navegador.irParaGerente(gerente);
        } else if (usuario instanceof Cliente cliente) {
            navegador.irParaCliente(cliente);
        }
    }

    private boolean cadastrar(String perfil, String nome, String email, String senha, String confirmacao) {
        try {
            if (!senha.equals(confirmacao)) {
                throw new IllegalArgumentException("A confirmação da senha não confere.");
            }
            Usuario novo = PERFIL_CLIENTE.equals(perfil)
                    ? new Cliente(nome, email, senha)
                    : new GerenteArmazem(nome, email, senha);
            sistema.cadastrarUsuario(novo);
            sistema.salvar();
            Alertas.info("Cadastro", "Usuário cadastrado com sucesso. Agora é só entrar.");
            return true;
        } catch (IllegalArgumentException | RegraDeNegocioException e) {
            Alertas.erro("Dados inválidos", e.getMessage());
        } catch (IOException e) {
            Alertas.erro("Erro ao salvar", "Usuário cadastrado, mas não foi possível gravar o arquivo: " + e.getMessage());
            return true;
        }
        return false;
    }

    private static GridPane criarGrade() {
        GridPane grade = new GridPane();
        grade.setHgap(10);
        grade.setVgap(10);
        return grade;
    }
}
