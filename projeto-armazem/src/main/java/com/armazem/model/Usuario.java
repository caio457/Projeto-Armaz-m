package com.armazem.model;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Superclasse abstrata dos usuários do sistema (Cliente e GerenteArmazem).
 * A senha nunca é guardada em texto puro: apenas o hash SHA-256 é armazenado.
 */
public abstract class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nome = "";
    private String email;
    private String senhaHash;

    protected Usuario() { }

    protected Usuario(String nome, String email, String senha) {
        setNome(nome);
        setEmail(email);
        setSenha(senha);
    }

    public String getNome() { return nome; }

    public void setNome(String nome) { this.nome = Validador.texto(nome, 3, "O nome"); }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = Validador.email(email); }

    /** Valida a senha (mínimo de 6 caracteres) e guarda somente o hash. */
    public void setSenha(String senha) {
        Validador.senha(senha);
        this.senhaHash = hash(senha);
    }

    public boolean autenticar(String emailInformado, String senhaInformada) {
        if (email == null || senhaHash == null || emailInformado == null || senhaInformada == null) {
            return false;
        }
        return email.equalsIgnoreCase(emailInformado.trim()) && senhaHash.equals(hash(senhaInformada));
    }

    /** Nome do perfil, definido por cada subclasse. */
    public abstract String getPerfil();

    private static String hash(String senha) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(senha.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    @Override
    public String toString() {
        return nome + " (" + getPerfil() + ")";
    }
}
