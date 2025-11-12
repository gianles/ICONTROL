package com.icontrol.dao;

import com.icontrol.db.ConnectionFactory;
import com.icontrol.model.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductoDaoSqlite implements ProductoDao {

    @Override
    public Producto insertar(Producto p) throws SQLException {
        // No hace falta especificar 'activo' porque tiene DEFAULT 1 en la tabla
        String sql = """
            INSERT INTO producto (referencia, descripcion, pvp, stock, stock_minimo, id_proveedor)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getReferencia());
            ps.setString(2, p.getDescripcion());
            ps.setDouble(3, p.getPvp());
            ps.setInt(4, p.getStock());
            ps.setInt(5, p.getStockMinimo());

            if (p.getIdProveedor() != null) {
                ps.setLong(6, p.getIdProveedor());
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getLong(1));
                }
            }
        }

        return p;
    }

    /** Lista sólo productos activos */
    @Override
    public List<Producto> buscarTodos() throws SQLException {
        return buscarTodos(false);
    }

    /**
     * Si incluirInactivos = true, devuelve todos; si no, sólo activos.
     * Además, trae el agregado de unidades vendidas por producto (alias 'vendidos').
     */
    public List<Producto> buscarTodos(boolean incluirInactivos) throws SQLException {
        List<Producto> lista = new ArrayList<>();

        String filtroActivo = incluirInactivos ? "" : "WHERE p.activo = 1";

        String sql = """
            SELECT
                p.*,
                IFNULL((
                    SELECT SUM(lv.cantidad)
                    FROM linea_venta lv
                    WHERE lv.id_producto = p.id
                ), 0) AS vendidos
            FROM producto p
            %s
            ORDER BY p.descripcion
            """.formatted(filtroActivo);

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Producto p = mapRow(rs);
                // 👇 nuevo: setear las unidades vendidas
                p.setVendidos(rs.getInt("vendidos"));
                lista.add(p);
            }
        }

        return lista;
    }

    @Override
    public Optional<Producto> buscarPorReferencia(String referencia) throws SQLException {
        String sql = "SELECT * FROM producto WHERE referencia = ? AND activo = 1";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, referencia);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                } else {
                    return Optional.empty();
                }
            }
        }
    }

    private Producto mapRow(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setId(rs.getLong("id"));
        p.setReferencia(rs.getString("referencia"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setPvp(rs.getDouble("pvp"));
        p.setStock(rs.getInt("stock"));
        p.setStockMinimo(rs.getInt("stock_minimo"));

        long idProv = rs.getLong("id_proveedor");
        if (rs.wasNull()) {
            p.setIdProveedor(null);
        } else {
            p.setIdProveedor(idProv);
        }

        // activo (compatibilidad por si no existe la columna en BDs antiguas)
        int activo = 1;
        try { activo = rs.getInt("activo"); } catch (SQLException ignore) { }
        p.setActivo(activo == 1);

        // OJO: 'vendidos' lo seteamos en buscarTodos(...) tras mapRow(rs)
        return p;
    }

    @Override
    public void actualizar(Producto p) throws SQLException {
        String sql = """
        UPDATE producto
        SET referencia = ?, descripcion = ?, pvp = ?, stock = ?, stock_minimo = ?, id_proveedor = ?
        WHERE id = ?
        """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getReferencia());
            ps.setString(2, p.getDescripcion());
            ps.setDouble(3, p.getPvp());
            ps.setInt(4, p.getStock());
            ps.setInt(5, p.getStockMinimo());

            if (p.getIdProveedor() != null) {
                ps.setLong(6, p.getIdProveedor());
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            ps.setLong(7, p.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public int contarPorProveedor(long idProveedor) throws SQLException {
        String sql = "SELECT COUNT(*) FROM producto WHERE id_proveedor = ? AND activo = 1";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, idProveedor);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    @Override
    public Optional<Producto> buscarPorId(long id) throws SQLException {
        String sql = "SELECT * FROM producto WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                } else {
                    return Optional.empty();
                }
            }
        }
    }

    /**
     * Antes borraba físicamente. Ahora hace baja lógica para evitar romper ventas.
     */
    @Override
    public void eliminar(long id) throws SQLException {
        desactivar(id);
    }

    /** Marca el producto como inactivo (baja lógica) */
    public void desactivar(long id) throws SQLException {
        String sql = "UPDATE producto SET activo = 0 WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    /** Restaura un producto previamente inactivo */
    public void reactivar(long id) throws SQLException {
        String sql = "UPDATE producto SET activo = 1 WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    // 🔹 Prueba rápida de funcionamiento del DAO
    public static void main(String[] args) {
        try {
            // Inicializa la base de datos (crea tablas si no existen)
            com.icontrol.db.DatabaseInitializer.initialize();

            // Crea una instancia del DAO
            ProductoDaoSqlite dao = new ProductoDaoSqlite();

            // Inserta un producto de prueba
            Producto nuevo = new Producto("FILT-001", "Filtro de aceite", 15.99, 10, 2, null);
            dao.insertar(nuevo);
            System.out.println("✅ Producto insertado: " + nuevo);

            // Lista activos
            System.out.println("📦 Productos activos:");
            for (Producto p : dao.buscarTodos()) {
                System.out.println(" - " + p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
