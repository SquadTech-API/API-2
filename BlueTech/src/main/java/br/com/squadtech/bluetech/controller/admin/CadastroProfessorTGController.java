package br.com.squadtech.bluetech.controller.admin;

import br.com.squadtech.bluetech.dao.ProfessorTGDAO;
import br.com.squadtech.bluetech.model.ProfessorTG;
import com.jfoenix.controls.JFXButton;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.util.Optional;

public class CadastroProfessorTGController {

    @FXML private TextField txtNome; // NOVO CAMPO
    @FXML private TextField txtEmail;
    @FXML private TextField txtCargo;
    @FXML private ComboBox<String> comboTipoTG;
    @FXML private TextField txtCursoVinculado;
    @FXML private TextArea txtFormacaoAcademica;
    @FXML private TextArea txtAreasEspecializacao;

    @FXML private TableView<ProfessorTG> tableViewProfessores;
    @FXML private TableColumn<ProfessorTG, String> colNome;
    @FXML private TableColumn<ProfessorTG, String> colEmail;
    @FXML private TableColumn<ProfessorTG, String> colCargo;
    @FXML private TableColumn<ProfessorTG, String> colTipoTG;
    @FXML private TableColumn<ProfessorTG, String> colAcoes;

    private ProfessorTGDAO professorTGDAO;
    private ObservableList<ProfessorTG> listaProfessores;
    private ProfessorTG professorEmEdicao;

    @FXML
    void initialize() {
        professorTGDAO = new ProfessorTGDAO();
        listaProfessores = FXCollections.observableArrayList();

        // Configurar ComboBox
        comboTipoTG.getItems().addAll("TG1", "TG2", "AMBOS", "NENHUM");

        // Configurar colunas da tabela
        configurarTabela();

        // Carregar dados
        carregarProfessores();

        System.out.println("CadastroProfessorTGController inicializado!");
    }

    private void configurarTabela() {
        // Configurar colunas de dados
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCargo.setCellValueFactory(new PropertyValueFactory<>("cargo"));
        colTipoTG.setCellValueFactory(new PropertyValueFactory<>("tipoTG"));

        // Configurar coluna de ações (Editar/Excluir)
        colAcoes.setCellValueFactory(cellData -> new SimpleStringProperty(""));
        colAcoes.setCellFactory(col -> new TableCell<ProfessorTG, String>() {
            private final HBox container = new HBox(5);
            private final JFXButton btnEditar = new JFXButton("Editar");
            private final JFXButton btnExcluir = new JFXButton("Excluir");

            {
                btnEditar.getStyleClass().add("button-small");
                btnExcluir.getStyleClass().add("button-danger-small");

                btnEditar.setOnAction(event -> {
                    ProfessorTG professor = getTableView().getItems().get(getIndex());
                    editarProfessor(professor);
                });

                btnExcluir.setOnAction(event -> {
                    ProfessorTG professor = getTableView().getItems().get(getIndex());
                    excluirProfessor(professor);
                });

                container.getChildren().addAll(btnEditar, btnExcluir);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

        tableViewProfessores.setItems(listaProfessores);
    }

    private void carregarProfessores() {
        listaProfessores.clear();
        listaProfessores.addAll(professorTGDAO.listarTodos());
    }

    @FXML
    private void salvarProfessor() {
        if (validarCampos()) {
            ProfessorTG professor;

            if (professorEmEdicao != null) {
                // Modo edição
                professor = professorEmEdicao;
            } else {
                // Modo cadastro
                professor = new ProfessorTG();
            }

            // Preencher dados do professor
            professor.setNome(txtNome.getText());
            professor.setEmail(txtEmail.getText());
            professor.setCargo(txtCargo.getText());
            professor.setTipoTG(comboTipoTG.getValue());
            professor.setCursoVinculado(txtCursoVinculado.getText());
            professor.setFormacaoAcademica(txtFormacaoAcademica.getText());
            professor.setAreasEspecializacao(txtAreasEspecializacao.getText());

            if (professorTGDAO.salvar(professor)) {
                mostrarAlerta("Sucesso",
                        professorEmEdicao != null ? "Professor atualizado com sucesso!" : "Professor cadastrado com sucesso!",
                        Alert.AlertType.INFORMATION);

                limparCampos();
                carregarProfessores();
            } else {
                mostrarAlerta("Erro", "Erro ao salvar professor!", Alert.AlertType.ERROR);
            }
        }
    }

    private void editarProfessor(ProfessorTG professor) {
        professorEmEdicao = professor;

        // Preencher campos com dados do professor
        txtNome.setText(professor.getNome());
        txtEmail.setText(professor.getEmail());
        txtCargo.setText(professor.getCargo());
        comboTipoTG.setValue(professor.getTipoTG());
        txtCursoVinculado.setText(professor.getCursoVinculado());
        txtFormacaoAcademica.setText(professor.getFormacaoAcademica());
        txtAreasEspecializacao.setText(professor.getAreasEspecializacao());
    }

    private void excluirProfessor(ProfessorTG professor) {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmação");
        confirmacao.setHeaderText("Excluir Professor");
        confirmacao.setContentText("Tem certeza que deseja excluir o professor " + professor.getNome() + "?");

        Optional<ButtonType> resultado = confirmacao.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            if (professorTGDAO.excluir(professor.getId())) {
                mostrarAlerta("Sucesso", "Professor excluído com sucesso!", Alert.AlertType.INFORMATION);
                carregarProfessores();
            } else {
                mostrarAlerta("Erro", "Erro ao excluir professor!", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void limparCampos() {
        txtNome.clear();
        txtEmail.clear();
        txtCargo.clear();
        comboTipoTG.getSelectionModel().clearSelection();
        txtCursoVinculado.clear();
        txtFormacaoAcademica.clear();
        txtAreasEspecializacao.clear();
        professorEmEdicao = null;
    }

    private boolean validarCampos() {
        if (txtNome.getText().isEmpty() ||
                txtEmail.getText().isEmpty() ||
                txtCargo.getText().isEmpty() ||
                comboTipoTG.getValue() == null) {

            mostrarAlerta("Validação", "Preencha todos os campos obrigatórios!", Alert.AlertType.WARNING);
            return false;
        }

        // Validação básica de email
        if (!txtEmail.getText().contains("@")) {
            mostrarAlerta("Validação", "Informe um e-mail válido!", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}