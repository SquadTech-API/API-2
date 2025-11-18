package br.com.squadtech.bluetech.controller.aluno;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import br.com.squadtech.bluetech.dao.FeedbackDAO;
import br.com.squadtech.bluetech.model.Feedback;
import br.com.squadtech.bluetech.model.FeedbackItem;
import java.time.format.DateTimeFormatter;
import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import br.com.squadtech.bluetech.dao.TGSecaoDAO;
import br.com.squadtech.bluetech.dao.TGVersaoDAO;
import br.com.squadtech.bluetech.dao.NotifierEvents;
import br.com.squadtech.bluetech.model.SecaoContext;
import br.com.squadtech.bluetech.model.TGSecao;
import br.com.squadtech.bluetech.model.TGVersao;
import br.com.squadtech.bluetech.model.SessaoUsuario;
import br.com.squadtech.bluetech.model.Usuario;
import br.com.squadtech.bluetech.util.MarkdownBuilderUtil;
import br.com.squadtech.bluetech.util.MarkdownParserUtil;
import br.com.squadtech.bluetech.util.Toast;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditarSecaoAPIController implements SupportsMainController {

    private final FeedbackDAO feedbackDAO = new FeedbackDAO();
    private final DateTimeFormatter dtFeedback = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Logger log = LoggerFactory.getLogger(EditarSecaoAPIController.class);

    @FXML
    private URL location;

    @FXML
    private ResourceBundle resources;

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnSalvar;

    @FXML
    private Button btnVoltar;

    @FXML
    private TextArea txtFeedbackOrientador;

    @FXML
    private TextArea txtMarkdownEditor;

    @FXML
    void cancelaMudancas(ActionEvent event) {
        // Recarrega a tela de visualização sem salvar
        voltarTelaSecaoAPI(event);
    }

    @FXML
    void salvarNovaVersaoSessaoAPI(ActionEvent event) {
        Integer idSecao = SecaoContext.getIdSecaoSelecionada();
        if (idSecao == null) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Nenhuma seção selecionada para salvar versão.");
            return;
        }

        Usuario user = SessaoUsuario.getUsuarioLogado();
        if (user == null || user.getEmail() == null) {
            showAlert(Alert.AlertType.ERROR, "Sessão expirada", "Faça login novamente.");
            return;
        }

        String markdown = txtMarkdownEditor.getText();
        if (markdown == null || markdown.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Conteúdo vazio", "O editor está vazio. Insira o conteúdo da nova versão.");
            return;
        }

        // Remover rodapé/meta para evitar contaminação
        String markdownSemMeta = removeRodapeMeta(markdown);

        // Cria nova versão e copia TODOS os campos da anterior como base
        TGVersao novaVersao = new TGVersao();
        novaVersao.setIdSecao(idSecao);
        novaVersao.setMarkdownContent(markdownSemMeta);

        TGSecaoDAO secaoDAO = new TGSecaoDAO();
        TGSecao secao = secaoDAO.findByIdAndEmail(idSecao, user.getEmail());
        if (secao == null) {
            showAlert(Alert.AlertType.ERROR, "Acesso negado", "Esta seção não pertence ao seu usuário.");
            return;
        }
        if (secao.getIdVersao() != null) {
            TGVersaoDAO versaoDAO = new TGVersaoDAO();
            TGVersao versaoAnterior = versaoDAO.findById(secao.getIdVersao());
            if (versaoAnterior != null) {
                // Copia TODOS os campos estruturados da anterior
                novaVersao.setSemestre(versaoAnterior.getSemestre());
                novaVersao.setAno(versaoAnterior.getAno());
                novaVersao.setSemestreAno(versaoAnterior.getSemestreAno());
                novaVersao.setEmpresa(versaoAnterior.getEmpresa());
                novaVersao.setProblema(versaoAnterior.getProblema());
                novaVersao.setSolucao(versaoAnterior.getSolucao());
                novaVersao.setRepositorio(versaoAnterior.getRepositorio());
                novaVersao.setLinkedin(versaoAnterior.getLinkedin());
                novaVersao.setTecnologias(versaoAnterior.getTecnologias());
                novaVersao.setContribuicoes(versaoAnterior.getContribuicoes());
                novaVersao.setHardSkills(versaoAnterior.getHardSkills());
                novaVersao.setSoftSkills(versaoAnterior.getSoftSkills());
            }
        }

        // Re-parseia o Markdown para ATUALIZAR apenas os campos encontrados (com fallback para os copiados se falhar)
        MarkdownParserUtil.safeParseIntoFields(novaVersao, markdownSemMeta);  // Novo método, veja abaixo

        // Opcional: Validação - Se algum campo crítico ficou null (ex.: parsing falhou e não tinha anterior), alerta o usuário
        if (novaVersao.getSemestre() == null || novaVersao.getAno() == null || novaVersao.getSemestreAno() == null || novaVersao.getEmpresa() == null) {
            showAlert(Alert.AlertType.WARNING, "Aviso", "Alguns metadados parecem ausentes no Markdown. Verifique o cabeçalho e tente novamente. Salvando com valores anteriores.");
        }

        TGVersaoDAO versaoDAO = new TGVersaoDAO();
        versaoDAO.ensureSchemaUpToDate();
        versaoDAO.createTableIfNotExists();
        Integer idVersao = versaoDAO.insertReturningId(novaVersao);
        if (idVersao == null) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Falha ao salvar nova versão.");
            return;
        }

        secaoDAO.updateIdVersao(idSecao, idVersao);

        // Enfileira notificação assíncrona ao professor via helper central
        NotifierEvents.onNewVersionSubmitted(idVersao.longValue());
        if (btnSalvar != null && btnSalvar.getScene() != null) {
            Toast.show(btnSalvar.getScene(), "Notificação de envio enfileirada.");
        }

        showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Nova versão salva e promovida com sucesso.");
        voltarTelaSecaoAPI(event);
    }

    @FXML
    void voltarTelaSecaoAPI(ActionEvent event) {
        if (painelPrincipalController != null) {
            try {
                // Recupera a seção atual do contexto (definida na tela anterior)
                Integer idSecao = SecaoContext.getIdSecaoSelecionada();
                if (idSecao == null) {
                    // Sem seção selecionada, apenas não navega (ou poderia exibir um alerta futuramente)
                    return;
                }
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
        assert txtMarkdownEditor != null : "fx:id=\"txtMarkdownEditor\" was not injected: check your FXML file 'EditarSecaoAPI.fxml'.";

        Integer idVersaoSelecionada = SecaoContext.getIdVersaoSelecionada();
        TGVersaoDAO versaoDAO = new TGVersaoDAO();
        TGSecaoDAO secaoDAO = new TGSecaoDAO();
        TGSecao secao = null;
        Integer idSecao = SecaoContext.getIdSecaoSelecionada();
        Usuario user = SessaoUsuario.getUsuarioLogado();
        if (idSecao != null && user != null && user.getEmail() != null) {
            try { secao = secaoDAO.findByIdAndEmail(idSecao, user.getEmail()); } catch (Exception ignore) {}
        }

        if (idVersaoSelecionada != null) {
            try {
                TGVersao v = versaoDAO.findById(idVersaoSelecionada);
                if (v != null) {
                    if (v.getMarkdownContent() != null && !v.getMarkdownContent().isBlank()) {
                        txtMarkdownEditor.setText(v.getMarkdownContent());
                    } else {
                        txtMarkdownEditor.setText(MarkdownBuilderUtil.buildMarkdownFromVersao(v, secao));
                    }
                    // <<< ADICIONADO: carrega o feedback desta versão
                    preencherFeedbackOrientador(v);
                }
            } catch (Exception e) {
                log.error("Erro ao carregar versão selecionada para edição", e);
            }
        } else if (secao != null && secao.getIdVersao() > 0) {
            try {
                TGVersao v = versaoDAO.findById(secao.getIdVersao());
                if (v != null) {
                    if (v.getMarkdownContent() != null && !v.getMarkdownContent().isBlank()) {
                        txtMarkdownEditor.setText(v.getMarkdownContent());
                    } else {
                        txtMarkdownEditor.setText(MarkdownBuilderUtil.buildMarkdownFromVersao(v, secao));
                    }
                    // <<< ADICIONADO: feedback da última versão da seção
                    preencherFeedbackOrientador(v);
                }
            } catch (Exception e) { /* ignora erros de leitura aqui */ }
        }

    }


    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private PainelPrincipalController painelPrincipalController;

    @Override
    public void setPainelPrincipalController(PainelPrincipalController controller) {
        this.painelPrincipalController = controller;
    }

    // Remove bloco de rodapé começando em '---' e quaisquer linhas "API número:", "Versão:", "Data da versão:" que eventualmente estejam no corpo
    private String removeRodapeMeta(String markdown) {
        if (markdown == null) return null;
        String[] ls = markdown.replace("\r", "").split("\n");
        StringBuilder out = new StringBuilder();
        boolean meta = false;
        for (String raw : ls) {
            String t = raw.trim();
            if (!meta && (t.equals("---") || t.startsWith("API número:") || t.startsWith("Versão:") || t.startsWith("Data da versão:"))) {
                meta = true; // inicia corte de rodapé
                continue;
            }
            if (meta) continue; // ignora tudo depois do meta
            out.append(raw).append("\n");
        }
        return out.toString();
    }
    private void preencherFeedbackOrientador(TGVersao v) {
        if (v == null) {
            txtFeedbackOrientador.setText("Nenhuma versão selecionada.");
            return;
        }

        // tenta pegar o id da versão de forma robusta
        Long versaoId = null;
        try {
            if (v.getIdSecaoApi() != null) {
                versaoId = v.getIdSecaoApi().longValue();
            }
        } catch (Exception ignore) {}


        if (versaoId == null) {
            txtFeedbackOrientador.setText("Não foi possível identificar a versão para carregar o feedback.");
            return;
        }

        Feedback fb = feedbackDAO.buscarPorVersaoId(versaoId);
        if (fb == null) {
            txtFeedbackOrientador.setText("Nenhum feedback enviado pelo orientador para esta versão.");
            return;
        }

        StringBuilder sb = new StringBuilder();

        sb.append("Status: ").append(fb.getStatus()).append("\n");
        if (fb.getCreatedAt() != null) {
            sb.append("Data: ").append(fb.getCreatedAt().format(dtFeedback)).append("\n");
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

        txtFeedbackOrientador.setText(sb.toString());
    }

    /**
     * Tradução dos ids internos dos campos para algo legível pro aluno.
     */
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

}
