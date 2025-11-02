package br.com.squadtech.bluetech.controller.aluno;

import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import br.com.squadtech.bluetech.dao.PerfilAlunoDAO;
import br.com.squadtech.bluetech.model.PerfilAluno;
import br.com.squadtech.bluetech.model.SessaoUsuario;
import br.com.squadtech.bluetech.model.Usuario;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;

public class TelaPerfilAlunoController implements SupportsMainController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private AnchorPane AncorPanePerfilAluno;

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnSalvar;

    @FXML
    private Button btnSelecionarFoto;

    @FXML
    private ImageView imgFoto;

    @FXML
    private TextArea txtConhecimentosTecnicos;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtGithub;

    @FXML
    private TextArea txtHistoricoAcademico;

    @FXML
    private TextArea txtHistoricoProfissional;

    @FXML
    private TextField txtIdade;

    @FXML
    private TextField txtLinkedin;

    @FXML
    private TextArea txtMotivacao;

    @FXML
    private TextField txtNome;

    private PainelPrincipalController painelPrincipalController;

    private String caminhoFoto; //Para guardar o caminho da foto selecionada para salvar

    @Override
    public void setPainelPrincipalController(PainelPrincipalController controller) {
        this.painelPrincipalController = controller;
    }

    @FXML
    void handleCancelar(ActionEvent event) {
        // Recarrega os dados originais do usuário, descartando alterações feitas no formulário
        carregarPerfilUsuario();
        showAlert("Informação", "Alterações descartadas. Os dados originais foram recarregados.");
    }

    @FXML
    void handleSalvar(ActionEvent event) {
        Usuario usuario = SessaoUsuario.getUsuarioLogado();
        if (usuario == null || usuario.getEmail() == null) {
            showAlert("Erro", "Sessão expirada. Faça login novamente.");
            return;
        }
        PerfilAluno perfil = new PerfilAluno();
        perfil.setEmailUsuario(usuario.getEmail()); // força vínculo com usuário logado
        try {
            perfil.setIdade(txtIdade.getText().isEmpty() ? null : Integer.parseInt(txtIdade.getText()));
        } catch (NumberFormatException e) {
            showAlert("Erro", "Idade deve ser um número válido.");
            return;
        }
        perfil.setFoto(caminhoFoto);
        perfil.setHistoricoAcademico(txtHistoricoAcademico.getText());
        perfil.setMotivacao(txtMotivacao.getText());
        perfil.setHistoricoProfissional(txtHistoricoProfissional.getText());
        perfil.setLinkGithub(txtGithub.getText());
        perfil.setLinkLinkedin(txtLinkedin.getText());
        perfil.setConhecimentosTecnicos(txtConhecimentosTecnicos.getText());

        PerfilAlunoDAO dao = new PerfilAlunoDAO();
        boolean success;
        if (dao.existePerfil(perfil.getEmailUsuario())) {
            success = dao.atualizarPerfil(perfil);
        } else {
            success = dao.inserirPerfil(perfil);
        }

        if (success) {
            showAlert("Sucesso", "Perfil salvo com sucesso!");
            //Atualiza a foto novamente caso necessário ou não tenha carregado
            if (painelPrincipalController != null && caminhoFoto != null) {
                painelPrincipalController.updateFotoMenuAluno(caminhoFoto);
            }
        } else {
            showAlert("Erro", "Falha ao salvar perfil.");
        }
    }

    @FXML
    void handleSelecionarFoto(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Foto de Perfil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        Window stage = btnSelecionarFoto.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                //Obtém o email do usuário logado para nomear o arquivo unicamente
                Usuario usuario = SessaoUsuario.getUsuarioLogado();
                if (usuario == null || usuario.getEmail() == null) {
                    showAlert("Erro", "Usuário não encontrado. Faça login novamente.");
                    return;
                }
                String email = usuario.getEmail().trim();

                //Cria a pasta de destino para guardar as fotos localmente
                String userHome = System.getProperty("user.home");
                String pastaPerfil = userHome + "/BlueTech/Perfil";
                File dir = new File(pastaPerfil);
                if (!dir.exists()) {
                    boolean ok = dir.mkdirs();
                    if (!ok) {
                        showAlert("Erro", "Não foi possível criar a pasta de perfis.");
                        return;
                    }
                }

                //Extrai a extensão do arquivo original
                String originalName = selectedFile.getName();
                String extension;
                int lastDot = originalName.lastIndexOf('.');
                if (lastDot > 0 && lastDot < originalName.length() - 1) {
                    extension = originalName.substring(lastDot).toLowerCase();
                } else {
                    showAlert("Erro", "Arquivo selecionado não possui extensão válida.");
                    return;
                }

                //Sanitiza o email para nome de arquivo válido (substitui caracteres inválidos)
                String safeEmail = email
                        .replace("@", "_")
                        .replace(".", "_")
                        .replace("#", "")
                        .replace("%", "")
                        .replace("&", "")
                        .replace(" ", "_")
                        .replace("\\", "_")
                        .replace("/", "_")
                        .replace(":", "")
                        .replace("?", "")
                        .replace("*", "")
                        .replace("<", "")
                        .replace(">", "")
                        .replace("|", "")
                        .replace("\"", "");

                //Nome do arquivo: email_sanitizado.ext (ex: user_example_com.jpg)
                String fileName = safeEmail + extension;
                String destino = pastaPerfil + "/" + fileName;

                //Copia o arquivo (sobrescrevendo se já existir)
                Files.copy(selectedFile.toPath(), new File(destino).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                //Atualiza a ImageView
                Image image = new Image("file:" + destino);
                imgFoto.setImage(image);

                //Registra o caminho para salvar no BD
                caminhoFoto = destino;

                //Atualiza a foto no menu do aluno, se disponível
                if (painelPrincipalController != null) {
                    painelPrincipalController.updateFotoMenuAluno(destino);
                }

                showAlert("Sucesso", "Foto selecionada e salva como: " + fileName);
            } catch (IOException e) {
                showAlert("Erro", "Falha ao copiar a foto: " + e.getMessage());
            } catch (Exception e) {
                showAlert("Erro", "Erro inesperado: " + e.getMessage());
            }
        }
    }

    @FXML
    void initialize() {
        assert AncorPanePerfilAluno != null : "fx:id=\"AncorPanePerfilAluno\" was not injected: check your FXML file 'TelaPerfilAluno.fxml'.";
        assert btnCancelar != null : "fx:id=\"btnCancelar\" was not injected: check your FXML file 'TelaPerfilAluno.fxml'.";
        assert btnSalvar != null : "fx:id=\"btnSalvar\" was not injected: check your FXML file 'TelaPerfilAluno.fxml'.";
        assert btnSelecionarFoto != null : "fx:id=\"btnSelecionarFoto\" was not injected: check your FXML file 'TelaPerfilAluno.fxml'.";
        assert imgFoto != null : "fx:id=\"imgFoto\" was not injected: check your FXML file 'TelaPerfilAluno.fxml'.";
        assert txtConhecimentosTecnicos != null : "fx:id=\"txtConhecimentosTecnicos\" was not injected: check your FXML file 'TelaPerfilAluno.fxml'.";
        assert txtEmail != null : "fx:id=\"txtEmail\" was not injected: check your FXML file 'TelaPerfilAluno.fxml'.";
        assert txtGithub != null : "fx:id=\"txtGithub\" was not injected: check your FXML file 'TelaPerfilAluno.fxml'.";
        assert txtHistoricoAcademico != null : "fx:id=\"txtHistoricoAcademico\" was not injected: check your FXML file 'TelaPerfilAluno.fxml'.";
        assert txtHistoricoProfissional != null : "fx:id=\"txtHistoricoProfissional\" was not injected: check your FXML file 'TelaPerfilAluno.fxml'.";
        assert txtIdade != null : "fx:id=\"txtIdade\" was not injected: check your FXML file 'TelaPerfilAluno.fxml'.";
        assert txtLinkedin != null : "fx:id=\"txtLinkedin\" was not injected: check your FXML file 'TelaPerfilAluno.fxml'.";
        assert txtMotivacao != null : "fx:id=\"txtMotivacao\" was not injected: check your FXML file 'TelaPerfilAluno.fxml'.";
        assert txtNome != null : "fx:id=\"txtNome\" was not injected: check your FXML file 'TelaPerfilAluno.fxml'.";

        // Campos de identificação não devem ser editáveis pelo usuário
        txtEmail.setEditable(false);
        txtNome.setEditable(false);

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

            PerfilAlunoDAO dao = new PerfilAlunoDAO();
            PerfilAluno perfil = dao.getPerfilByEmail(usuario.getEmail());
            if (perfil != null) {
                txtIdade.setText(perfil.getIdade() != null ? perfil.getIdade().toString() : "");

                String fotoPath = perfil.getFoto();
                if (fotoPath != null && !fotoPath.isEmpty() && new File(fotoPath).exists()) {
                    imgFoto.setImage(new Image("file:" + fotoPath));
                    caminhoFoto = fotoPath;
                } else {
                    setDefaultImage();
                    caminhoFoto = null;
                }

                txtHistoricoAcademico.setText(perfil.getHistoricoAcademico() != null ? perfil.getHistoricoAcademico() : "");
                txtMotivacao.setText(perfil.getMotivacao() != null ? perfil.getMotivacao() : "");
                txtHistoricoProfissional.setText(perfil.getHistoricoProfissional() != null ? perfil.getHistoricoProfissional() : "");
                txtGithub.setText(perfil.getLinkGithub() != null ? perfil.getLinkGithub() : "");
                txtLinkedin.setText(perfil.getLinkLinkedin() != null ? perfil.getLinkLinkedin() : "");
                txtConhecimentosTecnicos.setText(perfil.getConhecimentosTecnicos() != null ? perfil.getConhecimentosTecnicos() : "");
            } else {
                // Sem perfil salvo: limpa campos (mantendo nome e email) e imagem padrão
                txtIdade.clear();
                txtHistoricoAcademico.clear();
                txtMotivacao.clear();
                txtHistoricoProfissional.clear();
                txtGithub.clear();
                txtLinkedin.clear();
                txtConhecimentosTecnicos.clear();
                setDefaultImage();
                caminhoFoto = null;
            }
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

