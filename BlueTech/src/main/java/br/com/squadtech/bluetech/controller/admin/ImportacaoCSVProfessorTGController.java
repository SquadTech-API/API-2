package br.com.squadtech.bluetech.controller.admin;

import br.com.squadtech.bluetech.dao.ProfessorTGDAO;
import br.com.squadtech.bluetech.model.ProfessorTG;
import com.jfoenix.controls.JFXButton;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class ImportacaoCSVProfessorTGController {

    @FXML private JFXButton btnSelecionarArquivo;
    @FXML private JFXButton btnValidar;
    @FXML private JFXButton btnImportar;
    @FXML private JFXButton btnLimpar;

    @FXML private Label lblArquivoSelecionado;
    @FXML private Label lblTotalRegistros;
    @FXML private Label lblValidos;
    @FXML private Label lblInvalidos;
    @FXML private Label lblImportados;

    @FXML private TableView<List<String>> tableViewPreview;
    @FXML private TableColumn<List<String>, String> colPreviewNome;
    @FXML private TableColumn<List<String>, String> colPreviewEmail;
    @FXML private TableColumn<List<String>, String> colPreviewCargo;
    @FXML private TableColumn<List<String>, String> colPreviewTipoTG;
    @FXML private TableColumn<List<String>, String> colPreviewCurso;

    @FXML private ProgressBar progressBar;
    @FXML private TextArea txtLog;

    private File arquivoCSV;
    private List<List<String>> dadosCSV;
    private ProfessorTGDAO professorTGDAO;
    private ObservableList<List<String>> dadosPreview;

    @FXML
    void initialize() {
        professorTGDAO = new ProfessorTGDAO();
        dadosCSV = new ArrayList<>();
        dadosPreview = FXCollections.observableArrayList();

        configurarTabelaPreview();
        atualizarEstatisticas();

        System.out.println("ImportacaoCSVProfessorTGController inicializado!");
    }

    private void configurarTabelaPreview() {
        // Configurar colunas da tabela de preview
        colPreviewNome.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().get(0)));
        colPreviewEmail.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().get(1)));
        colPreviewCargo.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().get(2)));
        colPreviewTipoTG.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().get(3)));
        colPreviewCurso.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().get(4)));

        tableViewPreview.setItems(dadosPreview);
    }

    @FXML
    private void selecionarArquivo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Arquivo CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Arquivos CSV", "*.csv")
        );

        Stage stage = (Stage) btnSelecionarArquivo.getScene().getWindow();
        arquivoCSV = fileChooser.showOpenDialog(stage);

        if (arquivoCSV != null) {
            lblArquivoSelecionado.setText(arquivoCSV.getName());
            carregarPreviewCSV();
            btnValidar.setDisable(false);
        }
    }

    private void carregarPreviewCSV() {
        try {
            dadosCSV.clear();
            dadosPreview.clear();

            BufferedReader reader = new BufferedReader(new FileReader(arquivoCSV));
            String linha;
            int contador = 0;

            while ((linha = reader.readLine()) != null && contador < 50) { // Limita preview a 50 linhas
                String[] campos = linha.split(",");
                if (campos.length >= 5) {
                    List<String> linhaDados = new ArrayList<>();
                    for (int i = 0; i < 5; i++) {
                        linhaDados.add(campos[i].trim());
                    }
                    dadosCSV.add(linhaDados);
                    if (contador < 10) { // Mostra apenas 10 linhas no preview
                        dadosPreview.add(linhaDados);
                    }
                }
                contador++;
            }
            reader.close();

            atualizarEstatisticas();
            adicionarLog("Arquivo carregado: " + dadosCSV.size() + " registros encontrados");

        } catch (Exception e) {
            adicionarLog("ERRO ao carregar arquivo: " + e.getMessage());
            mostrarAlerta("Erro", "Não foi possível carregar o arquivo CSV.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void validarDados() {
        if (dadosCSV.isEmpty()) {
            adicionarLog("Nenhum dado para validar.");
            return;
        }

        int validos = 0;
        int invalidos = 0;

        for (int i = 0; i < dadosCSV.size(); i++) {
            List<String> linha = dadosCSV.get(i);
            if (validarLinha(linha)) {
                validos++;
            } else {
                invalidos++;
                adicionarLog("Linha " + (i + 1) + " inválida: " + String.join(", ", linha));
            }
        }

        lblValidos.setText(String.valueOf(validos));
        lblInvalidos.setText(String.valueOf(invalidos));

        if (invalidos == 0) {
            btnImportar.setDisable(false);
            adicionarLog("Validação concluída: TODOS os registros são válidos!");
        } else {
            adicionarLog("Validação concluída: " + validos + " válidos, " + invalidos + " inválidos");
        }
    }

    private boolean validarLinha(List<String> linha) {
        // Validar se tem 5 campos
        if (linha.size() < 5) return false;

        String nome = linha.get(0);
        String email = linha.get(1);
        String cargo = linha.get(2);
        String tipoTG = linha.get(3);
        String curso = linha.get(4);

        // Validar campos obrigatórios
        if (nome.isEmpty() || email.isEmpty() || cargo.isEmpty() || tipoTG.isEmpty()) {
            return false;
        }

        // Validar email
        if (!email.contains("@")) {
            return false;
        }

        // Validar tipo TG
        if (!tipoTG.equals("TG1") && !tipoTG.equals("TG2") && !tipoTG.equals("AMBOS")) {
            return false;
        }

        return true;
    }

    @FXML
    private void importarProfessores() {
        if (dadosCSV.isEmpty()) {
            adicionarLog("Nenhum dado para importar.");
            return;
        }

        progressBar.setVisible(true);
        progressBar.setProgress(0);

        int total = dadosCSV.size();

        // 🔥 CORREÇÃO: Usar arrays para variáveis que serão modificadas no lambda
        final int[] importados = {0};
        final int[] erros = {0};

        new Thread(() -> {
            for (int i = 0; i < dadosCSV.size(); i++) {
                List<String> linha = dadosCSV.get(i);

                if (validarLinha(linha)) {
                    ProfessorTG professor = new ProfessorTG();
                    professor.setNome(linha.get(0));
                    professor.setEmail(linha.get(1));
                    professor.setCargo(linha.get(2));
                    professor.setTipoTG(linha.get(3));
                    professor.setCursoVinculado(linha.get(4));

                    if (professorTGDAO.salvar(professor)) {
                        importados[0]++;
                        adicionarLog("✓ Importado: " + professor.getNome());
                    } else {
                        erros[0]++;
                        adicionarLog("✗ Erro ao importar: " + professor.getNome());
                    }
                } else {
                    erros[0]++;
                    adicionarLog("✗ Dados inválidos na linha: " + (i + 1));
                }

                final int progress = i + 1;
                javafx.application.Platform.runLater(() -> {
                    progressBar.setProgress((double) progress / total);
                    lblImportados.setText(String.valueOf(importados[0]));
                });
            }

            javafx.application.Platform.runLater(() -> {
                progressBar.setVisible(false);
                adicionarLog("Importação concluída: " + importados[0] + " professores importados, " + erros[0] + " erros");
                btnImportar.setDisable(true);
            });
        }).start();
    }

    @FXML
    private void limparImportacao() {
        arquivoCSV = null;
        dadosCSV.clear();
        dadosPreview.clear();

        lblArquivoSelecionado.setText("Nenhum arquivo selecionado");
        txtLog.clear();

        btnValidar.setDisable(true);
        btnImportar.setDisable(true);
        progressBar.setVisible(false);

        atualizarEstatisticas();
        adicionarLog("Importação limpa. Pronto para novo arquivo.");
    }

    private void atualizarEstatisticas() {
        lblTotalRegistros.setText(String.valueOf(dadosCSV.size()));
        lblValidos.setText("0");
        lblInvalidos.setText("0");
        lblImportados.setText("0");
    }

    private void adicionarLog(String mensagem) {
        javafx.application.Platform.runLater(() -> {
            txtLog.appendText(mensagem + "\n");
        });
    }

    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}