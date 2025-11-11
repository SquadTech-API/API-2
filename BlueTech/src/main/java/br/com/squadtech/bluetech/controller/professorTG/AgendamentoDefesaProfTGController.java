package br.com.squadtech.bluetech.controller.professorTG;

import br.com.squadtech.bluetech.dao.AgendamentoDefesaDAO;
import br.com.squadtech.bluetech.dao.ProfessorDAO;
import br.com.squadtech.bluetech.dao.OrientaDAO;
import br.com.squadtech.bluetech.model.*;
import br.com.squadtech.bluetech.service.EmailService;
import br.com.squadtech.bluetech.config.SmtpProps;
import br.com.squadtech.bluetech.util.FlipTransitionUtil;
import br.com.squadtech.bluetech.util.Toast;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.scene.Node;

/** Controller semelhante ao orientador, para professor TG. */
public class AgendamentoDefesaProfTGController {

    @FXML private FlowPane flowCards;

    private final AgendamentoDefesaDAO agDAO = new AgendamentoDefesaDAO();
    private final ProfessorDAO professorDAO = new ProfessorDAO();
    private final OrientaDAO orientaDAO = new OrientaDAO();

    private static final java.time.format.DateTimeFormatter DF = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final java.time.format.DateTimeFormatter TF = java.time.format.DateTimeFormatter.ofPattern("HH:mm");

    @FXML void initialize(){ agDAO.createTableIfNotExists(); refresh(); }

    private void refresh(){
        flowCards.getChildren().clear();
        Usuario profUser = SessaoUsuario.getUsuarioLogado(); if (profUser==null) return;
        Professor prof = professorDAO.findByUsuarioEmail(profUser.getEmail()); if (prof==null) return;
        var orientacoes = orientaDAO.findByProfessorId(prof.getId()).stream().filter(Orienta::isAtivo).collect(Collectors.toSet());
        Set<String> emailsVinculados = orientacoes.stream()
                .map(o -> new br.com.squadtech.bluetech.dao.PerfilAlunoDAO().getEmailByPerfilId(o.getAlunoId().intValue()))
                .filter(e -> e != null && !e.isBlank()).collect(Collectors.toSet());
        var lista = agDAO.listByProfessor(prof.getId()).stream()
                .filter(a -> emailsVinculados.contains(a.getAlunoEmail()))
                .toList();
        for (AgendamentoDefesa a: lista) flowCards.getChildren().add(buildCard(a));
    }

    private StackPane buildCard(AgendamentoDefesa a){
        VBox front = new VBox(6); front.getStyleClass().add("card-solicitacao-recebida");
        Label lAluno = new Label("Aluno: "+a.getAlunoEmail());
        Label lOri = new Label("Orientador: "+SessaoUsuario.getUsuarioLogado().getEmail());
        Label lStatus = new Label("Status: "+a.getStatus());
        String st = mapStatusClass(a.getStatus()); if (st!=null) lStatus.getStyleClass().add(st);
        front.getChildren().addAll(lAluno, lOri, lStatus);
        Button flip = new Button("Responder / Detalhes"); front.getChildren().add(flip);

        // ícone de exclusão (reposicionado para não bloquear cliques)
        MaterialIconView delIcon = new MaterialIconView(MaterialIcon.DELETE);
        delIcon.setFill(Color.web("#e53935")); delIcon.setGlyphSize(18);
        Button btnDelete = new Button(); btnDelete.setGraphic(delIcon); btnDelete.getStyleClass().add("icon-button-ghost");
        StackPane wrapFront = new StackPane(front, btnDelete);
        StackPane.setAlignment(btnDelete, Pos.TOP_RIGHT);
        StackPane.setMargin(btnDelete, new Insets(4,4,0,0));
        btnDelete.setOnAction(e -> { if(!confirmDelete()) return; animateRemoval(wrapFront, () -> { agDAO.deleteById(a.getId()); toast("Agendamento excluído."); refresh(); }); });

        BorderPane back = new BorderPane(); back.setPadding(new Insets(10));
        VBox box = new VBox(8);
        if ("AGUARDANDO_ORIENTADOR".equals(a.getStatus())) {
            TextArea txt = new TextArea(); txt.setPromptText("Mensagem ao aluno");
            DatePicker dp = new DatePicker(); TextField tfHora = new TextField(); tfHora.setPromptText("Hora HH:MM");
            TextField tfSala = new TextField(); tfSala.setPromptText("Sala");
            Button enviar = new Button("Enviar Proposta");
            box.getChildren().addAll(new Label("Pedido do aluno:"), new Label(a.getMensagemAluno()), txt, dp, tfHora, tfSala, enviar);
            enviar.setOnAction(ev -> {
                LocalDate d = dp.getValue(); LocalTime h = parse(tfHora.getText()); String sala = tfSala.getText(); String msg = txt.getText();
                if (d==null||h==null||sala==null||sala.isBlank()||msg==null||msg.isBlank()){ toast("Campos obrigatórios."); return; }
                agDAO.proporAgendamento(a.getId(), msg.trim(), d, h, sala.trim(), false);
                emailAluno(a, "Proposta de defesa enviada."); toast("Proposta enviada."); refresh();
            });
        } else if ("AGUARDANDO_ALUNO".equals(a.getStatus()) || "REAGENDAMENTO".equals(a.getStatus())) {
            String dataTxt = a.getPropostaDataHora()!=null ? DF.format(a.getPropostaDataHora().toLocalDate()) : "-";
            String horaTxt = a.getPropostaDataHora()!=null ? TF.format(a.getPropostaDataHora().toLocalTime()) : "-";
            box.getChildren().addAll(new Label("Proposta atual:"), new Label("Data: "+dataTxt), new Label("Horário: "+horaTxt), new Label("Sala: "+a.getSala()), new Label("Mensagem aluno: "+safe(a.getMensagemAluno())));
            TextArea txtNovo = new TextArea(); DatePicker dp2 = new DatePicker();
            TextField tfH2 = new TextField(); tfH2.setPromptText("Hora HH:MM");
            TextField tfS2 = new TextField(); tfS2.setPromptText("Sala");
            Button reagendar = new Button("Reagendar");
            box.getChildren().addAll(txtNovo, dp2, tfH2, tfS2, reagendar);
            reagendar.setOnAction(ev -> {
                LocalDate d = dp2.getValue(); LocalTime h = parse(tfH2.getText()); String sala = tfS2.getText(); String msg = txtNovo.getText();
                if (d==null||h==null||sala==null||sala.isBlank()||msg==null||msg.isBlank()){ toast("Campos obrigatórios."); return; }
                agDAO.proporAgendamento(a.getId(), msg.trim(), d, h, sala.trim(), true);
                emailAluno(a, "Nova proposta de defesa."); toast("Reagendamento enviado."); refresh();
            });
        } else if ("AGENDADO".equals(a.getStatus())) {
            String dataTxt = a.getPropostaDataHora()!=null ? DF.format(a.getPropostaDataHora().toLocalDate()) : "-";
            String horaTxt = a.getPropostaDataHora()!=null ? TF.format(a.getPropostaDataHora().toLocalTime()) : "-";
            box.getChildren().addAll(new Label("Agendado:"), new Label("Data: "+dataTxt), new Label("Horário: "+horaTxt), new Label("Sala: "+a.getSala()));
            TextArea msgR = new TextArea(); DatePicker dpR = new DatePicker();
            TextField tfHR = new TextField(); tfHR.setPromptText("Hora HH:MM");
            TextField tfSR = new TextField(); tfSR.setPromptText("Sala");
            Button mod = new Button("Modificar Data");
            box.getChildren().addAll(msgR, dpR, tfHR, tfSR, mod);
            mod.setOnAction(ev -> {
                LocalDate d = dpR.getValue(); LocalTime h = parse(tfHR.getText()); String sala = tfSR.getText(); String msg = msgR.getText();
                if (d==null||h==null||sala==null||sala.isBlank()||msg==null||msg.isBlank()){ toast("Campos obrigatórios."); return; }
                agDAO.proporAgendamento(a.getId(), msg.trim(), d, h, sala.trim(), true);
                emailAluno(a, "Professor solicitou reagendar defesa."); toast("Proposta enviada."); refresh();
            });
        }
        Button voltar = new Button("Voltar"); box.getChildren().add(voltar);

        VBox backRoot = new VBox(8);
        backRoot.getStyleClass().addAll("card-agendamento", "card-flip-back");
        backRoot.setPadding(new Insets(10));
        backRoot.getChildren().addAll(box.getChildren());

        BorderPane container = new BorderPane(wrapFront);
        flip.setOnAction(ev -> FlipTransitionUtil.flip(container, () -> container.setCenter(backRoot)));
        voltar.setOnAction(ev -> FlipTransitionUtil.flip(container, () -> container.setCenter(wrapFront)));
        return new StackPane(container);
    }

    private String mapStatusClass(String s){
        if (s==null) return null;
        return switch (s) {
            case "AGUARDANDO_ORIENTADOR" -> "status-aguardando-orientador";
            case "AGUARDANDO_ALUNO" -> "status-aguardando-aluno";
            case "REAGENDAMENTO" -> "status-reagendamento";
            case "AGENDADO" -> "status-agendado";
            default -> null;
        }; }

    private LocalTime parse(String v){ try { if (v==null) return null; String[] p=v.trim().split(":"); if (p.length!=2) return null; return LocalTime.of(Integer.parseInt(p[0]), Integer.parseInt(p[1])); } catch(Exception e){ return null; } }
    private void toast(String m){ Scene sc = flowCards.getScene(); if (sc!=null) Toast.show(sc,m); }
    private String safe(String s){ return s==null?"":s; }
    private void emailAluno(AgendamentoDefesa a, String assunto){ try { EmailService email = new EmailService(SmtpProps.FROM,new EmailService.SmtpConfig(SmtpProps.HOST,SmtpProps.PORT,SmtpProps.USER,SmtpProps.PASS,SmtpProps.STARTTLS,SmtpProps.SSL)); email.send(a.getAlunoEmail(), assunto, "Status: "+a.getStatus()); } catch(Exception ignored){} }
    private boolean confirmDelete(){
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Excluir este agendamento?", ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null);
        return a.showAndWait().filter(bt -> bt==ButtonType.YES).isPresent();
    }
    private void animateRemoval(Node n, Runnable after){
        FadeTransition ft = new FadeTransition(Duration.millis(220), n);
        ft.setToValue(0.0);
        ft.setOnFinished(ev -> { if (after!=null) after.run(); });
        ft.play();
    }
}
