package com.icontrol.ui;

import com.icontrol.dao.ProveedorDao;
import com.icontrol.dao.ProveedorDaoSqlite;
import com.icontrol.model.Proveedor;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NuevoProveedorController {

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtNif;

    @FXML
    private TextField txtTelefono;

    @FXML
    private TextField txtEmail;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnEliminar;

    private final ProveedorDao proveedorDao = new ProveedorDaoSqlite();

    private Runnable onProveedorGuardado;

    private Proveedor proveedorEditar;

    public void setOnProveedorGuardado(Runnable onProveedorGuardado) {
        this.onProveedorGuardado = onProveedorGuardado;
    }

    public void setProveedorEditar(Proveedor proveedor) {
        this.proveedorEditar = proveedor;

        if (proveedor != null) {
            txtNombre.setText(proveedor.getNombre());
            txtNif.setText(proveedor.getNif());
            txtTelefono.setText(proveedor.getTelefono());
            txtEmail.setText(proveedor.getEmail());

            btnEliminar.setVisible(true);
        } else {
            btnEliminar.setVisible(false);
        }
    }

    private boolean validarFormulario() {
        String nombre = txtNombre.getText().trim();
        String nif = txtNif.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String email = txtEmail.getText().trim();

        StringBuilder errores = new StringBuilder();

        // Nombre obligatorio
        if (nombre.isEmpty()) {
            errores.append("- El nombre del proveedor es obligatorio.\n");
        }

        // Teléfono opcional, pero si se rellena, que parezca un teléfono
        if (!telefono.isEmpty()) {
            if (!telefono.matches("\\d{6,15}")) {
                errores.append("- El teléfono debe contener solo dígitos (6 a 15 caracteres).\n");
            }
        }

        // Email opcional, pero si se rellena, que tenga formato básico válido
        if (!email.isEmpty()) {
            if (!email.matches(".+@.+\\..+")) {
                errores.append("- El email no tiene un formato válido.\n");
            }
        }

        // NIF opcional, validación muy sencilla (solo ejemplo)
        if (!nif.isEmpty()) {
            if (nif.length() < 5) {
                errores.append("- El NIF es demasiado corto.\n");
            }
        }

        if (errores.length() > 0) {
            mostrarAlerta("Datos inválidos", errores.toString());
            return false;
        }

        return true;
    }


    @FXML
    private void onGuardarClick() {
        try {
            if (!validarFormulario()) {
                return; // hay errores, no seguimos
            }

            String nombre = txtNombre.getText().trim();
            String nif = txtNif.getText().trim();
            String telefono = txtTelefono.getText().trim();
            String email = txtEmail.getText().trim();

            if (proveedorEditar == null) {
                // NUEVO
                Proveedor nuevo = new Proveedor(nombre, nif, telefono, email);
                proveedorDao.insertar(nuevo);
            } else {
                // EDICIÓN
                proveedorEditar.setNombre(nombre);
                proveedorEditar.setNif(nif);
                proveedorEditar.setTelefono(telefono);
                proveedorEditar.setEmail(email);

                proveedorDao.actualizar(proveedorEditar);
            }

            if (onProveedorGuardado != null) {
                onProveedorGuardado.run();
            }

            cerrarVentana();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Se ha producido un error al guardar el proveedor:\n" + e.getMessage());
        }
    }

    @FXML
    private void onEliminarClick() {
        if (proveedorEditar == null) {
            mostrarAlerta("No disponible", "Solo se pueden eliminar proveedores existentes.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Seguro que deseas eliminar el proveedor:\n"
                + proveedorEditar.getNombre() + "?");

        var result = confirm.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            try {
                proveedorDao.eliminar(proveedorEditar.getId());

                if (onProveedorGuardado != null) {
                    onProveedorGuardado.run();
                }

                cerrarVentana();

            } catch (Exception e) {
                e.printStackTrace();
                mostrarAlerta("Error", "No se pudo eliminar el proveedor:\n" + e.getMessage());
            }
        }
    }

    @FXML
    private void onCancelarClick() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnGuardar.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
