package br.com.squadtech.bluetech;

import br.com.squadtech.bluetech.config.ConnectionFactory;
import br.com.squadtech.bluetech.config.DatabaseInitializer;
import br.com.squadtech.bluetech.notify.AsyncNotifier;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class App extends Application {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    @Override
    public void start(Stage stage) throws Exception {
        // 🔹 Inicializa o banco de dados (garante que tabelas e conexões estejam prontas)
        DatabaseInitializer.init();

        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/fxml/login/TelaLogin.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load());
        String iconPath = "/images/icone.png";

        try (InputStream iconStream = getClass().getResourceAsStream(iconPath)) {
            if (iconStream != null) {
                Image icon = new Image(iconStream);
                stage.getIcons().add(icon);
            } else {
                log.warn("Ícone da aplicação não encontrado em: {}", iconPath);
            }
        } catch (Exception e) {
            log.error("Erro ao carregar o ícone da aplicação.", e);
        }

        stage.setTitle("BlueTech - Plataforma de Gestão de TGs");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    @Override
    public void stop() {
        ConnectionFactory.closePool();
        // encerra fila de notificações em segundo plano
        AsyncNotifier.getInstance().shutdown();
    }

    public static void main(String[] args) {
        // Main local (por exemplo, rodando via plugin do Maven) continua funcionando
        // desde que o entrypoint padrão no JAR seja o Launcher.
        javafx.application.Application.launch(App.class, args);
    }

}
