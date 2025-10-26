package br.com.squadtech.bluetech.controller.professorOrientador;

import br.com.squadtech.bluetech.dao.PerfilAlunoDAO;
import br.com.squadtech.bluetech.model.PerfilAluno;
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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TelaAlunosController {

    @FXML private Button ButtonPesquisar;
    @FXML private ToggleButton ToggleButonTodosAlunos;
    @FXML private ToggleButton ToggleButtonCorrigidos;
    @FXML private ToggleButton ToggleButtonNaoCorrigidos;
    @FXML private TextField TextFieldNomePesquisa;
    @FXML private VBox VBoxListaAlunos;

    private PainelPrincipalOrientadorController painelPrincipalController;

    private final PerfilAlunoDAO perfilAlunoDAO = new PerfilAlunoDAO();

    // Lista de alunos carregada do banco
    private List<PerfilAluno> todosAlunos = new ArrayList<>();

    public void setPainelPrincipalController(PainelPrincipalOrientadorController painelPrincipalController) {
        this.painelPrincipalController = painelPrincipalController;
    }

    @FXML
    public void initialize() {
        carregarAlunosDoBanco();

        ToggleButonTodosAlunos.setSelected(true);

        ToggleButonTodosAlunos.setOnAction(e -> {
            ToggleButton tb = ToggleButonTodosAlunos;
            if (!tb.isSelected()) tb.setSelected(true);
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

    // -------- Carrega alunos do banco --------
    private void carregarAlunosDoBanco() {
        todosAlunos = perfilAlunoDAO.listarAlunosParaCard(null);
    }

    // -------- Filtro + render --------
    private void aplicarFiltroERender() {
        String termo = TextFieldNomePesquisa.getText() == null ? "" : TextFieldNomePesquisa.getText().trim().toLowerCase(Locale.ROOT);

        List<PerfilAluno> filtrados = new ArrayList<>();
        for (PerfilAluno a : todosAlunos) {
            boolean matchesNome = termo.isEmpty() || (a.getNomeAluno() != null && a.getNomeAluno().toLowerCase(Locale.ROOT).contains(termo));
            boolean matchesStatus = ToggleButonTodosAlunos.isSelected() ||
                    (ToggleButtonCorrigidos.isSelected() && a.getIdade() != null && a.getIdade() == 0) || // Exemplo: corrigido = idade 0
                    (ToggleButtonNaoCorrigidos.isSelected() && (a.getIdade() == null || a.getIdade() > 0)); // Exemplo: não corrigido
            if (matchesNome && matchesStatus) {
                filtrados.add(a);
            }
        }

        renderizarCards(filtrados);
    }

    private void renderizarCards(List<PerfilAluno> alunos) {
        VBoxListaAlunos.getChildren().clear();
        for (PerfilAluno a : alunos) {
            VBoxListaAlunos.getChildren().add(criarCard(a));
        }
    }

    // -------- UI do card --------
    private Node criarCard(PerfilAluno a) {
        HBox card = new HBox(8);
        card.getStyleClass().addAll("card", "student-row");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setCursor(Cursor.HAND);
        card.setPrefHeight(65);

        // Nome
        Label lblNome = new Label(a.getNomeAluno());
        lblNome.setStyle("-fx-font-size: 18px;");
        lblNome.setPrefWidth(438);

        // Spacer
        Region spacer = new Region();
        spacer.setPrefWidth(20);
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // “há X dias” (não temos diasSemEnvio do banco, então apenas deixei vazio)
        Label lblDias = new Label("");
        lblDias.setStyle("-fx-font-size: 18px;");
        lblDias.setPrefWidth(150);
        lblDias.setAlignment(Pos.CENTER_RIGHT);

        // Ícone de status
        String iconPath = (a.getIdade() != null && a.getIdade() == 0)
                ? "/images/check.png"
                : "/images/excla.png";

        ImageView iv = new ImageView(loadImageSafe(iconPath));
        iv.setFitWidth(28);
        iv.setFitHeight(28);
        iv.setPreserveRatio(true);

        // Monta o card
        card.getChildren().addAll(lblNome, spacer, lblDias, iv);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: rgba(0,0,0,0.04); -fx-background-radius: 8;"));
        card.setOnMouseExited(e -> card.setStyle(""));

        // Clique: abre tela do aluno específico
        card.setOnMouseClicked(e -> {
            if (painelPrincipalController != null) {
                painelPrincipalController.mostrarTelaAlunoEspecifico(a.getNomeAluno());
            }
        });

        return card;
    }

    private Image loadImageSafe(String classpathAbsolutePath) {
        try {
            URL url = getClass().getResource(classpathAbsolutePath);
            if (url != null) return new Image(url.toExternalForm());
        } catch (Exception ignored) {}
        return new Image("data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9b2Yw5sAAAAASUVORK5CYII=");
    }
}
