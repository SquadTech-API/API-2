package br.com.squadtech.bluetech.controller;

/**
 * Menus que desejam reagir à troca de conteúdo do painel principal
 * podem implementar esta interface para receber notificações.
 */
public interface MenuAware extends SupportsMainController {
    /**
     * Notificado sempre que um conteúdo é carregado no painel direito.
     * @param fxmlPath caminho do FXML carregado (como passado para o loader)
     * @param contentController instância do controller do conteúdo carregado
     */
    void onContentChanged(String fxmlPath, Object contentController);
}

