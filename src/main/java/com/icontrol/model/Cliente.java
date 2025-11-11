package com.icontrol.model;

public class Cliente {

    private long id;
    private String nombre;
    private String nif;
    private String telefono;
    private String email;

    public Cliente() {}

    public Cliente(long id, String nombre, String nif, String telefono, String email) {
        this.id = id;
        this.nombre = nombre;
        this.nif = nif;
        this.telefono = telefono;
        this.email = email;
    }

    public Cliente(String nombre, String nif, String telefono, String email) {
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
        return nombre != null ? nombre : ("Cliente #" + id);
    }
}
