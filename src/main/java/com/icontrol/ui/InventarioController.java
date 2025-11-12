package com.icontrol.ui;

import com.icontrol.dao.ProductoDao;
import com.icontrol.dao.ProductoDaoSqlite;
import com.icontrol.dao.VentaDao;
import com.icontrol.dao.VentaDaoSqlite;
import com.icontrol.model.Producto;
import com.icontrol.model.Venta;
import com.icontrol.security.Sesion;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.css.PseudoClass;
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

    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, String>  colReferencia;
    @FXML private TableColumn<Producto, String>  colDescripcion;
    @FXML private TableColumn<Producto, Number>  colPvp;
    @FXML private TableColumn<Producto, Number>  colStock;
    @FXML private TableColumn<Producto, Number>  colStockMinimo;
    @FXML private TableColumn<Producto, Number>  colVendidos;

    @FXML private Label statusLabel;
    @FXML private Label lblStatProductos;
    @FXML private Label lblStatStockBajo;
    @FXML private Label lblStatVentasHoy;
    @FXML private Label lblSaludo;

    // Controles del toolbar (para permisos / visibilidad)
    @FXML private Button btnActualizar;
    @FXML private Button btnNuevo;
    @FXML private Button btnEditar;
    @FXML private Button btnDesactivar;
    @FXML private Button btnProveedores;
    @FXML private Button btnNuevoCliente;
    @FXML private Button btnClientes;
    @FXML private Button btnNuevaVenta;
    @FXML private Button btnHistorialVentas;
    @FXML private Button btnUsuarios;
    @FXML private Button btnReactivar;
    @FXML private CheckBox chkMostrarInactivos;

    // Buscador (debe existir en el FXML con fx:id="txtBuscar")
    @FXML private TextField txtBuscar;

    // Datos + filtrado/orden
    private final ObservableList<Producto> masterDatos = FXCollections.observableArrayList();
    private FilteredList<Producto> filtrados;
    private SortedList<Producto> ordenados;

    private final ProductoDao productoDao = new ProductoDaoSqlite();
    private final VentaDao ventaDao = new VentaDaoSqlite();
    private PauseTransition mensajeTimer;

    @FXML
    private void initialize() {
        // Columnas
        colReferencia.setCellValueFactory(new PropertyValueFactory<>("referencia"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colPvp.setCellValueFactory(new PropertyValueFactory<>("pvp"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colStockMinimo.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));
        colVendidos.setCellValueFactory(new PropertyValueFactory<>("vendidos"));

        tablaProductos.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        if (statusLabel != null) {
            statusLabel.setVisible(false);
            statusLabel.managedProperty().bind(statusLabel.visibleProperty());
        }

        // Estilo filas inactivas
        final PseudoClass INACTIVO = PseudoClass.getPseudoClass("inactivo");
        tablaProductos.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Producto item, boolean empty) {
                super.updateItem(item, empty);
                boolean inactivo = !empty && item != null && !item.isActivo()
                        && chkMostrarInactivos != null && chkMostrarInactivos.isSelected();
                pseudoClassStateChanged(INACTIVO, inactivo);
            }
        });

        // Filtrar + ordenar
        filtrados = new FilteredList<>(masterDatos, p -> true);
        ordenados = new SortedList<>(filtrados);
        ordenados.comparatorProperty().bind(tablaProductos.comparatorProperty());
        tablaProductos.setItems(ordenados);

        // Buscador
        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> {
                final String q = newV == null ? "" : newV.trim().toLowerCase();
                filtrados.setPredicate(p -> {
                    if (q.isEmpty()) return true;
                    String ref = p.getReferencia() == null ? "" : p.getReferencia().toLowerCase();
                    String des = p.getDescripcion() == null ? "" : p.getDescripcion().toLowerCase();
                    return ref.contains(q) || des.contains(q);
                });
            });
        }

        // Inactivos + reactivar
        if (chkMostrarInactivos != null) {
            chkMostrarInactivos.setSelected(false);
            chkMostrarInactivos.selectedProperty().addListener((o, a, b) -> tablaProductos.refresh());
        }
        if (btnReactivar != null) btnReactivar.setDisable(true);

        tablaProductos.getSelectionModel().getSelectedItems()
                .addListener((javafx.collections.ListChangeListener<? super Producto>) c -> actualizarBotonReactivar());

        // Permisos por rol
        aplicarPermisos();

        // Cargar datos
        cargarProductos();

        // 👋 Saludo dinámico con nombre y rol
        if (lblSaludo != null && com.icontrol.security.Sesion.isLogged()) {
            var u = com.icontrol.security.Sesion.getUsuario();
            String nombre = (u.getNombre() != null && !u.getNombre().isBlank())
                    ? u.getNombre()
                    : u.getUsername();
            String rol = u.getRol().name();
            lblSaludo.setText(saludoSegunHora() + ", " + nombre + "  (" + rol + ")");
        }


    }

    /** Visibilidad/acciones según rol actual */
    private void aplicarPermisos() {
        boolean puedeEditar     = Sesion.puedeEditarInventario();   // ADMIN o ENCARGADO
        boolean puedeVender     = Sesion.puedeVender();              // ADMIN/ENCARGADO/USER
        boolean puedeProv       = Sesion.puedeVerProveedores();      // ADMIN o ENCARGADO
        boolean puedeUsuarios   = Sesion.puedeGestionarUsuarios();   // solo ADMIN
        boolean puedeHistorial  = Sesion.puedeVerHistorialVentas();  // ADMIN o ENCARGADO

        setVisibleManaged(btnNuevo, puedeEditar);
        setVisibleManaged(btnEditar, puedeEditar);
        setVisibleManaged(btnDesactivar, puedeEditar);
        setVisibleManaged(chkMostrarInactivos, puedeEditar);
        setVisibleManaged(btnReactivar, puedeEditar);

        setVisibleManaged(btnProveedores, puedeProv);
        setVisibleManaged(btnNuevaVenta, puedeVender);
        setVisibleManaged(btnHistorialVentas, puedeHistorial);
        setVisibleManaged(btnUsuarios, puedeUsuarios);
    }

    private void setVisibleManaged(Control c, boolean visible) {
        if (c == null) return;
        c.setVisible(visible);
        c.setManaged(visible);
    }

    private void actualizarBotonReactivar() {
        if (btnReactivar == null || chkMostrarInactivos == null) return;
        boolean enable = chkMostrarInactivos.isSelected()
                && !tablaProductos.getSelectionModel().getSelectedItems().isEmpty();
        btnReactivar.setDisable(!enable);
    }

    private void mostrarMensaje(String texto) {
        if (statusLabel == null) { System.out.println("STATUS: " + texto); return; }
        statusLabel.setText(texto);
        statusLabel.setVisible(true);
        if (mensajeTimer != null) mensajeTimer.stop();
        mensajeTimer = new PauseTransition(Duration.seconds(3));
        mensajeTimer.setOnFinished(e -> {
            statusLabel.setText("");
            statusLabel.setVisible(false);
        });
        mensajeTimer.play();
    }

    @FXML private void onActualizarClick() { cargarProductos(); mostrarMensaje("Inventario actualizado."); }

    @FXML private void onNuevoClick() {
        if (Sesion.puedeEditarInventario()) abrirFormularioProducto(null);
        else sinPermiso();
    }

    @FXML private void onEditarClick() {
        if (!Sesion.puedeEditarInventario()) { sinPermiso(); return; }
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) { mostrarAlerta("Sin selección", "Selecciona un producto para editar."); return; }
        abrirFormularioProducto(seleccionado);
    }

    @FXML
    private void onProveedoresClick() {
        if (!Sesion.puedeVerProveedores()) { sinPermiso(); return; }
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
        if (!Sesion.puedeVender()) { sinPermiso(); return; }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/ventas-view.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("ICONTROL - Nueva venta");
            stage.setScene(new Scene(root, 800, 600));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            cargarProductos();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la ventana de ventas:\n" + e.getMessage());
        }
    }

    @FXML
    private void onHistorialVentasClick() {
        if (!Sesion.puedeVerHistorialVentas()) { sinPermiso(); return; }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/ventas-historial-view.fxml"));
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
            controller.setOnClienteGuardado(() -> mostrarMensaje("Cliente creado correctamente."));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/clientes-view.fxml"));
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
    private void onDesactivarClick() {
        if (!Sesion.puedeEditarInventario()) { sinPermiso(); return; }
        List<Producto> seleccionados = tablaProductos.getSelectionModel().getSelectedItems();
        if (seleccionados == null || seleccionados.isEmpty()) {
            mostrarAlerta("Sin selección", "Selecciona uno o varios productos para desactivar.");
            return;
        }
        int total = seleccionados.size();
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar desactivación");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Seguro que quieres desactivar " + total + " producto(s)?");
        var result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                for (Producto p : List.copyOf(seleccionados)) productoDao.desactivar(p.getId());
                cargarProductos();
                mostrarMensaje(total + " producto(s) desactivado(s).");
            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlerta("Error", "No se pudieron desactivar los productos:\n" + e.getMessage());
            }
        }
    }

    @FXML
    private void onToggleInactivos() {
        if (!Sesion.puedeEditarInventario()) { sinPermiso(); return; }
        cargarProductos();
        if (chkMostrarInactivos != null) {
            mostrarMensaje(chkMostrarInactivos.isSelected()
                    ? "Mostrando productos inactivos."
                    : "Mostrando solo productos activos.");
        }
    }

    @FXML
    private void onReactivarClick() {
        if (!Sesion.puedeEditarInventario()) { sinPermiso(); return; }
        List<Producto> seleccionados = tablaProductos.getSelectionModel().getSelectedItems();
        if (seleccionados == null || seleccionados.isEmpty()) {
            mostrarAlerta("Sin selección", "Selecciona uno o varios productos inactivos para reactivar.");
            return;
        }
        int total = seleccionados.size();
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar reactivación");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Reactivar " + total + " producto(s)?");
        var result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                for (Producto p : List.copyOf(seleccionados)) productoDao.reactivar(p.getId());
                cargarProductos();
                mostrarMensaje(total + " producto(s) reactivado(s).");
            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlerta("Error", "No se pudo reactivar:\n" + e.getMessage());
            }
        }
    }

    private void abrirFormularioProducto(Producto producto) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/nuevo-producto-view.fxml"));
            Parent root = loader.load();
            NuevoProductoController controller = loader.getController();
            controller.setOnProductoGuardado(() -> {
                cargarProductos();
                if (producto == null) mostrarMensaje("Producto creado correctamente.");
                else mostrarMensaje("Producto actualizado correctamente.");
            });
            if (producto != null) controller.setProductoEditar(producto);
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
            boolean incluirInactivos = chkMostrarInactivos != null && chkMostrarInactivos.isSelected();
            List<Producto> lista = productoDao.buscarTodos(incluirInactivos);

            // 🔴 IMPORTANTE: actualizamos la master list para que el filtro/orden sigan funcionando
            masterDatos.setAll(lista);

            // Resumen y botón reactivar
            actualizarResumen(lista);
            actualizarBotonReactivar();

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarMensaje("Error al cargar productos.");
        }
    }

    @FXML
    private void onCerrarSesionClick() {
        try {
            Stage stageActual = (Stage) tablaProductos.getScene().getWindow();
            stageActual.close();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/login-view.fxml"));
            Parent root = loader.load();
            Stage loginStage = new Stage();
            loginStage.setTitle("ICONTROL - Login");
            loginStage.setScene(new Scene(root, 600, 400));
            loginStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cerrar la sesión:\n" + e.getMessage());
        }
    }

    @FXML
    private void onUsuariosClick() {
        if (!Sesion.puedeGestionarUsuarios()) { sinPermiso(); return; }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/usuarios-view.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("ICONTROL - Usuarios");
            stage.setScene(new Scene(root, 700, 500));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la gestión de usuarios:\n" + e.getMessage());
        }
    }

    private void actualizarResumen(List<Producto> productos) {
        int totalProductos = productos.size();
        long stockBajo = productos.stream().filter(p -> p.getStock() <= p.getStockMinimo()).count();

        int ventasHoy = 0;
        try {
            List<Venta> ventas = ventaDao.buscarTodas();
            LocalDate hoy = LocalDate.now();
            ventasHoy = (int) ventas.stream().filter(v -> v.getFecha().toLocalDate().equals(hoy)).count();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (lblStatProductos != null)  lblStatProductos.setText("Productos\n" + totalProductos);
        if (lblStatStockBajo != null)  lblStatStockBajo.setText("Stock bajo\n" + stockBajo);
        if (lblStatVentasHoy != null)  lblStatVentasHoy.setText("Ventas hoy\n" + ventasHoy);
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private String saludoSegunHora() {
        int h = java.time.LocalTime.now().getHour();
        if (h < 12)       return "🌅 Buenos días";
        else if (h < 19)  return "🌇 Buenas tardes";
        else              return "🌙 Buenas noches";
    }


    private void sinPermiso() {
        mostrarAlerta("Sin permiso", "Tu rol no tiene acceso a esta acción.");
    }
}
