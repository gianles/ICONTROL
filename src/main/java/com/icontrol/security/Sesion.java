package com.icontrol.security;

import com.icontrol.model.Usuario;

public final class Sesion {
    private static Usuario usuario;

    private Sesion() {}

    // -------------------
    // Gestión básica
    // -------------------
    public static void setUsuario(Usuario u) { usuario = u; }
    public static Usuario getUsuario()       { return usuario; }
    public static void clear()               { usuario = null; }
    public static boolean isLogged()         { return usuario != null; }

    // -------------------
    // Roles base
    // -------------------
    public static boolean isAdmin() {
        return usuario != null && usuario.getRol() == Rol.ADMIN;
    }

    public static boolean isEncargado() {
        return usuario != null && usuario.getRol() == Rol.ENCARGADO;
    }

    public static boolean isVentas() {
        return usuario != null && usuario.getRol() == Rol.VENTAS; // o Rol.VENTAS si así lo defines
    }

    // -------------------
    // Permisos específicos
    // -------------------

    /** Puede vender productos */
    public static boolean puedeVender() {
        return isLogged() && (isAdmin() || isEncargado() || isVentas());
    }

    /** Puede ver el inventario (todos los roles logueados) */
    public static boolean puedeVerInventario() {
        return isLogged();
    }

    /** Puede crear, editar o desactivar productos */
    public static boolean puedeEditarInventario() {
        return isLogged() && (isAdmin() || isEncargado());
    }

    /** Puede acceder o gestionar proveedores */
    public static boolean puedeVerProveedores() {
        return isLogged() && (isAdmin() || isEncargado());
    }

    /** Puede gestionar usuarios */
    public static boolean puedeGestionarUsuarios() {
        return isLogged() && isAdmin();
    }

    /** Puede acceder al historial de ventas */
    public static boolean puedeVerHistorialVentas() {
        return isLogged() && (isAdmin() || isEncargado());
    }

    /** Puede modificar contraseñas de otros usuarios */
    public static boolean puedeCambiarPasswords() {
        return isLogged() && isAdmin();
    }
}
