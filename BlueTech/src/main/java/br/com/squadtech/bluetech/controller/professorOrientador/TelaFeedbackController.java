package br.com.squadtech.bluetech.controller.professorOrientador;

import br.com.squadtech.bluetech.dao.FeedbackDAO;
import br.com.squadtech.bluetech.model.Feedback;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;

public class TelaFeedbackController {

    @FXML private TextArea ta_professorTG_Feedback;
    @FXML private Button btn_professorTG_finalizar;

    private long secaoId;

    private final FeedbackDAO feedbackDAO = new FeedbackDAO();

    public void setSecaoId(long secaoId) {
        this.secaoId = secaoId;
        carregarFeedbackExistente();
    }

    private void carregarFeedbackExistente() {
        // Aqui você pode buscar feedback existente no BD e popular o TextArea
        // Exemplo (pode ser ajustado conforme seu DAO):
        // Feedback feedback = feedbackDAO.buscarPorSecaoId(secaoId);
        // if (feedback != null) ta_professorTG_Feedback.setText(feedback.getComentario());
    }

    @FXML
    private void finalizar() {
        // Salvar ou atualizar feedback
        String comentario = ta_professorTG_Feedback.getText();
        Feedback feedback = new Feedback();
        feedback.setVersaoId((int) secaoId); // ajuste se necessário para pegar a versão correta
        feedback.setProfessorId(1);          // ajuste: pegar id do professor logado
        feedback.setStatus("AJUSTES");       // exemplo, você pode mudar
        feedback.setComentario(comentario);

        feedbackDAO.insert(feedback);

        // Fechar a tela
        btn_professorTG_finalizar.getScene().getWindow().hide();
    }
}
