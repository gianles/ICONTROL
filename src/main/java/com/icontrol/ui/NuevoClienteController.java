package com.icontrol.ui;

import com.icontrol.dao.ClienteDao;
import com.icontrol.dao.ClienteDaoSqlite;
import com.icontrol.model.Cliente;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NuevoClienteController {

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

    private final ClienteDao clienteDao = new ClienteDaoSqlite();

    /** Callback para avisar a la pantalla de lista de que se ha guardado algo */
    private Runnable onClienteGuardado;

    /** Cliente en modo edición; si es null, estamos en modo "nuevo" */
    private Cliente clienteEditar;

    public void setOnClienteGuardado(Runnable onClienteGuardado) {
        this.onClienteGuardado = onClienteGuardado;
    }

    /** Rellena los campos para editar un cliente existente */
    public void setClienteEditar(Cliente cliente) {
        this.clienteEditar = cliente;

        if (cliente != null) {
            txtNombre.setText(cliente.getNombre());
            txtNif.setText(cliente.getNif());
            txtTelefono.setText(cliente.getTelefono());
            txtEmail.setText(cliente.getEmail());
        }
    }

    @FXML
    private void onGuardarClick() {
        try {
            String nombre = txtNombre.getText().trim();
            String nif = txtNif.getText().trim();
            String telefono = txtTelefono.getText().trim();
            String email = txtEmail.getText().trim();

            StringBuilder errores = new StringBuilder();

            if (nombre.isEmpty()) {
                errores.append("- El nombre es obligatorio.\n");
            }

            if (!telefono.isEmpty() && !telefono.matches("\\d{6,15}")) {
                errores.append("- El teléfono debe tener entre 6 y 15 dígitos.\n");
            }

            if (!email.isEmpty() && !email.matches(".+@.+\\..+")) {
                errores.append("- El email no tiene un formato válido.\n");
            }

            if (errores.length() > 0) {
                mostrarAlerta("Datos inválidos", errores.toString());
                return;
            }

            if (clienteEditar == null) {
                // MODO NUEVO
                Cliente nuevo = new Cliente(nombre, nif, telefono, email);
                clienteDao.insertar(nuevo);
            } else {
                // MODO EDICIÓN
                clienteEditar.setNombre(nombre);
                clienteEditar.setNif(nif);
                clienteEditar.setTelefono(telefono);
                clienteEditar.setEmail(email);

                clienteDao.actualizar(clienteEditar);
            }

            if (onClienteGuardado != null) {
                onClienteGuardado.run();
            }

            cerrarVentana();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo guardar el cliente:\n" + e.getMessage());
        }
    }

    @FXML
    private void onCancelarClick() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        // Usamos cualquier control que seguro existe
        Stage stage = (Stage) txtNombre.getScene().getWindow();
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
