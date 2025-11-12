package com.icontrol.model;

import com.icontrol.security.Rol;

public class Usuario {
    private Long id;
    private String username;
    private String passwordHash;
    private Rol rol;  // 👈 enum en vez de String
    private String nombre;
    private String apellido;

    public Usuario() {}

    public Usuario(Long id, String username, String passwordHash,
                   Rol rol, String nombre, String apellido) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.nombre = nombre;
        this.apellido = apellido;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    @Override
    public String toString() {
        return username + " (" + rol + ")";
    }
}
