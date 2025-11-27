package br.com.squadtech.bluetech;

import br.com.squadtech.bluetech.config.ConnectionFactory;
import br.com.squadtech.bluetech.config.DatabaseInitializer;
import br.com.squadtech.bluetech.notify.AsyncNotifier;
import br.com.squadtech.bluetech.util.StageUtils;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * Classe principal do aplicativo BlueTech.
 * Exibe uma splash screen em vídeo antes de carregar a tela de login.
 */
public class App extends Application {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    @Override
    public void start(Stage stage) {
        // Garante que o código do JavaFX Toolkit já esteja pronto
        Platform.runLater(() -> showSplashScreen(stage));
    }

    /**
     * Exibe a splash screen em vídeo antes da aplicação principal.
     */
    private void showSplashScreen(Stage stage) {
        try {
            URL videoURL = getClass().getResource("/assets/SplashScreen.mp4");
            if (videoURL == null) {
                log.warn(
                        "Vídeo de splash não encontrado em /assets/SplashScreen.mp4. Iniciando aplicação principal...");
                startMainApp(new Stage());
                return;
            }

            StageUtils.applyAppIcon(stage);
            stage.initStyle(StageStyle.UNDECORATED); // Remove bordas e barra de título
            stage.setTitle("Carregando BlueTech...");
            stage.setResizable(false);
            stage.setOpacity(0); // Invisível até o vídeo carregar

            // Cena básica com Group
            Group root = new Group();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

            // Cria o MediaPlayer após o stage estar visível (evita deadlocks)
            Media media = new Media(videoURL.toExternalForm());
            MediaPlayer player = new MediaPlayer(media);
            MediaView view = new MediaView(player);
            root.getChildren().add(view);

            // Timeout de segurança — se o vídeo travar, avança para o login
            PauseTransition timeout = new PauseTransition(Duration.seconds(8));
            timeout.setOnFinished(ev -> {
                log.warn("Timeout de splash atingido. Abrindo tela principal...");
                safeCloseSplash(player, stage);
            });
            timeout.play();

            player.setOnReady(() -> {
                timeout.stop(); // Cancela o timeout se o vídeo iniciou normalmente

                player.seek(Duration.seconds(0.05));
                player.pause();

                Platform.runLater(() -> {
                    double videoWidth = player.getMedia().getWidth();
                    double videoHeight = player.getMedia().getHeight();

                    // Ajusta MediaView automaticamente ao tamanho do vídeo
                    view.setPreserveRatio(true);
                    view.setFitWidth(videoWidth);
                    view.setFitHeight(videoHeight);

                    // Atualiza a janela conforme o tamanho do vídeo real
                    stage.sizeToScene();
                    stage.centerOnScreen();
                    stage.setOpacity(1);

                    // Força renderização inicial e inicia o vídeo
                    try {
                        view.snapshot(null, null);
                    } catch (Exception ignored) {
                    }
                    player.play();
                });
            });

            player.setOnError(() -> {
                log.error("Erro na reprodução da splash screen: {}", player.getError());
                safeCloseSplash(player, stage);
            });

            player.setOnEndOfMedia(() -> safeCloseSplash(player, stage));

            stage.setOnCloseRequest(e -> {
                e.consume(); // Evita duplo fechamento
                safeCloseSplash(player, stage);
            });

        } catch (Exception e) {
            log.error("Erro ao exibir splash screen", e);
            try {
                startMainApp(new Stage());
            } catch (Exception ex) {
                log.error("Erro ao iniciar aplicação principal", ex);
            }
        }
    }

    /**
     * Fecha com segurança a splash e inicia a tela principal.
     */
    private void safeCloseSplash(MediaPlayer player, Stage splashStage) {
        Platform.runLater(() -> {
            try {
                if (player != null) {
                    player.stop();
                    player.dispose();
                }
                splashStage.close();
                startMainApp(new Stage());
            } catch (Exception e) {
                log.error("Erro ao encerrar splash e iniciar aplicação principal", e);
            }
        });
    }

    /**
     * Carrega a tela principal (TelaLogin.fxml)
     */
    private void startMainApp(Stage stage) throws Exception {
        DatabaseInitializer.init();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/login/TelaLogin.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        StageUtils.applyAppIcon(stage);

        stage.setTitle("BlueTech - Plataforma de Gestão de TGs");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void stop() {
        ConnectionFactory.closePool();
        AsyncNotifier.getInstance().shutdown();
    }

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
