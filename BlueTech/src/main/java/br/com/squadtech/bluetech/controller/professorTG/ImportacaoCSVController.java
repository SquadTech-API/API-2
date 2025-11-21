package br.com.squadtech.bluetech.controller.professorTG;

import br.com.squadtech.bluetech.controller.MenuAware;
import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import br.com.squadtech.bluetech.dao.ProfessorDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ImportacaoCSVController implements MenuAware, SupportsMainController {

    private static final Logger log = LoggerFactory.getLogger(ImportacaoCSVController.class);

    private PainelPrincipalController painelPrincipalController;
    private ProfessorDAO professorDAO = new ProfessorDAO();

    // Campos da tela
    @FXML private Button btnSelecionarArquivo;
    @FXML private Button btnImportar;
    @FXML private Button btnLimpar;
    @FXML private Label lblArquivoSelecionado;
    @FXML private Label lblStatus;
    @FXML private ProgressBar progressBar;
    @FXML private TableView<ProfessorCSV> tableViewPreview;
    @FXML private TableColumn<ProfessorCSV, String> colNome;
    @FXML private TableColumn<ProfessorCSV, String> colEmail;
    @FXML private TableColumn<ProfessorCSV, String> colCargo;
    @FXML private TableColumn<ProfessorCSV, String> colCurso;
    @FXML private TableColumn<ProfessorCSV, String> colFormacao;
    @FXML private TableColumn<ProfessorCSV, String> colEspecializacao;

    private File arquivoCSV;
    private ObservableList<ProfessorCSV> dadosPreview = FXCollections.observableArrayList();

    @Override
    public void setPainelPrincipalController(PainelPrincipalController painelPrincipalController) {
        this.painelPrincipalController = painelPrincipalController;
    }

    @Override
    public void onContentChanged(String fxmlPath, Object contentController) {
        log.debug("Conteúdo alterado para: {}", fxmlPath);
    }

    @FXML
    void initialize() {
        log.info("ImportacaoCSVController inicializado");
        configurarTabela();
        atualizarStatus("Selecione um arquivo CSV para importar");
    }

    private void configurarTabela() {
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCargo.setCellValueFactory(new PropertyValueFactory<>("cargo"));
        colCurso.setCellValueFactory(new PropertyValueFactory<>("curso"));
        colFormacao.setCellValueFactory(new PropertyValueFactory<>("formacao"));
        colEspecializacao.setCellValueFactory(new PropertyValueFactory<>("especializacao"));

        // Configurar larguras
        colNome.setPrefWidth(150);
        colEmail.setPrefWidth(200);
        colCargo.setPrefWidth(120);
        colCurso.setPrefWidth(120);
        colFormacao.setPrefWidth(150);
        colEspecializacao.setPrefWidth(150);

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
            lblArquivoSelecionado.setText("Arquivo: " + arquivoCSV.getName());
            carregarPreviewCSV();
        } else {
            lblArquivoSelecionado.setText("Nenhum arquivo selecionado");
        }
    }

    @FXML
    private void importarDados() {
        if (dadosPreview.isEmpty()) {
            mostrarErro("Nenhum dado para importar. Selecione um arquivo CSV válido.");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Importação");
        confirmacao.setHeaderText("Importar Professores em Massa");
        confirmacao.setContentText(String.format(
                "Deseja importar %d professores?\n\nEsta ação criará:\n• Usuários na tabela 'usuario'\n• Professores na tabela 'professor'",
                dadosPreview.size()
        ));

        if (confirmacao.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            executarImportacao();
        }
    }

    @FXML
    private void limparDados() {
        dadosPreview.clear();
        arquivoCSV = null;
        lblArquivoSelecionado.setText("Nenhum arquivo selecionado");
        progressBar.setProgress(0);
        atualizarStatus("Dados limpos. Selecione um novo arquivo CSV.");
    }

    private void carregarPreviewCSV() {
        dadosPreview.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(arquivoCSV))) {
            String linha;
            boolean primeiraLinha = true;
            int contador = 0;

            while ((linha = br.readLine()) != null && contador < 100) { // Limita preview a 100 linhas
                if (primeiraLinha) {
                    primeiraLinha = false;
                    continue; // Pula cabeçalho
                }

                ProfessorCSV professor = parseLinhaCSV(linha);
                if (professor != null && validarDadosProfessor(professor)) {
                    dadosPreview.add(professor);
                    contador++;
                }
            }

            atualizarStatus(String.format("Preview carregado: %d professores", dadosPreview.size()));
            btnImportar.setDisable(dadosPreview.isEmpty());

        } catch (IOException e) {
            log.error("Erro ao ler arquivo CSV", e);
            mostrarErro("Erro ao ler arquivo: " + e.getMessage());
        }
    }

    private ProfessorCSV parseLinhaCSV(String linha) {
        try {
            // Formato esperado: nome,email,cargo,curso,formacao,especializacao
            String[] campos = linha.split(";"); // Usando ponto e vírgula como separador

            if (campos.length >= 6) {
                return new ProfessorCSV(
                        campos[0].trim(), // nome
                        campos[1].trim(), // email
                        campos[2].trim(), // cargo
                        campos[3].trim(), // curso
                        campos[4].trim(), // formacao
                        campos[5].trim()  // especializacao
                );
            }
        } catch (Exception e) {
            log.warn("Linha CSV inválida: {}", linha);
        }
        return null;
    }

    private boolean validarDadosProfessor(ProfessorCSV professor) {
        if (professor.getNome() == null || professor.getNome().trim().isEmpty()) {
            return false;
        }
        if (professor.getEmail() == null || professor.getEmail().trim().isEmpty() ||
                !professor.getEmail().contains("@")) {
            return false;
        }
        if (professor.getCargo() == null || professor.getCargo().trim().isEmpty()) {
            return false;
        }
        return true;
    }

    private void executarImportacao() {
        progressBar.setProgress(0);
        btnImportar.setDisable(true);
        btnSelecionarArquivo.setDisable(true);

        new Thread(() -> {
            try {
                int total = dadosPreview.size();
                int sucessos = 0;
                int erros = 0;
                List<String> errosDetalhados = new ArrayList<>();

                for (int i = 0; i < total; i++) {
                    ProfessorCSV professor = dadosPreview.get(i);

                    try {
                        boolean salvo = salvarProfessor(professor);
                        if (salvo) {
                            sucessos++;
                        } else {
                            erros++;
                            errosDetalhados.add(professor.getEmail() + " - Erro ao salvar no banco");
                        }
                    } catch (Exception e) {
                        erros++;
                        errosDetalhados.add(professor.getEmail() + " - " + e.getMessage());
                        log.error("Erro ao importar professor: {}", professor.getEmail(), e);
                    }

                    final int progresso = i + 1;
                    final double progressoPercentual = (double) progresso / total;

                    javafx.application.Platform.runLater(() -> {
                        progressBar.setProgress(progressoPercentual);
                        atualizarStatus(String.format("Importando... %d/%d", progresso, total));
                    });
                }

                final int finalSucessos = sucessos;
                final int finalErros = erros;
                final List<String> finalErrosDetalhados = errosDetalhados;

                javafx.application.Platform.runLater(() -> {
                    progressBar.setProgress(1.0);
                    btnImportar.setDisable(false);
                    btnSelecionarArquivo.setDisable(false);

                    if (finalErros == 0) {
                        mostrarMensagemSucesso(String.format(
                                "Importação concluída com sucesso!\n%d professores importados.", finalSucessos
                        ));
                        limparDados();
                    } else {
                        StringBuilder mensagem = new StringBuilder();
                        mensagem.append(String.format(
                                "Importação parcialmente concluída:\n• Sucessos: %d\n• Erros: %d",
                                finalSucessos, finalErros
                        ));

                        if (!finalErrosDetalhados.isEmpty()) {
                            mensagem.append("\n\nErros detalhados:\n");
                            for (int i = 0; i < Math.min(finalErrosDetalhados.size(), 5); i++) {
                                mensagem.append("• ").append(finalErrosDetalhados.get(i)).append("\n");
                            }
                            if (finalErrosDetalhados.size() > 5) {
                                mensagem.append("• ... e mais ").append(finalErrosDetalhados.size() - 5).append(" erros");
                            }
                        }

                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Importação com Erros");
                        alert.setHeaderText("Importação Concluída com Alertas");
                        alert.setContentText(mensagem.toString());
                        alert.showAndWait();
                    }
                });

            } catch (Exception e) {
                log.error("Erro durante importação", e);
                javafx.application.Platform.runLater(() -> {
                    mostrarErro("Erro durante importação: " + e.getMessage());
                    btnImportar.setDisable(false);
                    btnSelecionarArquivo.setDisable(false);
                });
            }
        }).start();
    }

    private boolean salvarProfessor(ProfessorCSV professor) {
        // Gera senha temporária
        String senhaTemporaria = gerarSenhaTemporaria();
        String senhaHash = BCrypt.hashpw(senhaTemporaria, BCrypt.gensalt());

        // Salva no banco (já salva nas tabelas usuario E professor)
        return professorDAO.salvarProfessorOrientador(
                professor.getEmail(),
                professor.getNome(),
                senhaHash,
                "ORIENTADOR", // Tipo fixo para professores orientadores
                professor.getCargo(),
                professor.getCurso(),
                professor.getFormacao(),
                professor.getEspecializacao()
        );
    }

    private String gerarSenhaTemporaria() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder senha = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 8; i++) {
            senha.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }

        return senha.toString();
    }

    private void atualizarStatus(String mensagem) {
        lblStatus.setText(mensagem);
        log.info("Status: {}", mensagem);
    }

    private void mostrarMensagemSucesso(String mensagem) {
        log.info("✅ SUCESSO: {}", mensagem);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarErro(String mensagem) {
        log.error("❌ ERRO: {}", mensagem);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    /**
     * Classe DTO para representar os dados do CSV
     */
    public static class ProfessorCSV {
        private String nome;
        private String email;
        private String cargo;
        private String curso;
        private String formacao;
        private String especializacao;

        public ProfessorCSV(String nome, String email, String cargo, String curso,
                            String formacao, String especializacao) {
            this.nome = nome;
            this.email = email;
            this.cargo = cargo;
            this.curso = curso;
            this.formacao = formacao;
            this.especializacao = especializacao;
        }

        // Getters
        public String getNome() { return nome; }
        public String getEmail() { return email; }
        public String getCargo() { return cargo; }
        public String getCurso() { return curso; }
        public String getFormacao() { return formacao; }
        public String getEspecializacao() { return especializacao; }
    }
}