package br.com.squadtech.bluetech.controller.aluno;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import br.com.squadtech.bluetech.dao.TGPortifolioDAO;
import br.com.squadtech.bluetech.dao.TGSecaoDAO;
import br.com.squadtech.bluetech.dao.TGVersaoDAO;
import br.com.squadtech.bluetech.model.TGSecao;
import br.com.squadtech.bluetech.model.TGVersao;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.AnchorPane;

public class CriarSecaoAPIController implements SupportsMainController {

    @FXML private ResourceBundle resources;
    @FXML private URL location;

    @FXML private Button btnSalvar, btnVoltar, btnCancelar;

    @FXML private TextField txtAno, txtEmpresa, txtRepositorio, txtLinkedin, txtSemestre, txtSemestreAno;
    @FXML private TextArea txtContribuicoes, txtHardSkills, txtProblema, txtSolucao, txtTecnologias, txtSoftSkills;

    private final Map<TextInputControl, String> initialValues = new HashMap<>();
    private PainelPrincipalController painelPrincipalController;

    @FXML
    void salvarNovaSecaoAPI(ActionEvent event) {
        Integer ano = null;
        String anoStr = txtAno.getText();
        if (anoStr != null && !anoStr.isBlank()) {
            try {
                ano = Integer.parseInt(anoStr.trim());
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Ano inválido", "O campo Ano deve conter apenas números.");
                return;
            }
        }

        try {
            TGPortifolioDAO portifolioDAO = new TGPortifolioDAO();
            portifolioDAO.createTableIfNotExists();

            Long alunoId = getAlunoIdAtual();
            Long portifolioId = portifolioDAO.getOrCreatePortifolioForAluno(alunoId);

            TGSecaoDAO secaoDAO = new TGSecaoDAO();
            secaoDAO.createTableIfNotExists();

            int apiNumero = 1;
            while (secaoDAO.findByApiNumeroAndPortifolio(apiNumero, portifolioId) != null) {
                apiNumero++;
                if (apiNumero > 6) {
                    showAlert(Alert.AlertType.ERROR, "Limite de seções", "Todas as 6 seções já foram criadas para este portfólio.");
                    return;
                }
            }

            TGSecao novaSecao = new TGSecao(portifolioId, apiNumero, "PENDENTE", false);
            novaSecao.setDataValidacao(LocalDateTime.now());

            Long secaoId = secaoDAO.insertReturningId(novaSecao);
            if (secaoId == null) {
                showAlert(Alert.AlertType.ERROR, "Erro", "Falha ao criar a nova seção.");
                return;
            }

            TGVersao versao = new TGVersao(
                    safeText(txtSemestre),
                    ano,
                    safeText(txtSemestreAno),
                    safeText(txtEmpresa),
                    safeText(txtProblema),
                    safeText(txtSolucao),
                    safeText(txtRepositorio),
                    safeText(txtLinkedin),
                    safeText(txtTecnologias),
                    safeText(txtContribuicoes),
                    safeText(txtHardSkills),
                    safeText(txtSoftSkills)
            );
            versao.setSecaoId(secaoId);

            TGVersaoDAO versaoDAO = new TGVersaoDAO();
            versaoDAO.createTableIfNotExists();

            int proximoNumeroVersao = versaoDAO.getUltimoNumeroVersao(secaoId) + 1;
            Long idVersao = versaoDAO.insertReturningId(versao, proximoNumeroVersao);

            if (idVersao != null) {
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Nova entrega criada com sucesso.");
                clearAllInputs();
                snapshotInitialValues();
                voltarEntregasAluno(null);
            } else {
                showAlert(Alert.AlertType.ERROR, "Erro", "Falha ao salvar a versão da nova seção.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Ocorreu um erro: " + e.getMessage());
        }
    }

    @FXML
    void voltarEntregasAluno(ActionEvent event) {
        if (painelPrincipalController != null) {
            try {
                painelPrincipalController.loadContent("/fxml/aluno/TelaEntregasAluno.fxml");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erro", "Falha ao carregar TelaEntregasAluno.fxml");
            }
        } else {
            System.err.println("PainelPrincipalController não foi injetado em CriarSecaoAPIController.");
        }
    }

    @FXML
    void handleCancelar(ActionEvent event) {
        restoreInitialValues();
    }

    @FXML
    void initialize() {
        Platform.runLater(this::snapshotInitialValues);
    }

    private void snapshotInitialValues() {
        initialValues.clear();
        for (TextInputControl input : getAllInputs()) {
            initialValues.put(input, input.getText());
        }
    }

    private void restoreInitialValues() {
        for (TextInputControl input : getAllInputs()) {
            String original = initialValues.get(input);
            input.setText(original == null ? "" : original);
        }
    }

    private void clearAllInputs() {
        for (TextInputControl input : getAllInputs()) {
            input.clear();
        }
    }

    private String safeText(TextInputControl input) {
        String v = input.getText();
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    private java.util.List<TextInputControl> getAllInputs() {
        return Arrays.asList(
                txtSemestre, txtAno, txtSemestreAno, txtEmpresa, txtProblema, txtSolucao,
                txtRepositorio, txtLinkedin, txtTecnologias, txtContribuicoes, txtHardSkills, txtSoftSkills
        );
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public void setPainelPrincipalController(PainelPrincipalController controller) {
        this.painelPrincipalController = controller;
    }

    private Long getAlunoIdAtual() {
        return 1L; // Substituir pelo ID real do aluno logado
    }

    /**
     * Abre a tela de edição de seção passando o ID da última versão.
     */
    public void abrirEditarSecaoComFeedback(Long idUltimaVersao) {
        if (painelPrincipalController == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/aluno/EditarSecaoAPI.fxml"));
            Parent root = loader.load();

            EditarSecaoAPIController controller = loader.getController();
            controller.setVersaoId(idUltimaVersao);

            // Usa o método loadContent do PainelPrincipalController para exibir
            painelPrincipalController.loadContent("/fxml/aluno/EditarSecaoAPI.fxml");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Falha ao abrir EditarSecaoAPI.fxml");
        }
    }
}
