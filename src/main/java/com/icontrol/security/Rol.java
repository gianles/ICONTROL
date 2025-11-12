package com.icontrol.security;

public enum Rol {
    ADMIN("Administrador"),
    VENTAS("Ventas"),
    ENCARGADO("Encargado");

    private final String label;

    Rol(String label) { this.label = label; }

    public String label() { return label; }

    @Override public String toString() { return label; }

    /** Convierte el texto de BD a enum de forma tolerante (nombre o etiqueta, case-insensitive). */
    public static Rol fromDb(String v) {
        if (v == null || v.isBlank()) return VENTAS;
        // 1) Intento por nombre de enum (ADMIN, VENTAS, ENCARGADO…)
        try {
            return Rol.valueOf(v.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) { }

        // 2) Intento por etiqueta legible ("Administrador", "Ventas", "Encargado")
        for (Rol r : values()) {
            if (r.label.equalsIgnoreCase(v.trim())) return r;
        }

        // 3) Por defecto
        return VENTAS;
    }
}
