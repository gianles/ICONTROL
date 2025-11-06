package com.icontrol.ui;

import com.icontrol.dao.ProductoDao;
import com.icontrol.dao.ProductoDaoSqlite;
import com.icontrol.model.Producto;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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

    @FXML
    private TextField txtIdProveedor;

    @FXML
    private Button btnGuardar;

    private final ProductoDao productoDao = new ProductoDaoSqlite();

    /** Callback opcional para avisar al inventario de que hay cambios */
    private Runnable onProductoCreado;

    public void setOnProductoCreado(Runnable onProductoCreado) {
        this.onProductoCreado = onProductoCreado;
    }

    @FXML
    private void onGuardarClick() {
        try {
            String referencia = txtReferencia.getText().trim();
            String descripcion = txtDescripcion.getText().trim();
            String pvpStr = txtPvp.getText().trim();
            String stockStr = txtStock.getText().trim();
            String stockMinStr = txtStockMinimo.getText().trim();
            String idProvStr = txtIdProveedor.getText().trim();

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

            Long idProveedor = null;
            if (!idProvStr.isEmpty()) {
                try {
                    idProveedor = Long.parseLong(idProvStr);
                } catch (NumberFormatException e) {
                    mostrarAlerta("Formato incorrecto",
                            "El ID de proveedor debe ser un número entero.");
                    return;
                }
            }

            Producto nuevo = new Producto(referencia, descripcion, pvp, stock, stockMinimo, idProveedor);
            productoDao.insertar(nuevo);

            // Avisar al controlador de inventario (si se configuró)
            if (onProductoCreado != null) {
                onProductoCreado.run();
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
}
