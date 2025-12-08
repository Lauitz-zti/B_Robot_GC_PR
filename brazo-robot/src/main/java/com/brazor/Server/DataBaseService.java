package com.brazor.Server;

import java.sql.*;

public class DataBaseService {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/Brazo_Robot";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "12345"; //Contraseña

    public void init() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            System.out.println("Conexion exitosa");
        } catch (SQLException e) {
            System.err.println("!!!ERROR: No se pudo conectar a la base de datos");
        }
    }

    public void SaveEstados(float base, float hombro, float codo, float m1, float m2, float pinza) {
        new Thread(() -> {
            String sql = "INSERT INTO robot_state (base, hombro, codo, muneca1, muneca2, pinza) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setFloat(1, base);
                pstmt.setFloat(2, hombro);
                pstmt.setFloat(3, codo);
                pstmt.setFloat(4, m1);
                pstmt.setFloat(5, m2);
                pstmt.setFloat(6, pinza);
                //aun en ver si ponemoss ip del controlador
                pstmt.executeUpdate();
                
            } catch (SQLException e) {
                System.err.println("Error guardando en BD: " + e.getMessage());
            }
        }).start();
    }
}