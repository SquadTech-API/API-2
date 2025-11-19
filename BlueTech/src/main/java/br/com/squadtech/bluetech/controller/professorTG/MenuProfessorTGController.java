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
    private Accordion accordionProfessorTG; // ainda declarado, mesmo que não usado

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

    // Referência ao painel principal unificado
    private PainelPrincipalController painelPrincipalController;

    @Override
    public void setPainelPrincipalController(PainelPrincipalController painelPrincipalController) {
        this.painelPrincipalController = painelPrincipalController;
    }

    @Override
    public void onContentChanged(String fxmlPath, Object contentController) {
        // Mantém compatibilidade com o comportamento anterior (ativar estilo quando a tela está aberta)
        if (btnPortfolios != null) {
            boolean active = fxmlPath.contains("VisualizarPortifolioTG.fxml");
            btnPortfolios.getStyleClass().remove("active");
            if (active) btnPortfolios.getStyleClass().add("active");
        }
    }

    // 🔹 Novo método: substitui o comportamento antigo do accordion
    @FXML
    private void abrirPortfolio(ActionEvent event) {
        if (painelPrincipalController == null) {
            log.error("PainelPrincipalController não foi injetado em MenuProfessorTGController.");
            return;
        }

        try {
            // carrega a tela do portfólio
            VisualizarPortifolioTGController controller =
                    painelPrincipalController.loadContentReturnController(
                            "/fxml/professorTG/VisualizarPortifolioTG.fxml",
                            VisualizarPortifolioTGController.class
                    );

            if (controller != null) {
                // no código original, esses parâmetros vinham do accordion
                // como não há mais semestres e cursos, chamamos criarCards() sem argumentos
                controller.criarCards(null, null);
            }

            log.info("Tela VisualizarPortifolioTG carregada com sucesso.");

        } catch (Exception e) {
            log.error("Erro ao carregar VisualizarPortifolioTG.fxml", e);
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirCadastrarOrientadores() {
        System.out.println("Abrindo tela de Cadastrar Orientadores...");
        // você pode adicionar aqui futuramente a navegação dessa tela
    }

    @FXML
    private void abrirAgendamentos(ActionEvent event) {
        System.out.println("Abrindo Agendamentos de TG...");
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
        if (painelPrincipalController == null) {
            log.error("PainelPrincipalController não foi injetado em MenuProfessorTGController.");
            return;
        }

        try {
            painelPrincipalController.loadContent("/fxml/professorTG/SolicitacaoAlunosOrientacao.fxml");
            log.info("Tela SolicitacaoAlunosOrientacao carregada com sucesso.");
        } catch (Exception e) {
            log.error("Erro ao carregar SolicitacaoAlunosOrientacao.fxml", e);
        }
    }

    @FXML
    void initialize() {
        assert btnPortfolios != null : "fx:id=\"btnPortfolios\" não foi injetado: verifique seu FXML 'MenuProfessorTG.fxml'.";
        assert btnCadastrarOrientadores != null : "fx:id=\"btnCadastrarOrientadores\" não foi injetado: verifique seu FXML.";
        assert btnAgendamentosTG != null : "fx:id=\"btnAgendamentosTG\" não foi injetado: verifique seu FXML.";
        assert btnProgressso != null : "fx:id=\"btnProgressso\" não foi injetado: verifique seu FXML.";
        assert btnOrientacao != null : "fx:id=\"btnOrientacao\" não foi injetado: verifique seu FXML.";
        assert imgViewFotoProfessorTG != null : "fx:id=\"imgViewFotoProfessorTG\" não foi injetado: verifique seu FXML.";
        assert lblProfessorTG != null : "fx:id=\"lblProfessorTG\" não foi injetado: verifique seu FXML.";
        assert lblSemestreTG != null : "fx:id=\"lblSemestreTG\" não foi injetado: verifique seu FXML.";
        assert lblTituloProfessorTG != null : "fx:id=\"lblTituloProfessorTG\" não foi injetado: verifique seu FXML.";
        assert vboxMenuProfessorTG != null : "fx:id=\"vboxMenuProfessorTG\" não foi injetado: verifique seu FXML.";
        assert splitPanelMenuProfessorTG != null : "fx:id=\"splitPanelMenuProfessorTG\" não foi injetado: verifique seu FXML.";

        log.info("MenuProfessorTGController inicializado com sucesso.");
    }
}
