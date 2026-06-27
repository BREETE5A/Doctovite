package model;

public class Facture {

    private int id;
    private int patientId;
    private String numeroFacture;
    private double montantTotal;
    private double montantPaye;
    private String modePaiement;
    private String statut;
    private String description;
    private String dateCree;

    // Affichage
    private String nomPatient;
    private String numeroDossier;

    public Facture() {}

    public double getMontantRestant() {
        return montantTotal - montantPaye;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public String getNumeroFacture() { return numeroFacture; }
    public void setNumeroFacture(String numeroFacture) { this.numeroFacture = numeroFacture; }
    public double getMontantTotal() { return montantTotal; }
    public void setMontantTotal(double montantTotal) { this.montantTotal = montantTotal; }
    public double getMontantPaye() { return montantPaye; }
    public void setMontantPaye(double montantPaye) { this.montantPaye = montantPaye; }
    public String getModePaiement() { return modePaiement; }
    public void setModePaiement(String modePaiement) { this.modePaiement = modePaiement; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDateCree() { return dateCree; }
    public void setDateCree(String dateCree) { this.dateCree = dateCree; }
    public String getNomPatient() { return nomPatient; }
    public void setNomPatient(String nomPatient) { this.nomPatient = nomPatient; }
    public String getNumeroDossier() { return numeroDossier; }
    public void setNumeroDossier(String numeroDossier) { this.numeroDossier = numeroDossier; }
}
