package com.icontrol.dao;

import com.icontrol.model.Producto;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ProductoDao {

    Producto insertar(Producto p) throws SQLException;

    List<Producto> buscarTodos() throws SQLException;

    Optional<Producto> buscarPorReferencia(String referencia) throws SQLException;

    void actualizar(Producto p) throws SQLException;

    void eliminar(long id) throws SQLException;

    int contarPorProveedor(long idProveedor) throws SQLException;

    Optional<Producto> buscarPorId(long id) throws SQLException;

    // 🔹 NUEVOS métodos para manejar el campo "activo"
    void desactivar(long id) throws SQLException;

    void reactivar(long id) throws SQLException;

    List<Producto> buscarTodos(boolean incluirInactivos) throws SQLException;
}
