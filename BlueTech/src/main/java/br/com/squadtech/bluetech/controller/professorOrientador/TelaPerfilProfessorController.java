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
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

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
        carregarPerfilUsuario();
       showAlert("Informação", "Alterações descartadas. Os dados originais foram recarregados.");
       txtSenhaAtual.clear();
       txtNovaSenha.clear();

    }

    @FXML
    void handleSalvar(ActionEvent event) {


    }

    @FXML
    void handleSelecionarFoto(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Foto de Perfil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(btnSelecionarFoto.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Obtém usuário logado
                Usuario usuario = SessaoUsuario.getUsuarioLogado();
                if (usuario == null || usuario.getEmail() == null) {
                    showAlert("Erro", "Usuário não encontrado. Faça login novamente.");
                    return;
                }

                String email = usuario.getEmail().trim();

                // pasta: C:/Users/<usuario>/BlueTech/PerfilProfessor
                String userHome = System.getProperty("user.home");
                String pastaPerfil = userHome + "/BlueTech/PerfilProfessor";
                File dir = new File(pastaPerfil);

                if (!dir.exists() && !dir.mkdirs()) {
                    showAlert("Erro", "Não foi possível criar a pasta de perfis.");
                    return;
                }

                // Descobre extensão do arquivo
                String originalName = selectedFile.getName();
                int lastDot = originalName.lastIndexOf('.');
                if (lastDot < 0) {
                    showAlert("Erro", "Arquivo selecionado não possui extensão válida.");
                    return;
                }

                String extension = originalName.substring(lastDot).toLowerCase();

                // Sanitiza email para nome do arquivo
                String safeEmail = email
                        .replace("@", "_")
                        .replace(".", "_")
                        .replaceAll("[^a-zA-Z0-9_]", "");

                String fileName = safeEmail + extension;
                String destino = pastaPerfil + "/" + fileName;

                // Copia o arquivo (sobrescreve se já existir)
                Files.copy(selectedFile.toPath(), new File(destino).toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                // Exibe na ImageView
                imgFoto.setImage(new Image("file:" + destino));

                // Guarda o caminho para salvar depois no banco
                caminhoFoto = destino;

                showAlert("Sucesso", "Foto selecionada com sucesso!");

            } catch (IOException e) {
                showAlert("Erro", "Falha ao copiar a foto: " + e.getMessage());
            } catch (Exception e) {
                showAlert("Erro", "Erro inesperado: " + e.getMessage());
            }
        }
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

        txtEmail.setEditable(false);

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

            if (perfil == null) {
                System.err.println("Professor não encontrado para o usuário: " + usuario.getEmail());
                setDefaultImage();
                txtCargo.setText("");
                caminhoFoto = null;
                return;
            }

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
