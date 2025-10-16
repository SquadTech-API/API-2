package br.com.squadtech.bluetech.controller.login;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

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

    /**
     * Carrega um FXML dentro do painel lateral de menu (painelPrincipalMenu).
     */
    public void loadMenu(String fxmlPath) throws IOException {
        Parent pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
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
     */
    public void loadContent(String fxmlPath) throws IOException {
        Parent pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
        painelPrincipalExibicao.getChildren().clear();
        painelPrincipalExibicao.getChildren().add(pane);

        AnchorPane.setTopAnchor(pane, 0.0);
        AnchorPane.setRightAnchor(pane, 0.0);
        AnchorPane.setBottomAnchor(pane, 0.0);
        AnchorPane.setLeftAnchor(pane, 0.0);
    }

    @FXML
    void initialize() {
        assert painelPrincipal != null : "fx:id=\"painelPrincipal\" was not injected: check your FXML file 'PainelPrincipal.fxml'.";
        assert painelPrincipalExibicao != null : "fx:id=\"painelPrincipalExibicao\" was not injected: check your FXML file 'PainelPrincipal.fxml'.";
        assert painelPrincipalMenu != null : "fx:id=\"painelPrincipalMenu\" was not injected: check your FXML file 'PainelPrincipal.fxml'.";

        //Responsividade do SplitPane: mantém largura fixa do menu (esquerda)
        painelPrincipalMenu.setMinWidth(260.0);
        painelPrincipalMenu.setPrefWidth(300.0);
        SplitPane.setResizableWithParent(painelPrincipalMenu, Boolean.FALSE);

        //Evita que conteúdos desenhem fora dos limites dos painéis (invadindo o outro lado)
        Rectangle clipMenu = new Rectangle();
        clipMenu.widthProperty().bind(painelPrincipalMenu.widthProperty());
        clipMenu.heightProperty().bind(painelPrincipalMenu.heightProperty());
        painelPrincipalMenu.setClip(clipMenu);

        Rectangle clipExibicao = new Rectangle();
        clipExibicao.widthProperty().bind(painelPrincipalExibicao.widthProperty());
        clipExibicao.heightProperty().bind(painelPrincipalExibicao.heightProperty());
        painelPrincipalExibicao.setClip(clipExibicao);

        //Ajusta a posição do divisor para refletir a largura preferida do menu (aprox. 300px)
        Platform.runLater(() -> fixDividerPosition());

        //Mantém a largura do menu estável em redimensionamentos de largura do SplitPane
        painelPrincipal.widthProperty().addListener((obs, oldW, newW) -> fixDividerPosition());

        //Impede o usuário de alterar a posição do divisor (trava o menu em ~300px)
        if (!painelPrincipal.getDividers().isEmpty()) {
            painelPrincipal.getDividers().get(0).positionProperty().addListener((obs, oldPos, newPos) -> {
                double desired = computeDesiredDividerPosition();
                if (Math.abs(newPos.doubleValue() - desired) > 0.0005) {
                    painelPrincipal.setDividerPositions(desired);
                }
            });
        }
    }

    private void fixDividerPosition() {
        double pos = computeDesiredDividerPosition();
        painelPrincipal.setDividerPositions(pos);
    }

    private double computeDesiredDividerPosition() {
        double total = Math.max(1.0, painelPrincipal.getWidth());
        double desired = painelPrincipalMenu.getPrefWidth() / total;
        // Evita valores extremos caso o total varie durante o layout
        desired = Math.max(0.1, Math.min(0.9, desired));
        return desired;
    }

}
