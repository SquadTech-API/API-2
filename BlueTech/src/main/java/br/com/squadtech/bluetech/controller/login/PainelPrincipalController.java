package br.com.squadtech.bluetech.controller.login;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.aluno.MenuAlunoController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;

public class PainelPrincipalController {

    @FXML private ResourceBundle resources;
    @FXML private URL location;

    @FXML private SplitPane painelPrincipal;
    @FXML private AnchorPane painelPrincipalExibicao;
    @FXML private AnchorPane painelPrincipalMenu;

    // Controller atual do menu, para atualizações como avatar
    private MenuAlunoController menuAlunoController;

    // ------------------ MÉTODOS PÚBLICOS ------------------

    /**
     * Retorna o painel de exibição (lado direito) para manipulação direta.
     */
    public AnchorPane getPainelPrincipalExibicao() {
        return painelPrincipalExibicao;
    }

    /**
     * Carrega um FXML no painel lateral (menu) e injeta controller se suportar.
     */
    public void loadMenu(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(fxmlPath)));
        Parent pane = loader.load();

        Object controller = loader.getController();
        if (controller instanceof MenuAlunoController menuCtrl) {
            this.menuAlunoController = menuCtrl;
            menuCtrl.setPainelPrincipalController(this);
        }

        painelPrincipalMenu.getChildren().setAll(pane);
        setAnchorPaneFullSize(pane);
    }

    /**
     * Carrega um FXML no painel de exibição (lado direito) e injeta controller se suportar SupportsMainController.
     * Este é usado para carregamentos simples onde a configuração do controller é automática.
     */
    public void loadContent(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(fxmlPath)));
        Parent pane = loader.load();

        Object controller = loader.getController();
        if (controller instanceof SupportsMainController supportsMain) {
            supportsMain.setPainelPrincipalController(this);
        }

        // Delega a exibição ao novo método loadRoot
        loadRoot(pane);
    }

    /**
     * 🔑 NOVO MÉTODO DE CORREÇÃO: Carrega um Parent (Root Node) já configurado no painel de exibição.
     *
     * Permite que controllers externos carreguem o FXML, configurem seu controller
     * (ex: chamando setVersaoId) e passem o resultado final para exibição,
     * evitando a recriação da instância.
     */
    public void loadRoot(Parent content) {
        painelPrincipalExibicao.getChildren().setAll(content);
        setAnchorPaneFullSize(content);
    }

    /**
     * Atualiza a imagem do aluno no menu.
     */
    public void updateFotoMenuAluno(String imagePath) {
        if (menuAlunoController != null) {
            menuAlunoController.updateFotoAluno(imagePath);
        }
    }

    // ------------------ MÉTODOS PRIVADOS ------------------

    @FXML
    void initialize() {
        assert painelPrincipal != null : "fx:id=\"painelPrincipal\" não injetado.";
        assert painelPrincipalExibicao != null : "fx:id=\"painelPrincipalExibicao\" não injetado.";
        assert painelPrincipalMenu != null : "fx:id=\"painelPrincipalMenu\" não injetado.";

        // Define largura fixa do menu
        painelPrincipalMenu.setMinWidth(260.0);
        painelPrincipalMenu.setPrefWidth(300.0);
        SplitPane.setResizableWithParent(painelPrincipalMenu, Boolean.FALSE);

        // Ajusta clips para evitar overflow
        setClip(painelPrincipalMenu);
        setClip(painelPrincipalExibicao);

        // Ajusta divisor do SplitPane
        Platform.runLater(this::fixDividerPosition);
        painelPrincipal.widthProperty().addListener((obs, oldW, newW) -> fixDividerPosition());

        // Trava o divisor para manter o menu fixo
        if (!painelPrincipal.getDividers().isEmpty()) {
            painelPrincipal.getDividers().get(0).positionProperty().addListener((obs, oldPos, newPos) -> {
                double desired = computeDesiredDividerPosition();
                if (Math.abs(newPos.doubleValue() - desired) > 0.0005) {
                    painelPrincipal.setDividerPositions(desired);
                }
            });
        }
    }

    /**
     * Calcula a posição ideal do divisor (~300px menu)
     */
    private double computeDesiredDividerPosition() {
        double total = Math.max(1.0, painelPrincipal.getWidth());
        double desired = painelPrincipalMenu.getPrefWidth() / total;
        return Math.max(0.1, Math.min(0.9, desired));
    }

    /**
     * Ajusta a posição do divisor do SplitPane
     */
    private void fixDividerPosition() {
        painelPrincipal.setDividerPositions(computeDesiredDividerPosition());
    }

    /**
     * Define clip em AnchorPane para evitar overflow
     */
    private void setClip(AnchorPane pane) {
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(pane.widthProperty());
        clip.heightProperty().bind(pane.heightProperty());
        pane.setClip(clip);
    }

    /**
     * Faz o Parent ocupar todo o espaço do AnchorPane
     */
    private void setAnchorPaneFullSize(Parent pane) {
        AnchorPane.setTopAnchor(pane, 0.0);
        AnchorPane.setRightAnchor(pane, 0.0);
        AnchorPane.setBottomAnchor(pane, 0.0);
        AnchorPane.setLeftAnchor(pane, 0.0);
    }
}