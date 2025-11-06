package com.icontrol.dao;

import com.icontrol.db.ConnectionFactory;
import com.icontrol.model.Proveedor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDaoSqlite implements ProveedorDao {

    @Override
    public Proveedor insertar(Proveedor p) throws SQLException {
        String sql = """
            INSERT INTO proveedor (nombre, nif, telefono, email)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getNombre());
            ps.setString(2, p.getNif());
            ps.setString(3, p.getTelefono());
            ps.setString(4, p.getEmail());

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
    public void actualizar(Proveedor p) throws SQLException {
        String sql = """
            UPDATE proveedor
            SET nombre = ?, nif = ?, telefono = ?, email = ?
            WHERE id = ?
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setString(2, p.getNif());
            ps.setString(3, p.getTelefono());
            ps.setString(4, p.getEmail());
            ps.setLong(5, p.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(long id) throws SQLException {
        String sql = "DELETE FROM proveedor WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Proveedor> buscarTodos() throws SQLException {
        String sql = "SELECT * FROM proveedor ORDER BY nombre";
        List<Proveedor> lista = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapRow(rs));
            }
        }

        return lista;
    }

    @Override
    public List<Proveedor> buscarPorNombre(String filtro) throws SQLException {
        String sql = "SELECT * FROM proveedor WHERE nombre LIKE ? ORDER BY nombre";
        List<Proveedor> lista = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + filtro + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRow(rs));
                }
            }
        }

        return lista;
    }

    private Proveedor mapRow(ResultSet rs) throws SQLException {
        Proveedor p = new Proveedor();
        p.setId(rs.getLong("id"));
        p.setNombre(rs.getString("nombre"));
        p.setNif(rs.getString("nif"));
        p.setTelefono(rs.getString("telefono"));
        p.setEmail(rs.getString("email"));
        return p;
    }
}
