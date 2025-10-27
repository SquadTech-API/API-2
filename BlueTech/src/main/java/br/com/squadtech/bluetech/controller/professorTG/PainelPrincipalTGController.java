package br.com.squadtech.bluetech.controller.professorTG;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class PainelPrincipalTGController {

    @FXML
    private AnchorPane painelPrincipalMenu;

    @FXML
    private AnchorPane painelPrincipalExibicao;

    @FXML
    public void initialize() {
        loadMenuTG();
        loadTelaProfessorTG();
    }

    public void loadMenuTG() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/professorTG/MenuProfessorTG.fxml"));
            Parent menu = loader.load();

            MenuProfessorTGController menuController = loader.getController();
            if (menuController != null) {
                menuController.setPainelPrincipalController(this);
            }

            painelPrincipalMenu.getChildren().setAll(menu);
            AnchorPane.setTopAnchor(menu, 0.0);
            AnchorPane.setBottomAnchor(menu, 0.0);
            AnchorPane.setLeftAnchor(menu, 0.0);
            AnchorPane.setRightAnchor(menu, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadTelaProfessorTG() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/professorTG/TelaProfessorTG.fxml"));
            Parent tela = loader.load();

            painelPrincipalExibicao.getChildren().setAll(tela);
            AnchorPane.setTopAnchor(tela, 0.0);
            AnchorPane.setBottomAnchor(tela, 0.0);
            AnchorPane.setLeftAnchor(tela, 0.0);
            AnchorPane.setRightAnchor(tela, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void mostrarVisualizarPortifolio(String semestre, String curso) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/professorTG/VisualizarPortifolioTG.fxml"));
            Parent tela = loader.load();

            VisualizarPortifolioTGController controller = loader.getController();
            if (controller != null) {
                controller.setPainelPrincipalController(this);
                controller.criarCards(semestre, curso);
            }

            painelPrincipalExibicao.getChildren().setAll(tela);
            AnchorPane.setTopAnchor(tela, 0.0);
            AnchorPane.setBottomAnchor(tela, 0.0);
            AnchorPane.setLeftAnchor(tela, 0.0);
            AnchorPane.setRightAnchor(tela, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void mostrarVisualizadorTG(String nomeAluno, String semestre, String curso) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/professorTG/VisualizadorTG.fxml"));
            Parent tela = loader.load();

            // IMPORTANTE: o tipo aqui deve ser exatamente o do controller da FXML VisualizadorTG
            VisualizadorTGController controller = loader.getController();
            if (controller != null) {
                controller.receberDadosAluno(nomeAluno, semestre, curso);
            }

            painelPrincipalExibicao.getChildren().setAll(tela);
            AnchorPane.setTopAnchor(tela, 0.0);
            AnchorPane.setBottomAnchor(tela, 0.0);
            AnchorPane.setLeftAnchor(tela, 0.0);
            AnchorPane.setRightAnchor(tela, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
