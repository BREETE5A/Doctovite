package controller;

import model.Connexion;
import model.Facture;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/**
 * GestionFacture — Classe qui gere les operations sur la table "factures".
 *
 * Une facture est creee apres une consultation pour facturer les soins.
 * Son statut est calcule automatiquement selon le montant paye :
 *   - EN_ATTENTE : rien n'a encore ete paye
 *   - PARTIEL    : une partie a ete payee
 *   - SOLDE      : la facture est entierement payee
 */
public class GestionFacture {

    private Connection obtenirConnexion() {
        return Connexion.getConnexion();
    }

    // ── LISTER TOUTES LES FACTURES ────────────────────────────────────────────
    /**
     * Retourne toutes les factures, de la plus recente a la plus ancienne.
     * Un JOIN avec "patients" permet d'afficher le nom du patient.
     */
    public List<Facture> listerTous() {
        List<Facture> liste = new ArrayList<>();

        String sql =
            "SELECT f.identifiant AS id_facture, " +
            "       f.patient_id, " +
            "       f.numero_facture, " +
            "       f.montant_total, " +
            "       f.montant_paye, " +
            "       f.mode_paiement, " +
            "       f.statut_paiement, " +
            "       f.description, " +
            "       f.date_creation, " +
            "       pa.nom_famille AS nom_patient, " +
            "       pa.prenom AS prenom_patient, " +
            "       pa.numero_dossier " +
            "FROM factures f " +
            "JOIN patients pa ON f.patient_id = pa.identifiant " +
            "ORDER BY f.date_creation DESC";

        try {
            ResultSet resultat = obtenirConnexion().createStatement().executeQuery(sql);
            while (resultat.next()) {
                liste.add(construireFacture(resultat));
            }
        } catch (SQLException e) {
            System.out.println("Erreur liste factures : " + e.getMessage());
        }

        return liste;
    }

    // ── AJOUTER UNE FACTURE ───────────────────────────────────────────────────
    /**
     * Insere une nouvelle facture dans la base.
     * Le numero de facture est genere avant l'appel a cette methode
     * avec la methode genererNumero().
     */
    public boolean ajouter(Facture f) {

        String sql = "INSERT INTO factures " +
                     "(patient_id, numero_facture, montant_total, montant_paye, mode_paiement, statut_paiement, description) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement requete = obtenirConnexion().prepareStatement(sql);
            requete.setInt(1, f.getPatientId());
            requete.setString(2, f.getNumeroFacture());
            requete.setDouble(3, f.getMontantTotal());
            requete.setDouble(4, f.getMontantPaye());
            requete.setString(5, f.getModePaiement());   // ESPECES, CARTE, VIREMENT, ASSURANCE
            requete.setString(6, f.getStatut());          // EN_ATTENTE, PARTIEL, SOLDE
            requete.setString(7, f.getDescription());

            int lignesInserees = requete.executeUpdate();
            return lignesInserees > 0;

        } catch (SQLException e) {
            System.out.println("Erreur ajout facture : " + e.getMessage());
            return false;
        }
    }

    // ── GENERER UN NUMERO DE FACTURE ──────────────────────────────────────────
    /**
     * Cree un numero de facture unique au format : FAC-ANNEE-NUMERO.
     * Exemple : FAC-2026-0047 signifie la 47eme facture de l'annee 2026.
     *
     * On compte le nombre de factures existantes et on ajoute 1.
     * Le format %04d ajoute des zeros devant si necessaire (ex: 0047).
     */
    public String genererNumero() {

        String sql = "SELECT COALESCE(MAX(identifiant), 0) + 1 AS prochain FROM factures";

        try {
            ResultSet resultat = obtenirConnexion().createStatement().executeQuery(sql);
            if (resultat.next()) {
                int annee   = Year.now().getValue();  // ex: 2026
                int prochain = resultat.getInt("prochain");
                return String.format("FAC-%d-%04d", annee, prochain);  // ex: FAC-2026-0047
            }
        } catch (SQLException e) {
            System.out.println("Erreur generation numero facture : " + e.getMessage());
        }

        // En cas d'erreur, on utilise le timestamp comme numero unique
        return "FAC-" + System.currentTimeMillis();
    }

    // ── METHODE PRIVEE : construire un objet Facture ──────────────────────────
    private Facture construireFacture(ResultSet rs) throws SQLException {
        Facture f = new Facture();
        f.setId(rs.getInt("id_facture"));
        f.setPatientId(rs.getInt("patient_id"));
        f.setNumeroFacture(rs.getString("numero_facture"));
        f.setMontantTotal(rs.getDouble("montant_total"));
        f.setMontantPaye(rs.getDouble("montant_paye"));
        f.setModePaiement(rs.getString("mode_paiement"));
        f.setStatut(rs.getString("statut_paiement"));
        f.setDescription(rs.getString("description"));

        Timestamp dateCree = rs.getTimestamp("date_creation");
        if (dateCree != null) {
            f.setDateCree(dateCree.toString().substring(0, 10));
        }

        // Nom complet du patient
        f.setNomPatient(rs.getString("prenom_patient") + " " + rs.getString("nom_patient"));
        f.setNumeroDossier(rs.getString("numero_dossier"));

        return f;
    }
}
