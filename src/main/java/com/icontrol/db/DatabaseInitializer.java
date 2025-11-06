package com.icontrol.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        try (Connection conn = ConnectionFactory.getConnection();
             Statement st = conn.createStatement()) {

            // Usuario
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS usuario (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL
                );
            """);

            // Cliente
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS cliente (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT NOT NULL,
                    nif TEXT,
                    telefono TEXT,
                    email TEXT
                );
            """);

            // Proveedor
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS proveedor (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT NOT NULL,
                    nif TEXT,
                    telefono TEXT,
                    email TEXT
                );
            """);

            // Producto
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS producto (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    referencia   TEXT NOT NULL UNIQUE,
                    descripcion  TEXT NOT NULL,
                    pvp          REAL NOT NULL,
                    stock        INTEGER NOT NULL DEFAULT 0,
                    stock_minimo INTEGER NOT NULL DEFAULT 0,
                    id_proveedor INTEGER,
                    FOREIGN KEY (id_proveedor) REFERENCES proveedor(id)
                );
            """);

            // Venta
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS venta (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    fecha      TEXT NOT NULL, -- ISO 8601
                    id_cliente INTEGER,
                    total      REAL NOT NULL,
                    iva_total  REAL NOT NULL,
                    FOREIGN KEY (id_cliente) REFERENCES cliente(id)
                );
            """);

            // Línea de venta
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS linea_venta (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_venta      INTEGER NOT NULL,
                    id_producto   INTEGER NOT NULL,
                    cantidad      INTEGER NOT NULL,
                    precio_unitario REAL NOT NULL,
                    iva           REAL NOT NULL,
                    FOREIGN KEY (id_venta) REFERENCES venta(id) ON DELETE CASCADE,
                    FOREIGN KEY (id_producto) REFERENCES producto(id)
                );
            """);

            // Índices útiles
            st.executeUpdate("""
                CREATE INDEX IF NOT EXISTS idx_producto_ref
                ON producto(referencia);
            """);

            st.executeUpdate("""
                CREATE INDEX IF NOT EXISTS idx_producto_desc
                ON producto(descripcion);
            """);

            System.out.println("✅ Base de datos inicializada correctamente.");

        } catch (SQLException e) {
            System.err.println("❌ Error al inicializar la base de datos: " + e.getMessage());
            throw new RuntimeException("Error inicializando la BD", e);
        }
    }

    // Pequeña prueba, por si quieres ejecutarla sola
    public static void main(String[] args) {
        initialize();
    }
}