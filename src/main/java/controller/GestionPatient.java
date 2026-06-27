package controller;

import model.Connexion;
import model.Patient;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * GestionPatient — Classe qui gere les operations sur la table "patients".
 * Elle permet de : lister, rechercher, ajouter, modifier et supprimer des patients.
 * Chaque patient a un numero de dossier genere automatiquement a sa creation.
 */
public class GestionPatient {

    // Recuperation de la connexion a la base de donnees
    private Connection obtenirConnexion() {
        return Connexion.getConnexion();
    }

    // ── LISTER TOUS LES PATIENTS ──────────────────────────────────────────────
    /**
     * Retourne la liste complete de tous les patients, tries par nom.
     */
    public List<Patient> listerTous() {

        List<Patient> liste = new ArrayList<>();

        // Simple requete SELECT pour recuperer tous les patients.
        // L'age sera calcule automatiquement par Patient.getAge()
        // qui utilise la dateNaissance stockee dans l'objet.
        String sql = "SELECT * FROM patients ORDER BY nom_famille";

        try {
            ResultSet resultat = obtenirConnexion().createStatement().executeQuery(sql);
            while (resultat.next()) {
                liste.add(construirePatient(resultat));
            }
        } catch (SQLException e) {
            System.out.println("Erreur liste patients : " + e.getMessage());
        }

        return liste;
    }

    // ── TROUVER UN PATIENT PAR SON ID ─────────────────────────────────────────
    public Patient trouverParId(int id) {
        String sql = "SELECT * FROM patients WHERE identifiant = ?";
        try {
            PreparedStatement stmt = obtenirConnexion().prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return construirePatient(rs);
        } catch (Exception e) {
            System.out.println("Erreur trouverParId patient : " + e.getMessage());
        }
        return null;
    }

    // ── RECHERCHER DES PATIENTS ───────────────────────────────────────────────
    /**
     * Recherche des patients par CIN, nom, prenom ou numero de dossier.
     * Le symbole % autour du terme signifie "contient ce mot n'importe ou".
     * Ex : rechercher("ali") trouve "Ali", "Malika", "Khalid".
     */
    public List<Patient> rechercher(String terme) {

        List<Patient> liste = new ArrayList<>();

        String sql = "SELECT * FROM patients " +
                     "WHERE cin LIKE ? OR nom_famille LIKE ? OR prenom LIKE ? OR numero_dossier LIKE ? " +
                     "ORDER BY nom_famille";

        try {
            PreparedStatement requete = obtenirConnexion().prepareStatement(sql);

            // On ajoute le % avant et apres le terme pour chercher "contient"
            String termeRecherche = "%" + terme + "%";
            requete.setString(1, termeRecherche);  // CIN contient le terme
            requete.setString(2, termeRecherche);  // nom contient le terme
            requete.setString(3, termeRecherche);  // prenom contient le terme
            requete.setString(4, termeRecherche);  // dossier contient le terme

            ResultSet resultat = requete.executeQuery();
            while (resultat.next()) {
                liste.add(construirePatient(resultat));
            }

        } catch (SQLException e) {
            System.out.println("Erreur recherche patients : " + e.getMessage());
        }

        return liste;
    }

    // ── AJOUTER UN PATIENT ────────────────────────────────────────────────────
    /**
     * Insere un nouveau patient dans la base de donnees.
     * On genere automatiquement un numero de dossier unique (ex : DOS-031).
     * Retourne true si l'ajout a reussi.
     */
    // Codes de retour pour l'ajout d'un patient
    public static final int AJOUT_OK           = 0;
    public static final int AJOUT_CIN_DUPLIQUE = 1;
    public static final int AJOUT_ERREUR_BD    = 2;

    // Verifie si un CIN patient existe deja
    public boolean cinExiste(String cin) {
        String sql = "SELECT identifiant FROM patients WHERE cin = ?";
        try {
            PreparedStatement stmt = obtenirConnexion().prepareStatement(sql);
            stmt.setString(1, cin);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            System.out.println("Erreur verification CIN patient : " + e.getMessage());
            return false;
        }
    }

    public int ajouterAvecCode(Patient p) {
        if (cinExiste(p.getCin())) return AJOUT_CIN_DUPLIQUE;

        String numeroDossier = genererNumeroDossier();
        String sql = "INSERT INTO patients " +
                     "(numero_dossier, cin, nom_famille, prenom, date_naissance, " +
                     " telephone, adresse_email, adresse, groupe_sanguin, antecedents) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement requete = obtenirConnexion().prepareStatement(sql);
            requete.setString(1, numeroDossier);
            requete.setString(2, p.getCin());
            requete.setString(3, p.getNom());
            requete.setString(4, p.getPrenom());
            if (p.getDateNaissance() != null) {
                requete.setDate(5, Date.valueOf(p.getDateNaissance()));
            } else {
                requete.setNull(5, java.sql.Types.DATE);
            }
            requete.setString(6, p.getTelephone());
            requete.setString(7, p.getEmail());
            requete.setString(8, p.getAdresse());
            requete.setString(9, p.getGroupeSanguin());
            requete.setString(10, p.getAntecedents());
            return requete.executeUpdate() > 0 ? AJOUT_OK : AJOUT_ERREUR_BD;
        } catch (SQLException e) {
            System.out.println("Erreur ajout patient SQL : " + e.getMessage());
            dernierMessage = e.getMessage();
            return AJOUT_ERREUR_BD;
        }
    }

    private String dernierMessage = "";
    public String getDerniereErreur() { return dernierMessage; }

    // ── MODIFIER UN PATIENT ───────────────────────────────────────────────────
    /**
     * Met a jour les informations d'un patient existant.
     * On identifie le patient grace a son identifiant (cle primaire).
     */
    public boolean modifier(Patient p) {

        String sql = "UPDATE patients " +
                     "SET cin = ?, nom_famille = ?, prenom = ?, date_naissance = ?, " +
                     "    telephone = ?, adresse_email = ?, adresse = ?, " +
                     "    groupe_sanguin = ?, antecedents = ? " +
                     "WHERE identifiant = ?";

        try {
            PreparedStatement requete = obtenirConnexion().prepareStatement(sql);
            requete.setString(1, p.getCin());
            requete.setString(2, p.getNom());
            requete.setString(3, p.getPrenom());

            if (p.getDateNaissance() != null) {
                requete.setDate(4, Date.valueOf(p.getDateNaissance()));
            } else {
                requete.setNull(4, java.sql.Types.DATE);
            }

            requete.setString(5, p.getTelephone());
            requete.setString(6, p.getEmail());
            requete.setString(7, p.getAdresse());
            requete.setString(8, p.getGroupeSanguin());
            requete.setString(9, p.getAntecedents());
            requete.setInt(10, p.getId());    // WHERE identifiant = ?

            int lignesModifiees = requete.executeUpdate();
            return lignesModifiees > 0;

        } catch (SQLException e) {
            System.out.println("Erreur modification patient : " + e.getMessage());
            return false;
        }
    }

    // ── SUPPRIMER UN PATIENT ──────────────────────────────────────────────────
    /**
     * Supprime definitivement un patient de la base de donnees.
     * Attention : cette action est irreversible.
     */
    public boolean supprimer(int idPatient) {

        String sql = "DELETE FROM patients WHERE identifiant = ?";

        try {
            PreparedStatement requete = obtenirConnexion().prepareStatement(sql);
            requete.setInt(1, idPatient);

            int lignesSupprimees = requete.executeUpdate();
            return lignesSupprimees > 0;

        } catch (SQLException e) {
            System.out.println("Erreur suppression patient : " + e.getMessage());
            return false;
        }
    }

    // ── METHODE PRIVEE : generer un numero de dossier ─────────────────────────
    /**
     * Cree un numero de dossier unique au format DOS-XXX.
     * On compte le nombre de patients existants et on ajoute 1.
     * Exemple : s'il y a 30 patients, le suivant aura DOS-031.
     */
    private String genererNumeroDossier() {
        // MAX evite les doublons si des patients ont ete supprimes
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(numero_dossier, 5) AS UNSIGNED)), 0) + 1 AS prochain " +
                     "FROM patients WHERE numero_dossier LIKE 'DOS-%'";
        try {
            ResultSet rs = obtenirConnexion().createStatement().executeQuery(sql);
            if (rs.next()) {
                return String.format("DOS-%03d", rs.getInt("prochain"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur generation numero dossier : " + e.getMessage());
        }
        return "DOS-" + System.currentTimeMillis();
    }

    // ── METHODE PRIVEE : construire un objet Patient depuis un ResultSet ───────
    /**
     * Lit une ligne de la base de donnees et construit l'objet Patient Java.
     * Appele a chaque ligne trouvee dans les methodes listerTous et rechercher.
     */
    private Patient construirePatient(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setId(rs.getInt("identifiant"));
        p.setNumeroDossier(rs.getString("numero_dossier"));
        p.setCin(rs.getString("cin"));
        p.setNom(rs.getString("nom_famille"));
        p.setPrenom(rs.getString("prenom"));

        // La date peut etre null si elle n'a pas ete renseignee
        Date dateNaissance = rs.getDate("date_naissance");
        if (dateNaissance != null) {
            p.setDateNaissance(dateNaissance.toLocalDate());
        }

        p.setTelephone(rs.getString("telephone"));
        p.setEmail(rs.getString("adresse_email"));
        p.setAdresse(rs.getString("adresse"));
        p.setGroupeSanguin(rs.getString("groupe_sanguin"));
        p.setAntecedents(rs.getString("antecedents"));


        return p;
    }
}
