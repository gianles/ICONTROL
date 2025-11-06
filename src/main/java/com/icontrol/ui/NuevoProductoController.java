package com.icontrol.ui;

import com.icontrol.dao.ProductoDao;
import com.icontrol.dao.ProductoDaoSqlite;
import com.icontrol.dao.ProveedorDao;
import com.icontrol.dao.ProveedorDaoSqlite;
import com.icontrol.model.Producto;
import com.icontrol.model.Proveedor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;


import java.sql.SQLException;
import java.util.List;

public class NuevoProductoController {

    @FXML
    private TextField txtReferencia;

    @FXML
    private TextField txtDescripcion;

    @FXML
    private TextField txtPvp;

    @FXML
    private TextField txtStock;

    @FXML
    private TextField txtStockMinimo;

    // 👇 Ahora usamos ComboBox en vez de TextField para proveedor
    @FXML
    private ComboBox<Proveedor> cmbProveedor;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnEliminar;

    private final ProductoDao productoDao = new ProductoDaoSqlite();
    private final ProveedorDao proveedorDao = new ProveedorDaoSqlite();

    /** Lista observable de proveedores para el ComboBox */
    private final ObservableList<Proveedor> proveedores = FXCollections.observableArrayList();

    /** Callback opcional para avisar al inventario de que hay cambios (crear o editar) */
    private Runnable onProductoGuardado;

    /** Producto que se está editando; si es null, el formulario funciona como "nuevo" */
    private Producto productoEditar;

    @FXML
    private void initialize() {
        cargarProveedores();
        configurarComboProveedor();
    }

    private void cargarProveedores() {
        try {
            List<Proveedor> lista = proveedorDao.buscarTodos();
            proveedores.setAll(lista);
            cmbProveedor.setItems(proveedores);
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudieron cargar los proveedores:\n" + e.getMessage());
        }
    }

    /** Para que el ComboBox muestre solo el nombre del proveedor */
    private void configurarComboProveedor() {
        cmbProveedor.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Proveedor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNombre());
            }
        });

        cmbProveedor.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Proveedor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNombre());
            }
        });
    }

    public void setOnProductoGuardado(Runnable onProductoGuardado) {
        this.onProductoGuardado = onProductoGuardado;
    }

    /** Rellena el formulario con los datos del producto a editar */
    public void setProductoEditar(Producto producto) {
        this.productoEditar = producto;

        if (producto != null) {
            txtReferencia.setText(producto.getReferencia());
            txtDescripcion.setText(producto.getDescripcion());
            txtPvp.setText(String.valueOf(producto.getPvp()));
            txtStock.setText(String.valueOf(producto.getStock()));
            txtStockMinimo.setText(String.valueOf(producto.getStockMinimo()));

            // Seleccionar proveedor correspondiente si tiene id_proveedor
            if (producto.getIdProveedor() != null) {
                Long idProv = producto.getIdProveedor();
                for (Proveedor prov : proveedores) {
                    if (prov.getId() == idProv) {
                        cmbProveedor.setValue(prov);
                        break;
                    }
                }
            } else {
                cmbProveedor.setValue(null);
            }

            // Mostrar botón eliminar solo en modo edición
            btnEliminar.setVisible(true);
        } else {
            btnEliminar.setVisible(false);
        }
    }

    @FXML
    private void onGuardarClick() {
        try {
            String referencia = txtReferencia.getText().trim();
            String descripcion = txtDescripcion.getText().trim();
            String pvpStr = txtPvp.getText().trim();
            String stockStr = txtStock.getText().trim();
            String stockMinStr = txtStockMinimo.getText().trim();

            // Validaciones sencillas
            if (referencia.isEmpty() || descripcion.isEmpty()
                    || pvpStr.isEmpty() || stockStr.isEmpty() || stockMinStr.isEmpty()) {
                mostrarAlerta("Datos incompletos",
                        "Referencia, descripción, PVP, stock y stock mínimo son obligatorios.");
                return;
            }

            double pvp;
            int stock;
            int stockMinimo;
            try {
                pvp = Double.parseDouble(pvpStr.replace(",", ".")); // por si ponen coma
                stock = Integer.parseInt(stockStr);
                stockMinimo = Integer.parseInt(stockMinStr);
            } catch (NumberFormatException e) {
                mostrarAlerta("Formato incorrecto",
                        "Revisa los campos numéricos (PVP, stock, stock mínimo).");
                return;
            }

            // 👇 Obtener id_proveedor desde el ComboBox (opcional)
            Proveedor proveedorSeleccionado = cmbProveedor.getValue();
            Long idProveedor = (proveedorSeleccionado != null) ? proveedorSeleccionado.getId() : null;

            // Decidimos si es nuevo o edición
            if (productoEditar == null) {
                // MODO NUEVO
                Producto nuevo = new Producto(referencia, descripcion, pvp, stock, stockMinimo, idProveedor);
                productoDao.insertar(nuevo);
                System.out.println("✅ Producto creado: " + nuevo);
            } else {
                // MODO EDICIÓN
                productoEditar.setReferencia(referencia);
                productoEditar.setDescripcion(descripcion);
                productoEditar.setPvp(pvp);
                productoEditar.setStock(stock);
                productoEditar.setStockMinimo(stockMinimo);
                productoEditar.setIdProveedor(idProveedor);

                productoDao.actualizar(productoEditar);
                System.out.println("✅ Producto actualizado: " + productoEditar);
            }

            // Avisar al controlador de inventario (si se configuró)
            if (onProductoGuardado != null) {
                onProductoGuardado.run();
            }

            cerrarVentana();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error",
                    "Se ha producido un error al guardar el producto:\n" + e.getMessage());
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

    @FXML
    private void onEliminarClick() {
        if (productoEditar == null) {
            mostrarAlerta("No disponible", "Solo se pueden eliminar productos existentes.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Seguro que deseas eliminar el producto?\n"
                + productoEditar.getReferencia() + " - " + productoEditar.getDescripcion());

        var result = confirm.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            try {
                productoDao.eliminar(productoEditar.getId());
                System.out.println("🗑️ Producto eliminado: " + productoEditar.getReferencia());

                if (onProductoGuardado != null) {
                    onProductoGuardado.run(); // refrescar tabla en inventario
                }

                cerrarVentana();

            } catch (Exception e) {
                e.printStackTrace();
                mostrarAlerta("Error", "No se pudo eliminar el producto:\n" + e.getMessage());
            }
        }
    }

    @FXML
    private void onNuevoProveedorClick() {
        try {
            // Id máximo actual para detectar el proveedor nuevo
            long maxIdAntes = proveedores.stream()
                    .mapToLong(Proveedor::getId)
                    .max()
                    .orElse(0L);

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ui/nuevo-proveedor-view.fxml")
            );
            Parent root = loader.load();

            // Podemos usar el mismo formulario de proveedor
            NuevoProveedorController controller = loader.getController();
            // No hace falta configurar nada especial, por defecto es "nuevo"

            Stage stage = new Stage();
            stage.setTitle("Nuevo proveedor");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.showAndWait(); // Esperamos a que cierre

            // Al volver, recargamos proveedores
            cargarProveedores();

            // Intentar seleccionar el proveedor recién creado (id > maxIdAntes)
            Proveedor nuevo = proveedores.stream()
                    .filter(p -> p.getId() > maxIdAntes)
                    .reduce((a, b) -> b) // último de los nuevos
                    .orElse(null);

            if (nuevo != null) {
                cmbProveedor.setValue(nuevo);
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error",
                    "No se pudo abrir el formulario de proveedor:\n" + e.getMessage());
        }
    }

}
