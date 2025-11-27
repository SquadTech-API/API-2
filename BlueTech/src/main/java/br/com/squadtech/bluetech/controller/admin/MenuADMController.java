package br.com.squadtech.bluetech.controller.admin;

import br.com.squadtech.bluetech.controller.MenuAware;
import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import br.com.squadtech.bluetech.util.LogoutHelper;
import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MenuADMController implements MenuAware, SupportsMainController {

    private static final Logger log = LoggerFactory.getLogger(MenuADMController.class);

    @FXML
    private Label lblTituloADM;

    @FXML
    private ImageView imgViewFotoADM;

    @FXML
    private Label lblADM;

    @FXML
    private Label lblSemestreADM;

    @FXML
    private VBox vboxMenuADM;

    @FXML
    private AnchorPane paneSuperiorMenuADM;

    @FXML
    private SplitPane splitPanelMenuADM;

    @FXML
    private Accordion accordionADM;

    @FXML
    private JFXButton btnTelaInicial;

    @FXML
    private JFXButton btnAddProfessor;

    @FXML
    private JFXButton btnCSV;

    @FXML
    private JFXButton btnLogout;

    // Referência ao painel principal unificado
    private PainelPrincipalController painelPrincipalController;

    @Override
    public void setPainelPrincipalController(PainelPrincipalController painelPrincipalController) {
        this.painelPrincipalController = painelPrincipalController;
    }

    @Override
    public void onContentChanged(String fxmlPath, Object contentController) {
        // Mantém compatibilidade com o comportamento anterior
        if (btnTelaInicial != null) {
            boolean active = fxmlPath.contains("TelaInicialADM.fxml");
            btnTelaInicial.getStyleClass().remove("active");
            if (active) btnTelaInicial.getStyleClass().add("active");
        }
    }

    @FXML
    private void abrirTelaInicial(ActionEvent event) {
        System.out.println("Abrindo Tela Inicial do ADM...");
        if (painelPrincipalController != null) {
            try {
                painelPrincipalController.loadContent("/fxml/admin/TelaInicialADM.fxml");
            } catch (Exception e) {
                log.error("Erro ao carregar TelaInicialADM.fxml", e);
            }
        }
    }

    @FXML
    private void abrirTelaAdicionarProfessor(ActionEvent event) {
        System.out.println("Abrindo tela de adicionar professor...");
        if (painelPrincipalController != null) {
            try {
                painelPrincipalController.loadContent("/fxml/admin/CadastroProfessorTG.fxml");
            } catch (Exception e) {
                log.error("Erro ao carregar CadastroProfessorTG.fxml", e);
            }
        }
    }

    @FXML
    private void AbrirTelaCSV(ActionEvent event) {
        System.out.println("Abrindo tela de CSV...");
        if (painelPrincipalController != null) {
            try {
                painelPrincipalController.loadContent("/fxml/admin/ImportacaoCSVProfessorTG.fxml");
            } catch (Exception e) {
                log.error("Erro ao carregar ImportacaoCSVProfessorTG.fxml", e);
            }
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        log.info("Administrador solicitou logout");
        LogoutHelper.performLogout(event, log);
    }

    @FXML
    void initialize() {
        // Validações dos componentes FXML
        assert btnTelaInicial != null : "fx:id=\"btnTelaInicial\" não foi injetado: verifique seu FXML 'MenuADM.fxml'.";
        assert btnAddProfessor != null : "fx:id=\"btnAddProfessor\" não foi injetado: verifique seu FXML.";
        assert btnCSV != null : "fx:id=\"btnCSV\" não foi injetado: verifique seu FXML.";
        assert imgViewFotoADM != null : "fx:id=\"imgViewFotoADM\" não foi injetado: verifique seu FXML.";
        assert lblADM != null : "fx:id=\"lblADM\" não foi injetado: verifique seu FXML.";
        assert lblSemestreADM != null : "fx:id=\"lblSemestreADM\" não foi injetado: verifique seu FXML.";
        assert lblTituloADM != null : "fx:id=\"lblTituloADM\" não foi injetado: verifique seu FXML.";
        assert vboxMenuADM != null : "fx:id=\"vboxMenuADM\" não foi injetado: verifique seu FXML.";
        assert splitPanelMenuADM != null : "fx:id=\"splitPanelMenuADM\" não foi injetado: verifique seu FXML.";
        assert btnLogout != null : "fx:id=\"btnLogout\" não foi injetado: verifique seu FXML.";

        log.info("MenuADMController inicializado com sucesso.");
    }
}