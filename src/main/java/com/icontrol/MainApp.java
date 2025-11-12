package com.icontrol;

import com.icontrol.db.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/login-view.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());

        stage.setTitle("ICONTROL - Acceso");
        stage.setScene(scene);
        // stage.setResizable(false); // si quieres bloquear el tamaño en el login
        stage.show();
    }

    public static void main(String[] args) {
        DatabaseInitializer.initialize(); // crea/actualiza tablas e inserta admin si falta
        launch(args);
    }
}
