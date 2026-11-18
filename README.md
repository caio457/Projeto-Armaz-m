# 📦 Sistema de Gestão de Armazém (Projeto Armazém)

![Java](https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge&logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-GUI-blue?style=for-the-badge&logo=java)
![CESUPA](https://img.shields.io/badge/CESUPA-Engenharia%20de%20Computa%C3%A7%C3%A3o-green?style=for-the-badge)

Sistema desktop desenvolvido em **Java** com interface gráfica em **JavaFX** para o controle de estoque, logística e pedidos de um centro de armazenagem e distribuição de mercadorias.

> **Contexto Acadêmico:** Projeto desenvolvido para a disciplina de **Programação 2** (Prof. Pedro Girotto), do curso de **Engenharia de Computação** — ARGO (Escola de Negócios, Tecnologia e Inovação do **CESUPA**).

---

## 📋 Índice

- [Sobre o Projeto](#-sobre-o-projeto)
- [Funcionalidades](#-funcionalidades)
- [Conceitos de POO Aplicados](#-conceitos-de-poo-aplicados)
- [Regras de Negócio e Cálculos](#-regras-de-negócio-e-cálculos)
- [Diagrama UML](#-diagrama-uml)
- [Tecnologias e Estrutura do Projeto](#-tecnologias-e-estrutura-do-projeto)
- [Como Executar o Sistema](#-como-executar-o-sistema)
- [Equipe](#-equipe)

---

## 🎯 Sobre o Projeto

O **Projeto Armazém** simula a operação de um centro de distribuição, permitindo a interação de dois perfis distintos de usuários por meio de telas gráficas interativas: **Gerente do Armazém** e **Cliente**. Toda a regra de negócio é isolada nas camadas de modelo, garantindo baixo acoplamento com a interface gráfica e persistência dos dados em arquivo.

---

## 🚀 Funcionalidades

### 👨‍💼 Área do Gerente do Armazém
* **Autenticação:** Cadastro e login com perfil de gerente.
* **Cadastro do Armazém:** Registro de nome, endereço completo (rua, número e cidade) e capacidade máxima de armazenagem (em kg).
* **Cadastro de Produtos:** Adição de itens ao estoque com geração automática de código sequencial e formulários dinâmicos para cada categoria:
  * **Alimento:** Categoria (grãos, laticínios, carnes, etc.), indicador de produto orgânico, data de validade e temperatura de conservação.
  * **Bebida:** Volume ($\text{ml}$), indicador de bebida alcoólica, data de validade e temperatura de conservação.
  * **Eletrônico:** Voltagem ($110\text{V}$ ou $220\text{V}$) e tempo de garantia (em meses).
* **Gerenciamento de Estoque:** Edição de dados, remoção de produtos e registro de reabastecimento (incluindo atualização de validade para novos lotes perecíveis).
* **Alertas Visuais:** Destaque automático para produtos com estoque baixo ($\text{quantidade} \le \text{estoque mínimo}$) e produtos perecíveis vencidos.
* **Persistência:** Salvamento e carregamento automático das alterações em arquivo.

### 🛒 Área do Cliente
* **Autenticação:** Cadastro e login com perfil de cliente.
* **Exploração de Armazéns:** Listagem dos armazéns disponíveis e visualização dos produtos aptos para venda (produtos vencidos são ocultados automaticamente).
* **Montagem de Pedido:** Seleção de produtos e quantidades desejadas, com soma automática caso um item já esteja no carrinho.
* **Resumo em Tempo Real:** Cálculo dinâmico do valor total do pedido (incluindo taxas de manuseio e embalagem especial) e do tempo estimado de separação.
* **Finalização de Pedido:** Validação de disponibilidade em estoque, baixa automática das quantidades e encerramento do pedido (`ABERTO` $\rightarrow$ `FINALIZADO`).

---

## 🧱 Conceitos de POO Aplicados

A arquitetura do sistema foi projetada para aplicar os pilares da Programação Orientada a Objetos:

* **Encapsulamento e Validação:** Atributos privados em todas as classes de modelo, com validações rigorosas nos métodos `set` e nos construtores parametrizados.
* **Construtores e Sobrecarga:** Múltiplas formas de instanciação (construtor padrão e parametrizado) com reaproveitamento via `this(...)` e `super(...)`.
* **Herança Simples, Multinível e Classes Abstratas:**
  * `Usuario` (abstrata) $\rightarrow$ `Cliente` e `GerenteArmazem`.
  * `Produto` (abstrata) $\rightarrow$ `Eletronico` e `ProdutoPerecivel` (abstrata) $\rightarrow$ `Alimento` e `Bebida`.
* **Polimorfismo:**
  * *Sobrescrita (`@Override`):* Métodos `calcularTaxaManuseio()` e `exibirDetalhes()` especializados em cada subclasse concreta de `Produto`.
  * *Sobrecarga:* Métodos `reabastecer(int quantidade)` em `Produto` e `reabastecer(int quantidade, LocalDate novaValidade)` em `ProdutoPerecivel`.
  * *Upcast, Downcast e `instanceof`:* Uso de coleções polimórficas (`List<Produto>`, `List<Usuario>`) e verificação de tipo apenas para acesso a atributos exclusivos (como data de validade).
* **Interfaces:**
  * `Fragil`: Implementada por `Eletronico` e `Bebida`, definindo `getTaxaEmbalagemEspecial()` e `getInstrucoesManuseio()`.
  * `GerenciavelEstoque`: Implementada por `Armazem`, padronizando operações de adição, atualização, remoção e busca de produtos.
* **Relacionamentos (Composição e Agregação):**
  * *Composição:* `Armazem` cria e gerencia seu próprio `Endereco`; `Pedido` cria e gerencia sua lista de `ItemPedido`.
  * *Agregação:* `Armazem` agrega `Produto`; `ItemPedido` referencia `Produto`; `Pedido` referencia `Cliente` e `Armazem`; `GerenteArmazem` referencia `Armazem`.

---

## 📐 Regras de Negócio e Cálculos

* **Capacidade do Armazém (RN03):** A carga total armazenada respeita o limite:
  $$\sum (\text{peso} \times \text{quantidade em estoque}) \le \text{capacidade máxima (kg)}$$
* **Taxa de Manuseio por Unidade (RN05):**
  * **Alimento:** $2\%$ do preço unitário.
  * **Bebida:** $3\%$ do preço unitário.
  * **Eletrônico:** $5\%$ do preço unitário.
* **Taxa de Embalagem Especial (RN06):** Produtos que implementam a interface `Fragil` (`Bebida` e `Eletronico`) possuem acréscimo fixo de $\text{R\$ } 4,00$ por unidade.
* **Cálculo do Item e Tempo de Espera (RN07 e RN08):**
  $$\text{Valor do Item} = (\text{Preço Unitário} + \text{Taxa de Manuseio} + \text{Taxa de Embalagem}) \times \text{Quantidade}$$
  $$\text{Tempo de Espera (min)} = \sum (\text{Tempo de Separação} \times \text{Quantidade})$$
* **Validações Principais (RN01):** Nomes com tamanho $\ge 3$ caracteres; preços, pesos, volumes e tempos de separação $> 0$; estoques $\ge 0$; garantia entre $0$ e $60$ meses; voltagem igual a $110$ ou $220$; e-mail contendo `@` e senha com tamanho $\ge 6$ caracteres.

---

## 📊 Diagrama UML

> *(Insira aqui a imagem do diagrama de classes UML desenvolvido pela equipe, por exemplo: `![Diagrama UML](docs/diagrama-uml.png)`)*

---

## 🛠️ Tecnologias e Estrutura do Projeto

* **Linguagem:** Java (JDK 17 ou superior)
* **Interface Gráfica:** JavaFX (FXML / Scene Builder)
* **Persistência de Dados:** Arquivos locais (Serialização / CSV / JSON)

### Estrutura de Diretórios

```text
projeto-armazem/
├── docs/                  # Diagrama UML e documentação complementar
├── data/                  # Arquivos de persistência salvos pelo sistema
├── src/
│   ├── model/             # Classes de domínio, interfaces e regras de negócio (sem imports JavaFX)
│   ├── persistence/       # Classes responsáveis por salvar e carregar os dados
│   ├── controller/        # Controladores das telas JavaFX
│   ├── view/              # Arquivos .fxml e folhas de estilo .css
│   └── Main.java          # Classe principal (ponto de entrada da aplicação)
└── README.md
```

---

## 💻 Como Executar o Sistema

### 1. Pré-requisitos
* **Java Development Kit (JDK) 17** ou superior instalado.
* **JavaFX SDK** compatível com a versão do seu JDK (caso não utilize Maven/Gradle com dependências gerenciadas).
* IDE de sua preferência (IntelliJ IDEA, Eclipse ou VS Code) ou terminal configurado.

### 2. Clonar o Repositório
```bash
git clone https://github.com/seu-usuario/projeto-armazem.git
cd projeto-armazem
```

### 3. Executar via IDE (IntelliJ IDEA / Eclipse)
1. Importe o projeto na sua IDE.
2. Caso utilize o **JavaFX SDK** manualmente, adicione a biblioteca do JavaFX ao *Build Path / Project Structure* e configure os **VM Options** na execução da classe `Main`:
   ```bash
   --module-path /caminho/para/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
   ```
3. Execute a classe `Main.java`.

### 4. Executar via Terminal (Maven — *opcional, caso aplicável*)
Se o projeto estiver configurado com Maven:
```bash
mvn clean javafx:run
```

---

## 👥 Equipe

Desenvolvido por:

* **Caio Gabriel Paixão Lima  
* **[Nome do Integrante 2
* **[Nome do Integrante 3
