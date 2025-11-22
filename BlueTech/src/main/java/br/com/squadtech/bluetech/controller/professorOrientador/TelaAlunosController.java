package br.com.squadtech.bluetech.controller.professorOrientador;

import br.com.squadtech.bluetech.dao.PerfilAlunoDAO;
import br.com.squadtech.bluetech.model.PerfilAluno;
import br.com.squadtech.bluetech.model.SessaoUsuario;
import br.com.squadtech.bluetech.model.Usuario;
import br.com.squadtech.bluetech.model.Professor;
import br.com.squadtech.bluetech.dao.ProfessorDAO;
import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TelaAlunosController implements SupportsMainController {

    @FXML private Button ButtonPesquisar;
    @FXML private ToggleButton ToggleButonTodosAlunos;
    @FXML private ToggleButton ToggleButtonCorrigidos;
    @FXML private ToggleButton ToggleButtonNaoCorrigidos;
    @FXML private TextField TextFieldNomePesquisa;
    @FXML private VBox VBoxListaAlunos;

    private PainelPrincipalController painelPrincipalController;

    private final PerfilAlunoDAO perfilAlunoDAO = new PerfilAlunoDAO();
    private List<PerfilAluno> todosAlunos = new ArrayList<>();

    private static final Logger log = LoggerFactory.getLogger(TelaAlunosController.class);

    @Override
    public void setPainelPrincipalController(PainelPrincipalController painelPrincipalController) {
        this.painelPrincipalController = painelPrincipalController;
    }

    @FXML
    public void initialize() {
        carregarAlunosDoBanco();

        ToggleButonTodosAlunos.setSelected(true);

        ToggleButonTodosAlunos.setOnAction(e -> {
            if (!ToggleButonTodosAlunos.isSelected()) ToggleButonTodosAlunos.setSelected(true);
            ToggleButtonCorrigidos.setSelected(false);
            ToggleButtonNaoCorrigidos.setSelected(false);
            aplicarFiltroERender();
        });

        ToggleButtonCorrigidos.setOnAction(e -> {
            boolean sel = ToggleButtonCorrigidos.isSelected();
            ToggleButonTodosAlunos.setSelected(!sel && !ToggleButtonNaoCorrigidos.isSelected());
            if (sel) ToggleButtonNaoCorrigidos.setSelected(false);
            aplicarFiltroERender();
        });

        ToggleButtonNaoCorrigidos.setOnAction(e -> {
            boolean sel = ToggleButtonNaoCorrigidos.isSelected();
            ToggleButonTodosAlunos.setSelected(!sel && !ToggleButtonCorrigidos.isSelected());
            if (sel) ToggleButtonCorrigidos.setSelected(false);
            aplicarFiltroERender();
        });

        ButtonPesquisar.setOnAction(e -> aplicarFiltroERender());
        TextFieldNomePesquisa.textProperty().addListener((obs, o, n) -> aplicarFiltroERender());

        aplicarFiltroERender();
    }

    private void carregarAlunosDoBanco() {
        // Resolve professorId pelo usuário logado
        Usuario u = SessaoUsuario.getUsuarioLogado();
        Long professorId = null;
        if (u != null && u.getEmail() != null) {
            ProfessorDAO profDAO = new ProfessorDAO();
            Professor p = profDAO.findByUsuarioEmail(u.getEmail());
            if (p != null && p.getId() != null) {
                professorId = p.getId();
            } else {
                log.warn("Professor não encontrado para o email do usuário logado: {}", u.getEmail());
            }
        }
        todosAlunos = perfilAlunoDAO.listarAlunosParaCard(null, professorId);
        if (professorId == null) {
            todosAlunos.clear();
        }
    }

    private void aplicarFiltroERender() {
        String termo = TextFieldNomePesquisa.getText() == null ? "" : TextFieldNomePesquisa.getText().trim().toLowerCase(Locale.ROOT);
        List<PerfilAluno> filtrados = new ArrayList<>();

        for (PerfilAluno a : todosAlunos) {
            boolean matchesNome = termo.isEmpty() || (a.getNomeAluno() != null && a.getNomeAluno().toLowerCase(Locale.ROOT).contains(termo));
            // Usamos o status do último feedback para determinar se está corrigido
            // 'APROVADO' significa corrigido. 'AJUSTES' ou null significa não corrigido/pendente.
            // status do último feedback (APROVADO / AJUSTES / null)
            String fb = a.getUltimoFeedbackStatus();

// Lógica correta:
            boolean isCorrigido = "APROVADO".equals(fb) || "AJUSTES".equals(fb);
            boolean isNaoCorrigido = !"APROVADO".equals(fb) && !"AJUSTES".equals(fb);
// isto pega: null, EM_ANDAMENTO, PENDENTE, etc.

            boolean matchesStatus =
                    ToggleButonTodosAlunos.isSelected() ||
                            (ToggleButtonCorrigidos.isSelected() && isCorrigido) ||
                            (ToggleButtonNaoCorrigidos.isSelected() && isNaoCorrigido);

            if (matchesNome && matchesStatus) filtrados.add(a);
        }

        renderizarCards(filtrados);
    }

    private void renderizarCards(List<PerfilAluno> alunos) {
        VBoxListaAlunos.getChildren().clear();
        for (PerfilAluno a : alunos) {
            VBoxListaAlunos.getChildren().add(criarCard(a));
        }
    }

    private Node criarCard(PerfilAluno a) {

        HBox card = new HBox(12);
        card.getStyleClass().addAll("card", "student-row");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setCursor(Cursor.HAND);
        card.setPrefHeight(90);

        VBox info = new VBox(4);

        // NOME
        Label lblNome = new Label(a.getNomeAluno());
        lblNome.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Idade
        Label lblIdade = new Label("Idade: " + (a.getIdade() != null ? a.getIdade() : "—") + " Anos");
        lblIdade.setStyle("-fx-font-size: 14px;");

        // TIPO TG
        Label lblLinkGithub = new Label("GitHub: " + (a.getLinkGithub() != null ? a.getLinkGithub() : "—"));
        lblLinkGithub.setStyle("-fx-font-size: 14px;");

        // STATUS
        String status = a.getUltimoFeedbackStatus() != null ? a.getUltimoFeedbackStatus() : "Pendente";
        Label lblStatus = new Label("Status: " + status);
        lblStatus.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        info.getChildren().addAll(lblNome, lblIdade, lblLinkGithub, lblStatus);

        // ÍCONE
        String iconPath = "APROVADO".equals(status)
                ? "/images/check.png"
                : "/images/excla.png";

        ImageView iv = new ImageView(loadImageSafe(iconPath));
        iv.setFitWidth(32);
        iv.setFitHeight(32);
        iv.setPreserveRatio(true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        card.getChildren().addAll(info, spacer, iv);

        // Hover
        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color: rgba(0,0,0,0.04); -fx-background-radius: 10;")
        );
        card.setOnMouseExited(e ->
                card.setStyle("")
        );

        // Clique → Abrir aluno específico
        card.setOnMouseClicked(e -> {
            try {
                TelaAlunoEspecificoController controller =
                        painelPrincipalController.loadContentReturnController(
                                "/fxml/professorOrientador/telaAlunoEspecifico.fxml",
                                TelaAlunoEspecificoController.class
                        );

                controller.setAlunoId(
                        a.getIdPerfilAluno() != null ? a.getIdPerfilAluno().longValue() : 0L,
                        a.getNomeAluno()
                );
            } catch (IOException ex) {
                log.error("Erro ao abrir tela do aluno específico", ex);
            }
        });

        return card;
    }

    private Image loadImageSafe(String classpathAbsolutePath) {
        try {
            URL url = getClass().getResource(classpathAbsolutePath);
            if (url != null) return new Image(url.toExternalForm());
        } catch (Exception ignored) {}
        // fallback 1x1 px
        return new Image("data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9b2Yw5sAAAAASUVORK5CYII=");
    }
}
