package com.icontrol.ui;

import com.icontrol.dao.UsuarioDao;
import com.icontrol.dao.UsuarioDaoSqlite;
import com.icontrol.model.Usuario;
import com.icontrol.security.Sesion;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    private final UsuarioDao usuarioDao = new UsuarioDaoSqlite();

    @FXML
    private void onLoginClick() {
        String u = txtUsuario.getText().trim();
        String p = txtPassword.getText();

        if (u.isEmpty() || p.isEmpty()) {
            lblError.setText("Introduce usuario y contraseña.");
            return;
        }

        try {
            java.util.Optional<Usuario> op = usuarioDao.validarLogin(u, p);
            if (op.isPresent()) {
                Usuario usuario = op.get();
                Sesion.setUsuario(usuario);

                // Cerrar ventana de login
                Stage stage = (Stage) txtUsuario.getScene().getWindow();
                stage.close();

                // Cargar vista principal (Inventario)
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/inventario-view.fxml"));
                Parent root = loader.load();

                Stage inventario = new Stage();
                inventario.setTitle("ICONTROL - Inventario");

                Scene scene = new Scene(root, 1100, 600);

                // 👇 Aplicar estilos globales
                scene.getStylesheets().add(
                        getClass().getResource("/styles/style.css").toExternalForm()
                );

                inventario.setScene(scene);
                inventario.show();

            } else {
                lblError.setText("Usuario o contraseña incorrectos.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            lblError.setText("Error de acceso: " + e.getMessage());
        }
    }

    @FXML
    private void onSalirClick() {
        Stage stage = (Stage) txtUsuario.getScene().getWindow();
        stage.close();
    }
}
