package controller;

import model.Connexion;
import model.RendezVous;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * GestionRendezVous — Classe qui gere les operations sur la table "rendez_vous".
 *
 * La requete de base fait trois jointures (JOIN) pour recuperer :
 *   - Les infos du patient (nom, CIN, age...)
 *   - Les infos du medecin (nom, specialite...)
 * Cela evite de faire plusieurs requetes separees.
 */
public class GestionRendezVous {

    private Connection obtenirConnexion() {
        return Connexion.getConnexion();
    }

    // ── REQUETE DE BASE ───────────────────────────────────────────────────────
    /**
     * Cette requete recupere les rendez-vous avec les infos completes
     * des patients et des medecins en une seule requete grace aux JOIN.
     *
     * Explication des JOIN :
     *   - JOIN patients : pour avoir le nom du patient depuis son id
     *   - JOIN medecins : pour avoir la specialite depuis l'id medecin
     *   - JOIN utilisateurs : pour avoir le nom du medecin (stocke dans utilisateurs)
     */
    private static final String BASE_SQL =
        "SELECT r.identifiant AS id_rdv, " +
        "       r.patient_id, " +
        "       r.medecin_id, " +
        "       r.date_rdv, " +
        "       r.heure_rdv, " +
        "       r.motif, " +
        "       r.statut, " +
        "       p.nom_famille AS nom_patient, " +
        "       p.prenom AS prenom_patient, " +
        "       p.cin AS cin_patient, " +
        "       p.numero_dossier, " +
        "       TIMESTAMPDIFF(YEAR, p.date_naissance, CURDATE()) AS age_patient, " +
        "       u.nom_famille AS nom_medecin, " +
        "       u.prenom AS prenom_medecin, " +
        "       ms.specialite " +
        "FROM rendez_vous r " +
        "JOIN patients p    ON r.patient_id = p.identifiant " +
        "JOIN medecins ms   ON r.medecin_id = ms.identifiant " +
        "JOIN utilisateurs u ON ms.utilisateur_id = u.identifiant ";

    // RDV actifs d'un medecin a une date (sans TERMINE ni ANNULE)
    public List<RendezVous> listerActifsParMedecinEtDate(int idMedecin, String date) {
        List<RendezVous> liste = new ArrayList<>();
        String sql = BASE_SQL +
            "WHERE r.medecin_id = ? AND r.date_rdv = ? " +
            "  AND r.statut NOT IN ('TERMINE', 'ANNULE') " +
            "ORDER BY r.heure_rdv";
        try {
            PreparedStatement stmt = obtenirConnexion().prepareStatement(sql);
            stmt.setInt(1, idMedecin);
            stmt.setString(2, date);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) liste.add(construireRendezVous(rs));
        } catch (SQLException e) {
            System.out.println("Erreur RDV actifs medecin+date : " + e.getMessage());
        }
        return liste;
    }

    // ── LISTER LES RDV ACTIFS D'UNE DATE (sans TERMINE ni ANNULE) ────────────
    /**
     * Pour le tableau de bord : on affiche seulement les RDV en attente.
     * Les RDV termines et annules disparaissent automatiquement.
     */
    public List<RendezVous> listerActifsParDate(String date) {
        List<RendezVous> liste = new ArrayList<>();
        String sql = BASE_SQL +
            "WHERE r.date_rdv = ? " +
            "  AND r.statut NOT IN ('TERMINE', 'ANNULE') " +
            "ORDER BY r.heure_rdv";
        try {
            PreparedStatement stmt = obtenirConnexion().prepareStatement(sql);
            stmt.setString(1, date);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                liste.add(construireRendezVous(rs));
            }
        } catch (SQLException e) {
            System.out.println("Erreur RDV actifs par date : " + e.getMessage());
        }
        return liste;
    }

    // ── LISTER LES RDV D'UNE DATE DONNEE ─────────────────────────────────────
    /**
     * Retourne tous les rendez-vous prevus a une date precise.
     * La date doit etre au format "yyyy-MM-dd" (ex: "2026-05-01").
     * Trie par heure pour afficher la file d'attente dans l'ordre.
     */
    public List<RendezVous> listerParDate(String date) {
        List<RendezVous> liste = new ArrayList<>();

        String sql = BASE_SQL + "WHERE r.date_rdv = ? ORDER BY r.heure_rdv";

        try {
            PreparedStatement requete = obtenirConnexion().prepareStatement(sql);
            requete.setString(1, date);

            ResultSet resultat = requete.executeQuery();
            while (resultat.next()) {
                liste.add(construireRendezVous(resultat));
            }
        } catch (SQLException e) {
            System.out.println("Erreur RDV par date : " + e.getMessage());
        }

        return liste;
    }

    // ── LISTER LES RDV D'UN MEDECIN A UNE DATE ───────────────────────────────
    /**
     * Retourne les rendez-vous d'UN medecin specifique pour UNE date.
     * Utilise dans le tableau de bord du medecin pour afficher ses consultations du jour.
     */
    public List<RendezVous> listerParMedecinEtDate(int idMedecin, String date) {
        List<RendezVous> liste = new ArrayList<>();

        String sql = BASE_SQL + "WHERE r.medecin_id = ? AND r.date_rdv = ? ORDER BY r.heure_rdv";

        try {
            PreparedStatement requete = obtenirConnexion().prepareStatement(sql);
            requete.setInt(1, idMedecin);
            requete.setString(2, date);

            ResultSet resultat = requete.executeQuery();
            while (resultat.next()) {
                liste.add(construireRendezVous(resultat));
            }
        } catch (SQLException e) {
            System.out.println("Erreur RDV medecin + date : " + e.getMessage());
        }

        return liste;
    }

    // ── LISTER TOUS LES RDV D'UN MEDECIN ─────────────────────────────────────
    /**
     * Retourne tous les rendez-vous d'un medecin, toutes dates confondues.
     * Utilise quand le medecin clique sur le bouton "Tous".
     */
    public List<RendezVous> listerParMedecin(int idMedecin) {
        List<RendezVous> liste = new ArrayList<>();

        String sql = BASE_SQL + "WHERE r.medecin_id = ? ORDER BY r.date_rdv, r.heure_rdv";

        try {
            PreparedStatement requete = obtenirConnexion().prepareStatement(sql);
            requete.setInt(1, idMedecin);

            ResultSet resultat = requete.executeQuery();
            while (resultat.next()) {
                liste.add(construireRendezVous(resultat));
            }
        } catch (SQLException e) {
            System.out.println("Erreur RDV par medecin : " + e.getMessage());
        }

        return liste;
    }

    // ── LISTER TOUS LES RDV (admin et secretaire) ─────────────────────────────
    /**
     * Retourne tous les rendez-vous de tous les medecins.
     * Utilise par l'administrateur et la secretaire.
     */
    public List<RendezVous> listerTous() {
        List<RendezVous> liste = new ArrayList<>();

        String sql = BASE_SQL + "ORDER BY r.date_rdv DESC, r.heure_rdv";

        try {
            ResultSet resultat = obtenirConnexion().createStatement().executeQuery(sql);
            while (resultat.next()) {
                liste.add(construireRendezVous(resultat));
            }
        } catch (SQLException e) {
            System.out.println("Erreur liste tous RDV : " + e.getMessage());
        }

        return liste;
    }

    // ── AJOUTER UN RENDEZ-VOUS ────────────────────────────────────────────────
    /**
     * Cree un nouveau rendez-vous dans la base.
     * Le statut par defaut est PLANIFIE si aucun statut n'est defini.
     */
    public boolean ajouter(RendezVous r) {

        String sql = "INSERT INTO rendez_vous (patient_id, medecin_id, date_rdv, heure_rdv, motif, statut) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement requete = obtenirConnexion().prepareStatement(sql);
            requete.setInt(1, r.getPatientId());
            requete.setInt(2, r.getMedecinId());
            requete.setString(3, r.getDateRdv());
            requete.setString(4, r.getHeureRdv());
            requete.setString(5, r.getMotif());

            // Si aucun statut n'est defini, on met PLANIFIE par defaut
            String statut = r.getStatut() != null ? r.getStatut() : "PLANIFIE";
            requete.setString(6, statut);

            int lignesInserees = requete.executeUpdate();
            return lignesInserees > 0;

        } catch (SQLException e) {
            System.out.println("Erreur ajout rendez-vous : " + e.getMessage());
            return false;
        }
    }

    // ── MODIFIER UN RENDEZ-VOUS ───────────────────────────────────────────────
    /**
     * Met a jour toutes les informations d'un rendez-vous existant.
     */
    public boolean modifier(RendezVous r) {

        String sql = "UPDATE rendez_vous " +
                     "SET patient_id = ?, medecin_id = ?, date_rdv = ?, " +
                     "    heure_rdv = ?, motif = ?, statut = ? " +
                     "WHERE identifiant = ?";

        try {
            PreparedStatement requete = obtenirConnexion().prepareStatement(sql);
            requete.setInt(1, r.getPatientId());
            requete.setInt(2, r.getMedecinId());
            requete.setString(3, r.getDateRdv());
            requete.setString(4, r.getHeureRdv());
            requete.setString(5, r.getMotif());
            requete.setString(6, r.getStatut());
            requete.setInt(7, r.getId());

            int lignesModifiees = requete.executeUpdate();
            return lignesModifiees > 0;

        } catch (SQLException e) {
            System.out.println("Erreur modification rendez-vous : " + e.getMessage());
            return false;
        }
    }

    // ── CHANGER LE STATUT D'UN RDV ────────────────────────────────────────────
    /**
     * Met a jour uniquement le statut d'un rendez-vous.
     * Les statuts possibles sont : PLANIFIE, CONFIRME, ANNULE, TERMINE, URGENCE.
     * Methode centrale utilisee par annuler() et par la declaration d'urgence.
     */
    public boolean changerStatut(int idRdv, String nouveauStatut) {

        String sql = "UPDATE rendez_vous SET statut = ? WHERE identifiant = ?";

        try {
            PreparedStatement requete = obtenirConnexion().prepareStatement(sql);
            requete.setString(1, nouveauStatut);
            requete.setInt(2, idRdv);

            int lignesModifiees = requete.executeUpdate();
            return lignesModifiees > 0;

        } catch (SQLException e) {
            System.out.println("Erreur changement statut RDV : " + e.getMessage());
            return false;
        }
    }

    // ── ANNULER UN RENDEZ-VOUS ────────────────────────────────────────────────
    /**
     * Raccourci pour passer le statut a ANNULE.
     * Appelle simplement changerStatut avec la valeur "ANNULE".
     */
    public boolean annuler(int idRdv) {
        return changerStatut(idRdv, "ANNULE");
    }

    // ── METHODE PRIVEE : construire un objet RendezVous ───────────────────────
    /**
     * Construit un objet RendezVous a partir d'une ligne de la base.
     * Comme la requete fait des JOIN, on peut lire le nom du patient ET du medecin.
     */
    private RendezVous construireRendezVous(ResultSet rs) throws SQLException {
        RendezVous r = new RendezVous();
        r.setId(rs.getInt("id_rdv"));
        r.setPatientId(rs.getInt("patient_id"));
        r.setMedecinId(rs.getInt("medecin_id"));
        r.setDateRdv(rs.getString("date_rdv"));
        r.setMotif(rs.getString("motif"));
        r.setStatut(rs.getString("statut"));

        // L'heure est stockee au format "HH:mm:ss" dans MySQL.
        // On prend seulement "HH:mm" (les 5 premiers caracteres).
        String heure = rs.getString("heure_rdv");
        r.setHeureRdv(heure != null ? heure.substring(0, 5) : "");

        // Infos du patient (viennent du JOIN avec "patients")
        r.setNomPatient(rs.getString("prenom_patient") + " " + rs.getString("nom_patient"));
        r.setNumeroDossierPatient(rs.getString("numero_dossier"));
        r.setCinPatient(rs.getString("cin_patient"));
        r.setAgePatient(rs.getInt("age_patient"));

        // Infos du medecin (viennent du JOIN avec "medecins" et "utilisateurs")
        r.setNomMedecin("Dr. " + rs.getString("prenom_medecin") + " " + rs.getString("nom_medecin"));
        r.setSpecialiteMedecin(rs.getString("specialite"));

        return r;
    }
}
