package com.icontrol.dao;

import com.icontrol.model.LineaVenta;
import com.icontrol.model.Venta;

import java.sql.SQLException;
import java.util.List;

public interface VentaDao {

    /** Inserta una venta con todas sus líneas en una única transacción. */
    Venta insertarConLineas(Venta venta) throws SQLException;

    List<Venta> buscarTodas() throws SQLException;


    // Más adelante puedes añadir: buscarPorId, buscarPorFecha, etc.

    List<LineaVenta> buscarLineasPorVenta(long idVenta) throws SQLException;
}
