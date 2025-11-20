package br.com.squadtech.bluetech.controller.professorOrientador;


import br.com.squadtech.bluetech.model.FeedbackItem;
import javafx.scene.control.TitledPane;
import javafx.scene.Node;
import javafx.geometry.Insets;
import br.com.squadtech.bluetech.dao.FeedbackDAO;
import br.com.squadtech.bluetech.viewmodel.FeedbackHistoricoVM; // o VM/DTO do histórico
import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import br.com.squadtech.bluetech.dao.PerfilAlunoDAO;
import br.com.squadtech.bluetech.dao.TGSecaoDAO;
import br.com.squadtech.bluetech.dao.TGSecaoDAO.CardDados;
import br.com.squadtech.bluetech.dao.ProfessorDAO;
import br.com.squadtech.bluetech.model.SessaoUsuario;
import br.com.squadtech.bluetech.model.Usuario;
import br.com.squadtech.bluetech.model.Professor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TelaAlunoEspecificoController implements SupportsMainController {

    private PainelPrincipalController painelPrincipalController;

    @Override
    public void setPainelPrincipalController(PainelPrincipalController controller) {
        this.painelPrincipalController = controller;
    }

    @FXML private Label lblNomeAluno;
    @FXML private ToggleButton tgS1, tgS2, tgS3, tgS4, tgS5, tgS6;
    @FXML private ScrollPane spSessao1, spSessao2, spSessao3, spSessao4, spSessao5, spSessao6;
    @FXML private VBox VBoxSessao1, VBoxSessao2, VBoxSessao3, VBoxSessao4, VBoxSessao5, VBoxSessao6;

    private long alunoId; // id_perfil_aluno

    private final FeedbackDAO feedbackDAO = new FeedbackDAO();
    private final TGSecaoDAO tgSecaoDAO = new TGSecaoDAO();
    private final PerfilAlunoDAO perfilAlunoDAO = new PerfilAlunoDAO();
    private static final Logger log = LoggerFactory.getLogger(TelaAlunoEspecificoController.class);

    public void setAlunoId(long alunoId, String nomeAluno) {
        this.alunoId = alunoId;
        lblNomeAluno.setText(nomeAluno);
        carregarSecoesDoAluno();
        configurarToggleButtons();
    }

    /**
     * Carrega todas as seções do aluno direto em código (sem FXML de card)
     */
    private void carregarSecoesDoAluno() {
        // Obter email do aluno a partir do id_perfil_aluno
        String email = perfilAlunoDAO.getEmailByPerfilId((int) alunoId);
        if (email == null || email.isBlank()) {
            limparVBoxes();
            return;
        }
        List<CardDados> secoes = tgSecaoDAO.listarCards(email);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        limparVBoxes();

        for (CardDados secao : secoes) {
            // Container do card (header + dropdown)
            VBox cardContainer = new VBox(6);
            cardContainer.setPadding(new Insets(10));
            cardContainer.setStyle(
                    "-fx-background-color: #ffffff;" +
                            "-fx-background-radius: 8;" +
                            "-fx-border-color: #e0e0e0;" +
                            "-fx-border-radius: 8;"
            );

            // ----- Header do card (igual ao seu HBox atual)
            HBox header = new HBox(10);
            header.setStyle("-fx-padding: 6 0 4 0;");

            Label lblTitulo = new Label("API " + secao.apiNumero);
            lblTitulo.setStyle("-fx-font-weight: bold; -fx-text-fill: #0a3d62;");

            Label lblStatus = new Label(secao.status != null ? secao.status : "Em andamento");
            lblStatus.setStyle("-fx-text-fill: #555555;");

            Label lblData = new Label(
                    secao.dataEnvio != null
                            ? "Enviado em: " + secao.dataEnvio.format(formatter)
                            : "Sem envio ainda"
            );
            lblData.setStyle("-fx-text-fill: #777777;");

            header.getChildren().addAll(lblTitulo, lblStatus, lblData);

            // Clique no header: abre feedback (se quiser manter essa navegação)
            header.setOnMouseClicked(e -> {
                Usuario u = SessaoUsuario.getUsuarioLogado();
                if (u != null && u.getEmail() != null) {
                    ProfessorDAO profDAO = new ProfessorDAO();
                    Professor p = profDAO.findByUsuarioEmail(u.getEmail());
                    if (p != null && p.getId() != null) {
                        abrirTelaFeedback(secao.idVersao, p.getId());
                    } else {
                        log.warn("Professor não encontrado para o email do usuário logado: {}", u.getEmail());
                    }
                } else {
                    log.warn("Usuário não logado, não é possível dar feedback.");
                }
            });

            // ----- Dropdown de feedback (se houver histórico)
            Node feedbackDrop = buildFeedbackDropdown(secao.idSecao);
            if (feedbackDrop != null) {
                cardContainer.getChildren().addAll(header, feedbackDrop);
            } else {
                cardContainer.getChildren().add(header);
            }

            // coloca no VBox da API correspondente
            int idx = Math.max(1, Math.min(6, secao.apiNumero)) - 1;
            VBox[] vboxes = {VBoxSessao1, VBoxSessao2, VBoxSessao3, VBoxSessao4, VBoxSessao5, VBoxSessao6};
            vboxes[idx].getChildren().add(cardContainer);
        }

    }

    private void limparVBoxes() {
        VBox[] vboxes = {VBoxSessao1, VBoxSessao2, VBoxSessao3, VBoxSessao4, VBoxSessao5, VBoxSessao6};
        for (VBox vbox : vboxes) vbox.getChildren().clear();
    }

    /**
     * Configura os ToggleButtons para mostrar a sessão correta
     */
    private void configurarToggleButtons() {
        ToggleButton[] toggles = {tgS1, tgS2, tgS3, tgS4, tgS5, tgS6};
        ScrollPane[] scrollers = {spSessao1, spSessao2, spSessao3, spSessao4, spSessao5, spSessao6};

        for (int i = 0; i < toggles.length; i++) {
            int index = i;
            toggles[i].setOnAction(e -> {
                for (int j = 0; j < scrollers.length; j++) {
                    scrollers[j].setVisible(j == index);
                    toggles[j].setSelected(j == index);
                }
            });
        }

        // Inicialmente mostra apenas a primeira sessão
        for (int j = 0; j < scrollers.length; j++) {
            scrollers[j].setVisible(j == 0);
            toggles[j].setSelected(j == 0);
        }
    }


    /**
     * Abre a tela de feedback para a seção
     */
    private void abrirTelaFeedback(long versaoId, long professorId) {
        try {
            // Carrega tela de feedback na área de exibição do painel principal
            TelaFeedbackController controller =
                    painelPrincipalController.loadContentReturnController(
                            "/fxml/professorOrientador/telaFeedback.fxml",
                            TelaFeedbackController.class
                    );

            controller.setAlunoVersao(versaoId, lblNomeAluno.getText(), professorId);

        } catch (IOException e) {
            log.error("Erro ao abrir tela de feedback", e);
        }
    }
    private Node buildFeedbackDropdown(int secaoId) {
        List<FeedbackHistoricoVM> historico = feedbackDAO.listarHistoricoPorSecaoId(secaoId);
        if (historico == null || historico.isEmpty()) return null;

        FeedbackHistoricoVM ultimo = historico.get(0);
        String dataUlt = (ultimo.getCriadoEm() != null)
                ? ultimo.getCriadoEm().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "-";
        String title = "Feedback (último: " + (ultimo.getStatus() != null ? ultimo.getStatus() : "-") + " • " + dataUlt + ")";

        VBox content = new VBox(6);
        content.setPadding(new Insets(6, 0, 0, 0));

        // Mostra todos (ou limite se quiser)
        for (FeedbackHistoricoVM vm : historico) {
            content.getChildren().add(buildFeedbackRow(vm));
        }

        TitledPane tp = new TitledPane(title, content);
        tp.setExpanded(false);
        tp.setAnimated(true);
        tp.setCollapsible(true);
        tp.setStyle("-fx-background-color: transparent;");

        return tp;
    }
    private Node buildFeedbackRow(FeedbackHistoricoVM vm) {
        String data = (vm.getCriadoEm() != null)
                ? vm.getCriadoEm().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "-";

        // Título do feedback (linha)
        String header = data +
                " — v" + vm.getVersaoNumero() +
                " / API " + vm.getApiNumero() +
                " — " + (vm.getStatus() != null ? vm.getStatus() : "-") +
                (vm.getComentarioGeral() != null && !vm.getComentarioGeral().isBlank() ? " — " + vm.getComentarioGeral() : "");

        // Container onde vou montar os itens quando expandir
        VBox itensBox = new VBox(4);
        itensBox.setPadding(new Insets(6, 6, 6, 10));

        TitledPane linha = new TitledPane("• " + header, itensBox);
        linha.setExpanded(false);

        // Lazy-load: só consulta os itens quando o usuário abrir
        linha.expandedProperty().addListener((obs, was, isNow) -> {
            if (isNow && itensBox.getChildren().isEmpty()) {
                try {
                    List<FeedbackItem> itens = feedbackDAO.listarItensPorFeedbackId(vm.getId());
                    if (itens.isEmpty()) {
                        itensBox.getChildren().add(new Label("Nenhum item detalhado."));
                    } else {
                        for (FeedbackItem it : itens) {
                            HBox row = new HBox(8);
                            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                            Label campo = new Label(it.getCampo() + ":");
                            campo.setStyle("-fx-font-weight: bold;");

                            Label status = new Label(it.getStatus() != null ? it.getStatus() : "-");

                            Label comentario = new Label(
                                    (it.getComentario() != null && !it.getComentario().isBlank())
                                            ? "— " + it.getComentario() : ""
                            );
                            comentario.setWrapText(true);

                            row.getChildren().addAll(campo, status, comentario);
                            itensBox.getChildren().add(row);
                        }
                    }
                } catch (Exception ex) {
                    itensBox.getChildren().add(new Label("Erro ao carregar itens do feedback."));
                    // se tiver logger aqui, log.error("...", ex);
                }
            }
        });

        return linha;
    }




}
