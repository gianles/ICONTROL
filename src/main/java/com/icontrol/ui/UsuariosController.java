package com.icontrol.ui;

import com.icontrol.dao.UsuarioDao;
import com.icontrol.dao.UsuarioDaoSqlite;
import com.icontrol.model.Usuario;
import com.icontrol.security.PasswordUtils;
import com.icontrol.security.Sesion;
import com.icontrol.security.Rol;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;
import javafx.scene.control.ComboBox;


public class UsuariosController {

    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, String> colUsername;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colApellido;
    @FXML private TableColumn<Usuario, String> colRol;

    @FXML private Button btnNuevo;
    @FXML private Button btnEditar;
    @FXML private Button btnResetPass;

    private final UsuarioDao usuarioDao = new UsuarioDaoSqlite();
    private final ObservableList<Usuario> datos = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));

        // Si Usuario.getRol() devuelve Rol (enum), lo convertimos a String para la tabla:
        colRol.setCellValueFactory(row ->
                new SimpleStringProperty(row.getValue().getRol() != null
                        ? row.getValue().getRol().name()
                        : "")
        );

        tablaUsuarios.setItems(datos);
        cargarUsuarios();

        // Solo ADMIN puede crear y resetear
        boolean admin = Sesion.isAdmin();
        if (btnNuevo != null) btnNuevo.setDisable(!admin);
        if (btnResetPass != null) btnResetPass.setDisable(!admin);
        // 👇 también protegemos el botón Editar
        if (btnEditar    != null) btnEditar.setDisable(!admin);
    }

    private void cargarUsuarios() {
        try {
            List<Usuario> lista = usuarioDao.listarTodos();
            datos.setAll(lista);
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudieron cargar los usuarios:\n" + e.getMessage());
        }
    }

    @FXML
    private void onNuevoClick() {
        Dialog<Usuario> dialog = new Dialog<>();
        dialog.setTitle("Nuevo usuario");
        dialog.initModality(Modality.APPLICATION_MODAL);

        ButtonType btCrear = new ButtonType("Crear", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btCrear, ButtonType.CANCEL);

        TextField txtUsername = new TextField();
        PasswordField txtPassword = new PasswordField();
        TextField txtNombre = new TextField();
        TextField txtApellido = new TextField();

        // ComboBox de enums (tipado sin diamante)
        ComboBox<Rol> cmbRol = new ComboBox<Rol>();
        cmbRol.setItems(FXCollections.observableArrayList(Rol.values()));
        cmbRol.getSelectionModel().select(Rol.VENTAS);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        grid.addRow(0, new Label("Usuario:"), txtUsername);
        grid.addRow(1, new Label("Contraseña:"), txtPassword);
        grid.addRow(2, new Label("Nombre:"), txtNombre);
        grid.addRow(3, new Label("Apellido:"), txtApellido);
        grid.addRow(4, new Label("Rol:"), cmbRol);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btCrear) {
                String user = txtUsername.getText().trim();
                String pass = txtPassword.getText().trim();
                String nom  = txtNombre.getText().trim();
                String ape  = txtApellido.getText().trim();
                Rol rol     = cmbRol.getValue();

                if (user.isEmpty() || pass.isEmpty()) {
                    mostrarAlerta("Datos incompletos", "Usuario y contraseña son obligatorios.");
                    return null;
                }

                Usuario u = new Usuario();
                u.setUsername(user);
                u.setPasswordHash(PasswordUtils.sha256(pass));
                u.setRol(rol);
                u.setNombre(nom.isEmpty() ? null : nom);
                u.setApellido(ape.isEmpty() ? null : ape);
                return u;
            }
            return null;
        });

        var result = dialog.showAndWait();
        result.ifPresent(u -> {
            try {
                usuarioDao.insertar(u);
                cargarUsuarios();
                mostrarInfo("Usuario creado", "Se creó el usuario " + u.getUsername());
            } catch (SQLException e) {
                if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique")) {
                    mostrarAlerta("Duplicado", "El nombre de usuario ya existe.");
                } else {
                    e.printStackTrace();
                    mostrarAlerta("Error", "No se pudo crear el usuario:\n" + e.getMessage());
                }
            }
        });
    }


    @FXML
    private void onResetPassClick() {
        Usuario sel = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAlerta("Sin selección", "Selecciona un usuario de la tabla.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Restablecer contraseña");
        dialog.setHeaderText("Usuario: " + sel.getUsername());
        dialog.setContentText("Nueva contraseña:");
        dialog.initModality(Modality.APPLICATION_MODAL);

        var res = dialog.showAndWait();
        if (res.isPresent()) {
            String nueva = res.get().trim();
            if (nueva.isEmpty()) {
                mostrarAlerta("Inválido", "La contraseña no puede estar vacía.");
                return;
            }
            try {
                usuarioDao.actualizarPassword(sel.getId(), PasswordUtils.sha256(nueva));
                mostrarInfo("Listo", "Contraseña actualizada para " + sel.getUsername());
            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlerta("Error", "No se pudo actualizar la contraseña:\n" + e.getMessage());
            }
        }
    }

    // Añade esto al final de la clase UsuariosController
    @FXML
    private void onEditarClick() {
        Usuario sel = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAlerta("Sin selección", "Selecciona un usuario para editar.");
            return;
        }
        if (!Sesion.isAdmin()) {
            mostrarAlerta("Acceso denegado", "Solo un administrador puede editar usuarios.");
            return;
        }

        // Campos (username no editable aquí)
        TextField txtUsername = new TextField(sel.getUsername());
        txtUsername.setDisable(true);

        TextField txtNombre   = new TextField(sel.getNombre()   == null ? "" : sel.getNombre());
        TextField txtApellido = new TextField(sel.getApellido() == null ? "" : sel.getApellido());

        ComboBox<Rol> cmbRol = new ComboBox<Rol>();
        cmbRol.setItems(FXCollections.observableArrayList(Rol.values()));
        cmbRol.getSelectionModel().select(sel.getRol());

        // Diálogo
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Editar usuario");
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(8); grid.setVgap(8);
        grid.setPadding(new Insets(10));
        grid.addRow(0, new Label("Usuario:"),  txtUsername);
        grid.addRow(1, new Label("Nombre:"),   txtNombre);
        grid.addRow(2, new Label("Apellido:"), txtApellido);
        grid.addRow(3, new Label("Rol:"),      cmbRol);

        d.getDialogPane().setContent(grid);

        var res = d.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK) return;

        // Validación: no dejar el sistema sin administradores
        Rol nuevoRol = cmbRol.getValue();
        long totalAdmins = datos.stream().filter(u -> u.getRol() == Rol.ADMIN).count();
        boolean estaQuitandoUltimoAdmin = (sel.getRol() == Rol.ADMIN) && (nuevoRol != Rol.ADMIN) && (totalAdmins <= 1);
        if (estaQuitandoUltimoAdmin) {
            mostrarAlerta("No permitido", "Debe existir al menos un usuario con rol ADMIN.");
            return;
        }

        // Actualizar en BD
        sel.setNombre(txtNombre.getText().trim().isEmpty() ? null : txtNombre.getText().trim());
        sel.setApellido(txtApellido.getText().trim().isEmpty() ? null : txtApellido.getText().trim());
        sel.setRol(nuevoRol);

        try {
            usuarioDao.actualizarDatos(sel);
            cargarUsuarios();
            mostrarInfo("Usuario actualizado", "Se actualizaron los datos de " + sel.getUsername());
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo actualizar el usuario:\n" + e.getMessage());
        }
    }

    @FXML
    private void onCerrarClick() {
        Stage stage = (Stage) tablaUsuarios.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void mostrarInfo(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
