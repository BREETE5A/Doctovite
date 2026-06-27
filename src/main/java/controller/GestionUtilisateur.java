package controller;

import model.Connexion;
import model.Utilisateur;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * GestionUtilisateur — Classe qui gere la table "utilisateurs".
 * Elle fait le lien entre Java et la base de donnees.
 */
public class GestionUtilisateur {

    // Codes de retour pour l'ajout d'un utilisateur
    public static final int AJOUT_OK            = 0;  // succes
    public static final int AJOUT_CIN_DUPLIQUE  = 1;  // CIN deja utilise (compte actif)
    public static final int AJOUT_CIN_DESACTIVE = 2;  // CIN existe mais compte desactive
    public static final int AJOUT_ERREUR_BD     = 3;  // erreur de base de donnees

    // Connexion a la base
    private Connection obtenirConnexion() {
        return Connexion.getConnexion();
    }

    // ── CONNEXION D'UN UTILISATEUR ────────────────────────────────────────────
    public Utilisateur connecter(String cin, String motDePasse) {
        String sql = "SELECT * FROM utilisateurs WHERE cin = ? AND mot_passe = ? AND actif = 1";
        try {
            PreparedStatement stmt = obtenirConnexion().prepareStatement(sql);
            stmt.setString(1, cin);
            stmt.setString(2, motDePasse);

            ResultSet resultat = stmt.executeQuery();
            if (resultat.next()) {
                return construireUtilisateur(resultat);
            }
        } catch (SQLException e) {
            System.out.println("Erreur connexion : " + e.getMessage());
        }
        return null;
    }

    // ── VERIFIER SI UN CIN EXISTE DEJA ────────────────────────────────────────
    /**
     * Verifie si un CIN existe dans la table utilisateurs.
     * Retourne un tableau de 2 booleens :
     *   case 0 = le CIN existe ?
     *   case 1 = est-ce qu'il est actif ?
     */
    public boolean[] verifierCin(String cin) {
        boolean[] resultat = {false, false};
        String sql = "SELECT actif FROM utilisateurs WHERE cin = ?";
        try {
            PreparedStatement stmt = obtenirConnexion().prepareStatement(sql);
            stmt.setString(1, cin);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                resultat[0] = true;
                resultat[1] = rs.getInt("actif") == 1;
            }
        } catch (SQLException e) {
            System.out.println("Erreur verification CIN : " + e.getMessage());
        }
        return resultat;
    }

    // ── AJOUTER UN UTILISATEUR (avec code de retour precis) ───────────────────
    public int ajouterAvecCode(Utilisateur u) {
        // On verifie d'abord si le CIN existe
        boolean[] check = verifierCin(u.getCin());
        if (check[0] && check[1]) return AJOUT_CIN_DUPLIQUE;
        if (check[0] && !check[1]) return AJOUT_CIN_DESACTIVE;

        String sql = "INSERT INTO utilisateurs " +
                     "(cin, nom_famille, prenom, mot_passe, role, adresse_email, num_telephone) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement stmt = obtenirConnexion().prepareStatement(sql);
            stmt.setString(1, u.getCin());
            stmt.setString(2, u.getNom());
            stmt.setString(3, u.getPrenom());
            stmt.setString(4, u.getMotDePasse());
            stmt.setString(5, u.getRole());
            stmt.setString(6, u.getEmail());
            stmt.setString(7, u.getTelephone());

            int lignes = stmt.executeUpdate();
            if (lignes > 0) return AJOUT_OK;
            return AJOUT_ERREUR_BD;
        } catch (SQLException e) {
            System.out.println("Erreur ajout utilisateur : " + e.getMessage());
            return AJOUT_ERREUR_BD;
        }
    }

    // ── REACTIVER UN COMPTE DESACTIVE ─────────────────────────────────────────
    /**
     * Reactive un compte qui avait ete desactive et met a jour ses infos.
     */
    public boolean reactiverEtMettreAJour(Utilisateur u) {
        String sql = "UPDATE utilisateurs " +
                     "SET nom_famille = ?, prenom = ?, mot_passe = ?, role = ?, " +
                     "    adresse_email = ?, num_telephone = ?, actif = 1 " +
                     "WHERE cin = ?";
        try {
            PreparedStatement stmt = obtenirConnexion().prepareStatement(sql);
            stmt.setString(1, u.getNom());
            stmt.setString(2, u.getPrenom());
            stmt.setString(3, u.getMotDePasse());
            stmt.setString(4, u.getRole());
            stmt.setString(5, u.getEmail());
            stmt.setString(6, u.getTelephone());
            stmt.setString(7, u.getCin());

            int lignes = stmt.executeUpdate();
            return lignes > 0;
        } catch (SQLException e) {
            System.out.println("Erreur reactivation : " + e.getMessage());
            return false;
        }
    }

    // ── MODIFIER L'EMAIL ET LE TELEPHONE ──────────────────────────────────────
    public boolean modifier(Utilisateur u) {
        String sql = "UPDATE utilisateurs SET adresse_email = ?, num_telephone = ? WHERE identifiant = ?";
        try {
            PreparedStatement stmt = obtenirConnexion().prepareStatement(sql);
            stmt.setString(1, u.getEmail());
            stmt.setString(2, u.getTelephone());
            stmt.setInt(3, u.getId());

            int lignes = stmt.executeUpdate();
            return lignes > 0;
        } catch (SQLException e) {
            System.out.println("Erreur modification utilisateur : " + e.getMessage());
            return false;
        }
    }

    // ── CHANGER LE MOT DE PASSE ───────────────────────────────────────────────
    public boolean changerMotDePasse(int idUtilisateur, String nouveauMotDePasse) {
        String sql = "UPDATE utilisateurs SET mot_passe = ? WHERE identifiant = ?";
        try {
            PreparedStatement stmt = obtenirConnexion().prepareStatement(sql);
            stmt.setString(1, nouveauMotDePasse);
            stmt.setInt(2, idUtilisateur);

            int lignes = stmt.executeUpdate();
            return lignes > 0;
        } catch (SQLException e) {
            System.out.println("Erreur changement mot de passe : " + e.getMessage());
            return false;
        }
    }

    // ── DESACTIVER UN COMPTE ──────────────────────────────────────────────────
    public boolean desactiver(int idUtilisateur) {
        String sql = "UPDATE utilisateurs SET actif = 0 WHERE identifiant = ?";
        try {
            PreparedStatement stmt = obtenirConnexion().prepareStatement(sql);
            stmt.setInt(1, idUtilisateur);

            int lignes = stmt.executeUpdate();
            return lignes > 0;
        } catch (SQLException e) {
            System.out.println("Erreur desactivation : " + e.getMessage());
            return false;
        }
    }

    // ── LISTER TOUS LES UTILISATEURS ACTIFS ──────────────────────────────────
    public List<Utilisateur> listerTous() {
        List<Utilisateur> liste = new ArrayList<>();
        String sql = "SELECT * FROM utilisateurs WHERE actif = 1 ORDER BY role, nom_famille";
        try {
            ResultSet resultat = obtenirConnexion().createStatement().executeQuery(sql);
            while (resultat.next()) {
                liste.add(construireUtilisateur(resultat));
            }
        } catch (SQLException e) {
            System.out.println("Erreur liste utilisateurs : " + e.getMessage());
        }
        return liste;
    }

    // ── LISTER LES UTILISATEURS PAR ROLE ─────────────────────────────────────
    /**
     * Retourne les utilisateurs actifs qui ont un certain role.
     * Utilise pour envoyer une notification a tous les administrateurs.
     */
    public List<Utilisateur> listerParRole(String role) {
        List<Utilisateur> liste = new ArrayList<>();
        String sql = "SELECT * FROM utilisateurs WHERE role = ? AND actif = 1 ORDER BY nom_famille";
        try {
            PreparedStatement stmt = obtenirConnexion().prepareStatement(sql);
            stmt.setString(1, role);

            ResultSet resultat = stmt.executeQuery();
            while (resultat.next()) {
                liste.add(construireUtilisateur(resultat));
            }
        } catch (SQLException e) {
            System.out.println("Erreur liste par role : " + e.getMessage());
        }
        return liste;
    }

    // ── TROUVER UN UTILISATEUR PAR SON ID ────────────────────────────────────
    public Utilisateur trouverParId(int idUtilisateur) {
        String sql = "SELECT * FROM utilisateurs WHERE identifiant = ?";
        try {
            PreparedStatement stmt = obtenirConnexion().prepareStatement(sql);
            stmt.setInt(1, idUtilisateur);

            ResultSet resultat = stmt.executeQuery();
            if (resultat.next()) {
                return construireUtilisateur(resultat);
            }
        } catch (SQLException e) {
            System.out.println("Erreur recherche par id : " + e.getMessage());
        }
        return null;
    }

    // ── CONSTRUIRE UN OBJET UTILISATEUR DEPUIS UN RESULTSET ──────────────────
    private Utilisateur construireUtilisateur(ResultSet ligne) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setId(ligne.getInt("identifiant"));
        u.setCin(ligne.getString("cin"));
        u.setNom(ligne.getString("nom_famille"));
        u.setPrenom(ligne.getString("prenom"));
        u.setMotDePasse(ligne.getString("mot_passe"));
        u.setRole(ligne.getString("role"));
        u.setEmail(ligne.getString("adresse_email"));
        u.setTelephone(ligne.getString("num_telephone"));
        return u;
    }
}
