package br.com.squadtech.bluetech.controller.professorTG;

import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import br.com.squadtech.bluetech.dao.ProfessorTGDAO;
import br.com.squadtech.bluetech.dao.UsuarioDAO;
import br.com.squadtech.bluetech.model.ProfessorTG;
import br.com.squadtech.bluetech.model.SessaoUsuario;
import br.com.squadtech.bluetech.model.Usuario;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * Tela de perfil do Professor TG.
 * Permite atualizar dados pessoais, senha e foto do professor
 * previamente cadastrados pelo administrador na tela de cadastro.
 */
public class TelaPerfilProfTGController implements SupportsMainController {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private TextField txtNome;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtCargo;
    @FXML
    private TextField txtCurso;
    @FXML
    private TextArea txtFormacao;
    @FXML
    private TextArea txtAreas;
    @FXML
    private PasswordField txtSenhaAtual;
    @FXML
    private PasswordField txtNovaSenha;
    @FXML
    private ImageView imgFoto;
    @FXML
    private Button btnSalvar;
    @FXML
    private Button btnCancelar;
    @FXML
    private Button btnSelecionarFoto;

    private final ProfessorTGDAO professorTGDAO = new ProfessorTGDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private PainelPrincipalController painelPrincipalController;
    private String caminhoFoto;

    @Override
    public void setPainelPrincipalController(PainelPrincipalController controller) {
        this.painelPrincipalController = controller;
    }

    @FXML
    private void initialize() {
        Objects.requireNonNull(txtNome, "txtNome não injetado");
        Objects.requireNonNull(txtEmail, "txtEmail não injetado");
        Objects.requireNonNull(txtCargo, "txtCargo não injetado");
        Objects.requireNonNull(txtCurso, "txtCurso não injetado");
        Objects.requireNonNull(txtFormacao, "txtFormacao não injetado");
        Objects.requireNonNull(txtAreas, "txtAreas não injetado");
        Objects.requireNonNull(txtSenhaAtual, "txtSenhaAtual não injetado");
        Objects.requireNonNull(txtNovaSenha, "txtNovaSenha não injetado");
        Objects.requireNonNull(imgFoto, "imgFoto não injetado");
        Objects.requireNonNull(btnSalvar, "btnSalvar não injetado");
        Objects.requireNonNull(btnCancelar, "btnCancelar não injetado");
        Objects.requireNonNull(btnSelecionarFoto, "btnSelecionarFoto não injetado");

        txtEmail.setEditable(false);
        carregarPerfil();
    }

    private void carregarPerfil() {
        Usuario usuario = SessaoUsuario.getUsuarioLogado();
        if (usuario == null) {
            showAlert(Alert.AlertType.ERROR, "Sessão expirada", "Faça login novamente.");
            return;
        }
        txtEmail.setText(usuario.getEmail());
        txtNome.setText(usuario.getNome());

        ProfessorTG professor = professorTGDAO.findByUsuarioEmail(usuario.getEmail());
        if (professor != null) {
            txtCargo.setText(professor.getCargo());
            txtCurso.setText(professor.getCursoVinculado());
            txtFormacao.setText(professor.getFormacaoAcademica());
            txtAreas.setText(professor.getAreasEspecializacao());
            caminhoFoto = professor.getFoto();
            carregarImagem(caminhoFoto);
        } else {
            carregarImagem(null);
        }
    }

    private void carregarImagem(String caminho) {
        try {
            if (caminho != null && !caminho.isBlank() && new File(caminho).exists()) {
                imgFoto.setImage(new Image(new File(caminho).toURI().toString()));
            } else {
                URL resource = getClass().getResource("/assets/Usuario.png");
                if (resource != null) {
                    imgFoto.setImage(new Image(resource.toExternalForm()));
                } else {
                    imgFoto.setImage(null);
                }
            }
        } catch (Exception e) {
            imgFoto.setImage(null);
        }
    }

    @FXML
    private void handleSelecionarFoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selecionar foto de perfil");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
        File selected = chooser.showOpenDialog(btnSelecionarFoto.getScene().getWindow());
        if (selected == null) return;

        Usuario usuario = SessaoUsuario.getUsuarioLogado();
        if (usuario == null) {
            showAlert(Alert.AlertType.ERROR, "Sessão expirada", "Faça login novamente");
            return;
        }

        try {
            String pastaBase = System.getProperty("user.home") + "/BlueTech/PerfilProfessorTG";
            File pasta = new File(pastaBase);
            if (!pasta.exists()) pasta.mkdirs();

            String extensao = selected.getName().contains(".")
                    ? selected.getName().substring(selected.getName().lastIndexOf('.')).toLowerCase()
                    : ".png";
            String safeEmail = usuario.getEmail().replaceAll("[^a-zA-Z0-9]", "_");
            String destino = pastaBase + "/" + safeEmail + extensao;

            Files.copy(selected.toPath(), new File(destino).toPath(), StandardCopyOption.REPLACE_EXISTING);
            caminhoFoto = destino;
            carregarImagem(destino);

            if (painelPrincipalController != null) {
                painelPrincipalController.updateFotoMenuAluno(destino);
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Falha ao copiar a foto: " + e.getMessage());
        }
    }

    @FXML
    private void handleSalvar() {
        Usuario usuario = SessaoUsuario.getUsuarioLogado();
        if (usuario == null) {
            showAlert(Alert.AlertType.ERROR, "Sessão expirada", "Faça login novamente");
            return;
        }

        if (txtNome.getText() == null || txtNome.getText().isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Validação", "Informe o nome");
            return;
        }
        if (txtCargo.getText() == null || txtCargo.getText().isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Validação", "Informe o cargo");
            return;
        }

        ProfessorTG professor = professorTGDAO.findByUsuarioEmail(usuario.getEmail());
        if (professor == null) {
            showAlert(Alert.AlertType.ERROR, "Registro não encontrado", "Solicite ao administrador que cadastre seu perfil.");
            return;
        }

        professor.setNome(txtNome.getText().trim());
        professor.setCargo(txtCargo.getText().trim());
        professor.setCursoVinculado(txtCurso.getText());
        professor.setFormacaoAcademica(txtFormacao.getText());
        professor.setAreasEspecializacao(txtAreas.getText());
        professor.setFoto(caminhoFoto);

        boolean dadosAtualizados = professorTGDAO.atualizarPerfil(professor);
        if (!dadosAtualizados) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível salvar os dados");
            return;
        }

        usuarioDAO.atualizarNomeUsuario(usuario.getEmail(), professor.getNome());
        usuario.setNome(professor.getNome());

        atualizarSenhaSeNecessario(usuario);
        showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Perfil salvo com sucesso!");
        carregarPerfil();
    }

    private void atualizarSenhaSeNecessario(Usuario usuario) {
        String senhaAtual = txtSenhaAtual.getText();
        String novaSenha = txtNovaSenha.getText();
        if ((senhaAtual == null || senhaAtual.isBlank()) && (novaSenha == null || novaSenha.isBlank())) {
            return; // não quer alterar senha
        }
        if (senhaAtual == null || senhaAtual.isBlank() || novaSenha == null || novaSenha.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Validação", "Informe senha atual e nova senha para alterar a senha.");
            return;
        }
        if (!BCrypt.checkpw(senhaAtual, usuario.getSenha())) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Senha atual incorreta.");
            return;
        }
        if (senhaAtual.equals(novaSenha)) {
            showAlert(Alert.AlertType.WARNING, "Validação", "A nova senha deve ser diferente da atual.");
            return;
        }
        String novoHash = BCrypt.hashpw(novaSenha, BCrypt.gensalt());
        if (usuarioDAO.updateSenhaProfessor(usuario.getEmail(), novoHash)) {
            usuario.setSenha(novoHash);
            txtSenhaAtual.clear();
            txtNovaSenha.clear();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível alterar a senha.");
        }
    }

    @FXML
    private void handleCancelar() {
        carregarPerfil();
        txtSenhaAtual.clear();
        txtNovaSenha.clear();
        showAlert(Alert.AlertType.INFORMATION, "Informação", "Alterações descartadas.");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

