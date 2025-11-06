package com.icontrol.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionFactory {

    // Ruta al archivo de BD
    private static final String URL = "jdbc:sqlite:src/main/resources/database/icontrol.db";

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
    }

    // Prueba rápida opcional
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("✅ Conexión a SQLite establecida correctamente.");
        } catch (SQLException e) {
            System.err.println("❌ Error al conectar con la base de datos: " + e.getMessage());
        }
    }
}