package com.icontrol.dao;

import com.icontrol.db.ConnectionFactory;
import com.icontrol.model.LineaVenta;
import com.icontrol.model.Venta;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class VentaDaoSqlite implements VentaDao {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Venta insertarConLineas(Venta venta) throws SQLException {
        String sqlVenta = "INSERT INTO venta (fecha, id_cliente, total, iva_total) VALUES (?, ?, ?, ?)";
        String sqlLinea = """
            INSERT INTO linea_venta (id_venta, id_producto, cantidad, precio_unitario, iva)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false); // empezamos transacción

            try (PreparedStatement psVenta = conn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psLinea = conn.prepareStatement(sqlLinea)) {

                // Insertar cabecera de venta
                String fechaStr = venta.getFecha().format(FMT);
                psVenta.setString(1, fechaStr);

                if (venta.getIdCliente() == null) {
                    psVenta.setNull(2, Types.INTEGER);
                } else {
                    psVenta.setLong(2, venta.getIdCliente());
                }

                psVenta.setDouble(3, venta.getTotal());
                psVenta.setDouble(4, venta.getIvaTotal());
                psVenta.executeUpdate();

                long idVenta;
                try (ResultSet rs = psVenta.getGeneratedKeys()) {
                    if (rs.next()) {
                        idVenta = rs.getLong(1);
                        venta.setId(idVenta);
                    } else {
                        throw new SQLException("No se pudo obtener el ID de la venta insertada.");
                    }
                }

                // Insertar líneas
                for (LineaVenta linea : venta.getLineas()) {
                    linea.setIdVenta(idVenta);

                    psLinea.setLong(1, linea.getIdVenta());
                    psLinea.setLong(2, linea.getIdProducto());
                    psLinea.setInt(3, linea.getCantidad());
                    psLinea.setDouble(4, linea.getPrecioUnitario());
                    psLinea.setDouble(5, linea.getIva());
                    psLinea.addBatch();
                }

                psLinea.executeBatch();

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }

        return venta;
    }

    @Override
    public List<Venta> buscarTodas() throws SQLException {
        String sql = """
        SELECT v.*,
               IFNULL((
                   SELECT SUM(lv.cantidad)
                   FROM linea_venta lv
                   WHERE lv.id_venta = v.id
               ), 0) AS num_items
        FROM venta v
        ORDER BY v.fecha DESC
        """;

        List<Venta> lista = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Venta v = new Venta();
                v.setId(rs.getLong("id"));

                String fechaStr = rs.getString("fecha");
                v.setFecha(LocalDateTime.parse(fechaStr, FMT));

                long idCli = rs.getLong("id_cliente");
                if (rs.wasNull()) {
                    v.setIdCliente(null);
                } else {
                    v.setIdCliente(idCli);
                }

                v.setTotal(rs.getDouble("total"));
                v.setIvaTotal(rs.getDouble("iva_total"));

                // 👇 nuevo: número de unidades vendidas
                v.setNumItems(rs.getInt("num_items"));

                lista.add(v);
            }
        }

        return lista;
    }


    @Override
    public List<LineaVenta> buscarLineasPorVenta(long idVenta) throws SQLException {
        String sql = "SELECT * FROM linea_venta WHERE id_venta = ?";
        List<LineaVenta> lista = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, idVenta);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LineaVenta lv = new LineaVenta();
                    lv.setId(rs.getLong("id"));
                    lv.setIdVenta(rs.getLong("id_venta"));
                    lv.setIdProducto(rs.getLong("id_producto"));

                    int cantidad = rs.getInt("cantidad");
                    double precioUnitario = rs.getDouble("precio_unitario");

                    // Estos setters ya recalculan el subtotal en la clase
                    lv.setCantidad(cantidad);
                    lv.setPrecioUnitario(precioUnitario);

                    lv.setIva(rs.getDouble("iva"));

                    lista.add(lv);
                }
            }
        }

        return lista;
    }

}
