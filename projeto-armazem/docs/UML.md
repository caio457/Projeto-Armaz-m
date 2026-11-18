# Diagrama de classes — Projeto Armazém

O GitHub renderiza o bloco Mermaid abaixo automaticamente. Para exportar como imagem (PNG/SVG),
cole o conteúdo em <https://mermaid.live>.

```mermaid
classDiagram
    direction TB

    class Usuario {
        <<abstract>>
        -String nome
        -String email
        -String senhaHash
        +setNome(String)
        +setEmail(String)
        +setSenha(String)
        +autenticar(String, String) boolean
        +getPerfil() String*
    }
    class Cliente {
        +getPerfil() String
    }
    class GerenteArmazem {
        -Armazem armazem
        +getArmazem() Armazem
        +setArmazem(Armazem)
        +getPerfil() String
    }

    class Endereco {
        -String rua
        -String numero
        -String cidade
    }

    class Armazem {
        -String nome
        -double capacidadeMaxima
        -int proximoCodigo
        +getCapacidadeOcupada() double
        +getEspacoDisponivel() double
        +reabastecerProduto(int, int)
        +reabastecerProduto(int, int, LocalDate)
        +getProdutosComEstoqueBaixo() List
        +getProdutosVencidos() List
        +getProdutosDisponiveis() List
    }

    class Produto {
        <<abstract>>
        -int codigo
        -String nome
        -String descricao
        -double precoUnitario
        -double peso
        -int quantidadeEstoque
        -int estoqueMinimo
        -int tempoSeparacao
        +reabastecer(int)
        +retirarDoEstoque(int)
        +isEstoqueBaixo() boolean
        +isVencido() boolean
        +calcularTaxaManuseio() double*
        +exibirDetalhes() String*
        +getTipo() String*
    }
    class ProdutoPerecivel {
        <<abstract>>
        -LocalDate dataValidade
        -double temperaturaConservacao
        +reabastecer(int, LocalDate)
        +isVencido() boolean
        +exibirDetalhes() String
    }
    class Alimento {
        -String categoria
        -boolean organico
        +calcularTaxaManuseio() double
        +exibirDetalhes() String
    }
    class Bebida {
        -int volumeMl
        -boolean alcoolica
        +calcularTaxaManuseio() double
        +exibirDetalhes() String
    }
    class Eletronico {
        -int voltagem
        -int garantiaMeses
        +calcularTaxaManuseio() double
        +exibirDetalhes() String
    }

    class Pedido {
        -int numero
        -StatusPedido status
        -LocalDateTime dataCriacao
        -LocalDateTime dataFinalizacao
        +adicionarItem(Produto, int)
        +removerItem(ItemPedido)
        +calcularTotal() double
        +calcularTempoEspera() int
        +finalizar()
    }
    class ItemPedido {
        -int quantidade
        +getValor() double
        +getTempoSeparacao() int
    }
    class StatusPedido {
        <<enumeration>>
        ABERTO
        FINALIZADO
    }

    class Fragil {
        <<interface>>
        +getTaxaEmbalagemEspecial() double
        +getInstrucoesManuseio() String
    }
    class GerenciavelEstoque {
        <<interface>>
        +adicionarProduto(Produto)
        +atualizarProduto(int, Produto)
        +removerProduto(int) boolean
        +buscarProduto(int) Produto
        +listarProdutos() List
    }
    class Persistivel {
        <<interface>>
        +salvar()
        +carregar()
    }
    class SistemaArmazem {
        -List usuarios
        -List armazens
        -List pedidos
        +cadastrarUsuario(Usuario)
        +autenticar(String, String) Usuario
        +registrarPedido(Pedido)
    }

    Usuario <|-- Cliente
    Usuario <|-- GerenteArmazem
    Produto <|-- ProdutoPerecivel
    Produto <|-- Eletronico
    ProdutoPerecivel <|-- Alimento
    ProdutoPerecivel <|-- Bebida

    Fragil <|.. Eletronico
    Fragil <|.. Bebida
    GerenciavelEstoque <|.. Armazem
    Persistivel <|.. SistemaArmazem

    Armazem *-- Endereco : composição
    Pedido *-- ItemPedido : composição
    Armazem o-- Produto : agregação
    ItemPedido o-- Produto : agregação
    Pedido o-- Cliente : agregação
    Pedido o-- Armazem : agregação
    GerenteArmazem o-- Armazem : gerencia
    Pedido --> StatusPedido
    SistemaArmazem o-- Usuario
    SistemaArmazem o-- Armazem
    SistemaArmazem o-- Pedido
```

Legenda: `*--` composição (o contido não existe sem o todo), `o--` agregação (objetos independentes),
`<|--` herança, `<|..` implementação de interface, `*` após o método indica método abstrato.
