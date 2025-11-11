package com.icontrol.ui;

import com.icontrol.dao.VentaDao;
import com.icontrol.dao.VentaDaoSqlite;
import com.icontrol.model.Venta;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TableRow;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.icontrol.dao.ClienteDao;
import com.icontrol.dao.ClienteDaoSqlite;
import com.icontrol.model.Cliente;

import java.util.HashMap;
import java.util.Map;

public class HistorialVentasController {

    @FXML
    private TableView<Venta> tablaVentas;

    @FXML
    private TableColumn<Venta, Number> colItems;

    @FXML
    private TableColumn<Venta, Number> colId;

    @FXML
    private TableColumn<Venta, String> colFecha;

    @FXML
    private TableColumn<Venta, String> colTotal;

    @FXML
    private TableColumn<Venta, String> colIva;

    @FXML
    private Label lblNumVentas;

    @FXML
    private Label lblTotalVentas;

    @FXML
    private Label lblTotalIva;

    @FXML
    private DatePicker dpDesde;

    @FXML
    private DatePicker dpHasta;

    // Lista completa de ventas (sin filtrar)
    private List<Venta> listaCompleta = new ArrayList<>();

    private final VentaDao ventaDao = new VentaDaoSqlite();
    private final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private TableColumn<Venta, String> colCliente;

    private final ClienteDao clienteDao = new ClienteDaoSqlite();
    private final Map<Long, String> nombresClientes = new HashMap<>();


    @FXML
    private void initialize() {
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleLongProperty(data.getValue().getId()));

        colFecha.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getFecha().format(FMT)
                )
        );

        colTotal.setCellValueFactory(data ->
                new SimpleStringProperty(
                        String.format("%.2f €", data.getValue().getTotal())
                )
        );

        colIva.setCellValueFactory(data ->
                new SimpleStringProperty(
                        String.format("%.2f €", data.getValue().getIvaTotal())
                )
        );

        // número de productos (unidades)
        colItems.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(data.getValue().getNumItems())
        );

        // 👇 Primero cargamos clientes en el mapa
        cargarClientesEnMapa();

        // 👇 Luego configuramos la columna Cliente
        colCliente.setCellValueFactory(data -> {
            Long idCli = data.getValue().getIdCliente();
            if (idCli == null) {
                return new SimpleStringProperty("");
            }
            String nombre = nombresClientes.get(idCli);
            return new SimpleStringProperty(nombre != null ? nombre : "");
        });

        cargarVentas();

        // RowFactory para doble clic → abre detalle
        tablaVentas.setRowFactory(tv -> {
            TableRow<Venta> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Venta venta = row.getItem();
                    abrirDetalleVenta(venta);
                }
            });
            return row;
        });
    }


    private void cargarClientesEnMapa() {
        try {
            nombresClientes.clear();
            for (Cliente c : clienteDao.buscarTodos()) {
                nombresClientes.put(c.getId(), c.getNombre());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // No mostramos alerta aquí para no molestar al usuario al abrir;
            // si falla, simplemente aparecerá vacío el nombre.
        }
    }


    private void abrirDetalleVenta(Venta venta) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ui/venta-detalle-view.fxml"));
            Parent root = loader.load();

            DetalleVentaController controller = loader.getController();
            controller.setVenta(venta);

            Stage stage = new Stage();
            stage.setTitle("Detalle venta #" + venta.getId());
            stage.setScene(new Scene(root, 700, 400));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir el detalle de la venta:\n" + e.getMessage());
        }
    }

    private void cargarVentas() {
        try {
            // Guardamos la lista completa
            listaCompleta = ventaDao.buscarTodas();
            // Y aplicamos el filtro actual (si hay fechas en los DatePicker)
            aplicarFiltroFechas();

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudieron cargar las ventas:\n" + e.getMessage());
        }
    }

    /** Aplica el filtro por fecha (dpDesde / dpHasta) sobre listaCompleta y actualiza tabla + totales */
    private void aplicarFiltroFechas() {
        LocalDate desde = dpDesde != null ? dpDesde.getValue() : null;
        LocalDate hasta = dpHasta != null ? dpHasta.getValue() : null;

        List<Venta> filtradas = new ArrayList<>();

        for (Venta v : listaCompleta) {
            LocalDate fechaVenta = v.getFecha().toLocalDate();

            boolean pasa = true;

            if (desde != null && fechaVenta.isBefore(desde)) {
                pasa = false;
            }
            if (hasta != null && fechaVenta.isAfter(hasta)) {
                pasa = false;
            }

            if (pasa) {
                filtradas.add(v);
            }
        }

        // Actualizar tabla
        ObservableList<Venta> datos = FXCollections.observableArrayList(filtradas);
        tablaVentas.setItems(datos);

        // Recalcular totales con la lista filtrada
        int numVentas = filtradas.size();
        double totalImporte = 0.0;
        double totalIva = 0.0;

        for (Venta v : filtradas) {
            totalImporte += v.getTotal();
            totalIva += v.getIvaTotal();
        }

        if (lblNumVentas != null) {
            lblNumVentas.setText(String.valueOf(numVentas));
        }
        if (lblTotalVentas != null) {
            lblTotalVentas.setText(String.format("%.2f €", totalImporte));
        }
        if (lblTotalIva != null) {
            lblTotalIva.setText(String.format("%.2f €", totalIva));
        }
    }

    @FXML
    private void onFiltrarClick() {
        aplicarFiltroFechas();
    }

    @FXML
    private void onLimpiarFiltroClick() {
        if (dpDesde != null) dpDesde.setValue(null);
        if (dpHasta != null) dpHasta.setValue(null);
        aplicarFiltroFechas();
    }

    @FXML
    private void onCerrarClick() {
        Stage stage = (Stage) tablaVentas.getScene().getWindow();
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
    private void onHoyClick() {
        LocalDate hoy = LocalDate.now();
        if (dpDesde != null) dpDesde.setValue(hoy);
        if (dpHasta != null) dpHasta.setValue(hoy);
        aplicarFiltroFechas();
    }

    @FXML
    private void onUltimos7DiasClick() {
        LocalDate hoy = LocalDate.now();
        LocalDate hace7 = hoy.minusDays(6); // hoy y los 6 días anteriores = 7 días en total

        if (dpDesde != null) dpDesde.setValue(hace7);
        if (dpHasta != null) dpHasta.setValue(hoy);
        aplicarFiltroFechas();
    }

}
