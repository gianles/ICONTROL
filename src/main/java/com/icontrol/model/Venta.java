package com.icontrol.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Venta {

    private long id;
    private LocalDateTime fecha;
    private Long idCliente;   // puede ser null
    private double total;
    private double ivaTotal;
    private int numItems;   // total de unidades vendidas en esta venta

    // Lineas asociadas a la venta (no son columnas de la tabla, es para trabajar en memoria)
    private List<LineaVenta> lineas = new ArrayList<>();

    public Venta() {
    }

    public Venta(long id, LocalDateTime fecha, Long idCliente, double total, double ivaTotal) {
        this.id = id;
        this.fecha = fecha;
        this.idCliente = idCliente;
        this.total = total;
        this.ivaTotal = ivaTotal;
    }

    public int getNumItems() {
        return numItems;
    }

    public void setNumItems(int numItems) {
        this.numItems = numItems;
    }


    public Venta(LocalDateTime fecha, Long idCliente, double total, double ivaTotal) {
        this(0, fecha, idCliente, total, ivaTotal);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getIvaTotal() {
        return ivaTotal;
    }

    public void setIvaTotal(double ivaTotal) {
        this.ivaTotal = ivaTotal;
    }

    public List<LineaVenta> getLineas() {
        return lineas;
    }

    public void setLineas(List<LineaVenta> lineas) {
        this.lineas = lineas;
    }

    public void addLinea(LineaVenta linea) {
        this.lineas.add(linea);
    }

    @Override
    public String toString() {
        return "Venta{" +
                "id=" + id +
                ", fecha=" + fecha +
                ", idCliente=" + idCliente +
                ", total=" + total +
                ", ivaTotal=" + ivaTotal +
                ", lineas=" + lineas.size() +
                '}';
    }
}
