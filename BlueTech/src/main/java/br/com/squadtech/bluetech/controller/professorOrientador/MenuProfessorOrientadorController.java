package br.com.squadtech.bluetech.controller.professorOrientador;

import br.com.squadtech.bluetech.config.SmtpProps;
import br.com.squadtech.bluetech.controller.MenuAware;
import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import br.com.squadtech.bluetech.dao.ProfessorDAO;
import br.com.squadtech.bluetech.model.Professor;
import br.com.squadtech.bluetech.model.SessaoUsuario;
import br.com.squadtech.bluetech.service.EmailService;
import com.jfoenix.controls.JFXButton;
import jakarta.mail.MessagingException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MenuProfessorOrientadorController implements MenuAware, SupportsMainController {
    private static final Logger log = LoggerFactory.getLogger(MenuProfessorOrientadorController.class);

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
                return;
            }

            // 2. Nome do professor vem direto do Usuario
            String nome = (usuario.getNome() != null && !usuario.getNome().isBlank())
                    ? usuario.getNome()
                    : usuario.getEmail();
            lblProfessorOri.setText(nome);

            // 3. Busca registro na tabela PROFESSOR para mostrar cargo / tipo TG
            ProfessorDAO profDAO = new ProfessorDAO();
            Professor professor = profDAO.findByUsuarioEmail(usuario.getEmail());

            if (professor != null) {
                String cargo = professor.getCargo();
                String tipoTG = professor.getTipoTG();   // "TG1", "TG2", "AMBOS", etc.

                StringBuilder sb = new StringBuilder();
                if (cargo != null && !cargo.isBlank()) {
                    sb.append(cargo);
                }
                if (tipoTG != null && !tipoTG.isBlank()) {
                    if (sb.length() > 0) sb.append(" • ");
                    sb.append("Tipo TG: ").append(tipoTG);
                }

                if (sb.length() == 0) {
                    sb.append("Perfil não informado");
                }


            } else {
                // Não tem linha na tabela professor, mas pelo menos mostra algo
            }

        } catch (Exception e) {
            log.error("Erro ao carregar dados do professor orientador", e);
            lblProfessorOri.setText("PROFESSOR ORIENTADOR: (erro ao carregar)");
        }
    }


    @FXML
    void initialize() {
        assert btnSolicitacoesOrientacao != null : "fx:id=\"btnSolicitacoesOrientacao\" was not injected: check your FXML file 'MenuProfessorOrientador.fxml'.";
        assert btnenviarEmail != null : "fx:id=\"btnenviarEmail\" was not injected: check your FXML file 'MenuProfessorOrientador.fxml'.";
        assert imgViewFotoProfessorOri != null : "fx:id=\"imgViewFotoProfessorOri\" was not injected: check your FXML file 'MenuProfessorOrientador.fxml'.";
        assert lblProfessorOri != null : "fx:id=\"lblProfessorOri\" was not injected: check your FXML file 'MenuProfessorOrientador.fxml'.";
        assert lblTituloProfessorOri != null : "fx:id=\"lblTituloProfessorOri\" was not injected: check your FXML file 'MenuProfessorOrientador.fxml'.";
        assert paneSuperiorMenuProfessorOri != null : "fx:id=\"paneSuperiorMenuProfessorOri\" was not injected: check your FXML file 'MenuProfessorOrientador.fxml'.";
        assert splitPanelMenuProfessorOri != null : "fx:id=\"splitPanelMenuProfessorOri\" was not injected: check your FXML file 'MenuProfessorOrientador.fxml'.";
        assert vboxMenuProfessorOri != null : "fx:id=\"vboxMenuProfessorOri\" was not injected: check your FXML file 'MenuProfessorOrientador.fxml'.";
        carregarDadosProfessor();
    }
}
