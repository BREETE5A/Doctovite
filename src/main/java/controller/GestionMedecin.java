package controller;

import model.Connexion;
import model.Medecin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * GestionMedecin — Classe qui gere les tables "medecins" et "utilisateurs".
 *
 * Un medecin est stocke dans DEUX tables :
 *   - "utilisateurs" pour le compte (CIN, mot de passe, role...)
 *   - "medecins" pour le profil medical (specialite, horaires...)
 * Les deux sont lies par le champ "utilisateur_id" dans la table "medecins".
 */
public class GestionMedecin {

    // Codes de retour pour l'ajout d'un medecin
    public static final int AJOUT_OK            = 0;
    public static final int AJOUT_CIN_DUPLIQUE  = 1;
    public static final int AJOUT_CIN_DESACTIVE = 2;
    public static final int AJOUT_ERREUR_BD     = 3;

    private Connection obtenirConnexion() {
        return Connexion.getConnexion();
    }

    // ── REQUETE DE BASE ───────────────────────────────────────────────────────
    private static final String BASE_SQL =
        "SELECT m.identifiant AS id_medecin, " +
        "       m.utilisateur_id, " +
        "       m.specialite, " +
        "       m.heure_debut, " +
        "       m.heure_fin, " +
        "       m.statut_presence, " +
        "       u.cin, " +
        "       u.nom_famille, " +
        "       u.prenom, " +
        "       u.mot_passe, " +
        "       u.adresse_email, " +
        "       u.num_telephone " +
        "FROM medecins m " +
        "JOIN utilisateurs u ON m.utilisateur_id = u.identifiant " +
        "WHERE 1=1 ";

    // ── LISTER TOUS LES MEDECINS ──────────────────────────────────────────────
    public List<Medecin> listerTous() {
        List<Medecin> liste = new ArrayList<>();
        String sql = BASE_SQL + "AND u.actif = 1 ORDER BY u.nom_famille";
        try {
            ResultSet resultat = obtenirConnexion().createStatement().executeQuery(sql);
            while (resultat.next()) {
                liste.add(construireMedecin(resultat));
            }
        } catch (SQLException e) {
            System.out.println("Erreur liste medecins : " + e.getMessage());
        }
        return liste;
    }

    // ── FILTRER PAR SPECIALITE ────────────────────────────────────────────────
    public List<Medecin> listerParSpecialite(String specialite) {
        List<Medecin> liste = new ArrayList<>();
        String sql = BASE_SQL + "AND u.actif = 1 AND m.specialite = ? ORDER BY u.nom_famille";
        try {
            PreparedStatement stmt = obtenirConnexion().prepareStatement(sql);
            stmt.setString(1, specialite);

            ResultSet resultat = stmt.executeQuery();
            while (resultat.next()) {
                liste.add(construireMedecin(resultat));
            }
        } catch (SQLException e) {
            System.out.println("Erreur filtre specialite : " + e.getMessage());
        }
        return liste;
    }

    // ── TROUVER UN MEDECIN PAR ID UTILISATEUR ────────────────────────────────
    public Medecin trouverParUtilisateurId(int idUtilisateur) {
        String sql = BASE_SQL + "AND m.utilisateur_id = ?";
        try {
            PreparedStatement stmt = obtenirConnexion().prepareStatement(sql);
            stmt.setInt(1, idUtilisateur);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Medecin m = construireMedecin(rs);
                rs.close();
                stmt.close();
                System.out.println("trouverParUtilisateurId(" + idUtilisateur
                    + ") -> medecin.id=" + m.getId() + " " + m.getNom());
                return m;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("Erreur recherche par userId : " + e.getMessage());
        }
        return null;
    }

    // ── TROUVER UN MEDECIN PAR ID ────────────────────────────────────────────
    public Medecin trouverParId(int idMedecin) {
        String sql = BASE_SQL + "AND m.identifiant = ?";
        try {
            PreparedStatement stmt = obtenirConnexion().prepareStatement(sql);
            stmt.setInt(1, idMedecin);

            ResultSet resultat = stmt.executeQuery();
            if (resultat.next()) {
                return construireMedecin(resultat);
            }
        } catch (SQLException e) {
            System.out.println("Erreur recherche medecin par id : " + e.getMessage());
        }
        return null;
    }

    // ── LISTER LES SPECIALITES DISPONIBLES ───────────────────────────────────
    public List<String> listerSpecialites() {
        List<String> liste = new ArrayList<>();
        String sql = "SELECT DISTINCT specialite FROM medecins ORDER BY specialite";
        try {
            ResultSet resultat = obtenirConnexion().createStatement().executeQuery(sql);
            while (resultat.next()) {
                liste.add(resultat.getString("specialite"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur liste specialites : " + e.getMessage());
        }
        return liste;
    }

    // ── AJOUTER UN MEDECIN (avec code de retour precis) ──────────────────────
    /**
     * Ajoute un medecin en deux etapes (utilisateurs puis medecins).
     * Verifie d'abord si le CIN est deja utilise.
     */
    public int ajouterAvecCode(Medecin m) {
        // Verification prealable du CIN dans la table utilisateurs
        GestionUtilisateur gu = new GestionUtilisateur();
        boolean[] check = gu.verifierCin(m.getCin());
        if (check[0] && check[1]) return AJOUT_CIN_DUPLIQUE;
        if (check[0] && !check[1]) return AJOUT_CIN_DESACTIVE;

        // Etape 1 : creer le compte utilisateur
        String sqlUtilisateur =
            "INSERT INTO utilisateurs (cin, nom_famille, prenom, mot_passe, role, adresse_email, num_telephone) " +
            "VALUES (?, ?, ?, ?, 'MEDECIN', ?, ?)";
        try {
            PreparedStatement stmt = obtenirConnexion().prepareStatement(
                sqlUtilisateur, Statement.RETURN_GENERATED_KEYS
            );
            stmt.setString(1, m.getCin());
            stmt.setString(2, m.getNom());
            stmt.setString(3, m.getPrenom());
            stmt.setString(4, m.getMotDePasse());
            stmt.setString(5, m.getEmail());
            stmt.setString(6, m.getTelephone());
            stmt.executeUpdate();

            ResultSet idsGeneres = stmt.getGeneratedKeys();
            if (!idsGeneres.next()) {
                System.out.println("Impossible de recuperer l'id du nouvel utilisateur.");
                return AJOUT_ERREUR_BD;
            }
            int idNouvelUtilisateur = idsGeneres.getInt(1);

            // Etape 2 : creer le profil medecin
            String sqlMedecin =
                "INSERT INTO medecins (utilisateur_id, specialite, heure_debut, heure_fin) " +
                "VALUES (?, ?, ?, ?)";
            PreparedStatement stmt2 = obtenirConnexion().prepareStatement(sqlMedecin);
            stmt2.setInt(1, idNouvelUtilisateur);
            stmt2.setString(2, m.getSpecialite());
            stmt2.setString(3, m.getHeureDebut());
            stmt2.setString(4, m.getHeureFin());

            int lignes = stmt2.executeUpdate();
            if (lignes > 0) return AJOUT_OK;
            return AJOUT_ERREUR_BD;
        } catch (SQLException e) {
            System.out.println("Erreur ajout medecin : " + e.getMessage());
            return AJOUT_ERREUR_BD;
        }
    }

    // ── REACTIVER UN ANCIEN MEDECIN DESACTIVE ─────────────────────────────────
    /**
     * Reactive un compte utilisateur desactive et met a jour le profil medecin.
     * Si le profil medecin n'existe plus, il est recree.
     */
    public boolean reactiverEtMettreAJour(Medecin m) {
        try {
            // 1) Reactiver le compte utilisateur
            String sqlUtil = "UPDATE utilisateurs " +
                             "SET nom_famille = ?, prenom = ?, mot_passe = ?, role = 'MEDECIN', " +
                             "    adresse_email = ?, num_telephone = ?, actif = 1 " +
                             "WHERE cin = ?";
            PreparedStatement stmt = obtenirConnexion().prepareStatement(sqlUtil);
            stmt.setString(1, m.getNom());
            stmt.setString(2, m.getPrenom());
            stmt.setString(3, m.getMotDePasse());
            stmt.setString(4, m.getEmail());
            stmt.setString(5, m.getTelephone());
            stmt.setString(6, m.getCin());
            stmt.executeUpdate();

            // 2) Recuperer l'id utilisateur
            String sqlId = "SELECT identifiant FROM utilisateurs WHERE cin = ?";
            PreparedStatement stmt2 = obtenirConnexion().prepareStatement(sqlId);
            stmt2.setString(1, m.getCin());
            ResultSet rs = stmt2.executeQuery();
            if (!rs.next()) return false;
            int idUtil = rs.getInt("identifiant");

            // 3) Verifier si un profil medecin existe deja
            String sqlCheck = "SELECT identifiant FROM medecins WHERE utilisateur_id = ?";
            PreparedStatement stmt3 = obtenirConnexion().prepareStatement(sqlCheck);
            stmt3.setInt(1, idUtil);
            ResultSet rsCheck = stmt3.executeQuery();

            if (rsCheck.next()) {
                // Le profil medecin existe : on le met a jour
                String sqlUpd = "UPDATE medecins SET specialite = ?, heure_debut = ?, heure_fin = ? " +
                                "WHERE utilisateur_id = ?";
                PreparedStatement stmt4 = obtenirConnexion().prepareStatement(sqlUpd);
                stmt4.setString(1, m.getSpecialite());
                stmt4.setString(2, m.getHeureDebut());
                stmt4.setString(3, m.getHeureFin());
                stmt4.setInt(4, idUtil);
                stmt4.executeUpdate();
            } else {
                // Le profil medecin n'existe pas : on le cree
                String sqlIns = "INSERT INTO medecins (utilisateur_id, specialite, heure_debut, heure_fin) " +
                                "VALUES (?, ?, ?, ?)";
                PreparedStatement stmt4 = obtenirConnexion().prepareStatement(sqlIns);
                stmt4.setInt(1, idUtil);
                stmt4.setString(2, m.getSpecialite());
                stmt4.setString(3, m.getHeureDebut());
                stmt4.setString(4, m.getHeureFin());
                stmt4.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            System.out.println("Erreur reactivation medecin : " + e.getMessage());
            return false;
        }
    }

    // ── MODIFIER UN MEDECIN ───────────────────────────────────────────────────
    public boolean modifier(Medecin m) {
        String sqlMedecin =
            "UPDATE medecins SET specialite = ?, heure_debut = ?, heure_fin = ? " +
            "WHERE identifiant = ?";
        String sqlUtilisateur =
            "UPDATE utilisateurs SET nom_famille = ?, prenom = ?, adresse_email = ?, num_telephone = ? " +
            "WHERE identifiant = ?";
        try {
            PreparedStatement stmt = obtenirConnexion().prepareStatement(sqlMedecin);
            stmt.setString(1, m.getSpecialite());
            stmt.setString(2, m.getHeureDebut());
            stmt.setString(3, m.getHeureFin());
            stmt.setInt(4, m.getId());
            stmt.executeUpdate();

            PreparedStatement stmt2 = obtenirConnexion().prepareStatement(sqlUtilisateur);
            stmt2.setString(1, m.getNom());
            stmt2.setString(2, m.getPrenom());
            stmt2.setString(3, m.getEmail());
            stmt2.setString(4, m.getTelephone());
            stmt2.setInt(5, m.getUtilisateurId());
            stmt2.executeUpdate();

            return true;
        } catch (SQLException e) {
            System.out.println("Erreur modification medecin : " + e.getMessage());
            return false;
        }
    }

    // ── CONSTRUIRE UN OBJET MEDECIN DEPUIS UN RESULTSET ──────────────────────
    private Medecin construireMedecin(ResultSet ligne) throws SQLException {
        Medecin m = new Medecin();
        m.setId(ligne.getInt("id_medecin"));
        m.setUtilisateurId(ligne.getInt("utilisateur_id"));
        m.setSpecialite(ligne.getString("specialite"));
        m.setStatut(ligne.getString("statut_presence"));

        String heureDebut = ligne.getString("heure_debut");
        String heureFin   = ligne.getString("heure_fin");
        m.setHeureDebut(heureDebut != null ? heureDebut.substring(0, 5) : "");
        m.setHeureFin(heureFin   != null ? heureFin.substring(0, 5)   : "");

        m.setCin(ligne.getString("cin"));
        m.setNom(ligne.getString("nom_famille"));
        m.setPrenom(ligne.getString("prenom"));
        m.setMotDePasse(ligne.getString("mot_passe"));
        m.setEmail(ligne.getString("adresse_email"));
        m.setTelephone(ligne.getString("num_telephone"));

        return m;
    }
}
