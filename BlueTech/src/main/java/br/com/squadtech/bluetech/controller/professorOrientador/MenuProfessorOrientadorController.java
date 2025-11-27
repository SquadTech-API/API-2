package br.com.squadtech.bluetech.controller.professorOrientador;

import br.com.squadtech.bluetech.config.SmtpProps;
import br.com.squadtech.bluetech.controller.MenuAware;
import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import br.com.squadtech.bluetech.dao.ProfessorDAO;
import br.com.squadtech.bluetech.model.Professor;
import br.com.squadtech.bluetech.model.SessaoUsuario;
import br.com.squadtech.bluetech.service.EmailService;
import br.com.squadtech.bluetech.util.LogoutHelper;
import com.jfoenix.controls.JFXButton;
import jakarta.mail.MessagingException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.geometry.Rectangle2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class MenuProfessorOrientadorController implements MenuAware, SupportsMainController {

    private static final Logger log = LoggerFactory.getLogger(MenuProfessorOrientadorController.class);
    private static final String DEFAULT_AVATAR = "/assets/Usuario.png";

    @FXML
    private Label lblTituloProfessorOri;

    @FXML
    private ImageView imgViewFotoProfessorOri;

    @FXML
    private Label lblProfessorOri;


    @FXML
    private AnchorPane paneSuperiorMenuProfessorOri;

    @FXML
    private SplitPane splitPanelMenuProfessorOri;

    @FXML
    private VBox vboxMenuProfessorOri;

    @FXML
    private Button btnListaAlunos;

    @FXML
    private JFXButton btnenviarEmail;

    @FXML
    private JFXButton btnSolicitacoesOrientacao;

    @FXML
    private JFXButton btnAgendamentoDefesa;

    @FXML
    private JFXButton btnEditarPerfilprofessor;

    @FXML
    private JFXButton btnProfessorTG;

    @FXML
    private JFXButton btnLogoutProfessorOri;

    @FXML
    private StackPane profileFrame;
    private final ProfessorDAO professorDAO = new ProfessorDAO();

    @FXML
    void abrirSolicitacoesOrientacao(ActionEvent event) {
        if (painelPrincipalController != null) {
            try {
                String fxmlPath = "/fxml/professorOrientador/SolicitacaoAlunosOrientacao.fxml";

                painelPrincipalController.loadContent(fxmlPath);
            } catch (IOException e) {
                log.error("Falha ao carregar SolicitacaoAlunosOrientacao.fxml", e);
            }
        } else {
            log.error("PainelPrincipalController não foi injetado em MenuProfessorOrientadorController.");
        }

    }

    @FXML
    void abrirAgendamentosDefesa() {
        if (painelPrincipalController != null) {
            try {
                painelPrincipalController.loadContent("/fxml/professorOrientador/AgendamentoDefesaOrientador.fxml");
            } catch (IOException e) {
                log.error("Falha ao carregar AgendamentoDefesaOrientador.fxml", e);
            }
        } else {
            log.error("PainelPrincipalController não foi injetado em MenuProfessorOrientadorController.");
        }
    }


    private PainelPrincipalController painelPrincipalController;

    @Override
    public void setPainelPrincipalController(PainelPrincipalController painelPrincipalController) {
        this.painelPrincipalController = painelPrincipalController;
    }

    @Override
    public void onContentChanged(String fxmlPath, Object contentController) {
        if (btnListaAlunos == null) return;
        boolean active = fxmlPath.contains("telaAlunos.fxml") || fxmlPath.contains("TelaOrientador.fxml");
        btnListaAlunos.getStyleClass().remove("active");
        if (active) btnListaAlunos.getStyleClass().add("active");
    }

    @FXML
    private void abrirListaAlunos() {
        if (painelPrincipalController == null) return;
        try {
            painelPrincipalController.loadContent("/fxml/professorOrientador/telaAlunos.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showInfo(String msg) {
        var alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setHeaderText("Notificação");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showError(String msg) {
        var alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setHeaderText("Erro");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void carregarDadosProfessor() {
        try {
            // 1. Usuário logado (onde está o NOME de verdade)
            var usuario = SessaoUsuario.getUsuarioLogado();
            if (usuario == null || usuario.getEmail() == null) {
                lblProfessorOri.setText("PROFESSOR ORIENTADOR: (não identificado)");
                // garantir invisibilidade do botão se não houver usuário
                if (btnProfessorTG != null) {
                    btnProfessorTG.setVisible(false);
                    btnProfessorTG.setManaged(false);
                }
                aplicarFoto(null);
                return;
            }

            // 2. Nome do professor vem direto do Usuario
            String nome = (usuario.getNome() != null && !usuario.getNome().isBlank())
                    ? usuario.getNome()
                    : usuario.getEmail();
            lblProfessorOri.setText("PROFESSOR ORIENTADOR: " + nome);

            // 3. Busca registro na tabela PROFESSOR para mostrar cargo / tipo TG (mantido por compatibilidade)
            ProfessorDAO profDAO = new ProfessorDAO();
            Professor professor = profDAO.findByUsuarioEmail(usuario.getEmail());
            aplicarFoto(professor != null ? professor.getFoto() : null);

            // ✅ VERIFICAÇÃO CORRETA: o tipo vem da tabela USUARIO.coluna tipo
            String tipoUsuario = null;
            try {
                tipoUsuario = usuario.getTipo(); // assume getter getTipo() em SessaoUsuario->Usuario
            } catch (Exception ex) {
                // se a sua classe Usuario usa outro nome para o campo, ajuste aqui
                log.debug("Não foi possível obter o tipo direto do objeto usuario: {}", ex.getMessage());
            }

            if (tipoUsuario == null && professor != null) {
                // fallback: se por algum motivo não estiver no usuário, tentar buscar no professor
                tipoUsuario = professor.getTipoTG();
            }

            if (btnProfessorTG != null) {
                boolean isProfTG = tipoUsuario != null && "PROF_TG".equalsIgnoreCase(tipoUsuario.trim());
                if (isProfTG) {
                    btnProfessorTG.setManaged(true);
                    btnProfessorTG.setVisible(true);
                } else {
                    btnProfessorTG.setVisible(false);
                    btnProfessorTG.setManaged(false);
                }
            }

        } catch (Exception e) {
            log.error("Erro ao carregar dados do professor orientador", e);
            lblProfessorOri.setText("PROFESSOR ORIENTADOR: (erro ao carregar)");
            if (btnProfessorTG != null) {
                btnProfessorTG.setVisible(false);
                btnProfessorTG.setManaged(false);
            }
        }
    }

    @FXML
    void abrirTelaPerfilProfessorOrientador() {
        if (painelPrincipalController != null) {
            try {
                painelPrincipalController.loadContent("/fxml/professorOrientador/TelaPerfilProfessorOrientador.fxml");
            } catch (IOException e) {
                log.error("Falha ao carregar TelaPerfilProfessorOrientador.fxml", e);
            }
        } else {
            log.error("PainelPrincipalController não foi injetado em MenuProfessorOrientadorController.");
        }
    }


    @FXML
    void abrirTelaPerfilTG(ActionEvent event) {
        if (painelPrincipalController != null) {
            try {
                painelPrincipalController.loadMenu("/fxml/professorTG/MenuProfessorTG.fxml");
                painelPrincipalController.loadContent("/fxml/professorTG/TelaProfessorTG.fxml");
            } catch (IOException e) {
                log.error("Falha ao carregar telas de Professor TG", e);
            }
        } else {
            log.error("PainelPrincipalController não foi injetado em MenuProfessorOrientadorController.");
        }
    }

    private void aplicarFoto(String caminho) {
        Image image;
        if (caminho == null || caminho.isBlank() || !new File(caminho).exists()) {
            var resource = getClass().getResource(DEFAULT_AVATAR);
            image = resource != null ? new Image(resource.toExternalForm(), false) : null;
        } else {
            image = new Image(new File(caminho).toURI().toString(), false);
        }
        imgViewFotoProfessorOri.setImage(image);
        Platform.runLater(this::processarImagemCircular);
    }

    public void updateFotoProfessorOrientador(String caminho) {
        aplicarFoto(caminho);
    }

    private void processarImagemCircular() {
        if (imgViewFotoProfessorOri == null) return;
        Image image = imgViewFotoProfessorOri.getImage();
        if (image == null || image.getWidth() == 0 || image.getHeight() == 0) return;
        double fitSize = 112.0;
        double side = Math.min(image.getWidth(), image.getHeight());
        double x = (image.getWidth() - side) / 2.0;
        double y = (image.getHeight() - side) / 2.0;
        imgViewFotoProfessorOri.setViewport(new Rectangle2D(x, y, side, side));
        imgViewFotoProfessorOri.setPreserveRatio(false);
        imgViewFotoProfessorOri.setSmooth(true);
        imgViewFotoProfessorOri.setFitWidth(fitSize);
        imgViewFotoProfessorOri.setFitHeight(fitSize);
        Circle clip = new Circle(fitSize / 2.0, fitSize / 2.0, fitSize / 2.0);
        imgViewFotoProfessorOri.setClip(clip);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        log.info("Professor orientador solicitou logout");
        LogoutHelper.performLogout(event, log);
    }

    @FXML
    void initialize() {
        // GARANTE QUE O BOTÃO COMEÇA INVISÍVEL E NÃO OCUPA ESPAÇO
        if (btnProfessorTG != null) {
            btnProfessorTG.setVisible(false);
            btnProfessorTG.setManaged(false);
        }

        assert btnSolicitacoesOrientacao != null : "fx:id=\"btnSolicitacoesOrientacao\" was not injected: check your FXML file 'MenuProfessorOrientador.fxml'.";
        assert btnenviarEmail != null : "fx:id=\"btnenviarEmail\" was not injected: check your FXML file 'MenuProfessorOrientador.fxml'.";
        assert imgViewFotoProfessorOri != null : "fx:id=\"imgViewFotoProfessorOri\" was not injected: check your FXML file 'MenuProfessorOrientador.fxml'.";
        assert lblProfessorOri != null : "fx:id=\"lblProfessorOri\" was not injected: check your FXML file 'MenuProfessorOrientador.fxml'.";
        assert lblTituloProfessorOri != null : "fx:id=\"lblTituloProfessorOri\" was not injected: check your FXML file 'MenuProfessorOrientador.fxml'.";
        assert paneSuperiorMenuProfessorOri != null : "fx:id=\"paneSuperiorMenuProfessorOri\" was not injected: check your FXML file 'MenuProfessorOrientador.fxml'.";
        assert splitPanelMenuProfessorOri != null : "fx:id=\"splitPanelMenuProfessorOri\" was not injected: check your FXML file 'MenuProfessorOrientador.fxml'.";
        assert vboxMenuProfessorOri != null : "fx:id=\"vboxMenuProfessorOri\" was not injected: check your FXML file 'MenuProfessorOrientador.fxml'.";
        assert btnProfessorTG != null : "fx:id=\"btnProfessorTG\" was not injected: check your FXML file 'MenuProfessorOrientador.fxml'.";
        assert btnLogoutProfessorOri != null : "fx:id=\"btnLogoutProfessorOri\" was not injected: check your FXML file 'MenuProfessorOrientador.fxml'.";

        profileFrame.setPrefSize(120, 120);
        profileFrame.setMinSize(120, 120);
        profileFrame.setMaxSize(120, 120);

        carregarDadosProfessor();
        Platform.runLater(this::processarImagemCircular);
    }
}
