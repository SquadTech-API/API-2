package br.com.squadtech.bluetech.controller.aluno;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import br.com.squadtech.bluetech.dao.SecaoAPIDAO;
import br.com.squadtech.bluetech.dao.TGSecaoDAO;
import br.com.squadtech.bluetech.model.SecaoAPI;
import br.com.squadtech.bluetech.model.TGSecao;
import java.time.LocalDateTime;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;

public class CriarSecaoAPIController implements SupportsMainController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnSalvar;

    @FXML
    private Button btnVoltar;

    @FXML
    private Button btnCancelar;

    @FXML
    private TextField txtAno;

    @FXML
    private TextArea txtContribuicoes;

    @FXML
    private TextField txtEmpresa;

    @FXML
    private TextArea txtHardSkills;

    @FXML
    private TextArea txtProblema;

    @FXML
    private TextField txtRepositorio;

    @FXML
    private TextField txtLinkedin;

    @FXML
    private TextField txtSemestre;

    @FXML
    private TextField txtSemestreAno;

    @FXML
    private TextArea txtSolucao;

    @FXML
    private TextArea txtTecnologias;

    @FXML
    private TextArea txtSoftSkills;

    // Guarda os valores iniciais para permitir cancelar e reverter mudanças
    private final Map<TextInputControl, String> initialValues = new HashMap<>();

    @FXML
    void salvarNovaSecaoAPI(ActionEvent event) {
        // Usuário não é mais necessário para TG_Versao
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

        SecaoAPI secao = new SecaoAPI(
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

        SecaoAPIDAO dao = new SecaoAPIDAO();
        Integer idVersao = dao.insertReturningId(secao);
        if (idVersao != null) {
            // Upsert em TG_Secao
            TGSecaoDAO tgSecaoDAO = new TGSecaoDAO();
            tgSecaoDAO.createTableIfNotExists();

            // Deriva API_Numero do campo semestre (ex: "1º Semestre" -> 1). Fallback para 1 se inválido.
            int apiNumero = parseApiNumeroFromSemestre(safeText(txtSemestre));
            TGSecao tgSecao = new TGSecao(apiNumero, LocalDateTime.now(), "Em andamento", idVersao);
            tgSecaoDAO.upsertByApiNumero(tgSecao);

            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Seção API salva com sucesso.");
            clearAllInputs();
            snapshotInitialValues();

            // Volta para a tela de entregas para exibir os cards atualizados
            voltarEntregasAluno(null);
        } else {
            showAlert(Alert.AlertType.ERROR, "Erro", "Falha ao salvar a Seção API. Verifique os dados e tente novamente.");
        }
    }

    private int parseApiNumeroFromSemestre(String semestre) {
        if (semestre == null) return 1;
        String s = semestre.replaceAll("[^0-9]", "");
        try { int n = Integer.parseInt(s); return (n >=1 && n <=6) ? n : 1; } catch (Exception e) { return 1; }
    }

    @FXML
    void voltarEntregasAluno(ActionEvent event) {
        if (painelPrincipalController != null) {
            try {
                String fxmlPath = "/fxml/aluno/TelaEntregasAluno.fxml";

                painelPrincipalController.loadContent(fxmlPath);
            } catch (IOException e) {
                System.err.println("Falha ao carregar TelaEntregasAluno.fxml");
                e.printStackTrace();

            }
        } else {
            System.err.println("Erro: PainelPrincipalController não foi injetado em CriarSecaoAPIController.");
        }

    }

    @FXML
    void handleCancelar(ActionEvent event) {
        clearAllInputs();
        // Também atualiza o snapshot, para o estado atual vazio ser o novo "inicial"
        snapshotInitialValues();
    }

    @FXML
    void initialize() {
        assert btnSalvar != null : "fx:id=\"btnSalvar\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert btnVoltar != null : "fx:id=\"btnVoltar\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert btnCancelar != null : "fx:id=\"btnCancelar\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert txtAno != null : "fx:id=\"txtAno\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert txtContribuicoes != null : "fx:id=\"txtContribuicoes\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert txtEmpresa != null : "fx:id=\"txtEmpresa\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert txtHardSkills != null : "fx:id=\"txtHardSkills\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert txtProblema != null : "fx:id=\"txtProblema\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert txtRepositorio != null : "fx:id=\"txtRepositorio\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert txtLinkedin != null : "fx:id=\"txtLinkedin\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert txtSemestre != null : "fx:id=\"txtSemestre\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert txtSemestreAno != null : "fx:id=\"txtSemestreAno\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert txtSolucao != null : "fx:id=\"txtSolucao\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert txtTecnologias != null : "fx:id=\"txtTecnologias\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert txtSoftSkills != null : "fx:id=\"txtSoftSkills\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";

        // Snapshot dos valores iniciais (após qualquer preenchimento automático externo)
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
                txtSemestre,
                txtAno,
                txtSemestreAno,
                txtEmpresa,
                txtProblema,
                txtSolucao,
                txtRepositorio,
                txtLinkedin,
                txtTecnologias,
                txtContribuicoes,
                txtHardSkills,
                txtSoftSkills
        );
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private PainelPrincipalController painelPrincipalController;

    @Override
    public void setPainelPrincipalController(PainelPrincipalController controller) {
        this.painelPrincipalController = controller;
    }
}
