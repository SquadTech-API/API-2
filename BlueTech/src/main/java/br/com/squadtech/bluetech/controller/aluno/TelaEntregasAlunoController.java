package br.com.squadtech.bluetech.controller.aluno;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;

//Implementando a classe de Interface para chamar o Painel Principal
public class TelaEntregasAlunoController implements SupportsMainController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnCriarSecao;

    @FXML
    private FlowPane flowCards;

    @FXML
    void CriarEntregaSessaoAPI(ActionEvent event) {
        //Usa a referência para carregar o novo conteúdo
        if (painelPrincipalController != null) {
            try {
                String fxmlPath = "/fxml/aluno/CriarSecaoAPI.fxml";

                painelPrincipalController.loadContent(fxmlPath);
            } catch (IOException e) {
                System.err.println("Falha ao carregar CriarSecaoAPI.fxml");
                e.printStackTrace();

            }
        } else {
            System.err.println("Erro: PainelPrincipalController não foi injetado em TelaEntregasAlunoController.");
        }
    }

    @FXML
    void initialize() {
        assert btnCriarSecao != null : "fx:id=\"btnCriarSecao\" was not injected: check your FXML file 'TelaEntregasAluno.fxml'.";
        assert flowCards != null : "fx:id=\"flowCards\" was not injected: check your FXML file 'TelaEntregasAluno.fxml'.";

    }
    //Variável para guardar a referência
    private PainelPrincipalController painelPrincipalController;

    //Método da Interface para injetar a referência
    @Override
    public void setPainelPrincipalController(PainelPrincipalController controller) {
        this.painelPrincipalController = controller;
    }
}
