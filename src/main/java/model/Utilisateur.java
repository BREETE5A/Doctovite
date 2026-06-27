package model;

public class Utilisateur {

    private int id;
    private String cin;
    private String nom;
    private String prenom;
    private String motDePasse;
    private String role;
    private String email;
    private String telephone;

    public Utilisateur() {}

    public Utilisateur(int id, String cin, String nom, String prenom,
                       String motDePasse, String role, String email, String telephone) {
        this.id = id;
        this.cin = cin;
        this.nom = nom;
        this.prenom = prenom;
        this.motDePasse = motDePasse;
        this.role = role;
        this.email = email;
        this.telephone = telephone;
    }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
}
