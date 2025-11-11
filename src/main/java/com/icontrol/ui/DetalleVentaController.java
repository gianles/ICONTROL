package com.icontrol.ui;

import com.icontrol.dao.ProductoDao;
import com.icontrol.dao.ProductoDaoSqlite;
import com.icontrol.dao.VentaDao;
import com.icontrol.dao.VentaDaoSqlite;
import com.icontrol.model.LineaVenta;
import com.icontrol.model.Producto;
import com.icontrol.model.Venta;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.icontrol.dao.ClienteDao;
import com.icontrol.dao.ClienteDaoSqlite;
import com.icontrol.model.Cliente;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.stage.Window;


public class DetalleVentaController {

    @FXML
    private Label lblCabecera;

    @FXML
    private Label lblTotal;

    @FXML
    private Label lblIva;

    @FXML
    private Label lblItems;

    // 👇 NUEVO: nombre del cliente
    @FXML
    private Label lblCliente;

    @FXML
    private TableView<LineaDetalle> tablaLineas;

    @FXML
    private TableColumn<LineaDetalle, String> colReferencia;

    @FXML
    private TableColumn<LineaDetalle, String> colDescripcion;

    @FXML
    private TableColumn<LineaDetalle, Number> colCantidad;

    @FXML
    private TableColumn<LineaDetalle, Number> colPvp;

    @FXML
    private TableColumn<LineaDetalle, Number> colSubtotal;

    @FXML
    private TableColumn<LineaDetalle, Number> colIvaLinea;

    private final VentaDao ventaDao = new VentaDaoSqlite();
    private final ProductoDao productoDao = new ProductoDaoSqlite();
    private final ClienteDao clienteDao = new ClienteDaoSqlite();

    private final ObservableList<LineaDetalle> lineas = FXCollections.observableArrayList();

    private final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private void initialize() {
        colReferencia.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getReferencia()));
        colDescripcion.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDescripcion()));
        colCantidad.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getCantidad()));
        colPvp.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getPvpUnitario()));
        colSubtotal.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getSubtotal()));
        colIvaLinea.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getIvaLinea()));

        tablaLineas.setItems(lineas);
    }

    public void setVenta(Venta venta) {
        // Cabecera básica
        lblCabecera.setText("Venta #" + venta.getId() + " - " +
                venta.getFecha().format(FMT));
        lblTotal.setText(String.format("%.2f €", venta.getTotal()));
        lblIva.setText(String.format("%.2f €", venta.getIvaTotal()));
        lblItems.setText(venta.getNumItems() + " unidad(es)");

        // 👇 Nombre del cliente
        try {
            Long idCli = venta.getIdCliente();
            if (idCli == null) {
                lblCliente.setText("(Sin cliente)");
            } else {
                Optional<Cliente> opCli = clienteDao.buscarPorId(idCli);
                if (opCli.isPresent()) {
                    lblCliente.setText(opCli.get().getNombre());
                } else {
                    lblCliente.setText("(Cliente no encontrado)");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            lblCliente.setText("(Error cargando cliente)");
        }

        // Cargar líneas desde BD
        try {
            List<LineaVenta> lineasBD = ventaDao.buscarLineasPorVenta(venta.getId());
            lineas.clear();

            for (LineaVenta lv : lineasBD) {
                Optional<Producto> opProd = productoDao.buscarPorId(lv.getIdProducto());
                String ref = opProd.map(Producto::getReferencia).orElse("(sin ref)");
                String desc = opProd.map(Producto::getDescripcion).orElse("(producto borrado)");

                LineaDetalle detalle = new LineaDetalle(
                        ref,
                        desc,
                        lv.getCantidad(),
                        lv.getPrecioUnitario(),
                        lv.getSubtotal(),
                        lv.getIva()
                );

                lineas.add(detalle);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudieron cargar las líneas de la venta:\n" + e.getMessage());
        }
    }

    @FXML
    private void onCerrarClick() {
        Stage stage = (Stage) tablaLineas.getScene().getWindow();
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
    private void onImprimirClick() {
        try {
            // Nodo que queremos imprimir (toda la ventana)
            Node root = tablaLineas.getScene().getRoot();
            Window owner = tablaLineas.getScene().getWindow();

            PrinterJob job = PrinterJob.createPrinterJob();
            if (job == null) {
                mostrarAlerta("Impresión", "No se pudo crear el trabajo de impresión.");
                return;
            }

            // Mostrar el diálogo de impresora al usuario
            boolean continuar = job.showPrintDialog(owner);
            if (!continuar) {
                job.endJob();
                return;
            }

            // Imprimir una página con el contenido del root
            boolean exito = job.printPage(root);

            if (exito) {
                job.endJob();
                mostrarAlerta("Impresión", "Factura enviada a la impresora.");
            } else {
                job.endJob();
                mostrarAlerta("Impresión", "No se pudo imprimir la factura.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Se produjo un error al imprimir:\n" + e.getMessage());
        }
    }


    /**
     * DTO para mostrar cada línea en la tabla.
     */
    public static class LineaDetalle {

        private final String referencia;
        private final String descripcion;
        private final int cantidad;
        private final double pvpUnitario;
        private final double subtotal;
        private final double ivaLinea;

        public LineaDetalle(String referencia, String descripcion,
                            int cantidad, double pvpUnitario,
                            double subtotal, double ivaLinea) {
            this.referencia = referencia;
            this.descripcion = descripcion;
            this.cantidad = cantidad;
            this.pvpUnitario = pvpUnitario;
            this.subtotal = subtotal;
            this.ivaLinea = ivaLinea;
        }

        public String getReferencia() {
            return referencia;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public int getCantidad() {
            return cantidad;
        }

        public double getPvpUnitario() {
            return pvpUnitario;
        }

        public double getSubtotal() {
            return subtotal;
        }

        public double getIvaLinea() {
            return ivaLinea;
        }
    }
}
