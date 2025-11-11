package com.icontrol.model;

public class LineaVenta {

    private long id;
    private long idVenta;
    private long idProducto;
    private int cantidad;
    private double precioUnitario;
    private double iva;       // IVA aplicado a esta línea
    private double subtotal;  // cantidad * precioUnitario (sin IVA o con, según definición que elijas)

    public LineaVenta() {
    }

    public LineaVenta(long id, long idVenta, long idProducto,
                      int cantidad, double precioUnitario,
                      double iva, double subtotal) {
        this.id = id;
        this.idVenta = idVenta;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.iva = iva;
        this.subtotal = subtotal;
    }

    // Constructor cómodo para crear líneas en memoria
    public LineaVenta(long idProducto, int cantidad, double precioUnitario, double iva) {
        this(0, 0, idProducto, cantidad, precioUnitario, iva,
                cantidad * precioUnitario);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(long idVenta) {
        this.idVenta = idVenta;
    }

    public long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(long idProducto) {
        this.idProducto = idProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        this.subtotal = this.cantidad * this.precioUnitario;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
        this.subtotal = this.cantidad * this.precioUnitario;
    }

    public double getIva() {
        return iva;
    }

    public void setIva(double iva) {
        this.iva = iva;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    @Override
    public String toString() {
        return "LineaVenta{" +
                "id=" + id +
                ", idVenta=" + idVenta +
                ", idProducto=" + idProducto +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                ", iva=" + iva +
                ", subtotal=" + subtotal +
                '}';
    }
}
