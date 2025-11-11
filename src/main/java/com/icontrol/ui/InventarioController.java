package com.icontrol.ui;

import com.icontrol.dao.ProductoDao;
import com.icontrol.dao.ProductoDaoSqlite;
import com.icontrol.dao.VentaDao;
import com.icontrol.dao.VentaDaoSqlite;
import com.icontrol.model.Producto;
import com.icontrol.model.Venta;
import javafx.animation.PauseTransition;
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
import javafx.util.Duration;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class InventarioController {

    @FXML
    private TableView<Producto> tablaProductos;

    @FXML
    private TableColumn<Producto, String> colReferencia;

    @FXML
    private TableColumn<Producto, String> colDescripcion;

    @FXML
    private TableColumn<Producto, Number> colPvp;

    @FXML
    private TableColumn<Producto, Number> colStock;

    @FXML
    private TableColumn<Producto, Number> colStockMinimo;

    @FXML
    private Label statusLabel;

    // 🔹 Labels del mini-dashboard
    @FXML
    private Label lblStatProductos;

    @FXML
    private Label lblStatStockBajo;

    @FXML
    private Label lblStatVentasHoy;

    private final ProductoDao productoDao = new ProductoDaoSqlite();
    private final VentaDao ventaDao = new VentaDaoSqlite();

    private PauseTransition mensajeTimer;

    @FXML
    private void initialize() {
        // Vincular columnas con propiedades de Producto (usa getters)
        colReferencia.setCellValueFactory(new PropertyValueFactory<>("referencia"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colPvp.setCellValueFactory(new PropertyValueFactory<>("pvp"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colStockMinimo.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));

        // Selección múltiple en la tabla
        tablaProductos.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // La barra de estado no se ve ni ocupa espacio al inicio
        if (statusLabel != null) {
            statusLabel.setVisible(false);
            statusLabel.managedProperty().bind(statusLabel.visibleProperty());
        }

        // Cargar datos al iniciar (y actualizar mini-dashboard)
        cargarProductos();
    }

    private void mostrarMensaje(String texto) {
        if (statusLabel == null) {
            System.out.println("STATUS: " + texto);
            return;
        }

        statusLabel.setText(texto);
        statusLabel.setVisible(true); // mostrar barra

        // Reiniciar temporizador si ya había uno
        if (mensajeTimer != null) {
            mensajeTimer.stop();
        }

        mensajeTimer = new PauseTransition(Duration.seconds(3));
        mensajeTimer.setOnFinished(e -> {
            statusLabel.setText("");
            statusLabel.setVisible(false); // ocultar barra
        });
        mensajeTimer.play();
    }

    @FXML
    private void onActualizarClick() {
        cargarProductos();
        mostrarMensaje("Inventario actualizado.");
    }

    @FXML
    private void onNuevoClick() {
        abrirFormularioProducto(null); // null = modo nuevo
    }

    @FXML
    private void onEditarClick() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Sin selección", "Selecciona un producto de la tabla para editar.");
            return;
        }
        abrirFormularioProducto(seleccionado);
    }

    @FXML
    private void onProveedoresClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/proveedores-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("ICONTROL - Proveedores");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la gestión de proveedores:\n" + e.getMessage());
        }
    }

    @FXML
    private void onNuevaVentaClick() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ui/ventas-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("ICONTROL - Nueva venta");
            stage.setScene(new Scene(root, 800, 600));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Después de una venta, refrescamos el inventario y el resumen
            cargarProductos();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la ventana de ventas:\n" + e.getMessage());
        }
    }

    @FXML
    private void onHistorialVentasClick() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ui/ventas-historial-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("ICONTROL - Historial de ventas");

            Scene scene = new Scene(root, 900, 550);
            stage.setScene(scene);
            stage.setMinWidth(850);
            stage.setMinHeight(500);

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir el historial de ventas:\n" + e.getMessage());
        }
    }

    @FXML
    private void onNuevoClienteGlobalClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/nuevo-cliente-view.fxml"));
            Parent root = loader.load();

            NuevoClienteController controller = loader.getController();
            controller.setOnClienteGuardado(() -> {
                mostrarMensaje("Cliente creado correctamente.");
            });

            Stage stage = new Stage();
            stage.setTitle("Nuevo cliente");
            stage.setScene(new Scene(root, 480, 260));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir el formulario de cliente:\n" + e.getMessage());
        }
    }

    @FXML
    private void onClientesClick() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ui/clientes-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("ICONTROL - Clientes");
            stage.setScene(new Scene(root, 800, 500));
            stage.setMinWidth(700);
            stage.setMinHeight(400);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la gestión de clientes:\n" + e.getMessage());
        }
    }

    @FXML
    private void onEliminarClick() {
        var seleccionados = tablaProductos.getSelectionModel().getSelectedItems();

        if (seleccionados == null || seleccionados.isEmpty()) {
            mostrarAlerta("Sin selección", "Selecciona uno o varios productos de la tabla para eliminar.");
            return;
        }

        int total = seleccionados.size();

        String textoConfirmacion;
        if (total == 1) {
            Producto p = seleccionados.get(0);
            textoConfirmacion = "¿Seguro que quieres eliminar el producto:\n"
                    + p.getReferencia() + " - " + p.getDescripcion() + "?";
        } else {
            textoConfirmacion = "¿Seguro que quieres eliminar los " + total + " productos seleccionados?";
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText(null);
        confirm.setContentText(textoConfirmacion);

        var result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                var copia = List.copyOf(seleccionados);

                for (Producto p : copia) {
                    productoDao.eliminar(p.getId());
                }

                cargarProductos();

                if (total == 1) {
                    mostrarMensaje("Producto eliminado correctamente.");
                } else {
                    mostrarMensaje(total + " productos eliminados correctamente.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlerta("Error", "No se pudieron eliminar los productos:\n" + e.getMessage());
                mostrarMensaje("Error al eliminar productos.");
            }
        }
    }

    /** Método general que abre el formulario, en modo nuevo o edición */
    private void abrirFormularioProducto(Producto producto) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/nuevo-producto-view.fxml"));
            Parent root = loader.load();

            NuevoProductoController controller = loader.getController();
            controller.setOnProductoGuardado(() -> {
                cargarProductos();
                if (producto == null) {
                    mostrarMensaje("Producto creado correctamente.");
                } else {
                    mostrarMensaje("Producto actualizado correctamente.");
                }
            });

            if (producto != null) {
                controller.setProductoEditar(producto);
            }

            Stage stage = new Stage();
            stage.setTitle(producto == null ? "Nuevo producto" : "Editar producto");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir el formulario:\n" + e.getMessage());
            mostrarMensaje("Error al abrir el formulario.");
        }
    }

    private void cargarProductos() {
        try {
            List<Producto> lista = productoDao.buscarTodos();
            ObservableList<Producto> datos = FXCollections.observableArrayList(lista);
            tablaProductos.setItems(datos);

            // 👉 actualizar mini-dashboard
            actualizarResumen(lista);

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarMensaje("Error al cargar productos.");
        }
    }

    /** Calcula Productos / Stock bajo / Ventas hoy y lo pinta en las tarjetas */
    private void actualizarResumen(List<Producto> productos) {
        // 1) Total productos
        int totalProductos = productos.size();

        // 2) Productos con stock bajo
        long stockBajo = productos.stream()
                .filter(p -> p.getStock() <= p.getStockMinimo())
                .count();

        // 3) Ventas de hoy
        int ventasHoy = 0;
        try {
            List<Venta> ventas = ventaDao.buscarTodas();
            LocalDate hoy = LocalDate.now();

            ventasHoy = (int) ventas.stream()
                    .filter(v -> v.getFecha().toLocalDate().equals(hoy))
                    .count();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (lblStatProductos != null) {
            lblStatProductos.setText("Productos\n" + totalProductos);
        }
        if (lblStatStockBajo != null) {
            lblStatStockBajo.setText("Stock bajo\n" + stockBajo);
        }
        if (lblStatVentasHoy != null) {
            lblStatVentasHoy.setText("Ventas hoy\n" + ventasHoy);
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
