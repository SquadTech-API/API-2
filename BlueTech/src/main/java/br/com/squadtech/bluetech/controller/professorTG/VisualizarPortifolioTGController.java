package br.com.squadtech.bluetech.controller.professorTG;

import br.com.squadtech.bluetech.dao.PerfilAlunoDAO;
import br.com.squadtech.bluetech.dao.TGSecaoDAO;
import br.com.squadtech.bluetech.dao.OrientaDAO;
import br.com.squadtech.bluetech.dao.ProfessorDAO;
import br.com.squadtech.bluetech.dao.TGPortifolioDAO;
import br.com.squadtech.bluetech.model.PerfilAluno;
import br.com.squadtech.bluetech.model.Orienta;
import br.com.squadtech.bluetech.model.Professor;
import br.com.squadtech.bluetech.model.TGPortifolio;
import br.com.squadtech.bluetech.dao.UsuarioDAO;
import br.com.squadtech.bluetech.model.Usuario;
import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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
    private ComboBox<String> comboSemestre;

    @FXML
    private ComboBox<String> comboPortifolio;

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
        comboCurso.getItems().addAll("Banco de Dados", "Análise de Sistemas");
        comboSemestre.getItems().addAll("5º Semestre", "6º Semestre");
    }

    @FXML
    private void buscarPortifolio(ActionEvent event) {
        String curso = comboCurso.getValue() != null ? comboCurso.getValue() : "";
        String semestre = comboSemestre.getValue() != null ? comboSemestre.getValue() : "";
        criarCards(semestre, curso);
    }

    public void criarCards(String semestre, String curso) {
        cardsBox.getChildren().clear();

        List<PerfilAluno> alunos = perfilAlunoDAO.listarAlunosParaCard(null, null);

        for (PerfilAluno a : alunos) {
            String nomeAluno = a.getNomeAluno();

            List<Orienta> orientacoes = orientaDAO.findByAlunoId(a.getIdPerfilAluno() != null ? a.getIdPerfilAluno().longValue() : -1L);
            String professores = "Sem professor";
            if (orientacoes != null && !orientacoes.isEmpty()) {
                professores = orientacoes.stream()
                        .map(o -> obterEmailProfessor(o.getProfessorId()))
                        .map(email -> {
                            if (email == null) return null;
                            Usuario u = usuarioDAO.findByEmail(email);
                            return u != null && u.getNome() != null ? u.getNome() : email;
                        })
                        .filter(n -> n != null && !n.isBlank())
                        .collect(Collectors.joining(", "));
                if (professores.isBlank()) professores = "Sem professor";
            }

            TGPortifolio portifolio = portifolioDAO.findByAlunoId(a.getIdPerfilAluno() != null ? a.getIdPerfilAluno().longValue() : -1L);
            String statusPortifolio;
            if (portifolio != null) {
                statusPortifolio = portifolio.getStatus() + (portifolio.getPercentualConclusao() != null ? (" - " + portifolio.getPercentualConclusao() + "%") : "");
            } else if (a.getEmailUsuario() != null) {
                int qtd = tgSecaoDAO.countSecoes(a.getEmailUsuario());
                statusPortifolio = qtd > 0 ? ("Seções enviadas: " + qtd) : "Sem envios";
            } else {
                statusPortifolio = "Sem dados";
            }

            Label t1 = new Label(nomeAluno);
            t1.getStyleClass().add("title");

            Label t2 = new Label("Orientador(es): " + professores);
            t2.getStyleClass().add("subtitle");

            Label t3 = new Label("Status: " + statusPortifolio);
            t3.getStyleClass().add("subtitle");

            VBox textBox = new VBox(4, t1, t2, t3);
            HBox.setHgrow(textBox, Priority.ALWAYS);

            Button eye = new Button("👁");
            eye.getStyleClass().add("eye-btn");
            eye.setFocusTraversable(false);

            HBox card = new HBox(12, textBox, eye);
            card.setAlignment(Pos.CENTER_LEFT);
            card.getStyleClass().add("card-item");
            card.setPadding(new Insets(12));

            card.setOnMouseClicked((MouseEvent e) -> abrirVisualizador(nomeAluno, semestre, curso));
            eye.setOnAction(e -> abrirVisualizador(nomeAluno, semestre, curso));

            cardsBox.getChildren().add(card);
        }
    }
    private Image carregarImagemPadrao() {
        try {
            return new Image(getClass().getResourceAsStream("/images/Usuario.png"));
        } catch (Exception e) {
            System.out.println("⚠️ Imagem padrão não encontrada no resources/assets/Usuario.png");
            return new Image("https://cdn-icons-png.flaticon.com/512/847/847969.png"); // fallback online
        }
    }

      private void abrirVisualizador(String nomeAluno, String semestre, String curso) {

        if (painelPrincipalController == null) return;
        try {
            VisualizadorTGController controller =
                    painelPrincipalController.loadContentReturnController(
                            "/fxml/professorTG/VisualizadorTG.fxml",
                            VisualizadorTGController.class
                    );
            if (controller != null) {
                controller.receberDadosAluno(nomeAluno, semestre, curso);
            }
        } catch (Exception ex) {
            // log via console para simplificar
            ex.printStackTrace();
        }
    }

    private String obterEmailProfessor(Long professorId) {
        if (professorId == null) return null;
        Professor p = professorDAO.findById(professorId);
        return p != null ? p.getUsuarioEmail() : null;
    }
}
