package br.com.squadtech.bluetech.util;

import br.com.squadtech.bluetech.model.SessaoUsuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;

/**
 * Centraliza o fluxo de logout para que todos os menus compartilhem o mesmo comportamento.
 */
public final class LogoutHelper {

    private LogoutHelper() {
    }

    public static void performLogout(ActionEvent event, Logger log) {
        SessaoUsuario.limparSessao();
        try {
            Parent root = FXMLLoader.load(LogoutHelper.class.getResource("/fxml/login/TelaLogin.fxml"));
            Stage newStage = new Stage();
            StageUtils.applyAppIcon(newStage);
            newStage.setTitle("BlueTech - Login");
            newStage.setScene(new Scene(root));
            newStage.show();
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            if (log != null) {
                log.error("Erro ao executar logout", e);
            } else {
                e.printStackTrace();
            }
        }
    }
}

