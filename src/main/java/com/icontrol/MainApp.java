package com.icontrol;

import com.icontrol.db.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/inventario-view.fxml"));
        BorderPane root = loader.load();

        Scene scene = new Scene(root, 900, 600);

        // 👇 Agrega aquí el CSS (dentro de start, después de crear la Scene)
        scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());

        stage.setTitle("ICONTROL - Inventario");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        // Inicializa la BD (crea tablas si no existen)
        DatabaseInitializer.initialize();

        // Lanza JavaFX
        launch(args);
    }
}
