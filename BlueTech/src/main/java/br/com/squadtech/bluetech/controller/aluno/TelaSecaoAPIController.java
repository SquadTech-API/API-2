package br.com.squadtech.bluetech.controller.aluno;

import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import br.com.squadtech.bluetech.dao.FeedbackDAO;
import br.com.squadtech.bluetech.dao.MensagensDAO;
import br.com.squadtech.bluetech.dao.OrientaDAO;
import br.com.squadtech.bluetech.dao.PerfilAlunoDAO;
import br.com.squadtech.bluetech.dao.TGSecaoDAO;
import br.com.squadtech.bluetech.dao.TGVersaoDAO;
import br.com.squadtech.bluetech.model.Feedback;
import br.com.squadtech.bluetech.model.FeedbackItem;
import br.com.squadtech.bluetech.model.Mensagens;
import br.com.squadtech.bluetech.model.Orienta;
import br.com.squadtech.bluetech.model.PerfilAluno;
import br.com.squadtech.bluetech.model.SecaoContext;
import br.com.squadtech.bluetech.model.SessaoUsuario;
import br.com.squadtech.bluetech.model.TGSecao;
import br.com.squadtech.bluetech.model.TGVersao;
import br.com.squadtech.bluetech.model.Usuario;
import br.com.squadtech.bluetech.util.MarkdownBuilderUtil;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class TelaSecaoAPIController implements SupportsMainController {

    private final FeedbackDAO feedbackDAO = new FeedbackDAO();
    private final MensagensDAO mensagensDAO = new MensagensDAO();
    private final PerfilAlunoDAO perfilAlunoDAO = new PerfilAlunoDAO();
    private final OrientaDAO orientaDAO = new OrientaDAO();
    private static final Logger log = LoggerFactory.getLogger(TelaSecaoAPIController.class);

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnEditarSecao;

    @FXML
    private Button btnEnviar;

    @FXML
    private Label lblStatus;

    @FXML
    private ListView<TGVersao> listVersoes;

    @FXML
    private TextArea txtChatVisualizacao;

    @FXML
    private TextArea txtFeedbacks;

    @FXML
    private WebView webViewMarkdown;

    @FXML
    private TextField txtMensagem;

    private final Parser mdParser = Parser.builder().build();
    private final HtmlRenderer mdRenderer = HtmlRenderer.builder().build();

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Guarda a seção atual para enriquecer metadados no Markdown
    private TGSecao secaoAtual;
    private FinalizableChatRefresher chatRefresher;
    private Long professorIdOrientador;
    private Long orientacaoId;
    private Integer alunoPerfilId;

    // Ajustado para assinar ActionEvent conforme esperado pelo FXML (onAction)
    @FXML
    public void abrirEdicaoSecao(ActionEvent event) {
        if (painelPrincipalController != null) {
            try {
                // Recupera a seção atual do contexto (definida na tela anterior)
                Integer idSecao = SecaoContext.getIdSecaoSelecionada();
                if (idSecao == null) {
                    // Sem seção selecionada, apenas não navega (ou poderia exibir um alerta futuramente)
                    return;
                }
                // Define a versão atualmente exibida (se houver) para que o editor saiba o que carregar
                TGVersao selecionada = listVersoes.getSelectionModel().getSelectedItem();
                if (selecionada != null && selecionada.getIdSecaoApi() != null) {
                    SecaoContext.setIdVersaoSelecionada(selecionada.getIdSecaoApi());
                } else if (this.secaoAtual != null && this.secaoAtual.getIdVersao() != null) {
                    SecaoContext.setIdVersaoSelecionada(this.secaoAtual.getIdVersao());
                } else {
                    SecaoContext.setIdVersaoSelecionada(null);
                }
                String fxmlPath = "/fxml/aluno/EditarSecaoAPI.fxml";
                painelPrincipalController.loadContent(fxmlPath);
            } catch (IOException e) {
                log.error("Falha ao carregar EditarSecaoAPI.fxml", e);
            }
        }
    }


    @FXML
    public void enviarMensagemOrientador(ActionEvent event) {
        if (orientacaoId == null || professorIdOrientador == null || alunoPerfilId == null) {
            return;
        }
        if (secaoAtual == null || secaoAtual.getIdSecao() == null) {
            return;
        }
        String conteudo = txtMensagem.getText();
        if (conteudo == null || conteudo.isBlank()) {
            return;
        }
        Mensagens msg = new Mensagens();
        msg.setDataHora(LocalDateTime.now());
        msg.setConteudo(conteudo.trim());
        msg.setAlunoId(alunoPerfilId);
        msg.setProfessorId(professorIdOrientador);
        msg.setOrientacaoId(orientacaoId);
        msg.setSecaoId(secaoAtual.getIdSecao());
        msg.setEnviadoPorProfessor(false);
        mensagensDAO.salvar(msg);
        txtMensagem.clear();
        carregarChat();
    }

    @FXML
    void initialize() {
        assert btnEditarSecao != null : "fx:id=\"btnEditarSecao\" was not injected: check your FXML file 'TelaSecaoAPI.fxml'.";
        assert btnEnviar != null : "fx:id=\"btnEnviar\" was not injected: check your FXML file 'TelaSecaoAPI.fxml'.";
        assert lblStatus != null : "fx:id=\"lblStatus\" was not injected: check your FXML file 'TelaSecaoAPI.fxml'.";
        assert listVersoes != null : "fx:id=\"listVersoes\" was not injected: check your FXML file 'TelaSecaoAPI.fxml'.";
        assert txtChatVisualizacao != null : "fx:id=\"txtChatVisualizacao\" was not injected: check your FXML file 'TelaSecaoAPI.fxml'.";
        assert txtFeedbacks != null : "fx:id=\"txtFeedbacks\" was not injected: check your FXML file 'TelaSecaoAPI.fxml'.";
        assert webViewMarkdown != null : "fx:id=\"webViewMarkdown\" was not injected: check your FXML file 'TelaSecaoAPI.fxml'.";
        assert txtMensagem != null : "fx:id=\"txtMensagem\" was not injected: check your FXML file 'TelaSecaoAPI.fxml'.";

        carregarMarkdownDaUltimaVersaoSelecionada();

        // Configura renderer da lista de versões para exibir id e data
        listVersoes.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(TGVersao item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String d = item.getCreatedAt() != null ? item.getCreatedAt().format(dtf) : "sem data";
                    String vtxt = item.getVersaoNumero() != null ? ("v" + item.getVersaoNumero()) : ("id=" + item.getIdSecaoApi());
                    setText(vtxt + " — " + d);
                }
            }
        });

        // Listener para seleção de versão
        listVersoes.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                renderVersaoToWebView(newV);
                carregarFeedbackDaVersao(newV); // <<< adiciona aqui
            }
        });

    }

    private void carregarMarkdownDaUltimaVersaoSelecionada() {
        Integer idSecao = SecaoContext.getIdSecaoSelecionada();
        if (idSecao == null) {
            renderHtmlInWebView("<p><i>Nenhuma seção selecionada.</i></p>");
            return;
        }

        Usuario user = SessaoUsuario.getUsuarioLogado();
        if (user == null || user.getEmail() == null) {
            renderHtmlInWebView("<p><i>Sessão expirada. Faça login novamente.</i></p>");
            return;
        }

        PerfilAluno perfilAluno = perfilAlunoDAO.getPerfilByEmail(user.getEmail());
        if (perfilAluno != null) {
            alunoPerfilId = perfilAluno.getIdPerfilAluno();
            Optional<Orienta> orientacao = orientaDAO.findByAlunoId(alunoPerfilId.longValue()).stream().filter(Orienta::isAtivo).findFirst();
            orientacaoId = orientacao.map(Orienta::getId).orElse(null);
            professorIdOrientador = orientacao.map(Orienta::getProfessorId).orElse(null);
            btnEnviar.setDisable(orientacaoId == null);
            if (orientacaoId == null) {
                txtChatVisualizacao.setText("Você ainda não possui orientador ativo.");
            }
        }

        TGSecaoDAO secaoDAO = new TGSecaoDAO();
        TGVersaoDAO versaoDAO = new TGVersaoDAO();
            // Garante que esquema/colunas da tabela TG_Versao estejam atualizados (evita missing column como Data_Criacao)
            versaoDAO.ensureSchemaUpToDate();

        try {
            TGSecao secao = secaoDAO.findByIdAndEmail(idSecao, user.getEmail());
            this.secaoAtual = secao;
            if (secao == null) {
                renderHtmlInWebView("<p><i>Seção não encontrada para o seu usuário.</i></p>");
                return;
            }
            Integer idVersao = secao.getIdVersao();
            if (idVersao == null || idVersao <= 0) {
                renderHtmlInWebView("<p><i>Nenhuma versão associada a esta seção.</i></p>");
                return;
            }

            TGVersao versao = versaoDAO.findById(idVersao);
            if (versao == null) {
                renderHtmlInWebView("<p><i>Versão referenciada não encontrada.</i></p>");
                return;
            }

            // Preenche o WebView com o conteúdo convertido de Markdown para HTML
            renderVersaoToWebView(versao);

            // Preenche status
            if (secao.getStatus() != null) lblStatus.setText(secao.getStatus());

            // Carrega histórico de versões vinculadas à seção
            List<TGVersao> historico = versaoDAO.listBySecaoId(idSecao);
            if (historico == null || historico.isEmpty()) {
                listVersoes.setItems(FXCollections.observableArrayList(versao));
                listVersoes.getSelectionModel().select(versao);
            } else {
                listVersoes.setItems(FXCollections.observableArrayList(historico));
                // Seleciona a versão atual na lista
                for (TGVersao v : historico) {
                    if (v.getIdSecaoApi() != null && v.getIdSecaoApi().equals(idVersao)) {
                        listVersoes.getSelectionModel().select(v);
                        break;
                    }
                }
            }

            // Feedbacks placeholder — se houver campo de feedback salvo nas versões, buscar e preencher
            carregarFeedbackDaVersao(versao);
            carregarChat();
            iniciarChatRefresher();


        } catch (Exception e) {
            log.error("Erro ao carregar versão", e);
            renderHtmlInWebView("<p><b>Erro ao carregar versão:</b> " + e.getMessage() + "</p>");
        }
    }

    private void carregarChat() {
        if (secaoAtual == null || secaoAtual.getIdSecao() == null || orientacaoId == null || professorIdOrientador == null || alunoPerfilId == null) {
            return;
        }
        List<Mensagens> mensagens = mensagensDAO.listarChat(professorIdOrientador, alunoPerfilId.longValue(), secaoAtual.getIdSecao(), 200);
        if (mensagens.isEmpty()) {
            txtChatVisualizacao.setText("Nenhuma mensagem ainda.");
            ultimaMensagem = null;
            return;
        }
        ultimaMensagem = mensagens.get(mensagens.size() - 1).getDataHora();
        if (chatRefresher != null) {
            chatRefresher.setUltimaMensagem(ultimaMensagem);
        }
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");
        for (Mensagens m : mensagens) {
            String autor = Boolean.TRUE.equals(m.getEnviadoPorProfessor()) ? "Professor" : "Você";
            String data = m.getDataHora() != null ? m.getDataHora().atZone(ZoneId.systemDefault()).format(formatter) : "";
            sb.append("[").append(data).append("] ").append(autor).append(": ").append(m.getConteudo()).append("\n");
        }
        txtChatVisualizacao.setText(sb.toString());
        txtChatVisualizacao.positionCaret(txtChatVisualizacao.getText().length());
    }

    private LocalDateTime ultimaMensagem;

    private void iniciarChatRefresher() {
        if (chatRefresher != null) {
            chatRefresher.parar();
        }
        chatRefresher = new FinalizableChatRefresher();
        chatRefresher.start();
    }

    private void renderVersaoToWebView(TGVersao v) {
        // Monta o markdown e converte para HTML
        String md;
        if (v.getMarkdownContent() != null && !v.getMarkdownContent().isBlank()) {
            md = v.getMarkdownContent();
        } else {
            md = MarkdownBuilderUtil.buildMarkdownFromVersao(v, this.secaoAtual);
        }
        String htmlBody = mdRenderer.render(mdParser.parse(md));
        // Pequeno CSS para melhorar leitura
        String css = "body{font-family: 'Segoe UI', Arial, sans-serif; padding:16px;} h1,h2,h3{color:#2b5797;} pre{background:#f5f5f5;padding:8px;}";
        String html = "<html><head><meta charset=\"utf-8\"><style>" + css + "</style></head><body>" + htmlBody + "</body></html>";
        renderHtmlInWebView(html);
    }

    private void renderHtmlInWebView(String html) {
        WebEngine engine = webViewMarkdown.getEngine();
        engine.loadContent(html);
    }


    private PainelPrincipalController painelPrincipalController;

    @Override
    public void setPainelPrincipalController(PainelPrincipalController controller) {
        this.painelPrincipalController = controller;
    }

    private void carregarFeedbackDaVersao(TGVersao v) {
        if (txtFeedbacks == null) return;
        txtFeedbacks.clear();

        if (v == null) {
            txtFeedbacks.setText("Nenhuma versão selecionada.");
            return;
        }

        Long versaoId = null;
        try {
            if (v.getIdSecaoApi() != null) {
                versaoId = v.getIdSecaoApi().longValue();
            }
        } catch (Exception ignore) {}


        if (versaoId == null) {
            txtFeedbacks.setText("Não foi possível identificar a versão para carregar o feedback.");
            return;
        }

        Feedback fb = feedbackDAO.buscarPorVersaoId(versaoId);
        if (fb == null) {
            txtFeedbacks.setText("Nenhum feedback para esta versão.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Status: ").append(fb.getStatus()).append("\n");
        if (fb.getCreatedAt() != null) {
            sb.append("Data: ").append(fb.getCreatedAt().format(dtf)).append("\n");
        }
        if (fb.getComentario() != null && !fb.getComentario().isBlank()) {
            sb.append("\nComentário geral:\n")
                    .append(fb.getComentario())
                    .append("\n");
        }
        if (fb.getItens() != null && !fb.getItens().isEmpty()) {
            sb.append("\nDetalhamento por campo:\n");
            for (FeedbackItem it : fb.getItens()) {
                String campoLabel = traduzCampo(it.getCampo());
                sb.append("• ")
                        .append(campoLabel)
                        .append(" — ")
                        .append(it.getStatus());
                if (it.getComentario() != null && !it.getComentario().isBlank()) {
                    sb.append(" | ").append(it.getComentario());
                }
                sb.append("\n");
            }
        }

        txtFeedbacks.setText(sb.toString());
    }

    private String traduzCampo(String campoId) {
        if (campoId == null) return "";
        return switch (campoId) {
            case "problema"      -> "Problema";
            case "solucao"       -> "Solução";
            case "repositorio"   -> "Repositório";
            case "linkedin"      -> "LinkedIn";
            case "tecnologias"   -> "Tecnologias";
            case "contribuicoes" -> "Contribuições";
            case "hardSkills"    -> "Hard Skills";
            case "softSkills"    -> "Soft Skills";
            default -> campoId;
        };
    }

    private class FinalizableChatRefresher extends Thread {
        private volatile boolean rodando = true;
        private LocalDateTime referencia;

        FinalizableChatRefresher() {
            super("chat-aluno-refresh");
            setDaemon(true);
            referencia = ultimaMensagem;
        }

        void setUltimaMensagem(LocalDateTime dt) {
            referencia = dt;
        }

        void parar() {
            rodando = false;
            interrupt();
        }

        @Override
        public void run() {
            while (rodando) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) { }
                if (!rodando || secaoAtual == null || orientacaoId == null || professorIdOrientador == null || alunoPerfilId == null || referencia == null) {
                    continue;
                }
                List<Mensagens> novas = mensagensDAO.listarChatApos(professorIdOrientador, alunoPerfilId.longValue(), secaoAtual.getIdSecao(), referencia, 100);
                if (!novas.isEmpty()) {
                    referencia = novas.get(novas.size() - 1).getDataHora();
                    Platform.runLater(() -> appendMensagens(novas));
                }
            }
        }

        private void appendMensagens(List<Mensagens> novas) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");
            StringBuilder sb = new StringBuilder(txtChatVisualizacao.getText());
            for (Mensagens m : novas) {
                String autor = Boolean.TRUE.equals(m.getEnviadoPorProfessor()) ? "Professor" : "Você";
                String data = m.getDataHora() != null ? m.getDataHora().atZone(ZoneId.systemDefault()).format(formatter) : "";
                sb.append("[").append(data).append("] ").append(autor).append(": ").append(m.getConteudo()).append("\n");
            }
            txtChatVisualizacao.setText(sb.toString());
            txtChatVisualizacao.positionCaret(txtChatVisualizacao.getText().length());
        }
    }

    public void dispose() {
        if (chatRefresher != null) {
            chatRefresher.parar();
        }
    }
}
