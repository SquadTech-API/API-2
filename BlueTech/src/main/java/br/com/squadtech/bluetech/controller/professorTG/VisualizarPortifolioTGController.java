package br.com.squadtech.bluetech.controller.professorTG;

import br.com.squadtech.bluetech.dao.*;
import br.com.squadtech.bluetech.model.*;
import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Collectors;

public class VisualizarPortifolioTGController implements SupportsMainController {

    @FXML
    private VBox cardsBox;

    @FXML
    private ComboBox<String> comboCurso;

    @FXML
    private TextField TextFieldNomePesquisa;

    @FXML
    private Button btnBuscar;

    private PainelPrincipalController painelPrincipalController;

    private final PerfilAlunoDAO perfilAlunoDAO = new PerfilAlunoDAO();
    private final TGSecaoDAO tgSecaoDAO = new TGSecaoDAO();
    private final OrientaDAO orientaDAO = new OrientaDAO();
    private final ProfessorDAO professorDAO = new ProfessorDAO();
    private final TGPortifolioDAO portifolioDAO = new TGPortifolioDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    public void setPainelPrincipalController(PainelPrincipalController controller) {
        this.painelPrincipalController = controller;
    }

    @FXML
    private void initialize() {
        // Inicialização se necessário
    }

    @FXML
    private void buscarPortifolio(ActionEvent event) {
        String termo = TextFieldNomePesquisa.getText() != null ? TextFieldNomePesquisa.getText().trim() : "";
        criarCards(termo);
    }

    // Sobrecarga para manter compatibilidade com chamadas antigas (ex:
    // MenuProfessorTGController)
    public void criarCards(String curso, String nomePesquisa) {
        criarCards(nomePesquisa);
    }

    public void criarCards(String termo) {
        cardsBox.getChildren().clear();

        // Busca alunos filtrando por nome ou email
        List<PerfilAluno> alunos = perfilAlunoDAO.buscarAlunosPorNomeOuEmail(termo);

        if (alunos.isEmpty()) {
            Label lbl = new Label("Nenhum aluno encontrado.");
            lbl.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");
            cardsBox.getChildren().add(lbl);
            return;
        }

        for (PerfilAluno a : alunos) {
            String nomeAluno = a.getNomeAluno();
            String emailAluno = a.getEmailUsuario();

            List<Orienta> orientacoes = orientaDAO.findByAlunoId(
                    a.getIdPerfilAluno() != null ? a.getIdPerfilAluno().longValue() : -1L);

            String professores = "Sem professor";
            if (orientacoes != null && !orientacoes.isEmpty()) {
                professores = orientacoes.stream()
                        .map(o -> obterEmailProfessor(o.getProfessorId()))
                        .map(email -> {
                            if (email == null)
                                return null;
                            Usuario u = usuarioDAO.findByEmail(email);
                            return u != null && u.getNome() != null ? u.getNome() : email;
                        })
                        .filter(n -> n != null && !n.isBlank())
                        .collect(Collectors.joining(", "));
                if (professores.isBlank())
                    professores = "Sem professor";
            }

            TGPortifolio portifolio = portifolioDAO.findByAlunoId(
                    a.getIdPerfilAluno() != null ? a.getIdPerfilAluno().longValue() : -1L);

            String statusPortifolio;
            if (portifolio != null) {
                statusPortifolio = portifolio.getStatus()
                        + (portifolio.getPercentualConclusao() != null
                                ? (" - " + portifolio.getPercentualConclusao() + "%")
                                : "");
            } else if (a.getEmailUsuario() != null) {
                int qtd = tgSecaoDAO.countSecoes(a.getEmailUsuario());
                statusPortifolio = qtd > 0 ? ("Seções enviadas: " + qtd) : "Sem envios";
            } else {
                statusPortifolio = "Sem dados";
            }

            Label t1 = new Label(nomeAluno);
            t1.getStyleClass().add("title");

            Label t2 = new Label("Email: " + emailAluno); // Mostra email
            t2.getStyleClass().add("subtitle");

            Label t3 = new Label("Orientador(es): " + professores);
            t3.getStyleClass().add("subtitle");

            Label t4 = new Label("Status: " + statusPortifolio);
            t4.getStyleClass().add("subtitle");

            VBox textBox = new VBox(4, t1, t2, t3, t4);
            HBox.setHgrow(textBox, Priority.ALWAYS);

            Button eye = new Button("👁");
            eye.getStyleClass().add("eye-btn");
            eye.setFocusTraversable(false);

            HBox card = new HBox(12, textBox, eye);
            card.setAlignment(Pos.CENTER_LEFT);
            card.getStyleClass().add("card-item");
            card.setPadding(new Insets(12));

            card.setOnMouseClicked((MouseEvent e) -> abrirVisualizador(emailAluno));
            eye.setOnAction(e -> abrirVisualizador(emailAluno));

            cardsBox.getChildren().add(card);
        }
    }

    private void abrirVisualizador(String emailAluno) {
        if (painelPrincipalController == null)
            return;
        try {
            VisualizadorTGController controller = painelPrincipalController.loadContentReturnController(
                    "/fxml/professorTG/VisualizadorTG.fxml",
                    VisualizadorTGController.class);
            if (controller != null) {
                controller.receberDadosAluno(emailAluno);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String obterEmailProfessor(Long professorId) {
        if (professorId == null)
            return null;
        Professor p = professorDAO.findById(professorId);
        return p != null ? p.getUsuarioEmail() : null;
    }
}
