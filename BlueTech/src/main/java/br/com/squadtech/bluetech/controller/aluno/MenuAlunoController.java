package br.com.squadtech.bluetech.controller.aluno;

import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import br.com.squadtech.bluetech.dao.PerfilAlunoDAO;
import br.com.squadtech.bluetech.model.PerfilAluno;
import br.com.squadtech.bluetech.model.SessaoUsuario;
import br.com.squadtech.bluetech.model.Usuario;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.squadtech.bluetech.controller.SupportsMainController;

public class MenuAlunoController implements SupportsMainController {

    private static final Logger log = LoggerFactory.getLogger(MenuAlunoController.class);

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private JFXButton btnAlunoSolicitarOrientacao;

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

    @FXML
    private StackPane profileFrame;

    //Referência para o controller principal, para podermos carregar conteúdos à direita
    private PainelPrincipalController painelPrincipalController;

    //Setter chamado pelo PainelPrincipalController após carregar este menu
    @Override
    public void setPainelPrincipalController(PainelPrincipalController controller) {
        this.painelPrincipalController = controller;
    }

    //Para carregar qualquer conteúdo no painel de exibição
    private void loadContentIntoMain(String fxmlPath) {
        if (painelPrincipalController == null) {
            // Sem referência ao painel principal, não há onde injetar o conteúdo
            log.error("[MenuAlunoController] painelPrincipalController é nulo. Verifique se foi configurado ao carregar o menu.");
            return;
        }
        try {
            painelPrincipalController.loadContent(fxmlPath);
        } catch (IOException e) {
            log.error("Erro ao carregar conteúdo FXML: {}", fxmlPath, e);
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
            // Reaplique o processamento ap��s atualizar a imagem
            Platform.runLater(this::applyImageProcessing);
        }
    }

    @FXML
    void SolicitarOrientacao(ActionEvent event) {
        loadContentIntoMain("/fxml/aluno/SolicitacaoOrientador.fxml");
    }

    @FXML
    void initialize() {
        assert btnAlunoSolicitarOrientacao != null : "fx:id=\"btnAlunoSolicitarOrientacao\" was not injected: check your FXML file 'MenuAluno.fxml'.";
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
        assert profileFrame != null : "fx:id=\"profileFrame\" was not injected: check your FXML file 'MenuAluno.fxml'.";

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

        // Garanta que o StackPane seja quadrado, combinando com os tamanhos do CSS (120x120)
        profileFrame.setPrefWidth(120);
        profileFrame.setPrefHeight(120);
        profileFrame.setMinWidth(120);
        profileFrame.setMinHeight(120);
        profileFrame.setMaxWidth(120);
        profileFrame.setMaxHeight(120);

        // Aplique o processamento de imagem inicial após o layout
        Platform.runLater(this::applyImageProcessing);
    }

    private void applyImageProcessing() {
        Image image = imgViewFotoAluno.getImage();
        if (image == null || image.getWidth() == 0 || image.getHeight() == 0) {
            return; // Nada a processar se não houver imagem válida
        }

        // Defina o tamanho desejado para a imagem interna (subtraindo a largura da borda: 120 - 2*4 = 112px)
        double fitSize = 112.0;

        // Calcule o viewport para cropar a imagem ao centro em formato quadrado
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        double side = Math.min(imageWidth, imageHeight);
        double x = (imageWidth - side) / 2.0;
        double y = (imageHeight - side) / 2.0;
        imgViewFotoAluno.setViewport(new Rectangle2D(x, y, side, side));

        // Configure o ImageView para escalar o conteúdo cropado para o tamanho interno
        imgViewFotoAluno.setPreserveRatio(false); // Desative preserveRatio após crop para quadrado
        imgViewFotoAluno.setSmooth(true); // Melhora a qualidade do escalonamento
        imgViewFotoAluno.setFitWidth(fitSize);
        imgViewFotoAluno.setFitHeight(fitSize);

        // Aplique o clip circular centralizado, ajustado para o tamanho interno
        double radius = fitSize / 2.0;
        Circle clip = new Circle(radius, radius, radius);
        imgViewFotoAluno.setClip(clip);
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