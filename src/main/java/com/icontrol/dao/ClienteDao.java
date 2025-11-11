package com.icontrol.dao;

import com.icontrol.model.Cliente;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ClienteDao {

    Cliente insertar(Cliente c) throws SQLException;

    void actualizar(Cliente c) throws SQLException;

    void eliminar(long id) throws SQLException;

    List<Cliente> buscarTodos() throws SQLException;

    List<Cliente> buscarPorNombre(String filtro) throws SQLException;

    Optional<Cliente> buscarPorId(long id) throws SQLException;
}
