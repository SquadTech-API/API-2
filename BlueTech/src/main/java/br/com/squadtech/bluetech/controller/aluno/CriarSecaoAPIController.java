package br.com.squadtech.bluetech.controller.aluno;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import br.com.squadtech.bluetech.dao.TGSecaoDAO;
import br.com.squadtech.bluetech.dao.TGVersaoDAO;
import br.com.squadtech.bluetech.model.TGVersao;
import br.com.squadtech.bluetech.model.TGSecao;
import br.com.squadtech.bluetech.model.SessaoUsuario;
import br.com.squadtech.bluetech.model.Usuario;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CriarSecaoAPIController implements SupportsMainController {

    private static final Logger log = LoggerFactory.getLogger(CriarSecaoAPIController.class);

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

    // Substitui os antigos TextFields por ChoiceBoxes
    @FXML
    private ChoiceBox<String> choiceSemestreCurso; // valores: "1º Semestre".."6º Semestre"

    @FXML
    private ChoiceBox<String> choiceSemestreAno; // valores: "1", "2"

    // Campo API preenchido automaticamente conforme semestre do curso
    @FXML
    private TextField txtApi;

    @FXML
    private TextArea txtSolucao;

    @FXML
    private TextArea txtTecnologias;

    @FXML
    private TextArea txtSoftSkills;

    // Guarda os valores iniciais para permitir cancelar e reverter mudanças (apenas TextInputControl)
    private final Map<TextInputControl, String> initialValues = new HashMap<>();

    @FXML
    void salvarNovaSecaoAPI(ActionEvent event) {
        // Obtém usuário logado
        Usuario user = SessaoUsuario.getUsuarioLogado();
        if (user == null || user.getEmail() == null) {
            showAlert(Alert.AlertType.ERROR, "Sessão inválida", "Faça login novamente para criar uma seção.");
            return;
        }
        String emailUsuario = user.getEmail();

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

        String semestreCursoSel = choiceSemestreCurso.getValue();
        if (semestreCursoSel == null) {
            showAlert(Alert.AlertType.WARNING, "Semestre do Curso", "Selecione o semestre do curso.");
            return;
        }
        String semestreAnoSel = choiceSemestreAno.getValue();
        if (semestreAnoSel == null) {
            showAlert(Alert.AlertType.WARNING, "Semestre do Ano", "Selecione o semestre do ano (1 ou 2).");
            return;
        }

        TGVersao secao = new TGVersao(
                semestreCursoSel,
                ano,
                semestreAnoSel,
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

        TGVersaoDAO dao = new TGVersaoDAO();
        dao.createTableIfNotExists();

        // Regras: máximo 6 seções e não duplicar API por usuário
        TGSecaoDAO tgSecaoDAO = new TGSecaoDAO();
        tgSecaoDAO.createTableIfNotExists();
        tgSecaoDAO.ensureSchemaUpToDate();
        int apiNumero = parseApiNumeroFromSemestre(semestreCursoSel);
        if (apiNumero < 1 || apiNumero > 6) {
            showAlert(Alert.AlertType.ERROR, "API inválida", "O número da API deve estar entre 1 e 6.");
            return;
        }
        if (tgSecaoDAO.existsByApiNumero(apiNumero, emailUsuario)) {
            showAlert(Alert.AlertType.WARNING, "API já existente", "Você já possui uma seção para a API " + apiNumero + ". Exclua a existente antes de criar outra.");
            return;
        }
        int total = tgSecaoDAO.countSecoes(emailUsuario);
        if (total >= 6) {
            showAlert(Alert.AlertType.WARNING, "Limite de APIs", "Você já possui 6 APIs. Exclua uma para criar outra.");
            return;
        }

        Integer idVersao = dao.insertReturningId(secao);
        if (idVersao != null) {
            // Inserção simples (sem upsert)
            TGSecao tgSecao = new TGSecao(apiNumero, LocalDateTime.now(), "Em andamento", idVersao, emailUsuario);
            int idSecaoCriada = tgSecaoDAO.insert(tgSecao);

            // Vincula a versão recém-criada à seção para histórico
            if (idSecaoCriada > 0) {
                TGVersaoDAO versaoDAO = new TGVersaoDAO();
                versaoDAO.updateSecaoId(idVersao, idSecaoCriada);
            }

            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Seção API salva com sucesso.");
            clearAllInputs();
            snapshotInitialValues();
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
                log.error("Falha ao carregar TelaEntregasAluno.fxml", e);
            }
        } else {
            log.error("PainelPrincipalController não foi injetado em CriarSecaoAPIController.");
        }
    }

    @FXML
    void handleCancelar(ActionEvent event) {
        clearAllInputs();
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
        assert choiceSemestreCurso != null : "fx:id=\"choiceSemestreCurso\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert choiceSemestreAno != null : "fx:id=\"choiceSemestreAno\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert txtApi != null : "fx:id=\"txtApi\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert txtSolucao != null : "fx:id=\"txtSolucao\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert txtTecnologias != null : "fx:id=\"txtTecnologias\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert txtSoftSkills != null : "fx:id=\"txtSoftSkills\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";

        // Popular choiceboxes
        choiceSemestreCurso.getItems().setAll(
                "1º Semestre",
                "2º Semestre",
                "3º Semestre",
                "4º Semestre",
                "5º Semestre",
                "6º Semestre"
        );
        choiceSemestreAno.getItems().setAll("1", "2");

        // Listener para preencher API conforme semestre do curso
        choiceSemestreCurso.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                txtApi.clear();
            } else {
                String numero = newV.replaceAll("[^0-9]", "");
                txtApi.setText(numero);
            }
        });
        txtApi.setEditable(false);

        // Snapshot dos valores iniciais
        Platform.runLater(this::snapshotInitialValues);
    }

    private void snapshotInitialValues() {
        initialValues.clear();
        // Apenas campos de texto/área; choice boxes são restauradas manualmente, se necessário
        for (TextInputControl input : new TextInputControl[]{
                txtAno, txtEmpresa, txtProblema, txtSolucao, txtRepositorio, txtLinkedin,
                txtTecnologias, txtContribuicoes, txtHardSkills, txtSoftSkills, txtApi
        }) {
            if (input != null) {
                initialValues.put(input, input.getText());
            }
        }
    }

    private void clearAllInputs() {
        // Limpa textos
        for (TextInputControl input : new TextInputControl[]{
                txtAno, txtEmpresa, txtProblema, txtSolucao, txtRepositorio, txtLinkedin,
                txtTecnologias, txtContribuicoes, txtHardSkills, txtSoftSkills, txtApi
        }) {
            if (input != null) input.clear();
        }
        // Reseta escolhas
        if (choiceSemestreCurso != null) choiceSemestreCurso.setValue(null);
        if (choiceSemestreAno != null) choiceSemestreAno.setValue(null);
    }

    private String safeText(TextInputControl input) {
        String v = input.getText();
        return (v == null || v.isBlank()) ? null : v.trim();
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
