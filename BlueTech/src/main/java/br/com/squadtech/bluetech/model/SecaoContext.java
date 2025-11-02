package br.com.squadtech.bluetech.model;

public class SecaoContext {
    private static Integer idSecaoSelecionada;
    // Guarda a versão atualmente selecionada (opcional)
    private static Integer idVersaoSelecionada;

    public static void setIdSecaoSelecionada(Integer id) {
        idSecaoSelecionada = id;
    }

    public static Integer getIdSecaoSelecionada() {
        return idSecaoSelecionada;
    }

    public static void setIdVersaoSelecionada(Integer id) {
        idVersaoSelecionada = id;
    }

    public static Integer getIdVersaoSelecionada() {
        return idVersaoSelecionada;
    }

    public static void limpar() {
        idSecaoSelecionada = null;
        idVersaoSelecionada = null;
    }
}
