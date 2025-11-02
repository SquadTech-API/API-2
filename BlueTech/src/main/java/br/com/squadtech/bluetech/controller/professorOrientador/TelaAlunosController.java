package br.com.squadtech.bluetech.controller.professorOrientador;

import br.com.squadtech.bluetech.dao.PerfilAlunoDAO;
import br.com.squadtech.bluetech.model.PerfilAluno;
import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
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
        todosAlunos = perfilAlunoDAO.listarAlunosParaCard(null);
    }

    private void aplicarFiltroERender() {
        String termo = TextFieldNomePesquisa.getText() == null ? "" : TextFieldNomePesquisa.getText().trim().toLowerCase(Locale.ROOT);
        List<PerfilAluno> filtrados = new ArrayList<>();

        for (PerfilAluno a : todosAlunos) {
            boolean matchesNome = termo.isEmpty() || (a.getNomeAluno() != null && a.getNomeAluno().toLowerCase(Locale.ROOT).contains(termo));
            // Sem regra de corrigido real, usamos idade null como indicador de faltante (apenas placeholder)
            boolean corrigido = a.getIdade() != null && a.getIdade() >= 0; // qualquer idade cadastrada conta como preenchido
            boolean matchesStatus = ToggleButonTodosAlunos.isSelected() ||
                    (ToggleButtonCorrigidos.isSelected() && corrigido) ||
                    (ToggleButtonNaoCorrigidos.isSelected() && !corrigido);
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
        HBox card = new HBox(8);
        card.getStyleClass().addAll("card", "student-row");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setCursor(Cursor.HAND);
        card.setPrefHeight(65);

        Label lblNome = new Label(a.getNomeAluno());
        lblNome.setStyle("-fx-font-size: 18px;");
        lblNome.setPrefWidth(438);

        Region spacer = new Region();
        spacer.setPrefWidth(20);
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label lblDias = new Label("");
        lblDias.setStyle("-fx-font-size: 18px;");
        lblDias.setPrefWidth(150);
        lblDias.setAlignment(Pos.CENTER_RIGHT);

        String iconPath = (a.getIdade() != null) ? "/images/check.png" : "/images/excla.png";
        ImageView iv = new ImageView(loadImageSafe(iconPath));
        iv.setFitWidth(28);
        iv.setFitHeight(28);
        iv.setPreserveRatio(true);

        card.getChildren().addAll(lblNome, spacer, lblDias, iv);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: rgba(0,0,0,0.04); -fx-background-radius: 8;"));
        card.setOnMouseExited(e -> card.setStyle(""));

        // Clique: abre TelaAlunoEspecifico.fxml em nova janela
        card.setOnMouseClicked(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/professorOrientador/telaAlunoEspecifico.fxml"));
                Parent root = loader.load();

                TelaAlunoEspecificoController controller = loader.getController();
                controller.setAlunoId(
                        a.getIdPerfilAluno() != null ? a.getIdPerfilAluno().longValue() : 0L,
                        a.getNomeAluno()
                );

                Stage stage = new Stage();
                stage.setTitle("Aluno: " + a.getNomeAluno());
                stage.setScene(new Scene(root));
                stage.show();
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
