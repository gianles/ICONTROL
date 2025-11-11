package com.icontrol.ui;

import com.icontrol.dao.ProductoDao;
import com.icontrol.dao.ProductoDaoSqlite;
import com.icontrol.dao.VentaDao;
import com.icontrol.dao.VentaDaoSqlite;
import com.icontrol.dao.ClienteDao;
import com.icontrol.dao.ClienteDaoSqlite;
import com.icontrol.model.LineaVenta;
import com.icontrol.model.Producto;
import com.icontrol.model.Venta;
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
import java.time.LocalDateTime;
import java.util.Optional;

public class VentasController {

    @FXML
    private TextField txtReferencia;

    @FXML
    private Label lblDescripcion;

    @FXML
    private Label lblPvp;

    @FXML
    private TextField txtCantidad;

    @FXML
    private TableView<LineaVentaItem> tablaLineas;

    @FXML
    private TableColumn<LineaVentaItem, String> colReferencia;

    @FXML
    private TableColumn<LineaVentaItem, String> colDescripcion;

    @FXML
    private TableColumn<LineaVentaItem, Integer> colCantidad;

    @FXML
    private TableColumn<LineaVentaItem, Double> colPvp;

    @FXML
    private TableColumn<LineaVentaItem, Double> colSubtotal;

    @FXML
    private Label lblTotal;

    // 👇 Cliente seleccionado para la venta
    @FXML
    private ComboBox<Cliente> cmbCliente;

    private final ClienteDao clienteDao = new ClienteDaoSqlite();
    private final ObservableList<Cliente> clientes = FXCollections.observableArrayList();

    private final ProductoDao productoDao = new ProductoDaoSqlite();
    private final VentaDao ventaDao = new VentaDaoSqlite();

    private final ObservableList<LineaVentaItem> lineas = FXCollections.observableArrayList();

    // Producto encontrado en la búsqueda actual
    private Producto productoActual = null;

    // IVA fijo (por ejemplo 21 %)
    private static final double IVA = 0.21;

    @FXML
    private void initialize() {
        colReferencia.setCellValueFactory(new PropertyValueFactory<>("referencia"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPvp.setCellValueFactory(new PropertyValueFactory<>("pvpUnitario"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        tablaLineas.setItems(lineas);

        // 👇 cargar clientes para el ComboBox
        cargarClientes();

        actualizarTotal();
    }

    private void cargarClientes() {
        try {
            clientes.clear();
            clientes.addAll(clienteDao.buscarTodos());
            cmbCliente.setItems(clientes);
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudieron cargar los clientes:\n" + e.getMessage());
        }
    }

    @FXML
    private void onBuscarProductoClick() {
        String ref = txtReferencia.getText().trim();

        // Si hay referencia escrita, buscamos como antes
        if (!ref.isEmpty()) {
            try {
                Optional<Producto> op = productoDao.buscarPorReferencia(ref);
                if (op.isEmpty()) {
                    productoActual = null;
                    lblDescripcion.setText("");
                    lblPvp.setText("");
                    mostrarAlerta("No encontrado", "No se encontró ningún producto con esa referencia.");
                    return;
                }

                productoActual = op.get();
                lblDescripcion.setText(productoActual.getDescripcion());
                lblPvp.setText(String.format("%.2f €", productoActual.getPvp()));

            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlerta("Error", "No se pudo buscar el producto:\n" + e.getMessage());
            }
            return;
        }

        // Si NO hay referencia, abrimos el selector de productos
        abrirSelectorProducto();
    }

    private void abrirSelectorProducto() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/ui/selector-producto-view.fxml"));
            javafx.scene.Parent root = loader.load();

            SelectorProductoController controller = loader.getController();

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Seleccionar producto");
            stage.setScene(new javafx.scene.Scene(root, 700, 400));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

            Producto seleccionado = controller.getSeleccionado();
            if (seleccionado != null) {
                productoActual = seleccionado;
                txtReferencia.setText(seleccionado.getReferencia());
                lblDescripcion.setText(seleccionado.getDescripcion());
                lblPvp.setText(String.format("%.2f €", seleccionado.getPvp()));
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir el selector de productos:\n" + e.getMessage());
        }
    }


    @FXML
    private void onAgregarLineaClick() {
        if (productoActual == null) {
            mostrarAlerta("Sin producto", "Primero busca y selecciona un producto.");
            return;
        }

        String cantStr = txtCantidad.getText().trim();
        if (cantStr.isEmpty()) {
            mostrarAlerta("Cantidad vacía", "Introduce una cantidad.");
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(cantStr);
        } catch (NumberFormatException e) {
            mostrarAlerta("Cantidad inválida", "La cantidad debe ser un número entero.");
            return;
        }

        if (cantidad <= 0) {
            mostrarAlerta("Cantidad inválida", "La cantidad debe ser mayor que cero.");
            return;
        }

        // Comprobar stock disponible (sumando si ya existe línea de este producto)
        int cantidadYaEnLineas = lineas.stream()
                .filter(li -> li.getProducto().getId() == productoActual.getId())
                .mapToInt(LineaVentaItem::getCantidad)
                .sum();

        if (cantidad + cantidadYaEnLineas > productoActual.getStock()) {
            mostrarAlerta("Stock insuficiente",
                    "No hay stock suficiente para esa cantidad.\n" +
                            "Stock disponible: " + productoActual.getStock());
            return;
        }

        // Si ya existe línea de este producto, sumamos cantidad
        for (LineaVentaItem item : lineas) {
            if (item.getProducto().getId() == productoActual.getId()) {
                item.setCantidad(item.getCantidad() + cantidad);
                tablaLineas.refresh();
                actualizarTotal();
                limpiarCamposLinea();
                return;
            }
        }

        // Si no existe, creamos una línea nueva
        LineaVentaItem nueva = new LineaVentaItem(productoActual, cantidad, productoActual.getPvp());
        lineas.add(nueva);

        actualizarTotal();
        limpiarCamposLinea();
    }

    @FXML
    private void onEliminarLineaClick() {
        LineaVentaItem seleccionada = tablaLineas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta("Sin selección", "Selecciona una línea para eliminar.");
            return;
        }

        lineas.remove(seleccionada);
        actualizarTotal();
    }

    @FXML
    private void onConfirmarVentaClick() {
        if (lineas.isEmpty()) {
            mostrarAlerta("Sin líneas", "Añade al menos un producto a la venta.");
            return;
        }

        try {
            // Calcular totales
            double base = lineas.stream().mapToDouble(LineaVentaItem::getSubtotal).sum();
            double ivaTotal = base * IVA;
            double total = base + ivaTotal;

            Venta venta = new Venta();
            venta.setFecha(LocalDateTime.now());

            // 👇 Asociar cliente (si se seleccionó alguno)
            Cliente cli = cmbCliente.getValue();
            venta.setIdCliente(cli != null ? cli.getId() : null);

            venta.setTotal(total);
            venta.setIvaTotal(ivaTotal);

            // Crear líneas de venta "reales" para la BD
            for (LineaVentaItem item : lineas) {
                LineaVenta lv = new LineaVenta();
                lv.setIdProducto(item.getProducto().getId());
                lv.setCantidad(item.getCantidad());
                lv.setPrecioUnitario(item.getPvpUnitario());
                lv.setIva(item.getSubtotal() * IVA);
                lv.setSubtotal(item.getSubtotal());

                venta.addLinea(lv);
            }

            // Guardar venta y líneas
            ventaDao.insertarConLineas(venta);

            // Actualizar stock de productos
            for (LineaVentaItem item : lineas) {
                Producto p = item.getProducto();
                int nuevoStock = p.getStock() - item.getCantidad();
                p.setStock(nuevoStock);
                productoDao.actualizar(p);
            }

            mostrarAlerta("Venta registrada",
                    "La venta se ha registrado correctamente.\nTotal: " + String.format("%.2f €", venta.getTotal()));

            // Limpiar para una nueva venta
            lineas.clear();
            actualizarTotal();
            limpiarCamposLinea();
            cmbCliente.setValue(null);

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo registrar la venta:\n" + e.getMessage());
        }
    }

    @FXML
    private void onNuevoClienteClick() {
        try {
            // Id máximo actual (para localizar luego el nuevo cliente)
            long maxIdAntes = clientes.stream()
                    .mapToLong(Cliente::getId)
                    .max()
                    .orElse(0L);

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ui/nuevo-cliente-view.fxml"));
            Parent root = loader.load();

            NuevoClienteController controller = loader.getController();
            controller.setOnClienteGuardado(() -> {
                // Al guardar cliente, recargamos la lista
                cargarClientes();
            });

            Stage stage = new Stage();
            stage.setTitle("Nuevo cliente");
            stage.setScene(new Scene(root, 480, 260));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Después de recargar, intentamos seleccionar el cliente recién creado
            Cliente nuevo = clientes.stream()
                    .filter(c -> c.getId() > maxIdAntes)
                    .reduce((a, b) -> b) // último nuevo
                    .orElse(null);

            if (nuevo != null) {
                cmbCliente.setValue(nuevo);
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir el formulario de cliente:\n" + e.getMessage());
        }
    }

    @FXML
    private void onCerrarClick() {
        Stage stage = (Stage) tablaLineas.getScene().getWindow();
        stage.close();
    }

    private void limpiarCamposLinea() {
        txtReferencia.clear();
        txtCantidad.clear();
        lblDescripcion.setText("");
        lblPvp.setText("");
        productoActual = null;
    }

    private void actualizarTotal() {
        double base = lineas.stream().mapToDouble(LineaVentaItem::getSubtotal).sum();
        double ivaTotal = base * IVA;
        double total = base + ivaTotal;

        lblTotal.setText(String.format("%.2f €", total));
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Clase auxiliar para mostrar líneas de venta en la tabla.
     */
    public static class LineaVentaItem {

        private final Producto producto;
        private int cantidad;
        private final double pvpUnitario;

        public LineaVentaItem(Producto producto, int cantidad, double pvpUnitario) {
            this.producto = producto;
            this.cantidad = cantidad;
            this.pvpUnitario = pvpUnitario;
        }

        public Producto getProducto() {
            return producto;
        }

        public String getReferencia() {
            return producto.getReferencia();
        }

        public String getDescripcion() {
            return producto.getDescripcion();
        }

        public int getCantidad() {
            return cantidad;
        }

        public void setCantidad(int cantidad) {
            this.cantidad = cantidad;
        }

        public double getPvpUnitario() {
            return pvpUnitario;
        }

        public double getSubtotal() {
            return cantidad * pvpUnitario;
        }
    }
}
