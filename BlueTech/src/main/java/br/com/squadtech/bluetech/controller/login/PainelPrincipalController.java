package br.com.squadtech.bluetech.controller.login;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

import br.com.squadtech.bluetech.controller.MenuAware;
import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.aluno.MenuAlunoController;
import br.com.squadtech.bluetech.controller.professorTG.MenuProfessorTGController;
import br.com.squadtech.bluetech.controller.professorOrientador.MenuProfessorOrientadorController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;

public class PainelPrincipalController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private SplitPane painelPrincipal;

    @FXML
    private AnchorPane painelPrincipalExibicao;

    @FXML
    private AnchorPane painelPrincipalMenu;

    // Mantém referência ao controller do menu atual (para atualizar avatar, etc.)
    private MenuAlunoController menuAlunoController;
    private MenuProfessorOrientadorController menuProfessorOrientadorController;

    // Mantém referência a menus que desejam reagir à troca de conteúdo
    private MenuAware menuAware;

    // Mantém referência ao controller do menu atual, permitindo acesso a métodos específicos de cada menu
    private Object currentMenuController;

    /**
     * Carrega um FXML dentro do painel lateral de menu (painelPrincipalMenu).
     */
    public void loadMenu(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(fxmlPath)));
        Parent pane = loader.load();

        // Injeta este controller no menu se ele suportar tal referência
        Object controller = loader.getController();
        if (controller instanceof SupportsMainController supportsMain) {
            supportsMain.setPainelPrincipalController(this);
        }
        if (controller instanceof MenuAlunoController menuAlunoController) {
            this.menuAlunoController = menuAlunoController;
            // Garantir injeção explícita também para compatibilidade
            menuAlunoController.setPainelPrincipalController(this);
        }
        if (controller instanceof MenuProfessorOrientadorController orientadorController) {
            this.menuProfessorOrientadorController = orientadorController;
            orientadorController.setPainelPrincipalController(this);
        }
        this.menuAware = (controller instanceof MenuAware) ? (MenuAware) controller : null;
        this.currentMenuController = controller;

        painelPrincipalMenu.getChildren().clear();
        painelPrincipalMenu.getChildren().add(pane);

        //Faz o novo painel preencher todo o espaço
        AnchorPane.setTopAnchor(pane, 0.0);
        AnchorPane.setRightAnchor(pane, 0.0);
        AnchorPane.setBottomAnchor(pane, 0.0);
        AnchorPane.setLeftAnchor(pane, 0.0);
    }

    /**
     * Carrega um FXML dentro do painel de exibição (direita) do SplitPane.
     * Se o controller carregado implementar SupportsMainController, injeta este controller principal nele.
     */
    public void loadContent(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(fxmlPath)));
        Parent pane = loader.load();

        Object controller = loader.getController();
        if (controller instanceof SupportsMainController supportsMain) {
            supportsMain.setPainelPrincipalController(this);
        }

        // Aplica automaticamente a moldura dourada nas telas de conteúdo (painel direito), com possibilidade de opt-out
        if (!pane.getStyleClass().contains("no-panel-frame") && !pane.getStyleClass().contains("panel-golden-rounded")) {
            pane.getStyleClass().add("panel-golden-rounded");
        }

        painelPrincipalExibicao.getChildren().clear();
        painelPrincipalExibicao.getChildren().add(pane);

        AnchorPane.setTopAnchor(pane, 0.0);
        AnchorPane.setRightAnchor(pane, 0.0);
        AnchorPane.setBottomAnchor(pane, 0.0);
        AnchorPane.setLeftAnchor(pane, 0.0);

        // Notifica menu se aplicável
        if (menuAware != null) {
            menuAware.onContentChanged(fxmlPath, controller);
        }
    }

    /**
     * Variante de loadContent que retorna o controller carregado, para que o chamador possa configurar parâmetros.
     */
    public <T> T loadContentReturnController(String fxmlPath, Class<T> controllerType) throws IOException {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(fxmlPath)));
        Parent pane = loader.load();

        Object controller = loader.getController();
        if (controller instanceof SupportsMainController supportsMain) {
            supportsMain.setPainelPrincipalController(this);
        }

        if (!pane.getStyleClass().contains("no-panel-frame") && !pane.getStyleClass().contains("panel-golden-rounded")) {
            pane.getStyleClass().add("panel-golden-rounded");
        }

        painelPrincipalExibicao.getChildren().clear();
        painelPrincipalExibicao.getChildren().add(pane);

        AnchorPane.setTopAnchor(pane, 0.0);
        AnchorPane.setRightAnchor(pane, 0.0);
        AnchorPane.setBottomAnchor(pane, 0.0);
        AnchorPane.setLeftAnchor(pane, 0.0);

        // Notifica menu se aplicável
        if (menuAware != null) {
            menuAware.onContentChanged(fxmlPath, controller);
        }

        if (controllerType.isInstance(controller)) {
            return controllerType.cast(controller);
        }
        return null;
    }

    /**
     * Atualiza a imagem de perfil exibida no menu do aluno, caso esteja carregado.
     */
    public void updateFotoMenuAluno(String imagePath) {
        if (menuAlunoController != null) {
            menuAlunoController.updateFotoAluno(imagePath);
        }
        if (currentMenuController instanceof MenuProfessorTGController menuProfessorTGController) {
            menuProfessorTGController.updateFotoProfessorTG(imagePath);
        }
    }

    public void updateFotoMenuProfessorOrientador(String imagePath) {
        if (menuProfessorOrientadorController != null) {
            menuProfessorOrientadorController.updateFotoProfessorOrientador(imagePath);
        }
    }

    @FXML
    void initialize() {
        assert painelPrincipal != null : "fx:id=\"painelPrincipal\" was not injected: check your FXML file 'PainelPrincipal.fxml'.";
        assert painelPrincipalExibicao != null : "fx:id=\"painelPrincipalExibicao\" was not injected: check your FXML file 'PainelPrincipal.fxml'.";
        assert painelPrincipalMenu != null : "fx:id=\"painelPrincipalMenu\" was not injected: check your FXML file 'PainelPrincipal.fxml'.";

    }
}
