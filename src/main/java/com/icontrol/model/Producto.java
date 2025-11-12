package com.icontrol.model;

public class Producto {

    private long id;
    private String referencia;
    private String descripcion;
    private double pvp;
    private int stock;
    private int stockMinimo;
    private Long idProveedor;  // puede ser null
    private boolean activo = true;  // 👈 campo para control lógico
    private int vendidos;  // 👈 unidades vendidas acumuladas

    public Producto() { }

    public Producto(long id, String referencia, String descripcion,
                    double pvp, int stock, int stockMinimo,
                    Long idProveedor, boolean activo) {
        this.id = id;
        this.referencia = referencia;
        this.descripcion = descripcion;
        this.pvp = pvp;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.idProveedor = idProveedor;
        this.activo = activo;
    }

    // Constructor sin id, útil para nuevas inserciones
    public Producto(String referencia, String descripcion,
                    double pvp, int stock, int stockMinimo, Long idProveedor) {
        this(0, referencia, descripcion, pvp, stock, stockMinimo, idProveedor, true);
    }

    // Getters y setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getPvp() { return pvp; }
    public void setPvp(double pvp) { this.pvp = pvp; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }

    public Long getIdProveedor() { return idProveedor; }
    public void setIdProveedor(Long idProveedor) { this.idProveedor = idProveedor; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public int getVendidos() { return vendidos; }
    public void setVendidos(int vendidos) { this.vendidos = vendidos; }

    @Override
    public String toString() {
        return "Producto{" +
                "id=" + id +
                ", referencia='" + referencia + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", pvp=" + pvp +
                ", stock=" + stock +
                ", stockMinimo=" + stockMinimo +
                ", idProveedor=" + idProveedor +
                ", activo=" + activo +
                ", vendidos=" + vendidos +
                '}';
    }
}
