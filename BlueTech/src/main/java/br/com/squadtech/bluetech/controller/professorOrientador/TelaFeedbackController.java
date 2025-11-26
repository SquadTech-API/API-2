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
import javafx.scene.layout.Priority;

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
    @FXML private Button btnAprovado;
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
        if (btnAprovado != null) {
            btnAprovado.setDisable(true);
        }
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

        TextArea taConteudo = new TextArea(conteudo != null ? conteudo : "");
        taConteudo.setEditable(false);
        taConteudo.setStyle("-fx-opacity: 1; -fx-background-color: #f9f9f9;");
        taConteudo.getStyleClass().add("conteudo-aluno-area");

        HBox botoes = new HBox(10);
        RadioButton rbOk = new RadioButton("OK");
        RadioButton rbAjuste = new RadioButton("Ajuste");
        ToggleGroup group = new ToggleGroup();
        rbOk.setToggleGroup(group);
        rbAjuste.setToggleGroup(group);
        botoes.getChildren().addAll(rbOk, rbAjuste);

        TextArea taComentario = new TextArea();
        taComentario.setPromptText("Descreva o ajuste necessário...");
        taComentario.setVisible(false);
        taComentario.setManaged(false);
        // taComentario.setPrefHeight(60); // Removido para permitir crescimento automático

        group.selectedToggleProperty().addListener((obs, old, novo) -> {
            if (novo == null) return;
            boolean isAjuste = novo == rbAjuste;
            taComentario.setVisible(isAjuste);
            taComentario.setManaged(isAjuste);
            atualizarEstadoBotoes();
        });

        taConteudo.setWrapText(true);
        taConteudo.setPrefRowCount(1); // Altura mínima de 1 linha
        taConteudo.setPrefHeight(Region.USE_COMPUTED_SIZE); // Deixa o JavaFX calcular a altura
        taConteudo.setMinHeight(Region.USE_PREF_SIZE);

        VBox.setVgrow(taConteudo, Priority.ALWAYS);
        campoBox.getChildren().addAll(lblTitulo, taConteudo, botoes, taComentario);
        campoBox.setStyle("-fx-padding: 10; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        vbFeedbackForm.getChildren().add(campoBox);

        FeedbackItemComponents comp = new FeedbackItemComponents();
        comp.group = group;
        comp.rbOk = rbOk;
        comp.rbAjuste = rbAjuste;
        comp.taComentario = taComentario;
        feedbackComponents.put(campoId, comp);
    }

    /**
     * Atualiza o estado dos botões:
     *  - Finalizar: habilita se todos os campos estiverem selecionados E existir algum AJUSTE
     *  - Aprovado: habilita apenas se TODOS os campos estiverem selecionados e TODOS forem OK
     */
    private void atualizarEstadoBotoes() {
        boolean allSelected = feedbackComponents.values().stream()
                .allMatch(c -> c.group.getSelectedToggle() != null);

        boolean anyAjuste = feedbackComponents.values().stream()
                .anyMatch(c -> c.rbAjuste.isSelected());

        boolean allOk = allSelected && feedbackComponents.values().stream()
                .allMatch(c -> c.rbOk.isSelected());

        // Finalizar só funciona para casos com AJUSTE
        btn_professorTG_finalizar.setDisable(!(allSelected && anyAjuste));

        // Aprovado só quando tudo está OK
        if (btnAprovado != null) {
            btnAprovado.setDisable(!allOk);
        }
    }

    // Mantém por compatibilidade
    private void verificarCamposPreenchidos() {
        atualizarEstadoBotoes();
    }

    @FXML
    private void finalizar() {
        try {
            List<FeedbackItem> itens = new ArrayList<>();

            for (Map.Entry<String, FeedbackItemComponents> e : feedbackComponents.entrySet()) {
                String campo = e.getKey();
                FeedbackItemComponents c = e.getValue();
                String status = c.rbOk.isSelected() ? "OK" : "AJUSTE";
                String comentario = c.taComentario.getText().trim();

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

    @FXML
    private void aprovar() {
        // Garante visualmente que está tudo OK
        feedbackComponents.forEach((campo, comp) -> {
            comp.rbOk.setSelected(true);
            comp.taComentario.clear();
            comp.taComentario.setVisible(false);
            comp.taComentario.setManaged(false);
        });
        atualizarEstadoBotoes();
        finalizar(); // status geral = APROVADO lá no DAO, pois não haverá AJUSTE
    }

    private void preencherComFeedbackExistente() {
        try {
            var feedback = feedbackDAO.buscarPorVersaoId(versaoId);
            if (feedback == null) return;

            if (feedback.getComentario() != null) {
                ta_comentario_geral.setText(feedback.getComentario());
            }

            Map<String, FeedbackItem> itensPorCampo = new HashMap<>();
            if (feedback.getItens() != null) {
                for (FeedbackItem it : feedback.getItens()) {
                    if (it.getCampo() != null) {
                        itensPorCampo.put(it.getCampo(), it);
                    }
                }
            }

            feedbackComponents.forEach((campoId, comp) -> {
                FeedbackItem salvo = itensPorCampo.get(campoId);
                if (salvo == null) return;

                String st = salvo.getStatus() != null ? salvo.getStatus().toUpperCase() : "";
                if ("AJUSTE".equals(st)) {
                    comp.rbAjuste.setSelected(true);
                    comp.taComentario.setText(
                            salvo.getComentario() != null ? salvo.getComentario() : ""
                    );
                    comp.taComentario.setVisible(true);
                    comp.taComentario.setManaged(true);
                } else {
                    comp.rbOk.setSelected(true);
                    comp.taComentario.setVisible(false);
                    comp.taComentario.setManaged(false);
                }
            });

            atualizarEstadoBotoes();

        } catch (Exception e) {
            log.warn("Não foi possível pré-carregar feedback existente: {}", e.getMessage());
        }
    }
}
