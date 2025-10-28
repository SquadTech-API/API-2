package br.com.squadtech.bluetech.controller.login;

import br.com.squadtech.bluetech.dao.UsuarioDAO;
import br.com.squadtech.bluetech.model.Usuario;
import com.jfoenix.controls.JFXButton;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class TelaCadastroController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private JFXButton btnSignAlunoConf;

    @FXML
    private AnchorPane paneSignCadastro;

    @FXML
    private PasswordField passFldSignAluno;

    @FXML
    private PasswordField passFldSignAlunoConf;

    @FXML
    private TextField txtFldSignAlunoEmail;

    @FXML
    private TextField txtFldSignAlunoNome;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private void cadastrarAluno() {
        String email = txtFldSignAlunoEmail.getText();
        String nome = txtFldSignAlunoNome.getText();
        String senha = passFldSignAluno.getText();
        String senhaConf = passFldSignAlunoConf.getText();

        if (!senha.equals(senhaConf)) {
            showAlert("Senhas não coincidem!");
            return;
        }
        if (email.isEmpty() || nome.isEmpty() || senha.isEmpty()) {
            showAlert("Preencha todos os campos obrigatórios!");
            return;
        }

        try {
            // Todo usuário cadastrado aqui será do tipo ALUNO
            Usuario aluno = new Usuario(email, nome, senha, "ALUNO");
            usuarioDAO.insert(aluno); // Insere usando hash interno do DAO
            showAlert("Aluno cadastrado com sucesso!");

            // Limpa os campos após cadastro
            txtFldSignAlunoEmail.setText("");
            txtFldSignAlunoNome.setText("");
            passFldSignAluno.setText("");
            passFldSignAlunoConf.setText("");

        } catch (Exception e) {
            showAlert("Erro ao cadastrar: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.show();
    }

    @FXML
    void initialize() {
        assert btnSignAlunoConf != null : "fx:id=\"btnSignAlunoConf\" was not injected: check your FXML file 'TelaCadastro.fxml'.";
        assert paneSignCadastro != null : "fx:id=\"paneSignCadastro\" was not injected: check your FXML file 'TelaCadastro.fxml'.";
        assert passFldSignAluno != null : "fx:id=\"passFldSignAluno\" was not injected: check your FXML file 'TelaCadastro.fxml'.";
        assert passFldSignAlunoConf != null : "fx:id=\"passFldSignAlunoConf\" was not injected: check your FXML file 'TelaCadastro.fxml'.";
        assert txtFldSignAlunoEmail != null : "fx:id=\"txtFldSignAlunoEmail\" was not injected: check your FXML file 'TelaCadastro.fxml'.";
        assert txtFldSignAlunoNome != null : "fx:id=\"txtFldSignAlunoNome\" was not injected: check your FXML file 'TelaCadastro.fxml'.";

        btnSignAlunoConf.setOnAction(event -> cadastrarAluno());
    }

}
