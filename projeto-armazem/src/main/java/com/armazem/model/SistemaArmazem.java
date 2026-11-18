package com.armazem.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reúne usuários, armazéns e pedidos (ArrayList) e grava tudo em arquivo por
 * serialização de objetos (RN10). Não depende de nada do JavaFX.
 */
public class SistemaArmazem implements Persistivel {

    private final Path arquivo;
    private List<Usuario> usuarios = new ArrayList<>();
    private List<Armazem> armazens = new ArrayList<>();
    private List<Pedido> pedidos = new ArrayList<>();
    private int proximoNumeroPedido = 1;

    public SistemaArmazem() {
        this(Path.of("armazem.dat"));
    }

    public SistemaArmazem(Path arquivo) {
        this.arquivo = arquivo;
    }

    public void cadastrarUsuario(Usuario usuario) {
        if (usuario == null || usuario.getEmail() == null) {
            throw new IllegalArgumentException("Usuário inválido.");
        }
        for (Usuario existente : usuarios) {
            if (existente.getEmail().equalsIgnoreCase(usuario.getEmail())) {
                throw new RegraDeNegocioException("Já existe um usuário cadastrado com este e-mail.");
            }
        }
        usuarios.add(usuario);
    }

    /** Retorna o usuário autenticado ou null se e-mail/senha não conferem. */
    public Usuario autenticar(String email, String senha) {
        for (Usuario usuario : usuarios) {
            if (usuario.autenticar(email, senha)) {
                return usuario;
            }
        }
        return null;
    }

    public void cadastrarArmazem(Armazem armazem) {
        if (armazem == null) {
            throw new IllegalArgumentException("Armazém inválido.");
        }
        if (!armazens.contains(armazem)) {
            armazens.add(armazem);
        }
    }

    public List<Armazem> getArmazens() {
        return Collections.unmodifiableList(armazens);
    }

    public List<Usuario> getUsuarios() {
        return Collections.unmodifiableList(usuarios);
    }

    /** Registra um pedido já finalizado e lhe atribui o número sequencial. */
    public void registrarPedido(Pedido pedido) {
        if (pedido == null || pedido.getStatus() != StatusPedido.FINALIZADO) {
            throw new RegraDeNegocioException("Somente pedidos finalizados podem ser registrados.");
        }
        pedido.setNumero(proximoNumeroPedido++);
        pedidos.add(pedido);
    }

    public List<Pedido> getPedidosDoCliente(Cliente cliente) {
        return pedidos.stream().filter(pedido -> pedido.getCliente() == cliente).toList();
    }

    @Override
    public void salvar() throws IOException {
        Path temporario = Path.of(arquivo.toString() + ".tmp");
        try (ObjectOutputStream saida = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(temporario)))) {
            saida.writeObject(usuarios);
            saida.writeObject(armazens);
            saida.writeObject(pedidos);
            saida.writeInt(proximoNumeroPedido);
        }
        Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void carregar() throws IOException, ClassNotFoundException {
        if (!Files.exists(arquivo)) {
            return;
        }
        try (ObjectInputStream entrada = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(arquivo)))) {
            List<Usuario> usuariosLidos = (List<Usuario>) entrada.readObject();
            List<Armazem> armazensLidos = (List<Armazem>) entrada.readObject();
            List<Pedido> pedidosLidos = (List<Pedido>) entrada.readObject();
            int proximoLido = entrada.readInt();
            usuarios = usuariosLidos;
            armazens = armazensLidos;
            pedidos = pedidosLidos;
            proximoNumeroPedido = proximoLido;
        }
    }
}
