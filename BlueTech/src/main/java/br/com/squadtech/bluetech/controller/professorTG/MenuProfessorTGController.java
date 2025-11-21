package br.com.squadtech.bluetech.controller.professorTG;

import br.com.squadtech.bluetech.controller.MenuAware;
import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MenuProfessorTGController implements MenuAware, SupportsMainController {

    private static final Logger log = LoggerFactory.getLogger(MenuProfessorTGController.class);

    @FXML
    private Label lblTituloProfessorTG;

    @FXML
    private ImageView imgViewFotoProfessorTG;

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
    private JFXButton btnProgressso;

    @FXML
    private JFXButton btnOrientacao;

    // 🔥 NOVO BOTÃO 🔥
    @FXML
    private JFXButton btnImportarCSV;

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
    private void abrirProgressoAluno(ActionEvent event) {
        System.out.println("Abrindo tela de alunos em progresso...");
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

    @FXML
    void initialize() {
        assert btnPortfolios != null : "fx:id=\"btnPortfolios\" não foi injetado: verifique seu FXML 'MenuProfessorTG.fxml'.";
        assert btnCadastrarOrientadores != null : "fx:id=\"btnCadastrarOrientadores\" não foi injetado.";
        assert btnAgendamentosTG != null : "fx:id=\"btnAgendamentosTG\" não foi injetado.";
        assert btnProgressso != null : "fx:id=\"btnProgressso\" não foi injetado.";
        assert btnOrientacao != null : "fx:id=\"btnOrientacao\" não foi injetado.";
        assert btnImportarCSV != null : "fx:id=\"btnImportarCSV\" não foi injetado: verifique seu FXML.";
        assert imgViewFotoProfessorTG != null : "fx:id=\"imgViewFotoProfessorTG\" não foi injetado.";
        assert lblProfessorTG != null : "fx:id=\"lblProfessorTG\" não foi injetado.";
        assert lblSemestreTG != null : "fx:id=\"lblSemestreTG\" não foi injetado.";
        assert lblTituloProfessorTG != null : "fx:id=\"lblTituloProfessorTG\" não foi injetado.";
        assert vboxMenuProfessorTG != null : "fx:id=\"vboxMenuProfessorTG\" não foi injetado.";
        assert splitPanelMenuProfessorTG != null : "fx:id=\"splitPanelMenuProfessorTG\" não foi injetado.";

        log.info("MenuProfessorTGController inicializado com sucesso.");
    }
}
