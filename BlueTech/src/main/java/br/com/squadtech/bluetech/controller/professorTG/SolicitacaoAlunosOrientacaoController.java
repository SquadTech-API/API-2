package br.com.squadtech.bluetech.controller.professorTG;

import br.com.squadtech.bluetech.config.ConnectionFactory;
import br.com.squadtech.bluetech.config.SmtpProps;
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
import br.com.squadtech.bluetech.service.EmailService;
import br.com.squadtech.bluetech.util.FlipTransitionUtil;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.net.URL;
import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class SolicitacaoAlunosOrientacaoController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnAtualizarLista;

    @FXML
    private FlowPane flowCardsSolicitacoes;

    private final SolicitacaoDAO solicitacaoDAO = new SolicitacaoDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final ProfessorDAO professorDAO = new ProfessorDAO();
    private final PerfilAlunoDAO perfilAlunoDAO = new PerfilAlunoDAO();
    private final OrientaDAO orientaDAO = new OrientaDAO();

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    void atualizarSolicitacoes(ActionEvent event) {
        carregarSolicitacoes();
    }

    @FXML
    void initialize() {
        assert btnAtualizarLista != null : "fx:id=\"btnAtualizarLista\" was not injected: check your FXML file 'SolicitacaoAlunosOrientacao.fxml'.";
        assert flowCardsSolicitacoes != null : "fx:id=\"flowCardsSolicitacoes\" was not injected: check your FXML file 'SolicitacaoAlunosOrientacao.fxml'.";
        carregarSolicitacoes();
    }

    private void carregarSolicitacoes() {
        flowCardsSolicitacoes.getChildren().clear();
        Usuario profUser = SessaoUsuario.getUsuarioLogado();
        if (profUser == null) return;
        Professor prof = professorDAO.findByUsuarioEmail(profUser.getEmail());
        if (prof == null) return;

        List<Solicitacao> lista = solicitacaoDAO.listByProfessor(prof.getId());
        for (Solicitacao s : lista) {
            flowCardsSolicitacoes.getChildren().add(buildCard(prof, s));
        }
    }

    private BorderPane buildCard(Professor prof, Solicitacao s) {
        VBox frontContent = new VBox(6);
        frontContent.getStyleClass().addAll("card-solicitacao-recebida", "card-flip-front");
        Label titulo = new Label("Pedido de orientação");
        titulo.getStyleClass().add("card-title");
        Label data = new Label("Enviado: " + (s.getDataEnvio() != null ? DT.format(s.getDataEnvio()) : "-"));
        Label alunoInfo = new Label(buildAlunoInfo(s.getAlunoEmail()));
        Label status = new Label("Status: " + s.getStatus());
        String statusClass = mapStatusClass(s.getStatus());
        if (statusClass != null) status.getStyleClass().add(statusClass);
        frontContent.getChildren().addAll(titulo, data, alunoInfo, status);

        Button trashFront = buildTrashButton();
        trashFront.getStyleClass().add("icon-button-ghost");
        trashFront.setVisible("RECUSADO".equalsIgnoreCase(s.getStatus()));
        AnchorPane overlay = new AnchorPane(trashFront);
        AnchorPane.setTopAnchor(trashFront, 4.0);
        AnchorPane.setRightAnchor(trashFront, 4.0);
        StackPane front = new StackPane(frontContent, overlay);

        VBox back = new VBox(8);
        back.getStyleClass().addAll("card-solicitacao-recebida", "card-flip-back");
        Label msg = new Label(s.getMensagem());
        msg.setWrapText(true);
        HBox actions = new HBox(8);
        actions.getStyleClass().add("card-actions");
        Button aceitar = new Button("Aceitar");
        Button recusar = new Button("Recusar");
        Button excluir = buildTrashButton();
        actions.getChildren().addAll(aceitar, recusar, excluir);
        back.getChildren().addAll(new Label("Pedido:"), msg, actions);

        boolean pendente = "AGUARDANDO".equalsIgnoreCase(s.getStatus());
        aceitar.setDisable(!pendente);
        recusar.setDisable(!pendente);
        excluir.setVisible("RECUSADO".equalsIgnoreCase(s.getStatus()));

        BorderPane container = new BorderPane(front);
        container.setOnMouseClicked(e -> {
            FlipTransitionUtil.flip(container, () -> {
                container.setCenter(container.getCenter() == front ? back : front);
            });
        });

        aceitar.setOnAction(e -> aceitarSolicitacao(prof, s, container, new VBox(frontContent), back));
        recusar.setOnAction(e -> recusarSolicitacao(s, container, new VBox(frontContent)));
        trashFront.setOnAction(e -> {
            e.consume();
            if (!confirmDelete()) return;
            solicitacaoDAO.deleteById(s.getId());
            animateRemoval(container, () -> flowCardsSolicitacoes.getChildren().remove(container));
            toast("Solicitação excluída.");
        });
        excluir.setOnAction(e -> {
            if (!confirmDelete()) return;
            solicitacaoDAO.deleteById(s.getId());
            animateRemoval(container, () -> flowCardsSolicitacoes.getChildren().remove(container));
            toast("Solicitação excluída.");
        });

        return container;
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

    private String buildAlunoInfo(String alunoEmail) {
        Usuario aluno = usuarioDAO.findByEmail(alunoEmail);
        String nome = aluno != null ? aluno.getNome() : alunoEmail;
        return "Aluno: " + nome + " (" + alunoEmail + ")";
    }

    private void aceitarSolicitacao(Professor prof, Solicitacao s, BorderPane container, VBox front, VBox back) {
        PerfilAluno perfil = perfilAlunoDAO.getPerfilByEmail(s.getAlunoEmail());
        if (perfil == null) {
            toast("O aluno precisa completar o Perfil antes de aceitar a orientação.");
            return;
        }
        boolean jaTemAtivo = orientaDAO.findByAlunoId(Long.valueOf(perfil.getIdPerfilAluno()))
                .stream().anyMatch(Orienta::isAtivo);
        if (jaTemAtivo) {
            solicitacaoDAO.atualizarStatus(s.getId(), "RECUSADO");
            s.setStatus("RECUSADO");
            updateFrontAfterDecision(front, s);
            container.setCenter(front);
            toast("Aluno já possui orientador ativo. Solicitação recusada.");
            return;
        }

        solicitacaoDAO.atualizarStatus(s.getId(), "ACEITO");
        s.setStatus("ACEITO");
        Orienta o = new Orienta(prof.getId(), Long.valueOf(perfil.getIdPerfilAluno()));
        orientaDAO.insert(o);

        updateFrontAfterDecision(front, s);
        if (container.getCenter() == back) {
            FlipTransitionUtil.flip(container, () -> container.setCenter(front));
        } else {
            container.setCenter(front);
        }
        toast("Solicitação aceita.");

        try (Connection c = ConnectionFactory.getConnection()) {
            EmailService email = new EmailService(SmtpProps.FROM, new EmailService.SmtpConfig(SmtpProps.HOST, SmtpProps.PORT, SmtpProps.USER, SmtpProps.PASS, SmtpProps.STARTTLS, SmtpProps.SSL));
            Usuario aluno = usuarioDAO.findByEmail(s.getAlunoEmail());
            Usuario profUser = usuarioDAO.findByEmail(prof.getUsuarioEmail());
            String toAluno = aluno != null ? aluno.getEmail() : null;
            String toProf  = profUser != null ? profUser.getEmail() : null;
            if (toAluno != null) {
                email.send(toAluno, "Sua solicitação de orientação foi aceita", "Olá, %s!\n\nSua solicitação de orientação foi aceita por %s.\n\n— BlueTech".formatted(aluno.getNome(), profUser != null ? profUser.getNome() : "seu orientador"));
            }
            if (toProf != null) {
                email.send(toProf, "Você aceitou uma orientação", "Olá, %s!\n\nVocê aceitou orientar o aluno %s.\n\n— BlueTech".formatted(profUser.getNome(), aluno != null ? aluno.getNome() : s.getAlunoEmail()));
            }
        } catch (Exception ex) { }
    }

    private void recusarSolicitacao(Solicitacao s, BorderPane container, VBox front) {
        solicitacaoDAO.atualizarStatus(s.getId(), "RECUSADO");
        s.setStatus("RECUSADO");
        updateFrontAfterDecision(front, s);
        FlipTransitionUtil.flip(container, () -> container.setCenter(front));
        toast("Solicitação recusada.");

        try (Connection c = ConnectionFactory.getConnection()) {
            EmailService email = new EmailService(SmtpProps.FROM, new EmailService.SmtpConfig(SmtpProps.HOST, SmtpProps.PORT, SmtpProps.USER, SmtpProps.PASS, SmtpProps.STARTTLS, SmtpProps.SSL));
            Usuario aluno = usuarioDAO.findByEmail(s.getAlunoEmail());
            if (aluno != null) {
                email.send(aluno.getEmail(), "Sua solicitação de orientação foi recusada", "Olá, %s!\n\nSua solicitação de orientação foi recusada. Você pode tentar novamente com outro professor.\n\n— BlueTech".formatted(aluno.getNome()));
            }
        } catch (Exception ignored) { }
    }

    private void updateFrontAfterDecision(VBox front, Solicitacao s) {
        // simples: substitui último label (status)
        front.getChildren().removeIf(n -> n instanceof Label lbl && lbl.getText().startsWith("Status:"));
        Label novo = new Label("Status: " + s.getStatus());
        String statusClass = mapStatusClass(s.getStatus());
        if (statusClass != null) novo.getStyleClass().add(statusClass);
        front.getChildren().add(novo);
    }

    private void toast(String msg) {
        Scene sc = btnAtualizarLista.getScene();
        if (sc != null) Toast.show(sc, msg);
    }
}
