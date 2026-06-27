package controller;

import model.Connexion;
import model.Ordonnance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * GestionOrdonnance — Gere la table "prescriptions" (ordonnances).
 *
 * IMPORTANT : la table prescriptions doit exister dans la BD.
 * Si elle n'existe pas, executer le fichier creer_prescriptions.sql dans phpMyAdmin.
 */
public class GestionOrdonnance {

    private Connection obtenirConnexion() {
        return Connexion.getConnexion();
    }

    // Requete de base : recupere les ordonnances avec noms patient et medecin
    private static final String BASE_SQL =
        "SELECT p.identifiant AS id_ord, " +
        "       p.patient_id, " +
        "       p.medecin_id, " +
        "       p.medicaments, " +
        "       p.posologie, " +
        "       p.duree_traitement, " +
        "       p.observations, " +
        "       p.date_emission, " +
        "       pa.nom_famille AS nom_patient, " +
        "       pa.prenom AS prenom_patient, " +
        "       pa.numero_dossier, " +
        "       u.nom_famille AS nom_med, " +
        "       u.prenom AS prenom_med " +
        "FROM prescriptions p " +
        "JOIN patients pa    ON p.patient_id  = pa.identifiant " +
        "JOIN medecins m     ON p.medecin_id  = m.identifiant " +
        "JOIN utilisateurs u ON m.utilisateur_id = u.identifiant ";

    // Lister toutes les ordonnances (admin / secretaire)
    public List<Ordonnance> listerTous() {
        List<Ordonnance> liste = new ArrayList<>();
        String sql = BASE_SQL + "ORDER BY p.date_emission DESC";
        try {
            ResultSet rs = obtenirConnexion().createStatement().executeQuery(sql);
            while (rs.next()) liste.add(construire(rs));
        } catch (SQLException e) {
            System.out.println("Erreur listerTous ordonnances : " + e.getMessage());
        }
        return liste;
    }

    // Lister les ordonnances d'un medecin specifique
    public List<Ordonnance> listerParMedecin(int idMedecin) {
        List<Ordonnance> liste = new ArrayList<>();
        String sql = BASE_SQL + "WHERE p.medecin_id = ? ORDER BY p.date_emission DESC";
        try {
            PreparedStatement stmt = obtenirConnexion().prepareStatement(sql);
            stmt.setInt(1, idMedecin);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) liste.add(construire(rs));
        } catch (SQLException e) {
            System.out.println("Erreur listerParMedecin ordonnances : " + e.getMessage());
        }
        return liste;
    }

    // Rechercher par nom ou dossier du patient
    public List<Ordonnance> rechercherParNom(String terme) {
        List<Ordonnance> liste = new ArrayList<>();
        String sql = BASE_SQL +
            "WHERE pa.nom_famille LIKE ? OR pa.prenom LIKE ? OR pa.numero_dossier LIKE ? " +
            "ORDER BY p.date_emission DESC";
        try {
            PreparedStatement stmt = obtenirConnexion().prepareStatement(sql);
            String t = "%" + terme + "%";
            stmt.setString(1, t);
            stmt.setString(2, t);
            stmt.setString(3, t);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) liste.add(construire(rs));
        } catch (SQLException e) {
            System.out.println("Erreur recherche ordonnances : " + e.getMessage());
        }
        return liste;
    }

    // Ajouter une ordonnance
    public boolean ajouter(Ordonnance o) {
        // Verification des valeurs avant l'INSERT
        if (o.getPatientId() <= 0 || o.getMedecinId() <= 0) {
            erreurSql = "patient_id ou medecin_id invalide (valeur = 0)";
            return false;
        }
        if (o.getMedicaments() == null || o.getMedicaments().trim().isEmpty()) {
            erreurSql = "medicaments est vide";
            return false;
        }

        String sql = "INSERT INTO prescriptions " +
                     "(patient_id, medecin_id, medicaments, posologie, duree_traitement, observations) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            Connection con = Connexion.getConnexion();
            if (con == null) {
                erreurSql = "Connexion BD null";
                return false;
            }
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, o.getPatientId());
            stmt.setInt(2, o.getMedecinId());
            stmt.setString(3, o.getMedicaments());
            stmt.setString(4, o.getPosologie() != null ? o.getPosologie() : "");
            stmt.setString(5, o.getDuree() != null ? o.getDuree() : "");
            stmt.setString(6, o.getObservations() != null ? o.getObservations() : "");
            int n = stmt.executeUpdate();
            stmt.close();
            erreurSql = "";
            return n > 0;
        } catch (SQLException e) {
            erreurSql = e.getMessage();
            System.out.println("Erreur INSERT ordonnance : " + e.getMessage());
            return false;
        }
    }

    // Derniere erreur SQL
    private String erreurSql = "";
    public String getDerniereErreur() { return erreurSql; }

    // Construire un objet Ordonnance depuis un ResultSet
    private Ordonnance construire(ResultSet rs) throws SQLException {
        Ordonnance o = new Ordonnance();
        o.setId(rs.getInt("id_ord"));
        o.setPatientId(rs.getInt("patient_id"));
        o.setMedecinId(rs.getInt("medecin_id"));
        o.setMedicaments(rs.getString("medicaments"));
        o.setPosologie(rs.getString("posologie"));
        o.setDuree(rs.getString("duree_traitement"));
        o.setObservations(rs.getString("observations"));
        Timestamp ts = rs.getTimestamp("date_emission");
        o.setDateEmission(ts != null ? ts.toString().substring(0, 10) : "");
        o.setNomPatient(rs.getString("prenom_patient") + " " + rs.getString("nom_patient"));
        o.setNumeroDossier(rs.getString("numero_dossier"));
        o.setNomMedecin("Dr. " + rs.getString("prenom_med") + " " + rs.getString("nom_med"));
        return o;
    }
}
