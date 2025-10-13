package br.com.squadtech.bluetech.controller.login;

import com.jfoenix.controls.JFXButton;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

public class TelaLoginController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private JFXButton btnLogar;

    @FXML
    private JFXButton btnSignCadastro;

    @FXML
    private JFXButton btnSignLogin;

    @FXML
    private AnchorPane paneLoginAcc;

    @FXML
    private AnchorPane paneSignBtns;

    @FXML
    private AnchorPane paneSignData;

    @FXML
    private HBox paneSignHbox;

    @FXML
    private AnchorPane paneSignLogo;

    @FXML
    private SplitPane paneSignSplit;

    @FXML
    private PasswordField txtFldPass;

    @FXML
    private TextField txtFldUser;

    @FXML
    void handleSignCadastro(ActionEvent event) throws IOException {
        loadPane("/fxml/login/TelaCadastro.fxml");
    }

    @FXML
    void handleSignLogin(ActionEvent event) {
        paneSignData.getChildren().clear();
        paneSignData.getChildren().add(paneLoginAcc);

        AnchorPane.setTopAnchor(paneLoginAcc, 0.0);
        AnchorPane.setRightAnchor(paneLoginAcc, 0.0);
        AnchorPane.setBottomAnchor(paneLoginAcc, 0.0);
        AnchorPane.setLeftAnchor(paneLoginAcc, 0.0);
    }

    private void loadPane(String fxmlPath) throws IOException {
        AnchorPane pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
        paneSignData.getChildren().clear();
        paneSignData.getChildren().add(pane);

        //faz o novo painel preencher todo o espaço
        AnchorPane.setTopAnchor(pane, 0.0);
        AnchorPane.setRightAnchor(pane, 0.0);
        AnchorPane.setBottomAnchor(pane, 0.0);
        AnchorPane.setLeftAnchor(pane, 0.0);
    }

    @FXML
    void initialize() {
        assert btnLogar != null : "fx:id=\"btnLogar\" was not injected: check your FXML file 'TelaLogin.fxml'.";
        assert btnSignCadastro != null : "fx:id=\"btnSignCadastro\" was not injected: check your FXML file 'TelaLogin.fxml'.";
        assert btnSignLogin != null : "fx:id=\"btnSignLogin\" was not injected: check your FXML file 'TelaLogin.fxml'.";
        assert paneLoginAcc != null : "fx:id=\"paneLoginAcc\" was not injected: check your FXML file 'TelaLogin.fxml'.";
        assert paneSignBtns != null : "fx:id=\"paneSignBtns\" was not injected: check your FXML file 'TelaLogin.fxml'.";
        assert paneSignData != null : "fx:id=\"paneSignData\" was not injected: check your FXML file 'TelaLogin.fxml'.";
        assert paneSignHbox != null : "fx:id=\"paneSignHbox\" was not injected: check your FXML file 'TelaLogin.fxml'.";
        assert paneSignLogo != null : "fx:id=\"paneSignLogo\" was not injected: check your FXML file 'TelaLogin.fxml'.";
        assert paneSignSplit != null : "fx:id=\"paneSignSplit\" was not injected: check your FXML file 'TelaLogin.fxml'.";
        assert txtFldPass != null : "fx:id=\"txtFldPass\" was not injected: check your FXML file 'TelaLogin.fxml'.";
        assert txtFldUser != null : "fx:id=\"txtFldUser\" was not injected: check your FXML file 'TelaLogin.fxml'.";

    }

}
