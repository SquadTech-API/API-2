package br.com.squadtech.bluetech.controller.professorOrientador;

import br.com.squadtech.bluetech.service.EmailService;
import br.com.squadtech.bluetech.config.ConnectionFactory;
import br.com.squadtech.bluetech.config.SmtpProps;
import br.com.squadtech.bluetech.service.NotificationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.sql.Connection;

public class PainelPrincipalOrientadorController {

    @FXML private AnchorPane painelPrincipalMenu;
    @FXML private AnchorPane painelPrincipalExibicao;

    @FXML
    public void initialize() {
        loadMenuOrientador();
        mostrarTelaAlunos();
    }

    private void setAnchors(Parent node) {
        AnchorPane.setTopAnchor(node, 0.0);
        AnchorPane.setBottomAnchor(node, 0.0);
        AnchorPane.setLeftAnchor(node, 0.0);
        AnchorPane.setRightAnchor(node, 0.0);
    }

    public void loadMenuOrientador() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/professorOrientador/MenuProfessorOrientador.fxml"));
            Parent menu = loader.load();

            MenuProfessorOrientadorController menuController = loader.getController();
            if (menuController != null) {
                menuController.setPainelPrincipalController(this);
            }

            painelPrincipalMenu.getChildren().setAll(menu);
            setAnchors(menu);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void mostrarTelaAlunos() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/professorOrientador/telaalunos.fxml"));
            Parent tela = loader.load();

            TelaAlunosController controller = loader.getController();
            if (controller != null) {
                controller.setPainelPrincipalController(this);
            }

            painelPrincipalExibicao.getChildren().setAll(tela);
            setAnchors(tela);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Versão antiga sem parâmetro (mantida por compatibilidade) */
    public void mostrarTelaAlunoEspecifico() {
        mostrarTelaAlunoEspecifico(0L, null);
    }

    /**
     * ✅ Abre a tela do aluno específico (corrigido para usar o controller certo)
     * @param alunoId id do perfil_aluno
     * @param nomeAluno nome do aluno a ser exibido
     */
    public void mostrarTelaAlunoEspecifico(Long alunoId, String nomeAluno) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/professorOrientador/telaAlunoEspecifico.fxml"));
            Parent tela = loader.load();

            // 🔧 Controller corrigido aqui:
            TelaAlunoEspecificoController controller = loader.getController();
            if (controller != null) {
                controller.setPainelPrincipalController(this);
                if (alunoId != null) {
                    controller.setAlunoId(alunoId, nomeAluno);
                }
            }

            painelPrincipalExibicao.getChildren().setAll(tela);
            setAnchors(tela);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Exemplo: navegar para um dashboard TG dentro do mesmo painel
    public void enviarEmail() {
        try (Connection conn = ConnectionFactory.getConnection()) {

            // Cria o serviço de e-mail usando os dados fixos do SmtpProps
            EmailService emailService = new EmailService(
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

            // Instancia o serviço de notificação
            NotificationService notifier = new NotificationService(conn, emailService);

            // Escolha qual notificação quer testar:
            // long id = 1L; // substitua pelo ID real
            // String destinatario = notifier.notifyProfessorOnStudentSubmission(id);
            String destinatario = notifier.notifyStudentOnProfessorFeedback(1L);

            if (destinatario == null) {
                showInfo("Nenhum destinatário encontrado no banco.");
            } else {
                showInfo("✅ E-mail enviado com sucesso para:\n" + destinatario);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("❌ Erro ao enviar e-mail:\n" + e.getMessage());
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
