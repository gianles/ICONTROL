package com.icontrol.dao;

import com.icontrol.model.Usuario;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface UsuarioDao {
    Usuario insertar(Usuario u) throws SQLException;
    Optional<Usuario> buscarPorUsername(String username) throws SQLException;
    long contarUsuarios() throws SQLException;

    // NUEVOS:
    List<Usuario> listarTodos() throws SQLException;
    void actualizarPassword(long idUsuario, String nuevoHash) throws SQLException;
    // actualizar nombre, apellido y rol (usuario/username no se toca aquí)
    void actualizarDatos(Usuario u) throws SQLException;

    default Optional<Usuario> validarLogin(String username, String rawPassword) throws SQLException {
        var op = buscarPorUsername(username);
        if (op.isPresent()) {
            var u = op.get();
            if (com.icontrol.security.PasswordUtils.matches(rawPassword, u.getPasswordHash())) {
                return op;
            }
        }
        return Optional.empty();
    }
}
