package br.com.squadtech.bluetech.controller.professorOrientador;

import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import br.com.squadtech.bluetech.dao.FeedbackDAO;
import br.com.squadtech.bluetech.dao.TGVersaoDAO;
import br.com.squadtech.bluetech.model.FeedbackItem;
import br.com.squadtech.bluetech.model.TGVersao;
import br.com.squadtech.bluetech.util.Toast;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class TelaFeedbackController implements SupportsMainController {

    private static final Logger log = LoggerFactory.getLogger(TelaFeedbackController.class);

    @FXML private VBox vbFeedbackForm;
    @FXML private Label lblAlunoNome;
    @FXML private Button btn_professorTG_finalizar;
    @FXML private TextArea ta_comentario_geral;

    private PainelPrincipalController painelPrincipalController;

    private long versaoId;
    private long professorId;
    private final TGVersaoDAO tgVersaoDAO = new TGVersaoDAO();
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();

    private final Map<String, FeedbackItemComponents> feedbackComponents = new HashMap<>();

    @Override
    public void setPainelPrincipalController(PainelPrincipalController controller) {
        this.painelPrincipalController = controller;
    }

    private static class FeedbackItemComponents {
        ToggleGroup group;
        RadioButton rbOk;
        RadioButton rbAjuste;
        TextArea taComentario;
    }

    @FXML
    private void initialize() {
        btn_professorTG_finalizar.setDisable(true);
    }

    public void setAlunoVersao(long versaoId, String nomeAluno, long professorId) {
        this.versaoId = versaoId;
        this.professorId = professorId;
        lblAlunoNome.setText(nomeAluno);
        carregarCamposFeedback();
        preencherComFeedbackExistente();
    }

    private void carregarCamposFeedback() {
        TGVersao v = tgVersaoDAO.findById((int) versaoId);
        if (v == null) return;

        criarCampo("Problema:", v.getProblema(), "problema");
        criarCampo("Solução:", v.getSolucao(), "solucao");
        criarCampo("Repositório:", v.getRepositorio(), "repositorio");
        criarCampo("LinkedIn:", v.getLinkedin(), "linkedin");
        criarCampo("Tecnologias:", v.getTecnologias(), "tecnologias");
        criarCampo("Contribuições:", v.getContribuicoes(), "contribuicoes");
        criarCampo("Hard Skills:", v.getHardSkills(), "hardSkills");
        criarCampo("Soft Skills:", v.getSoftSkills(), "softSkills");
    }

    private void criarCampo(String titulo, String conteudo, String campoId) {
        VBox campoBox = new VBox(5);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 14));

        // TextField “placeholder” somente leitura
        TextField tf = new TextField(conteudo != null ? conteudo : "");
        tf.setEditable(false);
        tf.setStyle("-fx-opacity: 1; -fx-background-color: #f9f9f9;");

        // Botões de OK/Ajuste
        HBox botoes = new HBox(10);
        RadioButton rbOk = new RadioButton("OK");
        RadioButton rbAjuste = new RadioButton("Ajuste");
        ToggleGroup group = new ToggleGroup();
        rbOk.setToggleGroup(group);
        rbAjuste.setToggleGroup(group);
        botoes.getChildren().addAll(rbOk, rbAjuste);

        // Comentário (visível apenas se Ajuste)
        TextArea taComentario = new TextArea();
        taComentario.setPromptText("Descreva o ajuste necessário...");
        taComentario.setVisible(false);
        taComentario.setManaged(false);
        taComentario.setPrefHeight(60);

        group.selectedToggleProperty().addListener((obs, old, novo) -> {
            if (novo == null) return;
            boolean isAjuste = novo == rbAjuste;
            taComentario.setVisible(isAjuste);
            taComentario.setManaged(isAjuste);
            verificarCamposPreenchidos();
        });

        campoBox.getChildren().addAll(lblTitulo, tf, botoes, taComentario);
        campoBox.setStyle("-fx-padding: 10; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        vbFeedbackForm.getChildren().add(campoBox);

        FeedbackItemComponents comp = new FeedbackItemComponents();
        comp.group = group;
        comp.rbOk = rbOk;
        comp.rbAjuste = rbAjuste;
        comp.taComentario = taComentario;
        feedbackComponents.put(campoId, comp);
    }

    private void verificarCamposPreenchidos() {
        boolean allSelected = feedbackComponents.values().stream()
                .allMatch(c -> c.group.getSelectedToggle() != null);
        btn_professorTG_finalizar.setDisable(!allSelected);
    }

    @FXML
    private void finalizar() {
        try {
            // ===== monte os itens como você já faz acima =====
            List<FeedbackItem> itens = new ArrayList<>();
            boolean temAjuste = false;

            for (Map.Entry<String, FeedbackItemComponents> e : feedbackComponents.entrySet()) {
                String campo = e.getKey();
                FeedbackItemComponents c = e.getValue();
                String status = c.rbOk.isSelected() ? "OK" : "AJUSTE";
                String comentario = c.taComentario.getText().trim();

                if ("AJUSTE".equals(status)) temAjuste = true;
                if ("AJUSTE".equals(status) && comentario.isEmpty()) {
                    Toast.show(btn_professorTG_finalizar.getScene(),
                            "Informe o comentário para o campo: " + campo);
                    return;
                }
                itens.add(new FeedbackItem(null, campo, status, comentario));
            }

            String comentarioGeral = ta_comentario_geral.getText().trim();

            Long id = feedbackDAO.salvarFeedbackReturnIdAndNotify(
                    versaoId, professorId, comentarioGeral, itens);

            if (id != null && id > 0) {
                new Alert(Alert.AlertType.INFORMATION,
                        "Feedback enviado e aluno notificado!", ButtonType.OK).showAndWait();

                // troca a tela na MESMA janela
                painelPrincipalController.loadContent("/fxml/professorOrientador/telaOrientador.fxml");
            } else {
                new Alert(Alert.AlertType.ERROR, "Erro ao salvar feedback!").showAndWait();
            }

        } catch (IOException io) {
            log.error("Erro ao abrir tela do orientador: {}", io.getMessage(), io);
            new Alert(Alert.AlertType.ERROR, "Erro ao abrir tela do orientador!").showAndWait();

        } catch (Exception e) {
            log.error("Erro ao finalizar feedback: {}", e.getMessage(), e);
            new Alert(Alert.AlertType.ERROR, "Erro ao finalizar feedback!").showAndWait();
        }
    }

    private void preencherComFeedbackExistente() {
        try {
            // Busca o feedback já salvo para esta versão (se existir)
            var feedback = feedbackDAO.buscarPorVersaoId(versaoId);
            if (feedback == null) return;

            // Comentário geral
            if (feedback.getComentario() != null) {
                ta_comentario_geral.setText(feedback.getComentario());
            }

            // Mapeia itens por "campo" para facilitar acesso
            Map<String, FeedbackItem> itensPorCampo = new HashMap<>();
            if (feedback.getItens() != null) {
                for (FeedbackItem it : feedback.getItens()) {
                    if (it.getCampo() != null) {
                        itensPorCampo.put(it.getCampo(), it);
                    }
                }
            }

            // Itera pelos componentes montados em criarCampo(...) e aplica o que veio do banco
            feedbackComponents.forEach((campoId, comp) -> {
                FeedbackItem salvo = itensPorCampo.get(campoId);
                if (salvo == null) return;

                String st = salvo.getStatus() != null ? salvo.getStatus().toUpperCase() : "";
                if ("AJUSTE".equals(st)) {
                    comp.rbAjuste.setSelected(true);
                    // Mostra e preenche o comentário
                    comp.taComentario.setText(salvo.getComentario() != null ? salvo.getComentario() : "");
                    comp.taComentario.setVisible(true);
                    comp.taComentario.setManaged(true);
                } else {
                    // Trata como OK para qualquer coisa diferente de AJUSTE
                    comp.rbOk.setSelected(true);
                    // Garante que a área de comentário fique oculta
                    comp.taComentario.setVisible(false);
                    comp.taComentario.setManaged(false);
                }
            });

            // Se todos os itens tiverem seleção, habilita o botão
            verificarCamposPreenchidos();

        } catch (Exception e) {
            // só loga se quiser; não impede a tela de abrir
            // log.warn("Não foi possível pré-carregar feedback existente: {}", e.getMessage(), e);
        }
    }




}
