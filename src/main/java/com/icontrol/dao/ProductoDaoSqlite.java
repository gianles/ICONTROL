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

    @Override
    public List<Producto> buscarTodos() throws SQLException {
        List<Producto> lista = new ArrayList<>();

        String sql = "SELECT * FROM producto ORDER BY descripcion";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Producto p = mapRow(rs);
                lista.add(p);
            }
        }

        return lista;
    }

    @Override
    public Optional<Producto> buscarPorReferencia(String referencia) throws SQLException {
        String sql = "SELECT * FROM producto WHERE referencia = ?";

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
        String sql = "SELECT COUNT(*) FROM producto WHERE id_proveedor = ?";
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
    public void eliminar(long id) throws SQLException {
        String sql = "DELETE FROM producto WHERE id = ?";

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
            ProductoDao dao = new ProductoDaoSqlite();

            // Inserta un producto de prueba
            Producto nuevo = new Producto("FILT-001", "Filtro de aceite", 15.99, 10, 2, null);
            dao.insertar(nuevo);
            System.out.println("✅ Producto insertado: " + nuevo);

            // Lista todos los productos
            System.out.println("📦 Listado de productos:");
            for (Producto p : dao.buscarTodos()) {
                System.out.println(" - " + p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}