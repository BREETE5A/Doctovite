package model;

public class Medecin {

    private int id;
    private int utilisateurId;
    private String specialite;
    private String heureDebut;
    private String heureFin;
    private String statut;

    // Informations de l'utilisateur lié
    private String cin;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String motDePasse;

    public Medecin() {}

    public String getNomComplet() {
        return "Dr. " + prenom + " " + nom;
    }

    public String getHoraires() {
        return heureDebut + " - " + heureFin;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }
    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }
    public String getHeureDebut() { return heureDebut; }
    public void setHeureDebut(String heureDebut) { this.heureDebut = heureDebut; }
    public String getHeureFin() { return heureFin; }
    public void setHeureFin(String heureFin) { this.heureFin = heureFin; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String toString() { return getNomComplet() + " (" + specialite + ")"; }
}
