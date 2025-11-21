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

    private static final String ERROR_CLASS = "input-error";

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
            markPasswordInvalid();
            showAlert("Senhas não coincidem!");
            return;
        }
        if (email.isEmpty() || nome.isEmpty() || senha.isEmpty()) {
            showAlert("Preencha todos os campos obrigatórios!");
            return;
        }
        if (!isSenhaForte(senha)) {
            markPasswordInvalid();
            showAlert("A senha deve ter no mínimo 8 caracteres e incluir pelo menos uma letra maiúscula, um número e um caractere especial.");
            return;
        }

        clearPasswordError();

        try {
            Usuario aluno = new Usuario(email, nome, senha, "ALUNO");
            usuarioDAO.insert(aluno);
            showAlert("Aluno cadastrado com sucesso!");

            //Limpa os campos após cadastrar o usuário com sucesso
            txtFldSignAlunoEmail.setText("");
            txtFldSignAlunoNome.setText("");
            passFldSignAluno.setText("");
            passFldSignAlunoConf.setText("");
            clearPasswordError();

        } catch (Exception e) {
            showAlert("Erro ao cadastrar: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.show();
    }

    private boolean isSenhaForte(String senha) {
        if (senha == null || senha.length() < 8) {
            return false;
        }
        boolean hasUpper = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        for (char c : senha.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecial = true;
            }
            if (hasUpper && hasDigit && hasSpecial) {
                return true;
            }
        }
        return false;
    }

    private void markPasswordInvalid() {
        if (!passFldSignAluno.getStyleClass().contains(ERROR_CLASS)) {
            passFldSignAluno.getStyleClass().add(ERROR_CLASS);
        }
        if (!passFldSignAlunoConf.getStyleClass().contains(ERROR_CLASS)) {
            passFldSignAlunoConf.getStyleClass().add(ERROR_CLASS);
        }
    }

    private void clearPasswordError() {
        passFldSignAluno.getStyleClass().remove(ERROR_CLASS);
        passFldSignAlunoConf.getStyleClass().remove(ERROR_CLASS);
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

        passFldSignAluno.textProperty().addListener((obs, oldV, newV) -> {
            if (isSenhaForte(newV)) {
                clearPasswordError();
            }
        });
        passFldSignAlunoConf.textProperty().addListener((obs, oldV, newV) -> {
            if (newV.equals(passFldSignAluno.getText()) && isSenhaForte(newV)) {
                clearPasswordError();
            }
        });
    }

}
