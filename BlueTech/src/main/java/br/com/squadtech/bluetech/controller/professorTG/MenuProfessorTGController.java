package br.com.squadtech.bluetech.controller.professorTG;

import br.com.squadtech.bluetech.controller.MenuAware;
import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import br.com.squadtech.bluetech.dao.ProfessorTGDAO;
import br.com.squadtech.bluetech.model.ProfessorTG;
import br.com.squadtech.bluetech.model.SessaoUsuario;
import br.com.squadtech.bluetech.model.Usuario;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.geometry.Rectangle2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Objects;

public class MenuProfessorTGController implements MenuAware, SupportsMainController {

    private static final Logger log = LoggerFactory.getLogger(MenuProfessorTGController.class);
    private static final String DEFAULT_AVATAR = "/assets/Usuario.png";

    @FXML
    private Label lblTituloProfessorTG;

    @FXML
    private ImageView imgViewFotoProfTG;

    @FXML
    private Label lblProfessorTG;

    @FXML
    private Label lblSemestreTG;

    @FXML
    private VBox vboxMenuProfessorTG;

    @FXML
    private AnchorPane paneSuperiorMenuProfessorTG;

    @FXML
    private SplitPane splitPanelMenuProfessorTG;

    @FXML
    private Accordion accordionProfessorTG; // declarado, mesmo que não usado

    @FXML
    private JFXButton btnPortfolios;

    @FXML
    private JFXButton btnCadastrarOrientadores;

    @FXML
    private JFXButton btnAgendamentosTG;

    @FXML
    private JFXButton btnPerfilProfTG;

    @FXML
    private JFXButton btnOrientacao;

    // 🔥 NOVO BOTÃO 🔥
    @FXML
    private JFXButton btnImportarCSV;

    private StackPane profileFrame;

    private final ProfessorTGDAO professorTGDAO = new ProfessorTGDAO();

    private PainelPrincipalController painelPrincipalController;

    @Override
    public void setPainelPrincipalController(PainelPrincipalController painelPrincipalController) {
        this.painelPrincipalController = painelPrincipalController;
    }

    @Override
    public void onContentChanged(String fxmlPath, Object contentController) {
        if (btnPortfolios != null) {
            boolean active = fxmlPath.contains("VisualizarPortifolioTG.fxml");
            btnPortfolios.getStyleClass().remove("active");
            if (active) btnPortfolios.getStyleClass().add("active");
        }
    }

    @FXML
    private void abrirPortfolio(ActionEvent event) {
        if (painelPrincipalController == null) {
            log.error("PainelPrincipalController não foi injetado em MenuProfessorTGController.");
            return;
        }

        try {
            var controller = painelPrincipalController.loadContentReturnController(
                    "/fxml/professorTG/VisualizarPortifolioTG.fxml",
                    VisualizarPortifolioTGController.class
            );

            if (controller != null) {
                controller.criarCards(null, null);
            }

            log.info("Tela VisualizarPortifolioTG carregada com sucesso.");
        } catch (Exception e) {
            log.error("Erro ao carregar VisualizarPortifolioTG.fxml", e);
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirCadastrarOrientadores(ActionEvent event) {
        if (painelPrincipalController != null) {
            try {
                painelPrincipalController.loadContent("/fxml/professorTG/cadastroProfessores.fxml");
            } catch (Exception e) {
                log.error("Erro ao carregar cadastroProfessores.fxml", e);
            }
        }
    }

    @FXML
    private void abrirImportacaoCSV(ActionEvent event) {
        if (painelPrincipalController != null) {
            try {
                painelPrincipalController.loadContent("/fxml/professorTG/importacao_csv.fxml");
                log.info("Tela de importação CSV carregada com sucesso.");
            } catch (Exception e) {
                log.error("Erro ao carregar importacao_csv.fxml", e);
            }
        }
    }

    @FXML
    private void abrirAgendamentos(ActionEvent event) {
        if (painelPrincipalController != null) {
            try {
                painelPrincipalController.loadContent("/fxml/professorTG/AgendamentoDefesaProfTG.fxml");
            } catch (Exception e) {
                log.error("Erro ao carregar AgendamentoDefesaProfTG.fxml", e);
            }
        }
    }

    @FXML
    private void abrirPerfilProfTG(ActionEvent event) {
        if (painelPrincipalController == null) {
            log.error("PainelPrincipalController não foi injetado em MenuProfessorTGController.");
            return;
        }
        try {
            painelPrincipalController.loadContent("/fxml/professorTG/TelaPerfilProfTG.fxml");
        } catch (Exception e) {
            log.error("Erro ao carregar TelaPerfilProfTG.fxml", e);
        }
    }

    @FXML
    private void abrirOrientacao(ActionEvent event) {
        if (painelPrincipalController != null) {
            try {
                painelPrincipalController.loadMenu("/fxml/professorOrientador/MenuProfessorOrientador.fxml");
                painelPrincipalController.loadContent("/fxml/professorOrientador/TelaOrientador.fxml");
                log.info("Redirecionado para TelaOrientador.fxml a partir do MenuProfessorTG.");
            } catch (Exception e) {
                log.error("Erro ao carregar TelaOrientador.fxml", e);
            }
        }
    }

    private void atualizarNomeProfessor() {
        if (lblProfessorTG == null) {
            return;
        }

        Usuario usuario = SessaoUsuario.getUsuarioLogado();
        if (usuario == null || usuario.getEmail() == null) {
            lblProfessorTG.setText("PROFESSOR TG: (não identificado)");
            return;
        }

        String nomePreferencial = (usuario.getNome() != null && !usuario.getNome().isBlank())
                ? usuario.getNome()
                : usuario.getEmail();

        if ((nomePreferencial == null || nomePreferencial.equals(usuario.getEmail()))
                && "PROF_TG".equalsIgnoreCase(usuario.getTipo())) {
            ProfessorTGDAO dao = new ProfessorTGDAO();
            var registro = dao.findByUsuarioEmail(usuario.getEmail());
            if (registro != null && registro.getNome() != null && !registro.getNome().isBlank()) {
                nomePreferencial = registro.getNome();
            }
        }

        lblProfessorTG.setText("PROFESSOR TG: " + nomePreferencial);
    }

    private void carregarFotoPerfil() {
        Image image = resolveProfileImage();
        if (image == null) {
            setDefaultPhoto();
            return;
        }
        imgViewFotoProfTG.setImage(image);
        Platform.runLater(this::applyImageProcessing);
    }

    private Image resolveProfileImage() {
        Usuario usuario = SessaoUsuario.getUsuarioLogado();
        if (usuario == null) {
            return null;
        }
        ProfessorTG professor = professorTGDAO.findByUsuarioEmail(usuario.getEmail());
        if (professor == null || professor.getFoto() == null || professor.getFoto().isBlank()) {
            var resource = getClass().getResource(DEFAULT_AVATAR);
            return resource != null ? new Image(resource.toExternalForm(), false) : null;
        }
        File foto = new File(professor.getFoto());
        if (!foto.exists()) {
            var resource = getClass().getResource(DEFAULT_AVATAR);
            return resource != null ? new Image(resource.toExternalForm(), false) : null;
        }
        return new Image(foto.toURI().toString(), false);
    }

    private void setDefaultPhoto() {
        var resource = getClass().getResource(DEFAULT_AVATAR);
        if (resource == null) {
            imgViewFotoProfTG.setImage(null);
            return;
        }
        imgViewFotoProfTG.setImage(new Image(resource.toExternalForm(), false));
        Platform.runLater(this::applyImageProcessing);
    }

    private void applyImageProcessing() {
        if (imgViewFotoProfTG == null) return;
        Image image = imgViewFotoProfTG.getImage();
        if (image == null || image.getWidth() == 0 || image.getHeight() == 0) {
            return;
        }
        double fitSize = 112.0;
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        double side = Math.min(imageWidth, imageHeight);
        double x = (imageWidth - side) / 2.0;
        double y = (imageHeight - side) / 2.0;
        imgViewFotoProfTG.setViewport(new Rectangle2D(x, y, side, side));
        imgViewFotoProfTG.setPreserveRatio(false);
        imgViewFotoProfTG.setSmooth(true);
        imgViewFotoProfTG.setFitWidth(fitSize);
        imgViewFotoProfTG.setFitHeight(fitSize);
        double radius = fitSize / 2.0;
        Circle clip = new Circle(radius, radius, radius);
        imgViewFotoProfTG.setClip(clip);
    }

    public void updateFotoProfessorTG(String imagePath) {
        Image image;
        if (imagePath == null || imagePath.isBlank() || !new File(imagePath).exists()) {
            image = resolveProfileImage();
        } else {
            image = new Image(new File(imagePath).toURI().toString(), false);
        }
        imgViewFotoProfTG.setImage(image);
        Platform.runLater(this::applyImageProcessing);
    }

    @FXML
    void initialize() {
        assert btnPortfolios != null : "fx:id=\"btnPortfolios\" não foi injetado: verifique seu FXML 'MenuProfessorTG.fxml'.";
        assert btnCadastrarOrientadores != null : "fx:id=\"btnCadastrarOrientadores\" não foi injetado.";
        assert btnAgendamentosTG != null : "fx:id=\"btnAgendamentosTG\" não foi injetado.";
        assert btnPerfilProfTG != null : "fx:id=\"btnPerfilProfTG\" não foi injetado.";
        assert btnOrientacao != null : "fx:id=\"btnOrientacao\" não foi injetado.";
        assert btnImportarCSV != null : "fx:id=\"btnImportarCSV\" não foi injetado: verifique seu FXML.";
        assert imgViewFotoProfTG != null : "fx:id=\"imgViewFotoProfTG\" não foi injetado.";
        assert lblProfessorTG != null : "fx:id=\"lblProfessorTG\" não foi injetado.";
        assert lblSemestreTG != null : "fx:id=\"lblSemestreTG\" não foi injetado.";
        assert lblTituloProfessorTG != null : "fx:id=\"lblTituloProfessorTG\" não foi injetado.";
        assert vboxMenuProfessorTG != null : "fx:id=\"vboxMenuProfessorTG\" não foi injetado.";
        assert splitPanelMenuProfessorTG != null : "fx:id=\"splitPanelMenuProfessorTG\" não foi injetado.";

        profileFrame = (StackPane) paneSuperiorMenuProfessorTG.lookup("#profileFrame");
        if (profileFrame != null) {
            profileFrame.setPrefSize(120, 120);
            profileFrame.setMinSize(120, 120);
            profileFrame.setMaxSize(120, 120);
        }

        log.info("MenuProfessorTGController inicializado com sucesso.");

        atualizarNomeProfessor();
        carregarFotoPerfil();
        Platform.runLater(this::applyImageProcessing);
    }
}
