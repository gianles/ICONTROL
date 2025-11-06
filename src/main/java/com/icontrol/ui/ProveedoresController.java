package com.icontrol.ui;

import com.icontrol.dao.ProductoDao;
import com.icontrol.dao.ProductoDaoSqlite;
import com.icontrol.dao.ProveedorDao;
import com.icontrol.dao.ProveedorDaoSqlite;
import com.icontrol.model.Proveedor;
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

import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;


import java.sql.SQLException;
import java.util.List;

public class ProveedoresController {

    @FXML
    private TableView<Proveedor> tablaProveedores;

    @FXML
    private TableColumn<Proveedor, String> colNombre;

    @FXML
    private TableColumn<Proveedor, String> colNif;

    @FXML
    private TableColumn<Proveedor, String> colTelefono;

    @FXML
    private TableColumn<Proveedor, String> colEmail;

    @FXML
    private TextField txtBuscar;

    private final ProveedorDao proveedorDao = new ProveedorDaoSqlite();
    private final ProductoDao productoDao = new ProductoDaoSqlite();   // 👈 para contar productos

    @FXML
    private void initialize() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNif.setCellValueFactory(new PropertyValueFactory<>("nif"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        cargarProveedores();
    }

    private void cargarProveedores() {
        try {
            List<Proveedor> lista = proveedorDao.buscarTodos();
            ObservableList<Proveedor> datos = FXCollections.observableArrayList(lista);
            tablaProveedores.setItems(datos);
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudieron cargar los proveedores:\n" + e.getMessage());
        }
    }

    private void cargarProveedoresFiltrados(String filtro) {
        try {
            List<Proveedor> lista = proveedorDao.buscarPorNombre(filtro);
            ObservableList<Proveedor> datos = FXCollections.observableArrayList(lista);
            tablaProveedores.setItems(datos);
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudieron cargar los proveedores:\n" + e.getMessage());
        }
    }

    @FXML
    private void onBuscarKeyReleased() {
        String filtro = txtBuscar.getText().trim();
        if (filtro.isEmpty()) {
            cargarProveedores();
        } else {
            cargarProveedoresFiltrados(filtro);
        }
    }

    @FXML
    private void onNuevoClick() {
        abrirFormularioProveedor(null);
    }

    @FXML
    private void onEditarClick() {
        Proveedor seleccionado = tablaProveedores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Sin selección", "Selecciona un proveedor de la tabla para editar.");
            return;
        }
        abrirFormularioProveedor(seleccionado);
    }

    // 👇 NUEVO: eliminar con comprobación de productos asociados
    @FXML
    private void onEliminarClick() {
        Proveedor seleccionado = tablaProveedores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Sin selección", "Selecciona un proveedor para eliminar.");
            return;
        }

        // 1) Comprobar si tiene productos asociados
        try {
            int productosAsociados = productoDao.contarPorProveedor(seleccionado.getId());
            if (productosAsociados > 0) {

                String textoProductos;
                if (productosAsociados == 1) {
                    textoProductos = "Este proveedor tiene 1 producto asociado.\n";
                } else {
                    textoProductos = "Este proveedor tiene " + productosAsociados + " productos asociados.\n";
                }

                mostrarAlerta(
                        "No se puede eliminar",
                        textoProductos +
                                "Primero debes reasignar o eliminar esos productos."
                );
                return;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error",
                    "No se pudo comprobar si el proveedor tiene productos asociados:\n" + e.getMessage());
            return;
        }

        // 2) Si no tiene productos, pedir confirmación y eliminar
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Seguro que quieres eliminar el proveedor:\n"
                + seleccionado.getNombre() + "?");

        var result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                proveedorDao.eliminar(seleccionado.getId());
                cargarProveedores();
            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlerta("Error", "No se pudo eliminar el proveedor:\n" + e.getMessage());
            }
        }
    }

    @FXML
    private void onCerrarClick() {
        Stage stage = (Stage) tablaProveedores.getScene().getWindow();
        stage.close();
    }

    private void abrirFormularioProveedor(Proveedor proveedor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/nuevo-proveedor-view.fxml"));
            Parent root = loader.load();

            NuevoProveedorController controller = loader.getController();
            controller.setOnProveedorGuardado(this::cargarProveedores);

            if (proveedor != null) {
                controller.setProveedorEditar(proveedor);
            }

            Stage stage = new Stage();
            stage.setTitle(proveedor == null ? "Nuevo proveedor" : "Editar proveedor");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir el formulario:\n" + e.getMessage());
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    private void onExportarCsvClick() {
        try {
            // Elegir archivo destino
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exportar proveedores a CSV");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV (*.csv)", "*.csv")
            );
            fileChooser.setInitialFileName("proveedores.csv");

            File file = fileChooser.showSaveDialog(
                    tablaProveedores.getScene().getWindow()
            );
            if (file == null) {
                return; // usuario canceló
            }

            // Obtener datos de la BD
            List<Proveedor> lista = proveedorDao.buscarTodos();

            // Escribir CSV en UTF-8
            try (PrintWriter pw = new PrintWriter(
                    new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

                // Cabecera
                pw.println("id;nombre;nif;telefono;email");

                // Filas
                for (Proveedor p : lista) {
                    pw.printf("%d;%s;%s;%s;%s%n",
                            p.getId(),
                            csv(p.getNombre()),
                            csv(p.getNif()),
                            csv(p.getTelefono()),
                            csv(p.getEmail())
                    );
                }
            }

            mostrarAlerta("Exportación completada",
                    "Los proveedores se han exportado correctamente a:\n" + file.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo exportar el CSV:\n" + e.getMessage());
        }
    }

    /** Escapa texto para CSV (usa ; como separador, así que solo cuidamos comillas y saltos de línea) */
    private String csv(String value) {
        if (value == null) return "";
        String v = value.replace("\"", "\"\"");
        return "\"" + v + "\""; // lo envolvemos entre comillas
    }

}
