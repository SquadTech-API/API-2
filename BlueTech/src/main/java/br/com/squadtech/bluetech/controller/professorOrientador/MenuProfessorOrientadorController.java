package br.com.squadtech.bluetech.controller.professorOrientador;

import br.com.squadtech.bluetech.config.SmtpProps;
import br.com.squadtech.bluetech.controller.MenuAware;
import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import br.com.squadtech.bluetech.service.EmailService;
import jakarta.mail.MessagingException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;

public class MenuProfessorOrientadorController implements MenuAware, SupportsMainController {

    @FXML private Label lblTituloProfessorOri;
    @FXML private ImageView imgViewFotoProfessorOri;
    @FXML private Label lblProfessorOri;
    @FXML private Label lblSemestreOri;

    @FXML private Button btnListaAlunos;

    private PainelPrincipalController painelPrincipalController;

    @Override
    public void setPainelPrincipalController(PainelPrincipalController painelPrincipalController) {
        this.painelPrincipalController = painelPrincipalController;
    }

    @Override
    public void onContentChanged(String fxmlPath, Object contentController) {
        if (btnListaAlunos == null) return;
        boolean active = fxmlPath.contains("telaAlunos.fxml") || fxmlPath.contains("TelaOrientador.fxml");
        btnListaAlunos.getStyleClass().remove("active");
        if (active) btnListaAlunos.getStyleClass().add("active");
    }

    @FXML
    private void initialize() {
        lblProfessorOri.setText("PROFESSOR ORIENTADOR: Emanuel Mineda");
        lblSemestreOri.setText("SEMESTRE RESPONSÁVEL: 6º Semestre");
    }

    @FXML
    private void abrirListaAlunos() {
        if (painelPrincipalController == null) return;
        try {
            painelPrincipalController.loadContent("/fxml/professorOrientador/telaAlunos.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void enviarEmail() {
        TextInputDialog dlgTo = new TextInputDialog();
        dlgTo.setTitle("Enviar Email");
        dlgTo.setHeaderText("Destinatário");
        dlgTo.setContentText("E-mail para enviar:");
        var toOpt = dlgTo.showAndWait();
        if (toOpt.isEmpty()) return;

        TextInputDialog dlgSub = new TextInputDialog("Teste BlueTech");
        dlgSub.setTitle("Enviar Email");
        dlgSub.setHeaderText("Assunto");
        dlgSub.setContentText("Assunto:");
        var subOpt = dlgSub.showAndWait();
        if (subOpt.isEmpty()) return;

        TextInputDialog dlgBody = new TextInputDialog("Olá! Este é um teste do BlueTech.");
        dlgBody.setTitle("Enviar Email");
        dlgBody.setHeaderText("Corpo da mensagem");
        dlgBody.setContentText("Mensagem:");
        var bodyOpt = dlgBody.showAndWait();
        if (bodyOpt.isEmpty()) return;

        EmailService email = new EmailService(
                SmtpProps.FROM,
                new EmailService.SmtpConfig(
                        SmtpProps.HOST,
                        SmtpProps.PORT,
                        SmtpProps.USER,
                        SmtpProps.PASS,
                        SmtpProps.STARTTLS,
                        SmtpProps.SSL
                )
        );
        try {
            email.send(toOpt.get(), subOpt.get(), bodyOpt.get());
            showInfo("E-mail enviado para: " + toOpt.get());
        } catch (MessagingException e) {
            e.printStackTrace();
            showError("Falha ao enviar e-mail: " + e.getMessage());
        }
    }

    private void showInfo(String msg) {
        var alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setHeaderText("Notificação");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showError(String msg) {
        var alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setHeaderText("Erro");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
