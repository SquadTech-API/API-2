package br.com.squadtech.bluetech.controller.professorOrientador;

import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import br.com.squadtech.bluetech.dao.FeedbackDAO;
import br.com.squadtech.bluetech.dao.MensagensDAO;
import br.com.squadtech.bluetech.dao.OrientaDAO;
import br.com.squadtech.bluetech.dao.PerfilAlunoDAO;
import br.com.squadtech.bluetech.dao.ProfessorDAO;
import br.com.squadtech.bluetech.dao.TGSecaoDAO;
import br.com.squadtech.bluetech.dao.TGSecaoDAO.CardDados;
import br.com.squadtech.bluetech.model.FeedbackItem;
import br.com.squadtech.bluetech.model.Mensagens;
import br.com.squadtech.bluetech.model.Orienta;
import br.com.squadtech.bluetech.model.PerfilAluno;
import br.com.squadtech.bluetech.model.Professor;
import br.com.squadtech.bluetech.model.SessaoUsuario;
import br.com.squadtech.bluetech.model.Usuario;
import br.com.squadtech.bluetech.viewmodel.FeedbackHistoricoVM;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    @FXML private TextArea txtChatVisualizacao;
    @FXML private TextField txtChatMensagem;
    @FXML private Button btnEnviarMensagem;

    private final MensagensDAO mensagensDAO = new MensagensDAO();
    private final TGSecaoDAO tgSecaoDAO = new TGSecaoDAO();
    private final PerfilAlunoDAO perfilAlunoDAO = new PerfilAlunoDAO();
    private final OrientaDAO orientaDAO = new OrientaDAO();
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();
    private final ProfessorDAO professorDAO = new ProfessorDAO();
    private static final Logger log = LoggerFactory.getLogger(TelaAlunoEspecificoController.class);

    private long alunoId; // id_perfil_aluno
    private Integer secaoSelecionadaId;
    private Integer secaoSelecionadaApiNumero;
    private final Map<Integer, Integer> secaoIdPorApi = new HashMap<>();
    private Long orientacaoId;
    private Long professorId;
    private LocalDateTime ultimaMensagemCarregada;
    private final ExecutorService chatExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("chat-professor-refresh");
        return t;
    });

    public void setAlunoId(long alunoId, String nomeAluno) {
        this.alunoId = alunoId;
        lblNomeAluno.setText(nomeAluno);
        initializeChatContext();
        carregarSecoesDoAluno();
        configurarToggleButtons();
        selecionarPrimeiraSecaoDisponivel();
        carregarChat();
        agendarRefreshChat();
    }

    private void initializeChatContext() {
        Usuario u = SessaoUsuario.getUsuarioLogado();
        if (u == null || u.getEmail() == null) {
            btnEnviarMensagem.setDisable(true);
            txtChatVisualizacao.setText("Sessão expirada.");
            return;
        }
        Professor professor = professorDAO.findByUsuarioEmail(u.getEmail());
        if (professor == null || professor.getId() == null) {
            btnEnviarMensagem.setDisable(true);
            txtChatVisualizacao.setText("Professor não identificado.");
            return;
        }
        this.professorId = professor.getId();
        Optional<Orienta> orientacao = orientaDAO.findByAlunoId(alunoId).stream().filter(Orienta::isAtivo).findFirst();
        orientacaoId = orientacao.map(Orienta::getId).orElse(null);
        btnEnviarMensagem.setDisable(orientacaoId == null);
        if (orientacaoId == null) {
            txtChatVisualizacao.setText("Nenhuma orientação ativa para este aluno.");
        }
    }

    private void agendarRefreshChat() {
        chatExecutor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
                if (professorId == null || orientacaoId == null || secaoSelecionadaId == null || ultimaMensagemCarregada == null) {
                    continue;
                }
                List<Mensagens> novas = mensagensDAO.listarChatApos(professorId, alunoId, secaoSelecionadaId, ultimaMensagemCarregada, 50);
                if (!novas.isEmpty()) {
                    ultimaMensagemCarregada = novas.get(novas.size() - 1).getDataHora();
                    Platform.runLater(() -> appendMensagens(novas));
                }
            }
        });
    }

    private void carregarChat() {
        if (professorId == null || orientacaoId == null || secaoSelecionadaId == null) {
            return;
        }
        List<Mensagens> mensagens = mensagensDAO.listarChat(professorId, alunoId, secaoSelecionadaId, 200);
        ultimaMensagemCarregada = mensagens.isEmpty() ? null : mensagens.get(mensagens.size() - 1).getDataHora();
        if (mensagens.isEmpty()) {
            txtChatVisualizacao.setText("Sem mensagens nesta seção.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM HH:mm");
        for (Mensagens m : mensagens) {
            String autor = Boolean.TRUE.equals(m.getEnviadoPorProfessor()) ? "Você" : "Aluno";
            String data = m.getDataHora() != null ? m.getDataHora().atZone(ZoneId.systemDefault()).format(fmt) : "";
            sb.append("[").append(data).append("] ").append(autor).append(": ").append(m.getConteudo()).append("\n");
        }
        txtChatVisualizacao.setText(sb.toString());
        txtChatVisualizacao.positionCaret(txtChatVisualizacao.getText().length());
    }

    private void appendMensagens(List<Mensagens> novas) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM HH:mm");
        StringBuilder sb = new StringBuilder(txtChatVisualizacao.getText());
        for (Mensagens m : novas) {
            String autor = Boolean.TRUE.equals(m.getEnviadoPorProfessor()) ? "Você" : "Aluno";
            String data = m.getDataHora() != null ? m.getDataHora().atZone(ZoneId.systemDefault()).format(fmt) : "";
            sb.append("[").append(data).append("] ").append(autor).append(": ").append(m.getConteudo()).append("\n");
        }
        txtChatVisualizacao.setText(sb.toString());
        txtChatVisualizacao.positionCaret(txtChatVisualizacao.getText().length());
    }

    @FXML
    private void enviarMensagemAluno() {
        if (orientacaoId == null || professorId == null || secaoSelecionadaId == null) {
            return;
        }
        String conteudo = txtChatMensagem.getText();
        if (conteudo == null || conteudo.isBlank()) {
            return;
        }
        Mensagens msg = new Mensagens();
        msg.setDataHora(LocalDateTime.now());
        msg.setConteudo(conteudo.trim());
        msg.setAlunoId((int) alunoId);
        msg.setProfessorId(professorId);
        msg.setOrientacaoId(orientacaoId);
        msg.setSecaoId(secaoSelecionadaId);
        msg.setEnviadoPorProfessor(true);
        mensagensDAO.salvar(msg);
        txtChatMensagem.clear();
        carregarChat();
    }

    /**
     * Carrega todas as seções do aluno direto em código (sem FXML de card)
     */
    private void carregarSecoesDoAluno() {
        String email = perfilAlunoDAO.getEmailByPerfilId((int) alunoId);
        if (email == null || email.isBlank()) {
            limparVBoxes();
            secaoIdPorApi.clear();
            return;
        }
        List<CardDados> secoes = tgSecaoDAO.listarCards(email);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        limparVBoxes();
        secaoIdPorApi.clear();

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

            Node feedbackDrop = buildFeedbackDropdown(secao.idSecao);
            if (feedbackDrop != null) {
                cardContainer.getChildren().addAll(header, feedbackDrop);
            } else {
                cardContainer.getChildren().add(header);
            }

            VBox destino = obterVBoxPorApi(secao.apiNumero);
            if (destino != null) {
                destino.getChildren().add(cardContainer);
            } else {
                log.warn("API {} fora do intervalo esperado", secao.apiNumero);
            }

            secaoIdPorApi.put(secao.apiNumero, secao.idSecao);
        }

    }

    private void limparVBoxes() {
        VBox[] vboxes = {VBoxSessao1, VBoxSessao2, VBoxSessao3, VBoxSessao4, VBoxSessao5, VBoxSessao6};
        for (VBox vbox : vboxes) vbox.getChildren().clear();
    }

    private VBox obterVBoxPorApi(int apiNumero) {
        return switch (apiNumero) {
            case 1 -> VBoxSessao1;
            case 2 -> VBoxSessao2;
            case 3 -> VBoxSessao3;
            case 4 -> VBoxSessao4;
            case 5 -> VBoxSessao5;
            case 6 -> VBoxSessao6;
            default -> null;
        };
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
                definirSecaoSelecionada(index + 1);
            });
        }

        for (int j = 0; j < scrollers.length; j++) {
            scrollers[j].setVisible(j == 0);
            toggles[j].setSelected(j == 0);
        }
    }

    private void definirSecaoSelecionada(int apiNumero) {
        secaoSelecionadaApiNumero = apiNumero;
        secaoSelecionadaId = secaoIdPorApi.get(apiNumero);
        ultimaMensagemCarregada = null;
        if (secaoSelecionadaId == null) {
            txtChatVisualizacao.setText("Aluno ainda não possui entrega para esta seção.");
            return;
        }
        carregarChat();
    }

    private void selecionarPrimeiraSecaoDisponivel() {
        if (secaoIdPorApi.isEmpty()) {
            secaoSelecionadaId = null;
            txtChatVisualizacao.setText("Aluno ainda não possui seções cadastradas.");
            return;
        }
        int primeiroApi = secaoIdPorApi.keySet().stream().min(Integer::compareTo).orElse(1);
        definirSecaoSelecionada(primeiroApi);
        ToggleButton[] toggles = {tgS1, tgS2, tgS3, tgS4, tgS5, tgS6};
        if (primeiroApi >= 1 && primeiroApi <= toggles.length) {
            toggles[primeiroApi - 1].setSelected(true);
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
                ? vm.getCriadoEm().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
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
