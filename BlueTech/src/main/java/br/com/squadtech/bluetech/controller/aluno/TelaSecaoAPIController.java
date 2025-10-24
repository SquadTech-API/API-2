package br.com.squadtech.bluetech.controller.aluno;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import br.com.squadtech.bluetech.controller.SupportsMainController;
<<<<<<< HEAD
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
=======
>>>>>>> origin/SPRINT_2
import br.com.squadtech.bluetech.model.SecaoContext;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class TelaSecaoAPIController implements SupportsMainController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnEditarSecao;

    @FXML
    private Button btnEnviar;

    @FXML
    private Label lblStatus;

    @FXML
    private ListView<?> listVersoes;

    @FXML
    private TextArea txtChatVisualizacao;

    @FXML
    private TextArea txtFeedbacks;

    @FXML
    private TextArea txtMarkdown;

    @FXML
    private TextField txtMensagem;

    // Ajustado para assinar ActionEvent conforme esperado pelo FXML (onAction)
    @FXML
    void abrirEdicaoSecao(ActionEvent event) {
        if (painelPrincipalController != null) {
            try {
                // Recupera a seção atual do contexto (definida na tela anterior)
                Integer idSecao = SecaoContext.getIdSecaoSelecionada();
                if (idSecao == null) {
                    // Sem seção selecionada, apenas não navega (ou poderia exibir um alerta futuramente)
                    return;
                }
                String fxmlPath = "/fxml/aluno/EditarSecaoAPI.fxml";
                painelPrincipalController.loadContent(fxmlPath);
            } catch (IOException e) {
                System.err.println("Falha ao carregar EditarSecaoAPI.fxml");
                e.printStackTrace();
            }
        }
    }


    @FXML
    void enviarMensagemOrientador(ActionEvent event) {


    }

    @FXML
    void initialize() {
        assert btnEditarSecao != null : "fx:id=\"btnEditarSecao\" was not injected: check your FXML file 'TelaSecaoAPI.fxml'.";
        assert btnEnviar != null : "fx:id=\"btnEnviar\" was not injected: check your FXML file 'TelaSecaoAPI.fxml'.";
        assert lblStatus != null : "fx:id=\"lblStatus\" was not injected: check your FXML file 'TelaSecaoAPI.fxml'.";
        assert listVersoes != null : "fx:id=\"listVersoes\" was not injected: check your FXML file 'TelaSecaoAPI.fxml'.";
        assert txtChatVisualizacao != null : "fx:id=\"txtChatVisualizacao\" was not injected: check your FXML file 'TelaSecaoAPI.fxml'.";
        assert txtFeedbacks != null : "fx:id=\"txtFeedbacks\" was not injected: check your FXML file 'TelaSecaoAPI.fxml'.";
        assert txtMarkdown != null : "fx:id=\"txtMarkdown\" was not injected: check your FXML file 'TelaSecaoAPI.fxml'.";
        assert txtMensagem != null : "fx:id=\"txtMensagem\" was not injected: check your FXML file 'TelaSecaoAPI.fxml'.";

        carregarMarkdownDaUltimaVersaoSelecionada();
    }

    private void carregarMarkdownDaUltimaVersaoSelecionada() {
        Integer idSecao = SecaoContext.getIdSecaoSelecionada();
        if (idSecao == null) {
            txtMarkdown.setText("Nenhuma seção selecionada.");
            return;
        }
        // TODO: Buscar do banco os dados da versão mais recente da seção (usando idSecao -> TG_Secao -> TG_Versao)
        // Por enquanto, placeholder simples
        txtMarkdown.setText("# Seção API\n\nDados da última versão serão carregados aqui.");
    }
<<<<<<< HEAD

    private PainelPrincipalController painelPrincipalController;

    @Override
    public void setPainelPrincipalController(PainelPrincipalController controller) {
        this.painelPrincipalController = controller;

    }
=======
>>>>>>> origin/SPRINT_2
}
