package com.armazem.controller;

import com.armazem.model.Alimento;
import com.armazem.model.Armazem;
import com.armazem.model.Bebida;
import com.armazem.model.Eletronico;
import com.armazem.model.GerenteArmazem;
import com.armazem.model.Produto;
import com.armazem.model.ProdutoPerecivel;
import com.armazem.model.RegraDeNegocioException;
import com.armazem.model.SistemaArmazem;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Tela do Gerente do Armazém: cadastro do armazém, cadastro/edição de produtos,
 * alertas de estoque e validade, reabastecimento e salvamento.
 * Os métodos apenas leem os campos e delegam o trabalho às classes do modelo.
 */
public class GerenteController {

    private static final String TIPO_ALIMENTO = "Alimento";
    private static final String TIPO_BEBIDA = "Bebida";
    private static final String TIPO_ELETRONICO = "Eletrônico";

    private final SistemaArmazem sistema;
    private final GerenteArmazem gerente;
    private final Navegador navegador;
    private final BorderPane raiz = new BorderPane();

    // ---- aba do armazém
    private final TextField tfArmNome = new TextField();
    private final TextField tfArmRua = new TextField();
    private final TextField tfArmNumero = new TextField();
    private final TextField tfArmCidade = new TextField();
    private final TextField tfArmCapacidade = new TextField();
    private final Label lbOcupacao = new Label();
    private final Button btnSalvarArmazem = new Button();
    private Tab abaProdutos;

    // ---- aba de produtos
    private final ObservableList<Produto> dados = FXCollections.observableArrayList();
    private final TableView<Produto> tabela = new TableView<>();
    private final Label lbAlertas = new Label();
    private final Label lbTituloFormulario = new Label("Novo produto");
    private final ComboBox<String> cbTipo = new ComboBox<>();
    private final TextField tfNome = new TextField();
    private final TextField tfDescricao = new TextField();
    private final TextField tfPreco = new TextField();
    private final TextField tfPeso = new TextField();
    private final TextField tfQuantidade = new TextField();
    private final TextField tfMinimo = new TextField();
    private final TextField tfTempo = new TextField();
    private final DatePicker dpValidade = new DatePicker();
    private final TextField tfTemperatura = new TextField();
    private final TextField tfCategoria = new TextField();
    private final CheckBox cbOrganico = new CheckBox("Orgânico");
    private final TextField tfVolume = new TextField();
    private final CheckBox cbAlcoolica = new CheckBox("Bebida alcoólica");
    private final ComboBox<Integer> cbVoltagem = new ComboBox<>();
    private final TextField tfGarantia = new TextField();
    private final GridPane painelPerecivel = criarGrade();
    private final GridPane painelAlimento = criarGrade();
    private final GridPane painelBebida = criarGrade();
    private final GridPane painelEletronico = criarGrade();
    private final Button btnSalvarProduto = new Button("Adicionar produto");
    private Produto produtoEmEdicao;

    public GerenteController(SistemaArmazem sistema, GerenteArmazem gerente, Navegador navegador) {
        this.sistema = sistema;
        this.gerente = gerente;
        this.navegador = navegador;
        montar();
    }

    public Parent getView() {
        return raiz;
    }

    // =====================================================================
    // Montagem das telas
    // =====================================================================

    private void montar() {
        raiz.setTop(criarBarraSuperior());

        TabPane abas = new TabPane();
        abas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        abaProdutos = new Tab("Produtos", criarPainelProdutos());
        abas.getTabs().add(new Tab("Armazém", criarPainelArmazem()));
        abas.getTabs().add(abaProdutos);
        raiz.setCenter(abas);

        carregarArmazem();
        atualizarTabela();
    }

    private Node criarBarraSuperior() {
        Label lbUsuario = new Label("Gerente: " + gerente.getNome());
        lbUsuario.setStyle("-fx-font-weight: bold;");
        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);

        Button btnSalvar = new Button("Salvar alterações");
        btnSalvar.setOnAction(e -> salvar(true));
        Button btnSair = new Button("Sair");
        btnSair.setOnAction(e -> {
            salvar(false);
            navegador.irParaLogin();
        });

        HBox barra = new HBox(10, lbUsuario, espaco, btnSalvar, btnSair);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(10));
        barra.setStyle("-fx-background-color: #e8eef7;");
        return barra;
    }

    private Node criarPainelArmazem() {
        GridPane grade = criarGrade();
        grade.setPadding(new Insets(16));
        grade.addRow(0, new Label("Nome:"), tfArmNome);
        grade.addRow(1, new Label("Rua:"), tfArmRua);
        grade.addRow(2, new Label("Número:"), tfArmNumero);
        grade.addRow(3, new Label("Cidade:"), tfArmCidade);
        grade.addRow(4, new Label("Capacidade máxima (kg):"), tfArmCapacidade);
        btnSalvarArmazem.setOnAction(e -> salvarArmazem());
        grade.add(btnSalvarArmazem, 1, 5);
        grade.add(lbOcupacao, 1, 6);
        return grade;
    }

    private Node criarPainelProdutos() {
        configurarTabela();

        lbAlertas.setPadding(new Insets(6));
        Label legenda = new Label("Vermelho: produto vencido  |  Laranja: estoque baixo");
        legenda.setStyle("-fx-text-fill: #666666;");
        legenda.setPadding(new Insets(0, 6, 6, 6));

        Button btnEditar = new Button("Editar selecionado");
        btnEditar.setOnAction(e -> editarSelecionado());
        Button btnRemover = new Button("Remover selecionado");
        btnRemover.setOnAction(e -> removerSelecionado());
        Button btnReabastecer = new Button("Reabastecer selecionado");
        btnReabastecer.setOnAction(e -> reabastecerSelecionado());
        HBox acoes = new HBox(8, btnEditar, btnRemover, btnReabastecer);
        acoes.setPadding(new Insets(8));

        VBox esquerda = new VBox(lbAlertas, legenda, tabela, acoes);
        VBox.setVgrow(tabela, Priority.ALWAYS);

        ScrollPane formulario = new ScrollPane(criarFormularioProduto());
        formulario.setFitToWidth(true);
        formulario.setPrefWidth(380);

        BorderPane painel = new BorderPane();
        painel.setCenter(esquerda);
        painel.setRight(formulario);
        return painel;
    }

    private void configurarTabela() {
        tabela.setItems(dados);
        tabela.setPlaceholder(new Label("Nenhum produto cadastrado."));
        Tabelas.adicionarColuna(tabela, "Cód.", 50, p -> String.valueOf(p.getCodigo()));
        Tabelas.adicionarColuna(tabela, "Nome", 160, Produto::getNome);
        Tabelas.adicionarColuna(tabela, "Tipo", 85, Produto::getTipo);
        Tabelas.adicionarColuna(tabela, "Preço", 85, p -> Formatos.moeda(p.getPrecoUnitario()));
        Tabelas.adicionarColuna(tabela, "Peso (kg)", 70, p -> Formatos.numero(p.getPeso()));
        Tabelas.adicionarColuna(tabela, "Qtd.", 55, p -> String.valueOf(p.getQuantidadeEstoque()));
        Tabelas.adicionarColuna(tabela, "Mín.", 50, p -> String.valueOf(p.getEstoqueMinimo()));
        // instanceof + downcast só para um recurso exclusivo dos perecíveis
        Tabelas.adicionarColuna(tabela, "Validade", 90,
                p -> p instanceof ProdutoPerecivel perecivel ? Formatos.data(perecivel.getDataValidade()) : "-");
        Tabelas.adicionarColuna(tabela, "Situação", 110,
                p -> p.isVencido() ? "VENCIDO" : (p.isEstoqueBaixo() ? "ESTOQUE BAIXO" : "OK"));

        // destaque visual dos alertas (RN04)
        tabela.setRowFactory(tv -> new TableRow<Produto>() {
            @Override
            protected void updateItem(Produto produto, boolean vazio) {
                super.updateItem(produto, vazio);
                if (vazio || produto == null) {
                    setStyle("");
                } else if (produto.isVencido()) {
                    setStyle("-fx-background-color: #f8b4b4;");
                } else if (produto.isEstoqueBaixo()) {
                    setStyle("-fx-background-color: #fde2a7;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private Node criarFormularioProduto() {
        cbTipo.getItems().addAll(TIPO_ALIMENTO, TIPO_BEBIDA, TIPO_ELETRONICO);
        cbTipo.getSelectionModel().selectFirst();
        cbTipo.valueProperty().addListener((obs, antigo, novo) -> atualizarCamposPorTipo());
        cbVoltagem.getItems().addAll(110, 220);
        cbVoltagem.getSelectionModel().selectFirst();
        dpValidade.setPromptText("dd/mm/aaaa");

        GridPane comum = criarGrade();
        comum.addRow(0, new Label("Tipo:"), cbTipo);
        comum.addRow(1, new Label("Nome:"), tfNome);
        comum.addRow(2, new Label("Descrição:"), tfDescricao);
        comum.addRow(3, new Label("Preço unitário (R$):"), tfPreco);
        comum.addRow(4, new Label("Peso (kg):"), tfPeso);
        comum.addRow(5, new Label("Qtd. em estoque:"), tfQuantidade);
        comum.addRow(6, new Label("Estoque mínimo:"), tfMinimo);
        comum.addRow(7, new Label("Separação (min/un):"), tfTempo);

        painelPerecivel.addRow(0, new Label("Validade:"), dpValidade);
        painelPerecivel.addRow(1, new Label("Temperatura (°C):"), tfTemperatura);
        painelAlimento.addRow(0, new Label("Categoria:"), tfCategoria);
        painelAlimento.addRow(1, new Label(""), cbOrganico);
        painelBebida.addRow(0, new Label("Volume (ml):"), tfVolume);
        painelBebida.addRow(1, new Label(""), cbAlcoolica);
        painelEletronico.addRow(0, new Label("Voltagem (V):"), cbVoltagem);
        painelEletronico.addRow(1, new Label("Garantia (meses):"), tfGarantia);

        btnSalvarProduto.setOnAction(e -> salvarProduto());
        Button btnLimpar = new Button("Limpar");
        btnLimpar.setOnAction(e -> limparFormulario());
        lbTituloFormulario.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        VBox formulario = new VBox(10, lbTituloFormulario, comum, painelPerecivel, painelAlimento,
                painelBebida, painelEletronico, new HBox(8, btnSalvarProduto, btnLimpar));
        formulario.setPadding(new Insets(12));
        atualizarCamposPorTipo();
        return formulario;
    }

    /** O formulário mostra só os campos do tipo de produto escolhido. */
    private void atualizarCamposPorTipo() {
        String tipo = cbTipo.getValue();
        boolean alimento = TIPO_ALIMENTO.equals(tipo);
        boolean bebida = TIPO_BEBIDA.equals(tipo);
        boolean eletronico = TIPO_ELETRONICO.equals(tipo);
        mostrar(painelPerecivel, alimento || bebida);
        mostrar(painelAlimento, alimento);
        mostrar(painelBebida, bebida);
        mostrar(painelEletronico, eletronico);
    }

    private static void mostrar(Node no, boolean visivel) {
        no.setVisible(visivel);
        no.setManaged(visivel);
    }

    private static GridPane criarGrade() {
        GridPane grade = new GridPane();
        grade.setHgap(10);
        grade.setVgap(8);
        return grade;
    }

    // =====================================================================
    // Armazém
    // =====================================================================

    private void carregarArmazem() {
        Armazem armazem = gerente.getArmazem();
        if (armazem == null) {
            tfArmNome.clear();
            tfArmRua.clear();
            tfArmNumero.clear();
            tfArmCidade.clear();
            tfArmCapacidade.clear();
            btnSalvarArmazem.setText("Cadastrar armazém");
            lbOcupacao.setText("Cadastre o armazém para liberar a aba de produtos.");
        } else {
            tfArmNome.setText(armazem.getNome());
            tfArmRua.setText(armazem.getEndereco().getRua());
            tfArmNumero.setText(armazem.getEndereco().getNumero());
            tfArmCidade.setText(armazem.getEndereco().getCidade());
            tfArmCapacidade.setText(Formatos.editavel(armazem.getCapacidadeMaxima()));
            btnSalvarArmazem.setText("Salvar dados do armazém");
            lbOcupacao.setText("Ocupado: " + Formatos.numero(armazem.getCapacidadeOcupada()) + " kg  |  Disponível: "
                    + Formatos.numero(armazem.getEspacoDisponivel()) + " kg");
        }
        abaProdutos.setDisable(armazem == null);
    }

    private void salvarArmazem() {
        try {
            String nome = tfArmNome.getText();
            String rua = tfArmRua.getText();
            String numero = tfArmNumero.getText();
            String cidade = tfArmCidade.getText();
            double capacidade = Formatos.lerDouble(tfArmCapacidade.getText(), "Capacidade máxima");

            Armazem armazem = gerente.getArmazem();
            if (armazem == null) {
                armazem = new Armazem(nome, rua, numero, cidade, capacidade);
                sistema.cadastrarArmazem(armazem);
                gerente.setArmazem(armazem);
            } else {
                armazem.atualizarDados(nome, rua, numero, cidade, capacidade);
            }
            carregarArmazem();
            atualizarTabela();
            salvar(false);
            Alertas.info("Armazém", "Dados do armazém salvos com sucesso.");
        } catch (IllegalArgumentException | RegraDeNegocioException e) {
            Alertas.erro("Dados inválidos", e.getMessage());
        }
    }

    // =====================================================================
    // Produtos
    // =====================================================================

    private void atualizarTabela() {
        Armazem armazem = gerente.getArmazem();
        if (armazem == null) {
            dados.clear();
            lbAlertas.setText("");
            return;
        }
        dados.setAll(armazem.listarProdutos());
        tabela.refresh();

        int vencidos = armazem.getProdutosVencidos().size();
        int baixos = armazem.getProdutosComEstoqueBaixo().size();
        if (vencidos + baixos > 0) {
            lbAlertas.setText("ATENÇÃO: " + vencidos + " produto(s) vencido(s) e " + baixos + " com estoque baixo.");
            lbAlertas.setStyle("-fx-font-weight: bold; -fx-text-fill: #b00020;");
        } else {
            lbAlertas.setText("Nenhum alerta de estoque ou validade.");
            lbAlertas.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        }
        carregarArmazem();
    }

    private void salvarProduto() {
        Armazem armazem = gerente.getArmazem();
        try {
            Produto produto = montarProduto();
            boolean edicao = produtoEmEdicao != null;
            if (edicao) {
                armazem.atualizarProduto(produtoEmEdicao.getCodigo(), produto);
            } else {
                armazem.adicionarProduto(produto);
            }
            String mensagem = edicao
                    ? "Produto atualizado."
                    : "Produto adicionado com o código " + produto.getCodigo() + ".";
            limparFormulario();
            atualizarTabela();
            Alertas.info("Produto", mensagem);
        } catch (IllegalArgumentException | RegraDeNegocioException e) {
            Alertas.erro("Não foi possível salvar o produto", e.getMessage());
        }
    }

    /** Lê o formulário e cria o objeto do tipo escolhido; os construtores validam os dados (RN01). */
    private Produto montarProduto() {
        String nome = tfNome.getText();
        String descricao = tfDescricao.getText();
        double preco = Formatos.lerDouble(tfPreco.getText(), "Preço unitário");
        double peso = Formatos.lerDouble(tfPeso.getText(), "Peso");
        int quantidade = Formatos.lerInt(tfQuantidade.getText(), "Quantidade em estoque");
        int minimo = Formatos.lerInt(tfMinimo.getText(), "Estoque mínimo");
        int tempo = Formatos.lerInt(tfTempo.getText(), "Tempo de separação");

        String tipo = cbTipo.getValue();
        if (TIPO_ALIMENTO.equals(tipo)) {
            double temperatura = Formatos.lerDouble(tfTemperatura.getText(), "Temperatura de conservação");
            return new Alimento(nome, descricao, preco, peso, quantidade, minimo, tempo,
                    dpValidade.getValue(), temperatura, tfCategoria.getText(), cbOrganico.isSelected());
        }
        if (TIPO_BEBIDA.equals(tipo)) {
            double temperatura = Formatos.lerDouble(tfTemperatura.getText(), "Temperatura de conservação");
            int volume = Formatos.lerInt(tfVolume.getText(), "Volume");
            return new Bebida(nome, descricao, preco, peso, quantidade, minimo, tempo,
                    dpValidade.getValue(), temperatura, volume, cbAlcoolica.isSelected());
        }
        if (TIPO_ELETRONICO.equals(tipo)) {
            int garantia = Formatos.lerInt(tfGarantia.getText(), "Garantia");
            return new Eletronico(nome, descricao, preco, peso, quantidade, minimo, tempo,
                    cbVoltagem.getValue(), garantia);
        }
        throw new IllegalArgumentException("Selecione o tipo do produto.");
    }

    private void editarSelecionado() {
        Produto produto = tabela.getSelectionModel().getSelectedItem();
        if (produto == null) {
            Alertas.aviso("Edição", "Selecione um produto na tabela.");
            return;
        }
        limparFormulario();
        produtoEmEdicao = produto;
        lbTituloFormulario.setText("Editando o produto " + produto.getCodigo());
        btnSalvarProduto.setText("Salvar edição");
        cbTipo.setDisable(true);   // o tipo não muda na edição

        tfNome.setText(produto.getNome());
        tfDescricao.setText(produto.getDescricao());
        tfPreco.setText(Formatos.editavel(produto.getPrecoUnitario()));
        tfPeso.setText(Formatos.editavel(produto.getPeso()));
        tfQuantidade.setText(String.valueOf(produto.getQuantidadeEstoque()));
        tfMinimo.setText(String.valueOf(produto.getEstoqueMinimo()));
        tfTempo.setText(String.valueOf(produto.getTempoSeparacao()));

        // instanceof + downcast para preencher os campos exclusivos de cada tipo
        if (produto instanceof ProdutoPerecivel perecivel) {
            dpValidade.setValue(perecivel.getDataValidade());
            tfTemperatura.setText(Formatos.editavel(perecivel.getTemperaturaConservacao()));
        }
        if (produto instanceof Alimento alimento) {
            cbTipo.setValue(TIPO_ALIMENTO);
            tfCategoria.setText(alimento.getCategoria());
            cbOrganico.setSelected(alimento.isOrganico());
        } else if (produto instanceof Bebida bebida) {
            cbTipo.setValue(TIPO_BEBIDA);
            tfVolume.setText(String.valueOf(bebida.getVolumeMl()));
            cbAlcoolica.setSelected(bebida.isAlcoolica());
        } else if (produto instanceof Eletronico eletronico) {
            cbTipo.setValue(TIPO_ELETRONICO);
            cbVoltagem.setValue(eletronico.getVoltagem());
            tfGarantia.setText(String.valueOf(eletronico.getGarantiaMeses()));
        }
    }

    private void removerSelecionado() {
        Produto produto = tabela.getSelectionModel().getSelectedItem();
        if (produto == null) {
            Alertas.aviso("Remoção", "Selecione um produto na tabela.");
            return;
        }
        if (Alertas.confirmar("Remover produto", "Deseja remover \"" + produto.getNome() + "\"?")) {
            gerente.getArmazem().removerProduto(produto.getCodigo());
            if (produtoEmEdicao == produto) {
                limparFormulario();
            }
            atualizarTabela();
        }
    }

    private void reabastecerSelecionado() {
        Produto produto = tabela.getSelectionModel().getSelectedItem();
        if (produto == null) {
            Alertas.aviso("Reabastecimento", "Selecione um produto na tabela.");
            return;
        }
        boolean perecivel = produto instanceof ProdutoPerecivel;

        TextField tfQtd = new TextField();
        DatePicker dpNovaValidade = new DatePicker();
        GridPane grade = criarGrade();
        grade.addRow(0, new Label("Quantidade a adicionar:"), tfQtd);
        if (perecivel) {
            grade.addRow(1, new Label("Validade do novo lote:"), dpNovaValidade);
        }

        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Reabastecer");
        dialogo.setHeaderText("Reabastecer: " + produto.getNome());
        dialogo.getDialogPane().setContent(grade);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> resposta = dialogo.showAndWait();
        if (resposta.isEmpty() || resposta.get() != ButtonType.OK) {
            return;
        }
        try {
            int quantidade = Formatos.lerInt(tfQtd.getText(), "Quantidade");
            Armazem armazem = gerente.getArmazem();
            if (perecivel) {
                armazem.reabastecerProduto(produto.getCodigo(), quantidade, dpNovaValidade.getValue());
            } else {
                armazem.reabastecerProduto(produto.getCodigo(), quantidade);
            }
            atualizarTabela();
        } catch (IllegalArgumentException | RegraDeNegocioException e) {
            Alertas.erro("Não foi possível reabastecer", e.getMessage());
        }
    }

    private void limparFormulario() {
        produtoEmEdicao = null;
        tabela.getSelectionModel().clearSelection();
        cbTipo.setDisable(false);
        lbTituloFormulario.setText("Novo produto");
        btnSalvarProduto.setText("Adicionar produto");
        for (TextField campo : List.of(tfNome, tfDescricao, tfPreco, tfPeso, tfQuantidade, tfMinimo, tfTempo,
                tfTemperatura, tfCategoria, tfVolume, tfGarantia)) {
            campo.clear();
        }
        dpValidade.setValue(null);
        cbOrganico.setSelected(false);
        cbAlcoolica.setSelected(false);
        cbVoltagem.getSelectionModel().selectFirst();
    }

    // =====================================================================
    // Salvar (RN10)
    // =====================================================================

    private boolean salvar(boolean mostrarConfirmacao) {
        try {
            sistema.salvar();
            if (mostrarConfirmacao) {
                Alertas.info("Salvo", "Alterações salvas com sucesso.");
            }
            return true;
        } catch (IOException e) {
            Alertas.erro("Erro ao salvar", "Não foi possível gravar o arquivo: " + e.getMessage());
            return false;
        }
    }
}
