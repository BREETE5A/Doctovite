package model;

import java.sql.Connection;
import java.sql.DriverManager;

public class Connexion {

    private static Connection con = null;

    private static final String URL  = "jdbc:mysql://localhost:3306/doctovite"
                                     + "?useSSL=false&serverTimezone=UTC"
                                     + "&characterEncoding=UTF-8"
                                     + "&autoReconnect=true"
                                     + "&connectTimeout=5000"
                                     + "&socketTimeout=30000";
    private static final String USER = "root";
    private static final String PASS = "";

    public static Connection getConnexion() {
        try {
            // Si la connexion est null, fermee ou invalide, on en cree une nouvelle
            if (con == null || con.isClosed() || !con.isValid(2)) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                con = DriverManager.getConnection(URL, USER, PASS);
            }
        } catch (Exception e) {
            System.out.println("Erreur connexion BD : " + e.getMessage());
            con = null;
        }
        return con;
    }
}
