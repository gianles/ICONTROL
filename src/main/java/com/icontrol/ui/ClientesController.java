package com.icontrol.ui;

import com.icontrol.dao.ClienteDao;
import com.icontrol.dao.ClienteDaoSqlite;
import com.icontrol.model.Cliente;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class ClientesController {

    @FXML
    private TableView<Cliente> tablaClientes;

    @FXML
    private TableColumn<Cliente, String> colNombre;

    @FXML
    private TableColumn<Cliente, String> colNif;

    @FXML
    private TableColumn<Cliente, String> colTelefono;

    @FXML
    private TableColumn<Cliente, String> colEmail;

    @FXML
    private TextField txtBuscar;

    private final ClienteDao clienteDao = new ClienteDaoSqlite();

    @FXML
    private void initialize() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNif.setCellValueFactory(new PropertyValueFactory<>("nif"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        cargarClientes();
    }

    private void cargarClientes() {
        try {
            List<Cliente> lista = clienteDao.buscarTodos();
            ObservableList<Cliente> datos = FXCollections.observableArrayList(lista);
            tablaClientes.setItems(datos);
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudieron cargar los clientes:\n" + e.getMessage());
        }
    }

    private void cargarClientesFiltrados(String filtro) {
        try {
            List<Cliente> lista = clienteDao.buscarPorNombre(filtro);
            ObservableList<Cliente> datos = FXCollections.observableArrayList(lista);
            tablaClientes.setItems(datos);
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudieron cargar los clientes:\n" + e.getMessage());
        }
    }

    @FXML
    private void onBuscarKeyReleased() {
        String filtro = txtBuscar.getText().trim();
        if (filtro.isEmpty()) {
            cargarClientes();
        } else {
            cargarClientesFiltrados(filtro);
        }
    }

    @FXML
    private void onNuevoClick() {
        abrirFormularioCliente(null);
    }

    @FXML
    private void onEditarClick() {
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Sin selección", "Selecciona un cliente de la tabla para editar.");
            return;
        }
        abrirFormularioCliente(seleccionado);
    }

    @FXML
    private void onEliminarClick() {
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Sin selección", "Selecciona un cliente de la tabla para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Seguro que quieres eliminar el cliente:\n"
                + seleccionado.getNombre() + " (" + seleccionado.getNif() + ")?");

        var result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                clienteDao.eliminar(seleccionado.getId());
                cargarClientes();
            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlerta("Error",
                        "No se pudo eliminar el cliente.\n" +
                                "Es posible que tenga ventas asociadas.\n\n" +
                                e.getMessage());
            }
        }
    }

    @FXML
    private void onCerrarClick() {
        Stage stage = (Stage) tablaClientes.getScene().getWindow();
        stage.close();
    }

    private void abrirFormularioCliente(Cliente cliente) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ui/nuevo-cliente-view.fxml"));
            Parent root = loader.load();

            NuevoClienteController controller = loader.getController();
            controller.setOnClienteGuardado(this::cargarClientes);

            if (cliente != null) {
                controller.setClienteEditar(cliente);
            }

            Stage stage = new Stage();
            stage.setTitle(cliente == null ? "Nuevo cliente" : "Editar cliente");
            stage.setScene(new Scene(root, 480, 260));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir el formulario de cliente:\n" + e.getMessage());
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
