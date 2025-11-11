package com.icontrol.ui;

import com.icontrol.dao.ProductoDao;
import com.icontrol.dao.ProductoDaoSqlite;
import com.icontrol.model.Producto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.SQLException;

public class SelectorProductoController {

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
    private TextField txtFiltro;

    private final ProductoDao productoDao = new ProductoDaoSqlite();

    private final ObservableList<Producto> datos = FXCollections.observableArrayList();
    private FilteredList<Producto> filtrados;

    private Producto seleccionado;

    public Producto getSeleccionado() {
        return seleccionado;
    }

    @FXML
    private void initialize() {
        colReferencia.setCellValueFactory(new PropertyValueFactory<>("referencia"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colPvp.setCellValueFactory(new PropertyValueFactory<>("pvp"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        try {
            datos.setAll(productoDao.buscarTodos());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        filtrados = new FilteredList<>(datos, p -> true);
        tablaProductos.setItems(filtrados);

        // Filtro por referencia o descripción
        txtFiltro.textProperty().addListener((obs, old, neu) -> {
            String filtro = neu == null ? "" : neu.toLowerCase();
            filtrados.setPredicate(p -> {
                if (filtro.isEmpty()) return true;
                return p.getReferencia().toLowerCase().contains(filtro)
                        || p.getDescripcion().toLowerCase().contains(filtro);
            });
        });

        // Doble clic = aceptar
        tablaProductos.setRowFactory(tv -> {
            TableRow<Producto> row = new TableRow<>();
            row.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2 && !row.isEmpty()) {
                    seleccionado = row.getItem();
                    cerrar();
                }
            });
            return row;
        });
    }

    @FXML
    private void onAceptarClick() {
        seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        cerrar();
    }

    @FXML
    private void onCancelarClick() {
        seleccionado = null;
        cerrar();
    }

    private void cerrar() {
        Stage stage = (Stage) tablaProductos.getScene().getWindow();
        stage.close();
    }
}
