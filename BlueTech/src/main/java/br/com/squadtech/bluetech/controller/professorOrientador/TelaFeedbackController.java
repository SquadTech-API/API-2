package br.com.squadtech.bluetech.controller.professorOrientador;

import br.com.squadtech.bluetech.dao.TGVersaoDAO;
import br.com.squadtech.bluetech.dao.FeedbackDAO;
import br.com.squadtech.bluetech.model.TGVersao;
import br.com.squadtech.bluetech.notify.NotifierFacade;
import br.com.squadtech.bluetech.dao.NotifierEvents;
import br.com.squadtech.bluetech.util.Toast;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.List;

public class TelaFeedbackController {

    @FXML private ScrollPane sp_professorTG_Info;
    @FXML private VBox vb_professorTG_InfoConteudo;
    @FXML private TextArea ta_professorTG_Feedback;
    @FXML private Button btn_professorTG_finalizar;
    @FXML private Label lblAlunoNome;

    private long secaoId;
    private long professorId; // professor logado
    private final TGVersaoDAO tgVersaoDAO = new TGVersaoDAO();
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();

    @FXML
    private void initialize() {
        // Inicialmente desabilita o botão se TextArea estiver vazio
        btn_professorTG_finalizar.setDisable(true);

        // Adiciona listener para habilitar/desabilitar botão conforme texto
        ta_professorTG_Feedback.textProperty().addListener((observable, oldValue, newValue) -> {
            btn_professorTG_finalizar.setDisable(newValue.trim().isEmpty());
        });
    }

    /**
     * Seta a seção/versão do TG do aluno e atualiza o nome do aluno.
     */
    public void setAlunoSecao(long secaoId, String nomeAluno) {
        this.secaoId = secaoId;

        if (lblAlunoNome != null) {
            lblAlunoNome.setText(nomeAluno != null ? nomeAluno : "Nome do Aluno");
        }

        carregarInformacoesAluno();
    }

    /**
     * Seta o professor que está logado (quem está dando o feedback)
     */
    public void setProfessorId(long professorId) {
        this.professorId = professorId;
    }

    /**
     * Carrega as informações da versão do aluno (somente leitura) no ScrollPane
     */
    private void carregarInformacoesAluno() {
        if (vb_professorTG_InfoConteudo == null) return;

        vb_professorTG_InfoConteudo.getChildren().clear();

        // Buscar a lista de versões ligadas à seção e pegar a mais recente
        TGVersao versao = null;
        List<TGVersao> lista = tgVersaoDAO.listBySecaoId((int) secaoId);
        if (lista != null && !lista.isEmpty()) {
            versao = lista.get(0); // já vem ordenado por Data_Criacao DESC
        }

        TextFlow textFlow = new TextFlow();

        if (versao != null) {
            adicionarSecao(textFlow, "Problema:", versao.getProblema());
            adicionarSecao(textFlow, "Solução:", versao.getSolucao());
            adicionarSecao(textFlow, "Repositório:", versao.getRepositorio());
            adicionarSecao(textFlow, "Linkedin:", versao.getLinkedin());
            adicionarSecao(textFlow, "Tecnologias:", versao.getTecnologias());
            adicionarSecao(textFlow, "Contribuições:", versao.getContribuicoes());
            adicionarSecao(textFlow, "Hard Skills:", versao.getHardSkills());
            adicionarSecao(textFlow, "Soft Skills:", versao.getSoftSkills());
        } else {
            textFlow.getChildren().add(criarConteudo("Nenhuma informação encontrada para esta seção."));
        }

        vb_professorTG_InfoConteudo.getChildren().add(textFlow);
        sp_professorTG_Info.setFitToWidth(true);
    }

    private void adicionarSecao(TextFlow textFlow, String titulo, String conteudo) {
        textFlow.getChildren().add(criarTitulo(titulo));
        textFlow.getChildren().add(criarConteudo(conteudo));
    }

    private Text criarTitulo(String texto) {
        Text t = new Text(texto + "\n");
        t.setFont(Font.font("System", FontWeight.BOLD, 16));
        return t;
    }

    private Text criarConteudo(String texto) {
        Text t = new Text((texto != null ? texto : "") + "\n\n");
        t.setFont(Font.font("System", FontWeight.NORMAL, 14));
        return t;
    }

    /**
     * Salva o feedback no banco e exibe alerta de sucesso antes de fechar a tela
     */
    @FXML
    private void finalizar() {
        String comentario = ta_professorTG_Feedback.getText().trim();
        if (comentario.isEmpty()) return;

        // Descobre a versão mais recente ligada a esta seção para salvar feedback
        List<TGVersao> lista = tgVersaoDAO.listBySecaoId((int) secaoId);
        Integer versaoId = (lista != null && !lista.isEmpty()) ? lista.get(0).getIdSecaoApi() : null;

        Long feedbackId = null;
        if (versaoId != null) {
            feedbackId = feedbackDAO.salvarFeedbackReturnId(versaoId, professorId, comentario, "AJUSTES");
        }

        Alert alert;
        if (feedbackId != null && feedbackId > 0) {
            // Enfileira notificação assíncrona ao aluno
            NotifierEvents.onFeedbackSaved(feedbackId);
            if (btn_professorTG_finalizar != null && btn_professorTG_finalizar.getScene() != null) {
                Toast.show(btn_professorTG_finalizar.getScene(), "Notificação de feedback enfileirada.");
            }
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sucesso");
            alert.setHeaderText(null);
            alert.setContentText("Feedback enviado com sucesso! Notificação de e-mail enfileirada.");
            alert.showAndWait().ifPresent(r -> {
                if (btn_professorTG_finalizar.getScene() != null) {
                    btn_professorTG_finalizar.getScene().getWindow().hide();
                }
            });
        } else {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.setContentText("Erro ao enviar feedback!");
            alert.showAndWait();
        }
    }
}
