package com.icontrol.dao;

import com.icontrol.db.ConnectionFactory;
import com.icontrol.model.Usuario;
import com.icontrol.security.Rol;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioDaoSqlite implements UsuarioDao {

    @Override
    public Usuario insertar(Usuario u) throws SQLException {
        String sql = """
            INSERT INTO usuario (username, password_hash, rol, nombre, apellido)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPasswordHash());
            ps.setString(3, u.getRol().name()); // 👈 Guardar enum como texto
            ps.setString(4, u.getNombre());
            ps.setString(5, u.getApellido());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) u.setId(rs.getLong(1));
            }
        }
        return u;
    }

    @Override
    public Optional<Usuario> buscarPorUsername(String username) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE username = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public long contarUsuarios() throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuario";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        }
    }

    @Override
    public List<Usuario> listarTodos() throws SQLException {
        String sql = "SELECT * FROM usuario ORDER BY username ASC";
        List<Usuario> lista = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(map(rs));
        }
        return lista;
    }

    @Override
    public void actualizarDatos(Usuario u) throws SQLException {
        String sql = "UPDATE usuario SET rol = ?, nombre = ?, apellido = ? WHERE id = ?";
        try (var conn = ConnectionFactory.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setString(1, u.getRol().name());

            if (u.getNombre() == null || u.getNombre().isBlank()) {
                ps.setNull(2, java.sql.Types.VARCHAR);
            } else {
                ps.setString(2, u.getNombre());
            }

            if (u.getApellido() == null || u.getApellido().isBlank()) {
                ps.setNull(3, java.sql.Types.VARCHAR);
            } else {
                ps.setString(3, u.getApellido());
            }

            ps.setLong(4, u.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void actualizarPassword(long idUsuario, String nuevoHash) throws SQLException {
        String sql = "UPDATE usuario SET password_hash = ? WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoHash);
            ps.setLong(2, idUsuario);
            ps.executeUpdate();
        }
    }

    /** (opcional) Actualizar rol */
    public void actualizarRol(long idUsuario, Rol nuevoRol) throws SQLException {
        String sql = "UPDATE usuario SET rol = ? WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoRol.name());
            ps.setLong(2, idUsuario);
            ps.executeUpdate();
        }
    }

    private Usuario map(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        // 👇 convertir texto → enum
        u.setRol(com.icontrol.security.Rol.fromDb(rs.getString("rol")));
        u.setNombre(rs.getString("nombre"));
        u.setApellido(rs.getString("apellido"));
        return u;
    }
}
