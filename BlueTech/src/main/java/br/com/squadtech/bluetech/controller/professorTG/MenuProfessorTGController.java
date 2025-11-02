package br.com.squadtech.bluetech.controller.professorTG;

import br.com.squadtech.bluetech.controller.MenuAware;
import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class MenuProfessorTGController implements MenuAware, SupportsMainController {

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
    private Accordion accordionProfessorTG;

    @FXML
    private JFXButton btnListaAlunos;

    @FXML private JFXButton btnSubBanco5;
    @FXML private JFXButton btnSubAnalise5;
    @FXML private JFXButton btnSubBanco6;
    @FXML private JFXButton btnSubAnalise6;

    // Referência ao painel principal unificado
    private PainelPrincipalController painelPrincipalController;

    private String semestreAtivo;
    private String cursoAtivo;

    @Override
    public void setPainelPrincipalController(PainelPrincipalController painelPrincipalController) {
        this.painelPrincipalController = painelPrincipalController;
    }

    @Override
    public void onContentChanged(String fxmlPath, Object contentController) {
        // Destaca o grupo correspondente quando certas telas estão ativas
        if (accordionProfessorTG != null) {
            if (fxmlPath.contains("VisualizarPortifolioTG.fxml") || fxmlPath.contains("VisualizadorTG.fxml") || fxmlPath.contains("TelaProfessorTG.fxml")) {
                if (!accordionProfessorTG.getPanes().isEmpty()) {
                    TitledPane tp = accordionProfessorTG.getPanes().get(0);
                    tp.setExpanded(true);
                    accordionProfessorTG.setExpandedPane(tp);
                }
            }
        }
        if (btnListaAlunos != null) {
            boolean active = fxmlPath.contains("VisualizarPortifolioTG.fxml") || fxmlPath.contains("VisualizadorTG.fxml") || fxmlPath.contains("TelaProfessorTG.fxml");
            btnListaAlunos.getStyleClass().remove("active");
            if (active) btnListaAlunos.getStyleClass().add("active");
        }
        // Atualiza estado dos subitens quando telas de portfólio/visualizador estão ativas
        if (fxmlPath.contains("VisualizarPortifolioTG.fxml") || fxmlPath.contains("VisualizadorTG.fxml")) {
            aplicarActiveSubmenu();
        } else {
            limparActiveSubmenu();
        }
    }

    private void aplicarActiveSubmenu() {
        limparActiveSubmenu();
        if (semestreAtivo == null || cursoAtivo == null) return;
        boolean s5 = semestreAtivo.startsWith("5");
        boolean isBanco = cursoAtivo.toLowerCase().contains("banco");
        if (s5 && isBanco && btnSubBanco5 != null) btnSubBanco5.getStyleClass().add("active");
        if (s5 && !isBanco && btnSubAnalise5 != null) btnSubAnalise5.getStyleClass().add("active");
        if (!s5 && isBanco && btnSubBanco6 != null) btnSubBanco6.getStyleClass().add("active");
        if (!s5 && !isBanco && btnSubAnalise6 != null) btnSubAnalise6.getStyleClass().add("active");
    }

    private void limparActiveSubmenu() {
        if (btnSubBanco5 != null) btnSubBanco5.getStyleClass().remove("active");
        if (btnSubAnalise5 != null) btnSubAnalise5.getStyleClass().remove("active");
        if (btnSubBanco6 != null) btnSubBanco6.getStyleClass().remove("active");
        if (btnSubAnalise6 != null) btnSubAnalise6.getStyleClass().remove("active");
    }

    @FXML
    private void abrirAgendamentosTG() {
        System.out.println("Abrindo Agendamentos de TG...");
    }

    @FXML
    private void abrirCadastrarOrientadores() {
        System.out.println("Abrindo Cadastrar Orientadores...");
    }

    @FXML
    private void abrirListaAlunos() {
        // Poderíamos carregar uma tela específica se existir
        System.out.println("Abrindo Lista de Alunos...");
    }

    @FXML
    private void selecionarBancoDados5() {
        abrirPortifolio("5º Semestre", "Banco de Dados");
    }

    @FXML
    private void selecionarAnaliseSistemas5() {
        abrirPortifolio("5º Semestre", "Análise de Sistemas");
    }

    @FXML
    private void selecionarBancoDados6() {
        abrirPortifolio("6º Semestre", "Banco de Dados");
    }

    @FXML
    private void selecionarAnaliseSistemas6() {
        abrirPortifolio("6º Semestre", "Análise de Sistemas");
    }

    private void abrirPortifolio(String semestre, String curso) {
        this.semestreAtivo = semestre;
        this.cursoAtivo = curso;
        if (painelPrincipalController == null) return;
        try {
            VisualizarPortifolioTGController controller =
                    painelPrincipalController.loadContentReturnController(
                            "/fxml/professorTG/VisualizarPortifolioTG.fxml",
                            VisualizarPortifolioTGController.class
                    );
            if (controller != null) {
                controller.criarCards(semestre, curso);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {
        lblTituloProfessorTG.setText("Painel do Professor TG");
        lblProfessorTG.setText("PROFESSOR TG: Emanuel Mineda");
        lblSemestreTG.setText("SEMESTRE RESPONSÁVEL: 6º Semestre");
    }
}
