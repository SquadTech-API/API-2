package br.com.squadtech.bluetech.model;

public class SecaoContext {
    private static Integer idSecaoSelecionada;

    public static void setIdSecaoSelecionada(Integer id) {
        idSecaoSelecionada = id;
    }

    public static Integer getIdSecaoSelecionada() {
        return idSecaoSelecionada;
    }

    public static void limpar() {
        idSecaoSelecionada = null;
    }
}

