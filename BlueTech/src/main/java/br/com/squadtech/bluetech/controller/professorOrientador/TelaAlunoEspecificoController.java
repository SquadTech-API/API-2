package br.com.squadtech.bluetech.controller.professorOrientador;

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
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TelaAlunoEspecificoController {

    @FXML private Label lblNomeAluno;
    @FXML private ToggleButton tgS1, tgS2, tgS3, tgS4, tgS5, tgS6;
    @FXML private ScrollPane spSessao1, spSessao2, spSessao3, spSessao4, spSessao5, spSessao6;
    @FXML private VBox VBoxSessao1, VBoxSessao2, VBoxSessao3, VBoxSessao4, VBoxSessao5, VBoxSessao6;

    private long alunoId; // id_perfil_aluno
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
            HBox card = new HBox(10);
            card.setStyle("-fx-padding: 10; -fx-border-color: #ccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-background-color: #f9f9f9;");

            Label lblTitulo = new Label("API " + secao.apiNumero);
            Label lblStatus = new Label(secao.status != null ? secao.status : "Em andamento");

            Label lblData = new Label(secao.dataEnvio != null
                    ? "Enviado em: " + secao.dataEnvio.format(formatter)
                    : "Sem envio ainda");

            card.getChildren().addAll(lblTitulo, lblStatus, lblData);
            int idSecao = secao.idSecao;
            card.setOnMouseClicked(e -> abrirTelaFeedback(idSecao));

            int idx = Math.max(1, Math.min(6, secao.apiNumero)) - 1;
            VBox[] vboxes = {VBoxSessao1, VBoxSessao2, VBoxSessao3, VBoxSessao4, VBoxSessao5, VBoxSessao6};
            vboxes[idx].getChildren().add(card);
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
    private void abrirTelaFeedback(long secaoId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/fxml/professorOrientador/TelaFeedback.fxml"));
            Parent root = loader.load();

            TelaFeedbackController controller = loader.getController();
            controller.setAlunoSecao(secaoId, lblNomeAluno.getText());

            // Resolve professorId pelo usuário logado
            Usuario u = SessaoUsuario.getUsuarioLogado();
            if (u != null && u.getEmail() != null) {
                ProfessorDAO profDAO = new ProfessorDAO();
                Professor p = profDAO.findByUsuarioEmail(u.getEmail());
                if (p != null && p.getId() != null) {
                    controller.setProfessorId(p.getId());
                } else {
                    log.warn("Professor não encontrado para o email do usuário logado: {}", u.getEmail());
                }
            }

            Stage stage = new Stage();
            stage.setTitle("Feedback da Seção");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            log.error("Erro ao abrir tela de feedback", e);
        }
    }
}
