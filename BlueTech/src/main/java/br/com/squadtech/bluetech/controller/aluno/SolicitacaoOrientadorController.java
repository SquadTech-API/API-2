package br.com.squadtech.bluetech.controller.aluno;

import br.com.squadtech.bluetech.dao.OrientaDAO;
import br.com.squadtech.bluetech.dao.PerfilAlunoDAO;
import br.com.squadtech.bluetech.dao.ProfessorDAO;
import br.com.squadtech.bluetech.dao.SolicitacaoDAO;
import br.com.squadtech.bluetech.dao.UsuarioDAO;
import br.com.squadtech.bluetech.model.Orienta;
import br.com.squadtech.bluetech.model.PerfilAluno;
import br.com.squadtech.bluetech.model.Professor;
import br.com.squadtech.bluetech.model.SessaoUsuario;
import br.com.squadtech.bluetech.model.Solicitacao;
import br.com.squadtech.bluetech.model.Usuario;
import br.com.squadtech.bluetech.util.Toast;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class SolicitacaoOrientadorController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnSolicitarOrientadorCancelar;

    @FXML
    private Button btnSolicitarOrientadorEnviarSolicitacao;

    @FXML
    private ChoiceBox<ProfessorOption> choiceSolicitacaoOrientadorLista;

    @FXML
    private ScrollPane exibirSolicitacaoOrientador;

    @FXML
    private FlowPane flowCardsSolicitacoes;

    @FXML
    private TextArea txtSolicitacaoOrientadorPedido;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final ProfessorDAO professorDAO = new ProfessorDAO();
    private final SolicitacaoDAO solicitacaoDAO = new SolicitacaoDAO();
    private final PerfilAlunoDAO perfilAlunoDAO = new PerfilAlunoDAO();
    private final OrientaDAO orientaDAO = new OrientaDAO();

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    void cancelarSolicitacao(ActionEvent event) {
        txtSolicitacaoOrientadorPedido.clear();
        choiceSolicitacaoOrientadorLista.getSelectionModel().clearSelection();
    }

    @FXML
    void enviarSolicitacao(ActionEvent event) {
        Usuario aluno = SessaoUsuario.getUsuarioLogado();
        if (aluno == null) {
            showToast("Sessão expirada. Faça login novamente.");
            return;
        }

        // Bloqueia se já possui orientador ativo
        PerfilAluno perfil = perfilAlunoDAO.getPerfilByEmail(aluno.getEmail());
        if (perfil != null) {
            boolean temAtivo = orientaDAO.findByAlunoId(Long.valueOf(perfil.getIdPerfilAluno()))
                    .stream().anyMatch(Orienta::isAtivo);
            if (temAtivo) {
                showToast("Você já possui um orientador ativo.");
                disableInputs(true);
                return;
            }
        }

        // Impede múltiplas solicitações pendentes
        Solicitacao pendente = solicitacaoDAO.findPendingByAlunoEmail(aluno.getEmail());
        if (pendente != null) {
            showToast("Você já possui uma solicitação pendente.");
            disableInputs(true);
            return;
        }

        ProfessorOption opt = choiceSolicitacaoOrientadorLista.getValue();
        String mensagem = txtSolicitacaoOrientadorPedido.getText();

        if (opt == null) {
            showToast("Selecione um professor.");
            return;
        }
        if (mensagem == null || mensagem.isBlank()) {
            showToast("Escreva sua apresentação e pedido.");
            return;
        }

        Solicitacao s = new Solicitacao(aluno.getEmail(), opt.professorId, mensagem.trim());
        solicitacaoDAO.insert(s);
        showToast("Solicitação enviada para " + opt.nome + ".");

        // Atualiza UI
        txtSolicitacaoOrientadorPedido.clear();
        choiceSolicitacaoOrientadorLista.getSelectionModel().clearSelection();
        refreshCard();
    }

    @FXML
    void initialize() {
        assert btnSolicitarOrientadorCancelar != null : "fx:id=\"btnSolicitarOrientadorCancelar\" was not injected: check your FXML file 'SolicitacaoOrientador.fxml'.";
        assert btnSolicitarOrientadorEnviarSolicitacao != null : "fx:id=\"btnSolicitarOrientadorEnviarSolicitacao\" was not injected: check your FXML file 'SolicitacaoOrientador.fxml'.";
        assert choiceSolicitacaoOrientadorLista != null : "fx:id=\"choiceSolicitacaoOrientadorLista\" was not injected: check your FXML file 'SolicitacaoOrientador.fxml'.";
        assert exibirSolicitacaoOrientador != null : "fx:id=\"exibirSolicitacaoOrientador\" was not injected: check your FXML file 'SolicitacaoOrientador.fxml'.";
        assert flowCardsSolicitacoes != null : "fx:id=\"flowCardsSolicitacoes\" was not injected: check your FXML file 'SolicitacaoOrientador.fxml'.";
        assert txtSolicitacaoOrientadorPedido != null : "fx:id=\"txtSolicitacaoOrientadorPedido\" was not injected: check your FXML file 'SolicitacaoOrientador.fxml'.";

        loadProfessoresElegiveis();
        refreshCard();
    }

    private void loadProfessoresElegiveis() {
        // Busca todos os usuários aptos (ORIENTADOR ou PROF_TG)
        List<Usuario> usuariosProf = usuarioDAO.listarPorTipos("ORIENTADOR", "PROF_TG");
        List<ProfessorOption> opcoes = new ArrayList<>();

        for (Usuario u : usuariosProf) {
            // Garante que exista um registro na tabela professor (FK para futuras ações)
            Professor prof = professorDAO.findByUsuarioEmail(u.getEmail());
            if (prof == null) {
                // cria um professor básico, tipo_tg NENHUM por padrão
                Professor novo = new Professor(u.getEmail(), null, "NENHUM");
                professorDAO.inserirProfessor(novo);
                prof = professorDAO.findByUsuarioEmail(u.getEmail());
            }
            if (prof != null && prof.getId() != null) {
                opcoes.add(new ProfessorOption(prof.getId(), u.getNome(), u.getEmail()));
            }
        }
        // Ordena por nome
        opcoes.sort((a,b) -> a.nome.compareToIgnoreCase(b.nome));
        choiceSolicitacaoOrientadorLista.getItems().setAll(opcoes);
    }

    private void refreshCard() {
        flowCardsSolicitacoes.getChildren().clear();
        Usuario aluno = SessaoUsuario.getUsuarioLogado();
        if (aluno == null) return;

        // Checa vínculo ativo e bloqueia inputs
        boolean ativo = false;
        PerfilAluno perfil = perfilAlunoDAO.getPerfilByEmail(aluno.getEmail());
        if (perfil != null) {
            ativo = orientaDAO.findByAlunoId(Long.valueOf(perfil.getIdPerfilAluno()))
                    .stream().anyMatch(Orienta::isAtivo);
        }

        Solicitacao ultima = solicitacaoDAO.findLatestByAlunoEmail(aluno.getEmail());
        if (ultima != null) {
            VBox cardContent = new VBox(6);
            cardContent.getStyleClass().add("card-solicitacao");
            Label titulo = new Label("Solicitação "+statusLabel(ultima.getStatus()));
            titulo.getStyleClass().add("card-title");

            Usuario profUser = null;
            Professor prof = professorDAO.findById(ultima.getProfessorId());
            if (prof != null) profUser = usuarioDAO.findByEmail(prof.getUsuarioEmail());

            String profNome = profUser != null ? profUser.getNome() : ("#"+ultima.getProfessorId());
            String profEmail = profUser != null ? profUser.getEmail() : "";

            Label data = new Label("Data do pedido: " + (ultima.getDataEnvio() != null ? DT.format(ultima.getDataEnvio()) : "-"));
            Label profInfo = new Label("Orientador: " + profNome + " (" + profEmail + ")");
            profInfo.getStyleClass().add("label-professor");
            Label status = new Label("Status: " + ultima.getStatus());
            String statusClass = mapStatusClass(ultima.getStatus());
            if (statusClass != null) status.getStyleClass().add(statusClass);

            HBox actions = new HBox(8);
            Button excluir = buildTrashButton();
            excluir.setVisible("RECUSADO".equalsIgnoreCase(ultima.getStatus()));
            actions.getChildren().add(excluir);

            cardContent.getChildren().addAll(titulo, data, profInfo, status, actions);

            // Overlay do ícone no canto superior direito
            Button trashFront = buildTrashButton();
            trashFront.getStyleClass().add("icon-button-ghost");
            trashFront.setVisible("RECUSADO".equalsIgnoreCase(ultima.getStatus()));
            AnchorPane overlay = new AnchorPane(trashFront);
            AnchorPane.setTopAnchor(trashFront, 4.0);
            AnchorPane.setRightAnchor(trashFront, 4.0);
            StackPane card = new StackPane(cardContent, overlay);

            flowCardsSolicitacoes.getChildren().add(card);

            excluir.setOnAction(e -> {
                if (!confirmDelete()) return;
                solicitacaoDAO.deleteById(ultima.getId());
                animateRemoval(card, () -> flowCardsSolicitacoes.getChildren().remove(card));
                disableInputs(false);
                showToast("Solicitação excluída.");
            });
            trashFront.setOnAction(e -> {
                e.consume();
                if (!confirmDelete()) return;
                solicitacaoDAO.deleteById(ultima.getId());
                animateRemoval(card, () -> flowCardsSolicitacoes.getChildren().remove(card));
                disableInputs(false);
                showToast("Solicitação excluída.");
            });
        }

        boolean pendente = ultima != null && "AGUARDANDO".equalsIgnoreCase(ultima.getStatus());
        disableInputs(pendente || ativo);
    }

    private Button buildTrashButton() {
        Button b = new Button();
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        icon.setFill(Color.web("#e74c3c"));
        icon.setGlyphSize(16);
        b.setGraphic(icon);
        b.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
        b.setTooltip(new javafx.scene.control.Tooltip("Excluir"));
        b.getStyleClass().add("button-secondary");
        return b;
    }

    private boolean confirmDelete() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Deseja excluir esta solicitação?", ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null);
        a.setTitle("Confirmação");
        a.getDialogPane().getStylesheets().add(getClass().getResource("/css/Style.css").toExternalForm());
        return a.showAndWait().filter(bt -> bt == ButtonType.YES).isPresent();
    }

    private void animateRemoval(javafx.scene.Node node, Runnable onFinished) {
        FadeTransition fade = new FadeTransition(Duration.millis(200), node);
        fade.setToValue(0.0);
        TranslateTransition slide = new TranslateTransition(Duration.millis(200), node);
        slide.setByY(-10);
        ParallelTransition pt = new ParallelTransition(fade, slide);
        pt.setOnFinished(e -> { if (onFinished != null) onFinished.run(); });
        pt.play();
    }

    private String mapStatusClass(String status) {
        if (status == null) return null;
        return switch (status.toUpperCase()) {
            case "AGUARDANDO" -> "status-aguardando";
            case "ACEITO" -> "status-aceito";
            case "RECUSADO" -> "status-recusado";
            default -> null;
        };
    }

    private void disableInputs(boolean disable) {
        btnSolicitarOrientadorEnviarSolicitacao.setDisable(disable);
        choiceSolicitacaoOrientadorLista.setDisable(disable);
        txtSolicitacaoOrientadorPedido.setDisable(disable);
    }

    private String statusLabel(String status) {
        if (status == null) return "";
        return switch (status.toUpperCase()) {
            case "AGUARDANDO" -> "(Aguardando)";
            case "ACEITO" -> "(Aceito)";
            case "RECUSADO" -> "(Recusado)";
            default -> "";
        };
    }

    private void showToast(String msg) {
        Scene scene = btnSolicitarOrientadorEnviarSolicitacao.getScene();
        if (scene != null) Toast.show(scene, msg);
    }

    // Wrapper para exibir no ChoiceBox
    public static class ProfessorOption {
        public final Long professorId;
        public final String nome;
        public final String email;
        public ProfessorOption(Long professorId, String nome, String email) {
            this.professorId = professorId;
            this.nome = nome;
            this.email = email;
        }
        @Override public String toString() { return nome + " (" + email + ")"; }
    }
}
