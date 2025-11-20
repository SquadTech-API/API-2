package br.com.squadtech.bluetech.controller.professorOrientador;

import br.com.squadtech.bluetech.dao.PerfilAlunoDAO;
import br.com.squadtech.bluetech.dao.ProfessorDAO;
import br.com.squadtech.bluetech.model.PerfilAluno;
import br.com.squadtech.bluetech.model.Professor;
import br.com.squadtech.bluetech.model.SessaoUsuario;
import br.com.squadtech.bluetech.model.Usuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.File;
import java.net.URL;

public class TelaPerfilProfessorController{

    @FXML
    private AnchorPane AncorPanePerfilProfessor;

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnSalvar;

    @FXML
    private Button btnSelecionarFoto;

    @FXML
    private ImageView imgFoto;

    @FXML
    private TextField txtCargo;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtNome;

    @FXML
    private TextField txtNovaSenha;

    @FXML
    private TextField txtSenhaAtual;

    private String caminhoFoto; //Para guardar o caminho da foto selecionada para salvar

    @FXML
    void handleCancelar(ActionEvent event) {
        // Recarrega os dados originais do usuário, descartando alterações feitas no formulário
        //carregarPerfilUsuario();
       // showAlert("Informação", "Alterações descartadas. Os dados originais foram recarregados.");

    }

    @FXML
    void handleSalvar(ActionEvent event) {

    }

    @FXML
    void handleSelecionarFoto(ActionEvent event) {

    }

    @FXML
     void initialize() {

        assert btnCancelar != null : "fx:id=\"btnCancelar\" was not injected: check your FXML file 'TelaPerfilProfessorOrientador.fxml'.";
        assert btnSalvar != null : "fx:id=\"btnSalvar\" was not injected: check your FXML file 'TelaPerfilProfessorOrientador.fxml'.";
        assert btnSelecionarFoto != null : "fx:id=\"btnSelecionarFoto\" was not injected: check your FXML file 'TelaPerfilProfessorOrientador.fxml'.";
        assert imgFoto != null : "fx:id=\"imgFoto\" was not injected: check your FXML file 'TelaPerfilProfessorOrientador.fxml'.";
        assert txtEmail != null : "fx:id=\"txtEmail\" was not injected: check your FXML file 'TelaPerfilProfessorOrientador.fxml'.";
        assert txtNome != null : "fx:id=\"txtNome\" was not injected: check your FXML file 'TelaPerfilProfessorOrientador.fxml'.";
        assert txtCargo != null : "fx id=\"txtCargo\" was not injected: check your FXML file 'TelaPerfilProfessorOrientador.fxml'.";

        //Carrega os dados do usuário logado e o perfil salvo (se houver)
        carregarPerfilUsuario();
    }

    private void setDefaultImage() {
        URL resource = getClass().getResource("/images/Usuario.png");
        if (resource != null) {
            imgFoto.setImage(new Image(resource.toExternalForm()));
        } else {
            imgFoto.setImage(null);
        }
    }

    private void carregarPerfilUsuario() {
        Usuario usuario = SessaoUsuario.getUsuarioLogado();
        if (usuario != null) {
            txtEmail.setText(usuario.getEmail());
            txtNome.setText(usuario.getNome());

            ProfessorDAO dao = new ProfessorDAO();
            Professor perfil = dao.findByUsuarioEmail(usuario.getEmail());

            String fotoPath = perfil.getFoto();
            if (fotoPath != null && !fotoPath.isEmpty() && new File(fotoPath).exists()) {
                imgFoto.setImage(new Image("file:" + fotoPath));
                caminhoFoto = fotoPath;
            } else {
                setDefaultImage();
                caminhoFoto = null;
            }

            txtCargo.setText(perfil.getCargo() != null ? perfil.getCargo() : "");

        }

    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }





}
