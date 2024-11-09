package de.friseur.friseur;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DatabaseConnectionTest {

    public static void main(String[] args) {
        try {
            String url = "jdbc:postgresql://your_host:5432/your_database";
            String user = "your_username";
            String password = "your_password";

            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to PostgreSQL database!");
        } catch (SQLException e) {
            System.err.println("Error connecting to PostgreSQL database: " + e.getMessage());
        }
    }
}
