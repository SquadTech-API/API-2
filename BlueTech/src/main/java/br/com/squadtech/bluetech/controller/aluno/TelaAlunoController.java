package br.com.squadtech.bluetech.controller.aluno;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Alert.AlertType;

public class TelaAlunoController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Hyperlink linkGithubSquadTech;

    @FXML
    void abrirRepoSquadTech(ActionEvent event) {
        final String url = "https://github.com/SquadTech-API/API-2";
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                throw new UnsupportedOperationException("Desktop API não suportada");
            }
        } catch (IOException | URISyntaxException | UnsupportedOperationException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erro ao abrir link");
            alert.setHeaderText(null);
            alert.setContentText("Não foi possível abrir o navegador. Você pode acessar: \n" + url);
            alert.showAndWait();
        }
    }

    @FXML
    void initialize() {
        assert linkGithubSquadTech != null : "fx:id=\"linkGithubSquadTech\" was not injected: check your FXML file 'TelaAluno.fxml'.";

    }

}
