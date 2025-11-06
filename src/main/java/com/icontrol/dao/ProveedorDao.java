package com.icontrol.dao;

import com.icontrol.model.Proveedor;

import java.sql.SQLException;
import java.util.List;

public interface ProveedorDao {

    Proveedor insertar(Proveedor p) throws SQLException;

    void actualizar(Proveedor p) throws SQLException;

    void eliminar(long id) throws SQLException;

    List<Proveedor> buscarTodos() throws SQLException;

    List<Proveedor> buscarPorNombre(String filtro) throws SQLException;
}
