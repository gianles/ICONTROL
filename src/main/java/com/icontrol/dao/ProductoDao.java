package com.icontrol.dao;

import com.icontrol.model.Producto;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ProductoDao {

    Producto insertar(Producto p) throws SQLException;

    List<Producto> buscarTodos() throws SQLException;

    Optional<Producto> buscarPorReferencia(String referencia) throws SQLException;
}
