package br.com.squadtech.bluetech.controller.aluno;

import java.net.URL;
import java.util.ResourceBundle;

import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.model.SecaoContext;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class TelaSecaoAPIController {

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

    @FXML
    void abrirEdicaoSecao(ActionEvent event) {

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
}
