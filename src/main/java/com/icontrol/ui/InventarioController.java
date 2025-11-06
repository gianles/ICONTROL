package com.icontrol.ui;

import com.icontrol.dao.ProductoDao;
import com.icontrol.dao.ProductoDaoSqlite;
import com.icontrol.model.Producto;
import com.icontrol.ui.NuevoProductoController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;



import java.sql.SQLException;
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

    private final ProductoDao productoDao = new ProductoDaoSqlite();

    @FXML
    private void initialize() {
        // Vincular columnas con propiedades de Producto (usa getters)
        colReferencia.setCellValueFactory(new PropertyValueFactory<>("referencia"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colPvp.setCellValueFactory(new PropertyValueFactory<>("pvp"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colStockMinimo.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));

        // Cargar datos al iniciar
        cargarProductos();
    }

    @FXML
    private void onActualizarClick() {
        cargarProductos();
    }

    @FXML
    private void onNuevoClick() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/ui/nuevo-producto-view.fxml"));

            javafx.scene.Parent root = loader.load();

            NuevoProductoController controller = loader.getController();
            // Cuando se cree un producto, recargamos la tabla
            controller.setOnProductoCreado(this::cargarProductos);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Nuevo producto");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarProductos() {
        try {
            List<Producto> lista = productoDao.buscarTodos();
            ObservableList<Producto> datos = FXCollections.observableArrayList(lista);
            tablaProductos.setItems(datos);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
