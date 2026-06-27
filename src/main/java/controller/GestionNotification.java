package controller;

import model.Connexion;
import model.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * GestionNotification — Gere les alertes entre utilisateurs.
 * Seule la methode envoyer() est utilisee par l'application.
 */
public class GestionNotification {

    private Connection obtenirConnexion() {
        return Connexion.getConnexion();
    }

    // ── ENVOYER UNE NOTIFICATION ──────────────────────────────────────────────
    public boolean envoyer(Notification n) {
        String sql = "INSERT INTO notifications (destinataire_id, expediteur_id, contenu, type_notif) " +
                     "VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement stmt = obtenirConnexion().prepareStatement(sql);
            stmt.setInt(1, n.getDestinataireId());
            stmt.setInt(2, n.getExpediteurId());
            stmt.setString(3, n.getContenu());
            stmt.setString(4, n.getType());
            int n2 = stmt.executeUpdate();
            stmt.close();
            return n2 > 0;
        } catch (SQLException e) {
            System.out.println("Erreur envoi notification : " + e.getMessage());
            return false;
        }
    }
}
