package com.icontrol.model;

public class Proveedor {

    private long id;
    private String nombre;
    private String nif;
    private String telefono;
    private String email;

    public Proveedor() {
    }

    public Proveedor(long id, String nombre, String nif, String telefono, String email) {
        this.id = id;
        this.nombre = nombre;
        this.nif = nif;
        this.telefono = telefono;
        this.email = email;
    }

    // Constructor sin id (para nuevos)
    public Proveedor(String nombre, String nif, String telefono, String email) {
        this(0, nombre, nif, telefono, email);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Proveedor{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", nif='" + nif + '\'' +
                ", telefono='" + telefono + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
