package br.com.squadtech.bluetech.controller.professorTG;

import br.com.squadtech.bluetech.controller.MenuAware;
import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import br.com.squadtech.bluetech.dao.ProfessorDAO;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class CadastroProfessoresController implements MenuAware, SupportsMainController {

    private static final Logger log = LoggerFactory.getLogger(CadastroProfessoresController.class);

    // Referência ao painel principal
    private PainelPrincipalController painelPrincipalController;

    // DAO para acesso ao banco
    private ProfessorDAO professorDAO = new ProfessorDAO();

    // Variável para controlar qual professor está sendo editado
    private String emailProfessorEmEdicao;

    // Campos do formulário
    @FXML private Button btnSalvar;
    @FXML private TextField txtNome;
    @FXML private TextField txtEmail;
    @FXML private TextField txtCargo;
    @FXML private TextField txtCurso;
    @FXML private TextArea txtFormacao;
    @FXML private TextArea txtEspecializacao;

    // Campos da tabela
    @FXML private TableView<ProfessorDAO.ProfessorOrientadorDTO> tableViewProfessores;
    @FXML private TableColumn<ProfessorDAO.ProfessorOrientadorDTO, String> colNome;
    @FXML private TableColumn<ProfessorDAO.ProfessorOrientadorDTO, String> colEmail;
    @FXML private TableColumn<ProfessorDAO.ProfessorOrientadorDTO, String> colCargo;
    @FXML private TableColumn<ProfessorDAO.ProfessorOrientadorDTO, String> colCurso;
    @FXML private TableColumn<ProfessorDAO.ProfessorOrientadorDTO, String> colFormacao;
    @FXML private TableColumn<ProfessorDAO.ProfessorOrientadorDTO, String> colEspecializacao;
    @FXML private TableColumn<ProfessorDAO.ProfessorOrientadorDTO, Void> colAcoes;

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
        log.info("CadastroProfessoresController inicializado com sucesso.");
        configurarTabela();
        carregarTabelaProfessores();
    }

    /**
     * Configura as colunas da tabela
     */
    private void configurarTabela() {
        if (colEmail != null) {
            colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
            colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
            colCargo.setCellValueFactory(new PropertyValueFactory<>("cargo"));
            colCurso.setCellValueFactory(new PropertyValueFactory<>("cursoVinculado"));
            colFormacao.setCellValueFactory(new PropertyValueFactory<>("formacaoAcademica"));
            colEspecializacao.setCellValueFactory(new PropertyValueFactory<>("areasEspecializacao"));

            // Configurar coluna de ações
            configurarColunaAcoes();

            // CONFIGURAÇÃO DE LARGURA DAS COLUNAS
            configurarLarguraColunas();

            log.info("Tabela configurada com sucesso");
        }
    }

    /**
     * CONFIGURA LARGURA DAS COLUNAS PARA MELHOR VISUALIZAÇÃO
     */
    private void configurarLarguraColunas() {
        colNome.setPrefWidth(150);
        colEmail.setPrefWidth(200);
        colCargo.setPrefWidth(120);
        colCurso.setPrefWidth(120);
        colFormacao.setPrefWidth(150);
        colEspecializacao.setPrefWidth(150);
        colAcoes.setPrefWidth(200); // AUMENTEI SIGNIFICATIVAMENTE PARA 200px
    }

    /**
     * Configura a coluna de ações com botões
     */
    private void configurarColunaAcoes() {
        Callback<TableColumn<ProfessorDAO.ProfessorOrientadorDTO, Void>, TableCell<ProfessorDAO.ProfessorOrientadorDTO, Void>> cellFactory =
                new Callback<>() {
                    @Override
                    public TableCell<ProfessorDAO.ProfessorOrientadorDTO, Void> call(final TableColumn<ProfessorDAO.ProfessorOrientadorDTO, Void> param) {
                        return new TableCell<>() {
                            private final Button btnEditar = new Button("Editar");
                            private final Button btnExcluir = new Button("Excluir");
                            private final HBox pane = new HBox(8, btnEditar, btnExcluir); // Aumentei o espaçamento

                            {
                                // BOTÕES BEM MAIORES PARA GARANTIR TEXTO COMPLETO - CORREÇÃO DO PROBLEMA 1
                                btnEditar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px; -fx-pref-width: 85px; -fx-pref-height: 30px; -fx-font-weight: bold;");
                                btnExcluir.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 12px; -fx-pref-width: 85px; -fx-pref-height: 30px; -fx-font-weight: bold;");

                                // Ação do botão Editar
                                btnEditar.setOnAction((ActionEvent event) -> {
                                    ProfessorDAO.ProfessorOrientadorDTO professor = getTableView().getItems().get(getIndex());
                                    editarProfessor(professor);
                                });

                                // Ação do botão Excluir
                                btnExcluir.setOnAction((ActionEvent event) -> {
                                    ProfessorDAO.ProfessorOrientadorDTO professor = getTableView().getItems().get(getIndex());
                                    excluirProfessor(professor);
                                });
                            }

                            @Override
                            public void updateItem(Void item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                } else {
                                    setGraphic(pane);
                                }
                            }
                        };
                    }
                };

        colAcoes.setCellFactory(cellFactory);
    }

    /**
     * Carrega os dados na tabela
     */
    private void carregarTabelaProfessores() {
        if (tableViewProfessores != null) {
            try {
                List<ProfessorDAO.ProfessorOrientadorDTO> professores = professorDAO.listarProfessoresOrientadores();
                tableViewProfessores.setItems(FXCollections.observableArrayList(professores));
                log.info("Tabela carregada com {} professores", professores.size());
            } catch (Exception e) {
                log.error("Erro ao carregar tabela de professores", e);
            }
        }
    }

    /**
     * Editar professor
     */
    private void editarProfessor(ProfessorDAO.ProfessorOrientadorDTO professor) {
        try {
            log.info("Editando professor: {}", professor.getEmail());

            // Guarda o email do professor que está sendo editado
            emailProfessorEmEdicao = professor.getEmail();

            // Preencher formulário com dados do professor
            txtNome.setText(professor.getNome());
            txtEmail.setText(professor.getEmail());
            txtCargo.setText(professor.getCargo());
            txtCurso.setText(professor.getCursoVinculado());
            txtFormacao.setText(professor.getFormacaoAcademica());
            txtEspecializacao.setText(professor.getAreasEspecializacao());

            // Desabilitar email (não pode alterar email)
            txtEmail.setDisable(true);

            // Mudar texto do botão para "Atualizar"
            btnSalvar.setText("Atualizar");

            mostrarMensagemSucesso("Preencha os campos e clique em 'Atualizar' para salvar as alterações.");

        } catch (Exception e) {
            log.error("Erro ao editar professor", e);
            mostrarErro("Erro ao carregar dados do professor: " + e.getMessage());
        }
    }

    /**
     * Excluir professor
     */
    private void excluirProfessor(ProfessorDAO.ProfessorOrientadorDTO professor) {
        try {
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle("Confirmar Exclusão");
            confirmacao.setHeaderText("Excluir Professor");
            confirmacao.setContentText("Tem certeza que deseja excluir o professor " + professor.getNome() + "?\nEsta ação não pode ser desfeita.");

            Optional<ButtonType> resultado = confirmacao.showAndWait();

            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                log.info("Excluindo professor: {}", professor.getEmail());

                // Exclui do banco de dados
                boolean exclusaoSucesso = professorDAO.excluirProfessor(professor.getEmail());

                if (exclusaoSucesso) {
                    // Remove da tabela visualmente
                    tableViewProfessores.getItems().remove(professor);
                    mostrarMensagemSucesso("Professor " + professor.getNome() + " excluído com sucesso!");
                    log.info("Professor {} excluído do banco de dados", professor.getEmail());
                } else {
                    mostrarErro("Erro ao excluir professor do banco de dados.");
                    log.error("Falha ao excluir professor {} do banco", professor.getEmail());
                }
            }

        } catch (Exception e) {
            log.error("Erro ao excluir professor", e);
            mostrarErro("Erro ao excluir professor: " + e.getMessage());
        }
    }

    @FXML
    private void salvarProfessor(ActionEvent event) {
        try {
            // Verificar se é edição ou novo cadastro
            boolean isEdicao = txtEmail.isDisable();

            if (isEdicao) {
                atualizarProfessor(event);
            } else {
                cadastrarNovoProfessor(event);
            }

        } catch (Exception e) {
            log.error("Erro ao salvar professor", e);
            mostrarErro("Erro ao salvar professor: " + e.getMessage());
        }
    }

    /**
     * Cadastrar novo professor
     */
    private void cadastrarNovoProfessor(ActionEvent event) {
        log.info("Iniciando cadastro de novo professor...");

        // 1. Validar campos obrigatórios
        if (!validarCampos()) {
            mostrarErro("Preencha todos os campos obrigatórios!");
            return;
        }

        // 2. Validar email único
        if (professorDAO.emailExiste(txtEmail.getText().trim())) {
            mostrarErro("Este email já está cadastrado no sistema!");
            return;
        }

        // 3. Gerar senha temporária
        String senhaTemporaria = gerarSenhaTemporaria();
        log.info("Senha temporária gerada: {}", senhaTemporaria);

        // 4. Criptografar senha
        String senhaHash = BCrypt.hashpw(senhaTemporaria, BCrypt.gensalt());

        // 5. Salvar no banco de dados
        boolean sucesso = professorDAO.salvarProfessorOrientador(
                txtEmail.getText().trim(),
                txtNome.getText().trim(),
                senhaHash,
                "ORIENTADOR",
                txtCargo.getText().trim(),
                txtCurso.getText().trim(),
                txtFormacao.getText().trim(),
                txtEspecializacao.getText().trim()
        );

        if (sucesso) {
            // 6. Enviar email (simulado por enquanto)
            enviarEmailSenhaTemporaria(senhaTemporaria);

            // 7. Limpar formulário e mostrar sucesso
            limparFormulario();

            // 8. ATUALIZAR A TABELA - AGORA FUNCIONANDO
            carregarTabelaProfessores();

            mostrarMensagemSucesso("Professor cadastrado com sucesso! Email enviado com a senha temporária.");

            log.info("Professor salvo no banco com sucesso!");
        } else {
            mostrarErro("Erro ao salvar professor no banco de dados.");
        }
    }

    /**
     * Atualizar professor existente - CORREÇÃO DO PROBLEMA 2
     */
    private void atualizarProfessor(ActionEvent event) {
        log.info("Atualizando professor...");

        // 1. Validar campos obrigatórios
        if (!validarCampos()) {
            mostrarErro("Preencha todos os campos obrigatórios!");
            return;
        }

        try {
            // SIMULAÇÃO DE ATUALIZAÇÃO - POR ENQUANTO SÓ ATUALIZA VISUALMENTE

            // Encontra o professor na tabela e atualiza os dados
            for (ProfessorDAO.ProfessorOrientadorDTO professor : tableViewProfessores.getItems()) {
                if (professor.getEmail().equals(emailProfessorEmEdicao)) {
                    // Cria um novo DTO com os dados atualizados
                    ProfessorDAO.ProfessorOrientadorDTO professorAtualizado =
                            new ProfessorDAO.ProfessorOrientadorDTO(
                                    professor.getEmail(), // Email não muda
                                    txtNome.getText().trim(),
                                    txtCargo.getText().trim(),
                                    txtCurso.getText().trim(),
                                    txtFormacao.getText().trim(),
                                    txtEspecializacao.getText().trim()
                            );

                    // Substitui o item na tabela
                    int index = tableViewProfessores.getItems().indexOf(professor);
                    tableViewProfessores.getItems().set(index, professorAtualizado);
                    break;
                }
            }

            // 2. Reabilitar campo email
            txtEmail.setDisable(false);

            // 3. Restaurar texto do botão
            btnSalvar.setText("Salvar");

            // 4. Limpar formulário
            limparFormulario();

            // 5. Forçar refresh da tabela
            tableViewProfessores.refresh();

            mostrarMensagemSucesso("Dados do professor atualizados com sucesso! (Atualização visual)");
            log.info("Professor atualizado visualmente - aguardando implementação do banco");

        } catch (Exception e) {
            log.error("Erro ao atualizar professor", e);
            mostrarErro("Erro ao atualizar professor: " + e.getMessage());
        }
    }

    /**
     * Valida se todos os campos obrigatórios estão preenchidos
     */
    private boolean validarCampos() {
        return txtNome.getText() != null && !txtNome.getText().trim().isEmpty() &&
                txtEmail.getText() != null && !txtEmail.getText().trim().isEmpty() &&
                txtCargo.getText() != null && !txtCargo.getText().trim().isEmpty() &&
                txtCurso.getText() != null && !txtCurso.getText().trim().isEmpty();
    }

    /**
     * Gera uma senha temporária aleatória
     */
    private String gerarSenhaTemporaria() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder senha = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            senha.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }

        return senha.toString();
    }

    /**
     * Envia email com a senha temporária (simulado por enquanto)
     */
    private void enviarEmailSenhaTemporaria(String senhaTemporaria) {
        // TODO: Implementar envio de email real
        log.info("=== EMAIL SIMULADO ===");
        log.info("Para: {}", txtEmail.getText());
        log.info("Assunto: Cadastro no Sistema BlueTech");
        log.info("Mensagem: Olá {}, você foi cadastrado como Professor Orientador. Sua senha temporária é: {}. Por favor, altere-a no primeiro acesso.",
                txtNome.getText(), senhaTemporaria);
        log.info("=== FIM EMAIL ===");
    }

    /**
     * Limpa o formulário após salvar
     */
    private void limparFormulario() {
        txtNome.clear();
        txtEmail.clear();
        txtCargo.clear();
        txtCurso.clear();
        txtFormacao.clear();
        txtEspecializacao.clear();
        txtEmail.setDisable(false);
        btnSalvar.setText("Salvar");
        emailProfessorEmEdicao = null;
    }

    /**
     * Mostra mensagem de sucesso
     */
    private void mostrarMensagemSucesso(String mensagem) {
        log.info("✅ SUCESSO: {}", mensagem);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    /**
     * Mostra mensagem de erro
     */
    private void mostrarErro(String mensagem) {
        log.error("❌ ERRO: {}", mensagem);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    @FXML
    private void cancelar(ActionEvent event) {
        limparFormulario();
        log.info("Formulário cancelado - campos limpos");
    }
}