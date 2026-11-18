# Projeto Armazém

Sistema de gestão de armazém em **Java + JavaFX** para a disciplina de Programação 2
(Engenharia de Computação, CESUPA — Prof. Pedro Girotto).

Dois perfis de usuário:

- **Gerente do Armazém**: cadastra o armazém, cadastra/edita/remove produtos (Alimento, Bebida, Eletrônico),
  reabastece o estoque, vê os alertas de estoque baixo e de produtos vencidos e salva as alterações.
- **Cliente**: vê os armazéns, escolhe um, monta um pedido, vê o total e o tempo estimado de espera
  e finaliza o pedido (o estoque do armazém é atualizado).

## Como executar

Requisitos: **JDK 17 ou superior** e **Maven 3.8+** (o Maven baixa o JavaFX sozinho).

```bash
mvn javafx:run
```

Os dados ficam no arquivo `armazem.dat`, criado na pasta onde o sistema é executado.
Ele é recarregado ao iniciar e gravado pelo botão **Salvar alterações**, ao sair da conta e ao fechar a janela.
Para começar do zero, apague o `armazem.dat`.

### Rodando pela IDE (IntelliJ, Eclipse, VS Code)

1. Importe como projeto Maven.
2. Execute a classe `com.armazem.app.Launcher` (ela não estende `Application`, então dispensa
   configurar o `--module-path` do JavaFX).

## Roteiro rápido de teste

1. **Cadastrar** um usuário com perfil *Gerente do Armazém* e entrar com ele.
2. Aba **Armazém**: cadastrar nome, endereço e capacidade (ex.: 1000 kg).
3. Aba **Produtos**: cadastrar os dois itens do exemplo do enunciado:
   - Alimento "Arroz 5 kg", R$ 25,00, peso 5 kg, estoque 100, mínimo 10, separação 2 min, validade futura, categoria "grãos".
   - Eletrônico "Monitor 24 pol.", R$ 800,00, peso 4 kg, estoque 20, mínimo 3, separação 5 min, 220 V, garantia 12 meses.
4. **Salvar alterações**, **Sair**, cadastrar um *Cliente* e entrar.
5. Abrir o armazém, adicionar 10 unidades do arroz e 2 do monitor.
   Resultado esperado: **total R$ 1.943,00** e **30 minutos** de espera.
6. Finalizar o pedido: o estoque cai para 90 e 18 unidades, e o pedido aparece em *Meus pedidos*.

## Estrutura do código

```
src/main/java/com/armazem/
├── model/        regras de negócio (nenhum import de javafx)
│   ├── Usuario (abstrata) ── Cliente, GerenteArmazem
│   ├── Produto (abstrata) ── ProdutoPerecivel (abstrata) ── Alimento, Bebida
│   │                    └── Eletronico
│   ├── Fragil, GerenciavelEstoque, Persistivel   (interfaces)
│   ├── Armazem, Endereco, Pedido, ItemPedido, StatusPedido
│   ├── SistemaArmazem                            (usuários, armazéns, pedidos e arquivo)
│   └── Validador, RegraDeNegocioException
├── controller/   telas JavaFX e tratamento de eventos
│   ├── LoginController, GerenteController, ClienteController
│   └── Alertas, Formatos, Tabelas, Navegador
└── app/          MainApp (Application) e Launcher
```

O diagrama de classes está em [`docs/UML.md`](docs/UML.md) (Mermaid, renderizado direto pelo GitHub).

## Onde cada conceito de POO foi aplicado

| Conceito | Onde |
|---|---|
| Encapsulamento e validação | atributos privados em todas as classes; os `set` e os construtores validam via `Validador` (RN01) |
| Sobrecarga de construtores | todas as classes concretas têm construtor padrão e parametrizado |
| Herança multinível e classes abstratas | `Produto` → `ProdutoPerecivel` → `Alimento`/`Bebida`; `Usuario` → `Cliente`/`GerenteArmazem` |
| `super(...)` e `super.metodo()` | construtores das subclasses; `exibirDetalhes()` em `Alimento`/`Bebida` |
| Polimorfismo (sobrescrita) | `calcularTaxaManuseio()`, `exibirDetalhes()`, `getTipo()`, `isVencido()` |
| Sobrecarga de método herdado | `reabastecer(int)` em `Produto` e `reabastecer(int, LocalDate)` em `ProdutoPerecivel` |
| Upcast, downcast, `instanceof` | listas de `Produto`; `instanceof` só para recursos exclusivos (validade dos perecíveis, `Fragil`) |
| Interfaces | `Fragil` (`Eletronico` + `Bebida`, usada por `ItemPedido` e `Pedido`), `GerenciavelEstoque` (`Armazem`), `Persistivel` (`SistemaArmazem`) |
| Composição | `Armazem` cria seu `Endereco`; `Pedido` cria seus `ItemPedido` |
| Agregação | `Armazem` → `Produto`; `ItemPedido` → `Produto`; `Pedido` → `Cliente` e `Armazem`; `GerenteArmazem` → `Armazem` |
| Coleções | `ArrayList` de produtos, itens, armazéns, usuários e pedidos |

## Regras de negócio

| Regra | Implementação |
|---|---|
| RN01 validação | `Validador` + `set`/construtores; erros aparecem em `Alert` |
| RN02 código sequencial | `Armazem.adicionarProduto` |
| RN03 capacidade | `Armazem.verificarCapacidade` (cadastro, edição e reabastecimento) |
| RN04 validade e alertas | `ProdutoPerecivel.isVencido`, `Armazem.getProdutosVencidos/ComEstoqueBaixo`; linhas coloridas na tela do Gerente; vencidos ocultos ao Cliente |
| RN05 taxa de manuseio | `calcularTaxaManuseio()` em cada tipo (2%, 3%, 5%) |
| RN06 embalagem especial | `Fragil.TAXA_EMBALAGEM_ESPECIAL` (R$ 4,00) |
| RN07 itens do pedido | `Pedido.adicionarItem`, `ItemPedido` |
| RN08 total e espera | `Pedido.calcularTotal`, `Pedido.calcularTempoEspera` |
| RN09 finalização | `Pedido.finalizar` (confere tudo antes de dar baixa) |
| RN10 salvar | `SistemaArmazem.salvar/carregar` (serialização de objetos) |

## Observações de projeto

- A senha não é guardada em texto puro: só o hash SHA-256.
- Cada produto guarda uma única data de validade; ao reabastecer um perecível, a validade do novo lote passa a valer.
- Na edição de um produto o tipo não pode ser trocado.
- Para publicar no GitHub: `git init`, `git add .`, `git commit -m "Projeto Armazém"` e depois `git remote add origin <url>` + `git push -u origin main`.
