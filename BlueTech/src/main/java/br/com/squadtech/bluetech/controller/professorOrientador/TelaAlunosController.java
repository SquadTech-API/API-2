package br.com.squadtech.bluetech.controller.professorOrientador;

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
import java.util.stream.Collectors;

public class TelaAlunosController {

    @FXML private Button ButtonPesquisar;
    @FXML private ToggleButton ToggleButonTodosAlunos;
    @FXML private ToggleButton ToggleButtonCorrigidos;
    @FXML private ToggleButton ToggleButtonNaoCorrigidos;
    @FXML private TextField TextFieldNomePesquisa;
    @FXML private VBox VBoxListaAlunos;

    private PainelPrincipalOrientadorController painelPrincipalController;

    // mock: “banco” em memória
    private final List<Aluno> todosAlunos = new ArrayList<>();

    public void setPainelPrincipalController(PainelPrincipalOrientadorController painelPrincipalController) {
        this.painelPrincipalController = painelPrincipalController;
    }

    @FXML
    public void initialize() {
        carregarMock();

        ToggleButonTodosAlunos.setSelected(true);

        ToggleButonTodosAlunos.setOnAction(e -> {
            ToggleButton tb = ToggleButonTodosAlunos;
            if (!tb.isSelected()) tb.setSelected(true); // sempre um ativo
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

    // -------- Mock data ----------
    private void carregarMock() {
        todosAlunos.clear();
        todosAlunos.add(new Aluno("Elder Henrique Menezes", 45, Status.NAO_CORRIGIDO));
        todosAlunos.add(new Aluno("Jhonatan Rossi", 14, Status.NAO_CORRIGIDO));
        todosAlunos.add(new Aluno("Guilherme Valim da Silva", 14, Status.NAO_CORRIGIDO));
        todosAlunos.add(new Aluno("Manuela Brito", 12, Status.NAO_CORRIGIDO));
        todosAlunos.add(new Aluno("Caio Castro da Silva", 12, Status.NAO_CORRIGIDO));
        todosAlunos.add(new Aluno("João Pedro Meneguel", 10, Status.NAO_CORRIGIDO));
        todosAlunos.add(new Aluno("Giovanna Faria Lima", 6, Status.NAO_CORRIGIDO));
        todosAlunos.add(new Aluno("Kelvin Diogo Nogueira", 2, Status.NAO_CORRIGIDO));
        todosAlunos.add(new Aluno("João Juscelino Santos", 0, Status.CORRIGIDO));
        todosAlunos.add(new Aluno("Maria Isabel Costa", 0, Status.CORRIGIDO));
        todosAlunos.add(new Aluno("Sarah Leal Oliveira", 0, Status.CORRIGIDO));
        todosAlunos.add(new Aluno("Jorge Benjoir Nair", 0, Status.CORRIGIDO));
        todosAlunos.add(new Aluno("Gabriel Teixeira Ribeiro", 0, Status.CORRIGIDO));
        todosAlunos.add(new Aluno("Gisele Silva de Castro", 0, Status.CORRIGIDO));
    }

    // -------- Filtro + render --------
    private void aplicarFiltroERender() {
        String termo = TextFieldNomePesquisa.getText() == null ? "" : TextFieldNomePesquisa.getText().trim().toLowerCase(Locale.ROOT);

        List<Aluno> filtrados = todosAlunos.stream()
                .filter(a -> termo.isEmpty() || a.nome.toLowerCase(Locale.ROOT).contains(termo))
                .filter(a -> {
                    if (ToggleButonTodosAlunos.isSelected()) return true;
                    if (ToggleButtonCorrigidos.isSelected()) return a.status == Status.CORRIGIDO;
                    if (ToggleButtonNaoCorrigidos.isSelected()) return a.status == Status.NAO_CORRIGIDO;
                    return true; // fallback
                })
                .collect(Collectors.toList());

        renderizarCards(filtrados);
    }

    private void renderizarCards(List<Aluno> alunos) {
        VBoxListaAlunos.getChildren().clear();
        for (Aluno a : alunos) {
            VBoxListaAlunos.getChildren().add(criarCard(a));
        }
    }

    // -------- UI do card --------
    private Node criarCard(Aluno a) {
        HBox card = new HBox(8);
        card.getStyleClass().addAll("card", "student-row");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setCursor(Cursor.HAND);
        card.setPrefHeight(65);

        // Nome
        Label lblNome = new Label(a.nome);
        lblNome.setStyle("-fx-font-size: 18px;");
        lblNome.setPrefWidth(438);

        // Spacer
        Region spacer = new Region();
        spacer.setPrefWidth(20);
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // “há X dias” (só para não corrigidos)
        Label lblDias = new Label(a.status == Status.NAO_CORRIGIDO ? "há " + a.diasSemEnvio + " dias" : "");
        lblDias.setStyle("-fx-font-size: 18px;");
        lblDias.setPrefWidth(150);
        lblDias.setAlignment(Pos.CENTER_RIGHT);

        // Ícone de status — carregado com classpath-safe
        String iconPath = (a.status == Status.CORRIGIDO)
                ? "/images/check.png"
                : (a.diasSemEnvio >= 30 ? "/images/exclav.png" : "/images/excla.png");

        ImageView iv = new ImageView(loadImageSafe(iconPath));
        iv.setFitWidth(28);
        iv.setFitHeight(28);
        iv.setPreserveRatio(true);

        // Monta o card
        card.getChildren().addAll(lblNome, spacer, lblDias, iv);

        // Hover/efeito opcional
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: rgba(0,0,0,0.04); -fx-background-radius: 8;"));
        card.setOnMouseExited(e -> card.setStyle(""));

        // Clique: abre tela do aluno específico (passando o nome)
        card.setOnMouseClicked(e -> {
            if (painelPrincipalController != null) {
                painelPrincipalController.mostrarTelaAlunoEspecifico(a.nome);
            }
        });

        return card;
    }

    /** Carrega imagem do classpath com fallback suave (evita IllegalArgumentException) */
    private Image loadImageSafe(String classpathAbsolutePath) {
        try {
            URL url = getClass().getResource(classpathAbsolutePath);
            if (url != null) {
                return new Image(url.toExternalForm());
            }
        } catch (Exception ignored) {}
        // fallback: 1x1 transparente (ou você pode apontar para um placeholder seu)
        return new Image("data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9b2Yw5sAAAAASUVORK5CYII=");
    }

    // -------- “Modelo” simples --------
    private enum Status { CORRIGIDO, NAO_CORRIGIDO }

    private static class Aluno {
        final String nome;
        final int diasSemEnvio; // só usado para exibir "há X dias"
        final Status status;

        Aluno(String nome, int diasSemEnvio, Status status) {
            this.nome = nome;
            this.diasSemEnvio = diasSemEnvio;
            this.status = status;
        }
    }
}
