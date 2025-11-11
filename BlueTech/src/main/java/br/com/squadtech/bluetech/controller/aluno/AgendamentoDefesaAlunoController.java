package br.com.squadtech.bluetech.controller.aluno;

import br.com.squadtech.bluetech.dao.*;
import br.com.squadtech.bluetech.model.*;
import br.com.squadtech.bluetech.service.EmailService;
import br.com.squadtech.bluetech.config.SmtpProps;
import br.com.squadtech.bluetech.util.FlipTransitionUtil;
import br.com.squadtech.bluetech.util.OrientadorHelper;
import br.com.squadtech.bluetech.util.Toast;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import java.time.format.DateTimeFormatter;

/** Controller da tela de agendamento de defesa para o aluno. */
public class AgendamentoDefesaAlunoController {

    @FXML private TextArea txtMensagemInicial;
    @FXML private FlowPane flowCards;
    @FXML private ScrollPane scrollCards;
    @FXML private Button btnSolicitar;

    private final AgendamentoDefesaDAO agDAO = new AgendamentoDefesaDAO();
    private final PerfilAlunoDAO perfilDAO = new PerfilAlunoDAO();
    private final OrientaDAO orientaDAO = new OrientaDAO();
    private final ProfessorDAO professorDAO = new ProfessorDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    void initialize() {
        agDAO.createTableIfNotExists();
        refreshCards();
    }

    @FXML
    void limpar() { txtMensagemInicial.clear(); }

    @FXML
    void enviarSolicitacao() {
        Usuario aluno = SessaoUsuario.getUsuarioLogado();
        if (aluno == null) { showToast("Sessão expirada."); return; }
        PerfilAluno perfil = perfilDAO.getPerfilByEmail(aluno.getEmail());
        if (perfil == null) { showToast("Perfil não encontrado."); return; }

        var orientacoes = orientaDAO.findByAlunoId((long) perfil.getIdPerfilAluno())
                .stream().filter(Orienta::isAtivo).toList();
        if (orientacoes.isEmpty()) { showToast("Nenhum orientador ativo vinculado."); return; }
        if (orientacoes.size() > 1) { showToast("Mais de um orientador ativo: regularize antes de agendar."); return; }
        Long professorId = orientacoes.get(0).getProfessorId();

        String msg = txtMensagemInicial.getText();
        if (msg == null || msg.isBlank()) { showToast("Escreva uma mensagem inicial."); return; }

        AgendamentoDefesa ultima = agDAO.findLatestByAluno(aluno.getEmail());
        if (ultima != null && ("AGUARDANDO_ORIENTADOR".equals(ultima.getStatus()) || "AGUARDANDO_ALUNO".equals(ultima.getStatus()) || "REAGENDAMENTO".equals(ultima.getStatus()) || "AGENDADO".equals(ultima.getStatus()))) {
            showToast("Você já possui um agendamento em andamento ou confirmado.");
            return;
        }

        AgendamentoDefesa novo = new AgendamentoDefesa(aluno.getEmail(), professorId, msg.trim());
        agDAO.inserirSolicitacao(novo);
        enviarEmailProfessor(novo, "Novo pedido de agendamento de defesa recebido.");
        showToast("Solicitação enviada.");
        txtMensagemInicial.clear();
        refreshCards();
    }

    private void refreshCards() {
        flowCards.getChildren().clear();
        Usuario aluno = SessaoUsuario.getUsuarioLogado();
        if (aluno == null) return;
        AgendamentoDefesa a = agDAO.findLatestByAluno(aluno.getEmail());
        if (a == null) return;
        flowCards.getChildren().add(buildCard(a));
    }

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    private StackPane buildCard(AgendamentoDefesa a) {
        VBox front = new VBox(6);
        front.getStyleClass().addAll("card-solicitacao", "card-agendamento"); // adiciona estilo padrao de card
        Label lblAluno = new Label("Aluno: "+a.getAlunoEmail());
        String orientadorEmail = OrientadorHelper.emailDoProfessorUsuario(a.getProfessorId()).orElse("");
        Label lblOrientador = new Label("Orientador: "+orientadorEmail);
        Label lblStatus = new Label("Status: "+a.getStatus());
        String stClass = mapStatusClass(a.getStatus());
        if (stClass != null) lblStatus.getStyleClass().add(stClass);
        front.getChildren().addAll(lblAluno, lblOrientador, lblStatus);

        if (a.getPropostaDataHora() != null) {
            front.getChildren().add(new Label(
                    "Proposta: " + DF.format(a.getPropostaDataHora().toLocalDate()) +
                            " | " + TF.format(a.getPropostaDataHora().toLocalTime()) +
                            " | Sala: " + safe(a.getSala())
            ));
        }
        Button flip = new Button("Ver detalhes / Ações");
        front.getChildren().add(flip);

        // Verso também como card estilizado
        VBox backRoot = new VBox(8);
        backRoot.getStyleClass().addAll("card-agendamento", "card-flip-back"); // reutiliza estilo de card + face traseira
        backRoot.setPadding(new Insets(10));

        if ("AGUARDANDO_ALUNO".equals(a.getStatus()) || "REAGENDAMENTO".equals(a.getStatus())) {
            String dataTxt = a.getPropostaDataHora()!=null ? DF.format(a.getPropostaDataHora().toLocalDate()) : "-";
            String horaTxt = a.getPropostaDataHora()!=null ? TF.format(a.getPropostaDataHora().toLocalTime()) : "-";
            backRoot.getChildren().addAll(
                    new Label("Defesa proposta:"),
                    new Label("Data: "+dataTxt),
                    new Label("Horário: "+horaTxt),
                    new Label("Sala: "+safe(a.getSala())),
                    new Label("Mensagem Prof: "+safe(a.getMensagemProfessor()))
            );
            HBox actions = new HBox(8);
            Button btnOk = new Button("Confirmar");
            Button btnNo = new Button("Recusar");
            actions.getChildren().addAll(btnOk, btnNo);
            backRoot.getChildren().add(actions);
            TextArea motivo = new TextArea(); motivo.setPromptText("Motivo da recusa e sugestões"); motivo.setVisible(false);
            backRoot.getChildren().add(motivo);
            Button enviarRecusa = new Button("Enviar Recusa"); enviarRecusa.setVisible(false);
            backRoot.getChildren().add(enviarRecusa);
            btnOk.setOnAction(e -> { agDAO.alunoConfirma(a.getId()); enviarEmailProfessor(a, "Aluno confirmou o agendamento."); showToast("Agendado!"); refreshCards(); });
            btnNo.setOnAction(e -> { motivo.setVisible(true); enviarRecusa.setVisible(true); });
            enviarRecusa.setOnAction(e -> {
                String m = motivo.getText();
                if (m==null || m.isBlank()){ showToast("Explique o motivo"); return; }
                agDAO.alunoRecusa(a.getId(), m.trim());
                enviarEmailProfessor(a, "Aluno recusou a proposta.");
                showToast("Recusa enviada");
                refreshCards();
            });
        } else if ("AGENDADO".equals(a.getStatus())) {
            String dataTxt = a.getPropostaDataHora()!=null ? DF.format(a.getPropostaDataHora().toLocalDate()) : "-";
            String horaTxt = a.getPropostaDataHora()!=null ? TF.format(a.getPropostaDataHora().toLocalTime()) : "-";
            backRoot.getChildren().addAll(
                    new Label("Defesa agendada para:"),
                    new Label("Data: "+dataTxt),
                    new Label("Horário: "+horaTxt),
                    new Label("Sala: "+safe(a.getSala()))
            );
        } else {
            backRoot.getChildren().add(new Label("Aguardando proposta do professor."));
        }
        Button voltar = new Button("Voltar");
        backRoot.getChildren().add(voltar);

        BorderPane container = new BorderPane(front);
        flip.setOnAction(e -> FlipTransitionUtil.flip(container, () -> container.setCenter(backRoot)));
        voltar.setOnAction(e -> FlipTransitionUtil.flip(container, () -> container.setCenter(front)));

        return new StackPane(container);
    }

    private String mapStatusClass(String status){
        if (status == null) return null;
        return switch (status) {
            case "AGUARDANDO_ORIENTADOR" -> "status-aguardando-orientador";
            case "AGUARDANDO_ALUNO" -> "status-aguardando-aluno";
            case "REAGENDAMENTO" -> "status-reagendamento";
            case "AGENDADO" -> "status-agendado";
            default -> null;
        };
    }

    private String safe(String s){ return s==null?"":s; }
    private void showToast(String m){ Scene sc = btnSolicitar.getScene(); if (sc!=null) Toast.show(sc,m); }

    private void enviarEmailProfessor(AgendamentoDefesa a, String assunto) {
        try {
            Professor prof = professorDAO.findById(a.getProfessorId()); if (prof==null) return;
            Usuario profUser = usuarioDAO.findByEmail(prof.getUsuarioEmail()); if (profUser==null) return;
            EmailService email = new EmailService(
                    SmtpProps.FROM,
                    new EmailService.SmtpConfig(SmtpProps.HOST, SmtpProps.PORT, SmtpProps.USER, SmtpProps.PASS, SmtpProps.STARTTLS, SmtpProps.SSL)
            );
            email.send(profUser.getEmail(), assunto, "Aluno: "+a.getAlunoEmail()+"\nStatus: "+a.getStatus());
        } catch (Exception ignored) {}
    }
}
