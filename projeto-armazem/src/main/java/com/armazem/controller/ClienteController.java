package com.armazem.controller;

import com.armazem.model.Armazem;
import com.armazem.model.Cliente;
import com.armazem.model.ItemPedido;
import com.armazem.model.Pedido;
import com.armazem.model.Produto;
import com.armazem.model.ProdutoPerecivel;
import com.armazem.model.RegraDeNegocioException;
import com.armazem.model.SistemaArmazem;
import java.io.IOException;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Tela do Cliente: lista de armazéns, produtos do armazém escolhido, montagem do pedido
 * (total e tempo de espera) e finalização. As regras ficam no modelo (Pedido, ItemPedido).
 */
public class ClienteController {

    private final SistemaArmazem sistema;
    private final Cliente cliente;
    private final Navegador navegador;
    private final BorderPane raiz = new BorderPane();

    private final ObservableList<Armazem> dadosArmazens = FXCollections.observableArrayList();
    private final ObservableList<Produto> dadosProdutos = FXCollections.observableArrayList();
    private final ObservableList<ItemPedido> dadosItens = FXCollections.observableArrayList();
    private final ObservableList<Pedido> dadosHistorico = FXCollections.observableArrayList();

    private final TableView<Armazem> tabelaArmazens = new TableView<>();
    private final TableView<Produto> tabelaProdutos = new TableView<>();
    private final TableView<ItemPedido> tabelaItens = new TableView<>();
    private final TableView<Pedido> tabelaHistorico = new TableView<>();

    private final Label lbArmazemAtual = new Label("2. Abra um armazém para ver os produtos.");
    private final TextField tfQuantidade = new TextField("1");
    private final Label lbTotal = new Label();
    private final Label lbTempo = new Label();

    /** Pedido em montagem (null enquanto nenhum armazém foi aberto). */
    private Pedido pedido;

    public ClienteController(SistemaArmazem sistema, Cliente cliente, Navegador navegador) {
        this.sistema = sistema;
        this.cliente = cliente;
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
        abas.getTabs().add(new Tab("Novo pedido", criarPainelPedido()));
        abas.getTabs().add(new Tab("Meus pedidos", criarPainelHistorico()));
        raiz.setCenter(abas);

        dadosArmazens.setAll(sistema.getArmazens());
        atualizarPedido();
        atualizarHistorico();
    }

    private Node criarBarraSuperior() {
        Label lbUsuario = new Label("Cliente: " + cliente.getNome());
        lbUsuario.setStyle("-fx-font-weight: bold;");
        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        Button btnSair = new Button("Sair");
        btnSair.setOnAction(e -> navegador.irParaLogin());

        HBox barra = new HBox(10, lbUsuario, espaco, btnSair);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(10));
        barra.setStyle("-fx-background-color: #e8eef7;");
        return barra;
    }

    private Node criarPainelPedido() {
        // --- 1. armazéns
        tabelaArmazens.setItems(dadosArmazens);
        tabelaArmazens.setPlaceholder(new Label("Nenhum armazém cadastrado ainda."));
        tabelaArmazens.setPrefHeight(140);
        Tabelas.adicionarColuna(tabelaArmazens, "Armazém", 220, Armazem::getNome);
        Tabelas.adicionarColuna(tabelaArmazens, "Endereço", 340, a -> a.getEndereco().toString());
        Tabelas.adicionarColuna(tabelaArmazens, "Capacidade (kg)", 130, a -> Formatos.numero(a.getCapacidadeMaxima()));
        Button btnAbrir = new Button("Abrir armazém selecionado");
        btnAbrir.setOnAction(e -> abrirArmazem());
        VBox secaoArmazens = new VBox(6, new Label("1. Escolha um armazém"), tabelaArmazens, btnAbrir);

        // --- 2. produtos
        tabelaProdutos.setItems(dadosProdutos);
        tabelaProdutos.setPlaceholder(new Label("Sem produtos para mostrar."));
        Tabelas.adicionarColuna(tabelaProdutos, "Cód.", 50, p -> String.valueOf(p.getCodigo()));
        Tabelas.adicionarColuna(tabelaProdutos, "Produto", 170, Produto::getNome);
        Tabelas.adicionarColuna(tabelaProdutos, "Tipo", 85, Produto::getTipo);
        Tabelas.adicionarColuna(tabelaProdutos, "Preço", 85, p -> Formatos.moeda(p.getPrecoUnitario()));
        Tabelas.adicionarColuna(tabelaProdutos, "c/ taxas", 90, p -> Formatos.moeda(ItemPedido.valorUnitarioComTaxas(p)));
        Tabelas.adicionarColuna(tabelaProdutos, "Disp.", 55, p -> String.valueOf(p.getQuantidadeEstoque()));
        // instanceof + downcast só para exibir a validade dos perecíveis
        Tabelas.adicionarColuna(tabelaProdutos, "Validade", 90,
                p -> p instanceof ProdutoPerecivel perecivel ? Formatos.data(perecivel.getDataValidade()) : "-");

        tfQuantidade.setPrefWidth(70);
        Button btnAdicionar = new Button("Adicionar ao pedido");
        btnAdicionar.setOnAction(e -> adicionarAoPedido());
        HBox linhaQuantidade = new HBox(8, new Label("Quantidade:"), tfQuantidade, btnAdicionar);
        linhaQuantidade.setAlignment(Pos.CENTER_LEFT);

        lbArmazemAtual.setStyle("-fx-font-weight: bold;");
        VBox secaoProdutos = new VBox(6, lbArmazemAtual, tabelaProdutos, linhaQuantidade);
        VBox.setVgrow(tabelaProdutos, Priority.ALWAYS);
        HBox.setHgrow(secaoProdutos, Priority.ALWAYS);

        // --- 3. pedido
        tabelaItens.setItems(dadosItens);
        tabelaItens.setPlaceholder(new Label("Nenhum item no pedido."));
        Tabelas.adicionarColuna(tabelaItens, "Produto", 150, i -> i.getProduto().getNome());
        Tabelas.adicionarColuna(tabelaItens, "Qtd.", 45, i -> String.valueOf(i.getQuantidade()));
        Tabelas.adicionarColuna(tabelaItens, "Unit. c/ taxas", 105, i -> Formatos.moeda(i.getValorUnitarioComTaxas()));
        Tabelas.adicionarColuna(tabelaItens, "Valor", 100, i -> Formatos.moeda(i.getValor()));

        Button btnRemover = new Button("Remover item selecionado");
        btnRemover.setOnAction(e -> removerItem());
        lbTotal.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Button btnFinalizar = new Button("Finalizar pedido");
        btnFinalizar.setStyle("-fx-font-weight: bold;");
        btnFinalizar.setOnAction(e -> finalizarPedido());

        VBox secaoPedido = new VBox(6, new Label("3. Seu pedido"), tabelaItens, btnRemover, lbTotal, lbTempo, btnFinalizar);
        secaoPedido.setPrefWidth(440);
        VBox.setVgrow(tabelaItens, Priority.ALWAYS);

        HBox inferior = new HBox(12, secaoProdutos, secaoPedido);
        VBox painel = new VBox(12, secaoArmazens, inferior);
        VBox.setVgrow(inferior, Priority.ALWAYS);
        painel.setPadding(new Insets(12));
        return painel;
    }

    private Node criarPainelHistorico() {
        tabelaHistorico.setItems(dadosHistorico);
        tabelaHistorico.setPlaceholder(new Label("Você ainda não finalizou nenhum pedido."));
        Tabelas.adicionarColuna(tabelaHistorico, "Nº", 50, p -> String.valueOf(p.getNumero()));
        Tabelas.adicionarColuna(tabelaHistorico, "Finalizado em", 140, p -> Formatos.dataHora(p.getDataFinalizacao()));
        Tabelas.adicionarColuna(tabelaHistorico, "Armazém", 200, p -> p.getArmazem().getNome());
        Tabelas.adicionarColuna(tabelaHistorico, "Itens", 60, p -> String.valueOf(p.getItens().size()));
        Tabelas.adicionarColuna(tabelaHistorico, "Total", 120, p -> Formatos.moeda(p.calcularTotal()));
        Tabelas.adicionarColuna(tabelaHistorico, "Espera (min)", 100, p -> String.valueOf(p.calcularTempoEspera()));
        Tabelas.adicionarColuna(tabelaHistorico, "Status", 100, p -> p.getStatus().name());

        VBox painel = new VBox(tabelaHistorico);
        VBox.setVgrow(tabelaHistorico, Priority.ALWAYS);
        painel.setPadding(new Insets(12));
        return painel;
    }

    // =====================================================================
    // Ações
    // =====================================================================

    private void abrirArmazem() {
        Armazem escolhido = tabelaArmazens.getSelectionModel().getSelectedItem();
        if (escolhido == null) {
            Alertas.aviso("Armazém", "Selecione um armazém na lista.");
            return;
        }
        boolean outroArmazem = pedido == null || pedido.getArmazem() != escolhido;
        if (outroArmazem) {
            boolean temItens = pedido != null && !pedido.getItens().isEmpty();
            if (temItens && !Alertas.confirmar("Trocar de armazém", "O pedido atual será descartado. Continuar?")) {
                return;
            }
            pedido = new Pedido(cliente, escolhido);
        }
        lbArmazemAtual.setText("2. Produtos de " + escolhido.getNome());
        atualizarProdutos();
        atualizarPedido();
    }

    private void adicionarAoPedido() {
        if (pedido == null) {
            Alertas.aviso("Pedido", "Abra um armazém primeiro.");
            return;
        }
        Produto produto = tabelaProdutos.getSelectionModel().getSelectedItem();
        if (produto == null) {
            Alertas.aviso("Pedido", "Selecione um produto na tabela.");
            return;
        }
        try {
            int quantidade = Formatos.lerInt(tfQuantidade.getText(), "Quantidade");
            pedido.adicionarItem(produto, quantidade);
            tfQuantidade.setText("1");
            atualizarPedido();
        } catch (IllegalArgumentException | RegraDeNegocioException e) {
            Alertas.erro("Não foi possível adicionar", e.getMessage());
        }
    }

    private void removerItem() {
        ItemPedido item = tabelaItens.getSelectionModel().getSelectedItem();
        if (pedido == null || item == null) {
            Alertas.aviso("Pedido", "Selecione um item do pedido.");
            return;
        }
        try {
            pedido.removerItem(item);
            atualizarPedido();
        } catch (RegraDeNegocioException e) {
            Alertas.erro("Não foi possível remover", e.getMessage());
        }
    }

    private void finalizarPedido() {
        if (pedido == null) {
            Alertas.aviso("Pedido", "Abra um armazém e adicione produtos antes de finalizar.");
            return;
        }
        try {
            pedido.finalizar();               // RN09: confere tudo e dá baixa no estoque
            sistema.registrarPedido(pedido);

            String resumo = "Pedido nº " + pedido.getNumero() + " finalizado.\n"
                    + "Total: " + Formatos.moeda(pedido.calcularTotal()) + "\n"
                    + "Tempo estimado de espera: " + pedido.calcularTempoEspera() + " min";
            List<String> instrucoes = pedido.getInstrucoesManuseio();
            if (!instrucoes.isEmpty()) {
                resumo += "\n\nItens frágeis (embalagem especial):\n- " + String.join("\n- ", instrucoes);
            }
            Armazem armazem = pedido.getArmazem();
            pedido = new Pedido(cliente, armazem);   // novo pedido aberto no mesmo armazém

            salvarEstoque();
            Alertas.info("Pedido finalizado", resumo);
            atualizarProdutos();
            atualizarPedido();
            atualizarHistorico();
        } catch (IllegalArgumentException | RegraDeNegocioException e) {
            Alertas.erro("Não foi possível finalizar", e.getMessage());
        }
    }

    private void salvarEstoque() {
        try {
            sistema.salvar();
        } catch (IOException e) {
            Alertas.erro("Erro ao salvar", "O pedido foi finalizado, mas não foi possível gravar o arquivo: " + e.getMessage());
        }
    }

    // =====================================================================
    // Atualização das telas
    // =====================================================================

    /** RN04: produtos vencidos não aparecem para o Cliente. */
    private void atualizarProdutos() {
        if (pedido == null) {
            dadosProdutos.clear();
        } else {
            dadosProdutos.setAll(pedido.getArmazem().getProdutosDisponiveis());
        }
        tabelaProdutos.refresh();
    }

    private void atualizarPedido() {
        if (pedido == null) {
            dadosItens.clear();
            lbTotal.setText("Total: " + Formatos.moeda(0));
            lbTempo.setText("Tempo estimado de espera: 0 min");
            return;
        }
        dadosItens.setAll(pedido.getItens());
        lbTotal.setText("Total: " + Formatos.moeda(pedido.calcularTotal()));
        lbTempo.setText("Tempo estimado de espera: " + pedido.calcularTempoEspera() + " min");
    }

    private void atualizarHistorico() {
        dadosHistorico.setAll(sistema.getPedidosDoCliente(cliente));
    }
}
