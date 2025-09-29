package br.com.squadtech.bluetech.controller.login;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;

public class telaLoginController {

    @FXML
    private Button btn_salvar;

    @FXML
    private Label lbl_acao1;

    @FXML
    private Label lbl_acao2;

    @FXML
    private Label lbl_nome;

    @FXML
    private Label lbl_nome1;

    @FXML
    private TextField txf_email;

    @FXML
    private TextField txf_senha;

    @FXML
    void clique_aqui(ActionEvent event) {
        try {
            // Carrega o FXML da próxima tela
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/aluno/Tela_ProjetoBT.fxml"));
            Scene novaCena = new Scene(fxmlLoader.load());

            // Pega a janela atual
            Stage palcoAtual = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Define a nova cena
            palcoAtual.setScene(novaCena);
            palcoAtual.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
