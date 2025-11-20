package br.com.squadtech.bluetech.controller.aluno;

import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import br.com.squadtech.bluetech.dao.FeedbackDAO;
import br.com.squadtech.bluetech.dao.NotifierEvents;
import br.com.squadtech.bluetech.dao.TGSecaoDAO;
import br.com.squadtech.bluetech.dao.TGVersaoDAO;
import br.com.squadtech.bluetech.model.*;
import br.com.squadtech.bluetech.util.MarkdownBuilderUtil;
import br.com.squadtech.bluetech.util.Toast;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class EditarSecaoAPIController implements SupportsMainController {

    private static final Logger log = LoggerFactory.getLogger(EditarSecaoAPIController.class);

    @FXML private URL location;
    @FXML private ResourceBundle resources;

    @FXML private Button btnCancelar;
    @FXML private Button btnSalvar;
    @FXML private Button btnVoltar;

    // Lado direito: texto consolidado do feedback
    @FXML private TextArea txtFeedbackOrientador;

    // Formulário da seção (lado esquerdo)
    @FXML private TextArea taProblema;
    @FXML private TextArea taSolucao;
    @FXML private TextArea taRepositorio;
    @FXML private TextArea taLinkedin;
    @FXML private TextArea taTecnologias;
    @FXML private TextArea taContribuicoes;
    @FXML private TextArea taHardSkills;
    @FXML private TextArea taSoftSkills;

    private PainelPrincipalController painelPrincipalController;

    private final TGSecaoDAO secaoDAO = new TGSecaoDAO();
    private final TGVersaoDAO versaoDAO = new TGVersaoDAO();
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();

    // Guardamos a seção e versão que estão sendo editadas
    private TGSecao secaoAtual;
    private TGVersao versaoBase;  // versão sobre a qual o aluno está fazendo ajustes
    private Feedback feedbackAtual; // último feedback da versão (se existir)

    @Override
    public void setPainelPrincipalController(PainelPrincipalController controller) {
        this.painelPrincipalController = controller;
    }

    @FXML
    void cancelaMudancas(ActionEvent event) {
        voltarTelaSecaoAPI(event);
    }

    @FXML
    void salvarNovaVersaoSessaoAPI(ActionEvent event) {
        try {
            if (secaoAtual == null || versaoBase == null) {
                showAlert(Alert.AlertType.ERROR, "Erro",
                        "Nenhuma seção/versão carregada para salvar.");
                return;
            }

            Usuario user = SessaoUsuario.getUsuarioLogado();
            if (user == null || user.getEmail() == null) {
                showAlert(Alert.AlertType.ERROR, "Sessão expirada", "Faça login novamente.");
                return;
            }

            Integer idSecao = secaoAtual.getIdSecao();
            if (idSecao == null) {
                showAlert(Alert.AlertType.ERROR, "Erro",
                        "ID da seção não encontrado.");
                return;
            }

            // === MONTA NOVA VERSÃO A PARTIR DO FORMULÁRIO ===
            TGVersao novaVersao = new TGVersao();
            novaVersao.setIdSecao(idSecao);

            // Copia metadados da versão anterior
            novaVersao.setSemestre(versaoBase.getSemestre());
            novaVersao.setAno(versaoBase.getAno());
            novaVersao.setSemestreAno(versaoBase.getSemestreAno());
            novaVersao.setEmpresa(versaoBase.getEmpresa());

            // Campos de conteúdo (alguns podem estar bloqueados, mas o texto é o mesmo)
            novaVersao.setProblema(taProblema.getText());
            novaVersao.setSolucao(taSolucao.getText());
            novaVersao.setRepositorio(taRepositorio.getText());
            novaVersao.setLinkedin(taLinkedin.getText());
            novaVersao.setTecnologias(taTecnologias.getText());
            novaVersao.setContribuicoes(taContribuicoes.getText());
            novaVersao.setHardSkills(taHardSkills.getText());
            novaVersao.setSoftSkills(taSoftSkills.getText());

            // Gera o Markdown a partir da própria TGVersao + metadados da seção
            String markdown = MarkdownBuilderUtil.buildMarkdownFromVersao(novaVersao, secaoAtual);
            novaVersao.setMarkdownContent(markdown);

            versaoDAO.ensureSchemaUpToDate();
            versaoDAO.createTableIfNotExists();
            Integer idVersao = versaoDAO.insertReturningId(novaVersao);
            if (idVersao == null) {
                showAlert(Alert.AlertType.ERROR, "Erro",
                        "Falha ao salvar nova versão.");
                return;
            }

            // Atualiza a seção para apontar para a nova versão
            secaoDAO.updateIdVersao(idSecao, idVersao);

            // Notifica professor (mantendo sua lógica atual)
            NotifierEvents.onNewVersionSubmitted(idVersao.longValue());
            if (btnSalvar != null && btnSalvar.getScene() != null) {
                Toast.show(btnSalvar.getScene(), "Notificação de envio enfileirada.");
            }

            showAlert(Alert.AlertType.INFORMATION, "Sucesso",
                    "Nova versão salva e enviada para o orientador.");
            voltarTelaSecaoAPI(event);

        } catch (Exception e) {
            log.error("Erro ao salvar nova versão da seção", e);
            showAlert(Alert.AlertType.ERROR, "Erro",
                    "Ocorreu um erro ao salvar a nova versão: " + e.getMessage());
        }
    }

    @FXML
    void voltarTelaSecaoAPI(ActionEvent event) {
        if (painelPrincipalController != null) {
            try {
                Integer idSecao = SecaoContext.getIdSecaoSelecionada();
                if (idSecao == null) return;
                String fxmlPath = "/fxml/aluno/TelaSecaoAPI.fxml";
                painelPrincipalController.loadContent(fxmlPath);
            } catch (IOException e) {
                log.error("Falha ao carregar TelaSecaoAPI.fxml", e);
            }
        }
    }

    @FXML
    void initialize() {
        assert btnCancelar != null : "fx:id=\"btnCancelar\" was not injected: check your FXML file 'EditarSecaoAPI.fxml'.";
        assert btnSalvar != null : "fx:id=\"btnSalvar\" was not injected: check your FXML file 'EditarSecaoAPI.fxml'.";
        assert btnVoltar != null : "fx:id=\"btnVoltar\" was not injected: check your FXML file 'EditarSecaoAPI.fxml'.";
        assert txtFeedbackOrientador != null : "fx:id=\"txtFeedbackOrientador\" was not injected: check your FXML file 'EditarSecaoAPI.fxml'.";

        assert taProblema != null : "fx:id=\"taProblema\" was not injected.";
        assert taSolucao != null : "fx:id=\"taSolucao\" was not injected.";
        assert taRepositorio != null : "fx:id=\"taRepositorio\" was not injected.";
        assert taLinkedin != null : "fx:id=\"taLinkedin\" was not injected.";
        assert taTecnologias != null : "fx:id=\"taTecnologias\" was not injected.";
        assert taContribuicoes != null : "fx:id=\"taContribuicoes\" was not injected.";
        assert taHardSkills != null : "fx:id=\"taHardSkills\" was not injected.";
        assert taSoftSkills != null : "fx:id=\"taSoftSkills\" was not injected.";

        carregarSecaoEVersaoEAplicarFeedback();
    }

    /**
     * Carrega a seção e a versão base, preenche o formulário
     * e aplica as regras de bloqueio com base no feedback.
     */
    private void carregarSecaoEVersaoEAplicarFeedback() {
        Integer idSecao = SecaoContext.getIdSecaoSelecionada();
        if (idSecao == null) return;

        Usuario user = SessaoUsuario.getUsuarioLogado();
        if (user == null || user.getEmail() == null) return;

        try {
            secaoAtual = secaoDAO.findByIdAndEmail(idSecao, user.getEmail());
        } catch (Exception e) {
            log.error("Erro ao buscar seção do aluno", e);
        }

        if (secaoAtual == null) {
            showAlert(Alert.AlertType.ERROR, "Erro",
                    "Seção não encontrada para o seu usuário.");
            return;
        }

        // Descobre qual versão vamos editar: a selecionada no contexto ou a atual da seção
        Integer idVersaoCtx = SecaoContext.getIdVersaoSelecionada();
        Integer idVersaoBase = (idVersaoCtx != null ? idVersaoCtx : secaoAtual.getIdVersao());
        if (idVersaoBase == null || idVersaoBase <= 0) {
            showAlert(Alert.AlertType.INFORMATION, "Aviso",
                    "Ainda não há versão desta seção para editar.");
            return;
        }

        versaoBase = versaoDAO.findById(idVersaoBase);
        if (versaoBase == null) {
            showAlert(Alert.AlertType.ERROR, "Erro",
                    "Versão selecionada não encontrada.");
            return;
        }

        // Preenche o formulário com os dados da versão
        preencherFormularioComVersao(versaoBase);

        // Carrega feedback mais recente dessa versão (se existir)
        Long versaoId = versaoBase.getIdSecaoApi() != null
                ? versaoBase.getIdSecaoApi().longValue()
                : null;

        if (versaoId != null) {
            feedbackAtual = feedbackDAO.buscarPorVersaoId(versaoId);
        }

        aplicarRegrasDeEdicaoComBaseNoFeedback();
    }

    private void preencherFormularioComVersao(TGVersao v) {
        taProblema.setText(nullSafe(v.getProblema()));
        taSolucao.setText(nullSafe(v.getSolucao()));
        taRepositorio.setText(nullSafe(v.getRepositorio()));
        taLinkedin.setText(nullSafe(v.getLinkedin()));
        taTecnologias.setText(nullSafe(v.getTecnologias()));
        taContribuicoes.setText(nullSafe(v.getContribuicoes()));
        taHardSkills.setText(nullSafe(v.getHardSkills()));
        taSoftSkills.setText(nullSafe(v.getSoftSkills()));
    }

    private void aplicarRegrasDeEdicaoComBaseNoFeedback() {
        if (feedbackAtual == null) {
            // Sem feedback -> aluno pode editar tudo normalmente
            txtFeedbackOrientador.setText("Nenhum feedback do orientador para esta versão ainda.");
            liberarCampo(taProblema);
            liberarCampo(taSolucao);
            liberarCampo(taRepositorio);
            liberarCampo(taLinkedin);
            liberarCampo(taTecnologias);
            liberarCampo(taContribuicoes);
            liberarCampo(taHardSkills);
            liberarCampo(taSoftSkills);
            return;
        }

        // Monta texto geral do feedback
        // Monta texto geral do feedback
        StringBuilder sb = new StringBuilder();
        sb.append("Status geral: ").append(feedbackAtual.getStatus()).append("\n\n");

        if (feedbackAtual.getComentario() != null && !feedbackAtual.getComentario().isBlank()) {
            sb.append("Comentário geral:\n")
                    .append(feedbackAtual.getComentario())
                    .append("\n\n");
        }

// Itens detalhados – só mostra os que estão em AJUSTE
        if (feedbackAtual.getItens() != null && !feedbackAtual.getItens().isEmpty()) {
            boolean temAjuste = feedbackAtual.getItens().stream()
                    .anyMatch(it -> "AJUSTE".equalsIgnoreCase(it.getStatus()));

            if (temAjuste) {
                sb.append("Ajustes específicos:\n");
                feedbackAtual.getItens().forEach(it -> {
                    if (!"AJUSTE".equalsIgnoreCase(it.getStatus())) {
                        // Se for OK, não mostra nada
                        return;
                    }

                    String comentario = (it.getComentario() != null && !it.getComentario().isBlank())
                            ? it.getComentario()
                            : "(sem comentário detalhado)";

                    sb.append("• ").append(it.getCampo()).append(":\n")
                            .append("   ➤ ").append(comentario).append("\n\n");
                });
            } else {
                // Não há nenhum campo marcado com ajuste
                sb.append("Todos os campos foram aprovados. Nenhum ajuste específico pendente.\n");
            }
        }

        txtFeedbackOrientador.setText(sb.toString());


        // Mapa campo -> status
        Map<String, String> statusPorCampo = new HashMap<>();
        if (feedbackAtual.getItens() != null) {
            feedbackAtual.getItens().forEach(it ->
                    statusPorCampo.put(it.getCampo(), it.getStatus())
            );
        }

        // Regra: se status == "AJUSTE" -> pode editar; se "OK" ou ausente -> bloqueado
        aplicarRegraCampo(taProblema, statusPorCampo.get("problema"));
        aplicarRegraCampo(taSolucao, statusPorCampo.get("solucao"));
        aplicarRegraCampo(taRepositorio, statusPorCampo.get("repositorio"));
        aplicarRegraCampo(taLinkedin, statusPorCampo.get("linkedin"));
        aplicarRegraCampo(taTecnologias, statusPorCampo.get("tecnologias"));
        aplicarRegraCampo(taContribuicoes, statusPorCampo.get("contribuicoes"));
        aplicarRegraCampo(taHardSkills, statusPorCampo.get("hardSkills"));
        aplicarRegraCampo(taSoftSkills, statusPorCampo.get("softSkills"));
    }

    private void aplicarRegraCampo(TextArea ta, String status) {
        if (status == null) {
            // Se não tem item de feedback pra esse campo:
            // - se já existe feedback geral, podemos optar por bloquear ou liberar.
            // Aqui vou deixar LIBERADO (pode ajustar texto geral).
            liberarCampo(ta);
            return;
        }

        if ("AJUSTE".equalsIgnoreCase(status)) {
            liberarCampo(ta);
        } else { // "OK"
            bloquearCampo(ta);
        }
    }

    private void bloquearCampo(TextArea ta) {
        ta.setEditable(false);
        ta.getStyleClass().add("campo-bloqueado-ok");
        // Garante que texto continue legível
        ta.setOpacity(1.0);
    }

    private void liberarCampo(TextArea ta) {
        ta.setEditable(true);
        ta.getStyleClass().remove("campo-bloqueado-ok");
        ta.setOpacity(1.0);
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
