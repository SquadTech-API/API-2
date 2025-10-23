package br.com.squadtech.bluetech.controller.aluno;

import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import br.com.squadtech.bluetech.dao.PerfilAlunoDAO;
import br.com.squadtech.bluetech.model.PerfilAluno;
import br.com.squadtech.bluetech.model.SessaoUsuario;
import br.com.squadtech.bluetech.model.Usuario;
import com.jfoenix.controls.JFXButton;
import javafx.scene.image.Image;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class MenuAlunoController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private JFXButton bntAlunoBaixarMD;

    @FXML
    private JFXButton btnAlunoEntregas;

    @FXML
    private JFXButton btnAlunoPerfil;

    @FXML
    private JFXButton btnAlunoPortifolio;

    @FXML
    private ImageView imgViewFotoAluno;

    @FXML
    private Label painelAluno;

    @FXML
    private AnchorPane paneSuperiorMenuAluno;

    @FXML
    private SplitPane splitPanelMenuAluno;

    @FXML
    private Label txtMenuAlunoCursoAluno;

    @FXML
    private Label txtMenuAlunoNomeAluno;

    @FXML
    private Label txtMenuAlunoOrientadorAluno;

    @FXML
    private VBox vboxMenuAluno;

    //Referência para o controller principal, para podermos carregar conteúdos à direita
    private PainelPrincipalController painelPrincipalController;

    //Setter chamado pelo PainelPrincipalController após carregar este menu
    public void setPainelPrincipalController(PainelPrincipalController controller) {
        this.painelPrincipalController = controller;
    }

    //Para carregar qualquer conteúdo no painel de exibição
    private void loadContentIntoMain(String fxmlPath) {
        if (painelPrincipalController == null) {
            // Sem referência ao painel principal, não há onde injetar o conteúdo
            System.err.println("[MenuAlunoController] painelPrincipalController é nulo. Verifique se foi configurado ao carregar o menu.");
            return;
        }
        try {
            painelPrincipalController.loadContent(fxmlPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    void AbrirEntregasSeccoes(ActionEvent event) {
        loadContentIntoMain("/fxml/aluno/TelaEntregasAluno.fxml");
    }

    @FXML
    void AbreTelaPerfilAluno(ActionEvent event) {
        loadContentIntoMain("/fxml/aluno/TelaPerfilAluno.fxml");
    }

    //Atualizar a foto no menu
    public void updateFotoAluno(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            Image image = new Image("file:" + imagePath);
            imgViewFotoAluno.setImage(image);
        }
    }

    @FXML
    void initialize() {
        assert bntAlunoBaixarMD != null : "fx:id=\"bntAlunoBaixarMD\" was not injected: check your FXML file 'MenuAluno.fxml'.";
        assert btnAlunoEntregas != null : "fx:id=\"btnAlunoEntregas\" was not injected: check your FXML file 'MenuAluno.fxml'.";
        assert btnAlunoPerfil != null : "fx:id=\"btnAlunoPerfil\" was not injected: check your FXML file 'MenuAluno.fxml'.";
        assert btnAlunoPortifolio != null : "fx:id=\"btnAlunoPortifolio\" was not injected: check your FXML file 'MenuAluno.fxml'.";
        assert imgViewFotoAluno != null : "fx:id=\"imgViewFotoAluno\" was not injected: check your FXML file 'MenuAluno.fxml'.";
        assert painelAluno != null : "fx:id=\"painelAluno\" was not injected: check your FXML file 'MenuAluno.fxml'.";
        assert paneSuperiorMenuAluno != null : "fx:id=\"paneSuperiorMenuAluno\" was not injected: check your FXML file 'MenuAluno.fxml'.";
        assert splitPanelMenuAluno != null : "fx:id=\"splitPanelMenuAluno\" was not injected: check your FXML file 'MenuAluno.fxml'.";
        assert txtMenuAlunoCursoAluno != null : "fx:id=\"txtMenuAlunoCursoAluno\" was not injected: check your FXML file 'MenuAluno.fxml'.";
        assert txtMenuAlunoNomeAluno != null : "fx:id=\"txtMenuAlunoNomeAluno\" was not injected: check your FXML file 'MenuAluno.fxml'.";
        assert txtMenuAlunoOrientadorAluno != null : "fx:id=\"txtMenuAlunoOrientadorAluno\" was not injected: check your FXML file 'MenuAluno.fxml'.";
        assert vboxMenuAluno != null : "fx:id=\"vboxMenuAluno\" was not injected: check your FXML file 'MenuAluno.fxml'.";

        //Mantém o topo com altura estável (~260px) e o bottom responsivo
        paneSuperiorMenuAluno.setMinHeight(200.0);
        paneSuperiorMenuAluno.setPrefHeight(260.0);
        SplitPane.setResizableWithParent(paneSuperiorMenuAluno, Boolean.FALSE);

        //Posiciona o divisor após layout e em redimensionamentos
        Platform.runLater(this::fixDividerPosition);
        splitPanelMenuAluno.heightProperty().addListener((obs, oldH, newH) -> fixDividerPosition());

        //Impede o usuário de arrastar o divisor (trava na posição desejada)
        if (!splitPanelMenuAluno.getDividers().isEmpty()) {
            splitPanelMenuAluno.getDividers().get(0).positionProperty().addListener((obs, oldPos, newPos) -> {
                double desired = computeDesiredDividerPosition();
                if (Math.abs(newPos.doubleValue() - desired) > 0.0005) {
                    splitPanelMenuAluno.setDividerPositions(desired);
                }
            });
        }

        //exibir o nome do Aluno logado na aplicação
        Usuario usuario = SessaoUsuario.getUsuarioLogado();
        if (usuario != null) {
            txtMenuAlunoNomeAluno.setText("Nome: " + usuario.getNome());
            //Carregar foto se existir perfil
            PerfilAlunoDAO dao = new PerfilAlunoDAO();
            if (dao.existePerfil(usuario.getEmail())) {
                PerfilAluno perfil = dao.getPerfilByEmail(usuario.getEmail());
                if (perfil != null && perfil.getFoto() != null) {
                    updateFotoAluno(perfil.getFoto());
                }
            }
        } else {
            txtMenuAlunoNomeAluno.setText("Bem-vindo!");
        }
    }

    private void fixDividerPosition() {
        splitPanelMenuAluno.setDividerPositions(computeDesiredDividerPosition());
    }

    private double computeDesiredDividerPosition() {
        double total = Math.max(1.0, splitPanelMenuAluno.getHeight());
        double desired = paneSuperiorMenuAluno.getPrefHeight() / total;
        //Limita para evitar posições extremas em alturas pequenas/grandes
        return Math.max(0.1, Math.min(0.9, desired));
    }

}