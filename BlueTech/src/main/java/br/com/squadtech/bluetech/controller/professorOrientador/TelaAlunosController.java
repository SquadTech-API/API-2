package br.com.squadtech.bluetech.controller.professorOrientador;


import br.com.squadtech.bluetech.dao.PerfilAlunoDAO;
import br.com.squadtech.bluetech.model.PerfilAluno;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.util.List;

public class TelaAlunosController {

    @FXML
    private Button ButtonPesquisar;

    @FXML
    private ToggleButton ButtonProximaTela;

    @FXML
    private HBox HboxAluno;

    private PerfilAlunoDAO perfilAlunoDAO = new PerfilAlunoDAO();

    @FXML
    private TextField TextFieldNomePesquisa;

    @FXML
    private ToggleButton ToggleButonTodosAlunos;

    @FXML
    private ToggleButton ToggleButtonCorrigidos;

    @FXML
    private ToggleButton ToggleButtonNaoCorrigidos;

    @FXML
    private VBox VBoxListaAlunos;

    @FXML
    void passarTelaAlunoEspecifico(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/professorOrientador/telaAlunoEspecifico.fxml"));
        Scene scene = new Scene(root);

        // pega a janela atual
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void initialize() {
        carregarAlunos();
    }

    private void carregarAlunos() {
        // Limpa a lista antes de preencher
        VBoxListaAlunos.getChildren().clear();

        // Busca todos os alunos com nome
        List<PerfilAluno> alunos = perfilAlunoDAO.listarAlunosComNome();

        for (PerfilAluno aluno : alunos) {
            HBox novo = criarHBoxAluno(aluno);
            VBoxListaAlunos.getChildren().add(novo);
        }
    }

    private HBox criarHBoxAluno(PerfilAluno aluno) {
        HBox modelo = HboxAluno;

        // Novo HBox
        HBox hbox = new HBox();
        hbox.setStyle(modelo.getStyle());
        hbox.setAlignment(modelo.getAlignment() != null ? modelo.getAlignment() : Pos.CENTER_LEFT);
        hbox.setSpacing(modelo.getSpacing());
        hbox.setPadding(modelo.getPadding() != null ? modelo.getPadding() : new javafx.geometry.Insets(10, 20, 10, 20));
        hbox.setPrefHeight(modelo.getPrefHeight());
        hbox.setPrefWidth(modelo.getPrefWidth());

        // Nome
        Label modeloNome = (Label) modelo.getChildren().get(0);
        Label lblNome = new Label(aluno.getNomeAluno()); // aqui usamos o nome vindo do join
        lblNome.setFont(modeloNome.getFont());
        lblNome.setPrefWidth(modeloNome.getPrefWidth());

        // Espaço flexível
        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);

        // Data (placeholder)
        Label modeloTempo = (Label) modelo.getChildren().get(2);
        Label lblTempo = new Label(""); // por enquanto vazio
        lblTempo.setFont(modeloTempo.getFont());
        lblTempo.setPrefWidth(modeloTempo.getPrefWidth());
        lblTempo.setVisible(false);
        lblTempo.setManaged(false);

        // Imagem (copiando do modelo)
        ImageView modeloImg = (ImageView) modelo.getChildren().get(3);
        ImageView img = new ImageView("");
        img.setImage(modeloImg.getImage()); // copia a mesma imagem do template
        img.setFitHeight(modeloImg.getFitHeight());
        img.setFitWidth(modeloImg.getFitWidth());
        img.setPreserveRatio(true);
        HBox.setMargin(img, HBox.getMargin(modeloImg)); // copia margin do template

        // Monta o HBox
        hbox.getChildren().addAll(lblNome, espaco, lblTempo, img);

        // Evento de clique para abrir próxima tela
        hbox.setOnMouseClicked(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/professorOrientador/telaAlunoEspecifico.fxml"));
                Parent root = loader.load();

                // Pega o controller da próxima tela
                ControllerTelaAlunoEspecificos controller = loader.getController();
                controller.receberAluno(aluno.getIdPerfilAluno(), aluno.getNomeAluno());

                // Fecha a janela atual
                Stage stageAtual = (Stage) hbox.getScene().getWindow();
                stageAtual.setScene(new Scene(root));
                stageAtual.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return hbox;
    }
}
