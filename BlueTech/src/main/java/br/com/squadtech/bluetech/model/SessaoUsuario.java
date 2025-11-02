package br.com.squadtech.bluetech.model;

import org.slf4j.MDC;

public class SessaoUsuario {
    private static Usuario usuarioLogado;

    public static void setUsuarioLogado(Usuario usuario) {
        usuarioLogado = usuario;
        if (usuario != null && usuario.getEmail() != null) {
            MDC.put("userEmail", usuario.getEmail());
        } else {
            MDC.remove("userEmail");
        }
    }

    public static Usuario getUsuarioLogado() {
        return usuarioLogado;
    }

    public static void limparSessao() {
        usuarioLogado = null;
        MDC.remove("userEmail");
    }
}
