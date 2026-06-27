package model;

public class RendezVous {

    private int id;
    private int patientId;
    private int medecinId;
    private String dateRdv;
    private String heureRdv;
    private String motif;
    private String statut;
    private String notes;

    // Infos affichage
    private String nomPatient;
    private String numeroDossierPatient;
    private String cinPatient;
    private int agePatient;
    private String nomMedecin;
    private String specialiteMedecin;

    public RendezVous() {}

    public String getStatutAffichage() {
        if (statut == null) return "-";
        switch (statut) {
            case "PLANIFIE":  return "Planifie";
            case "CONFIRME":  return "Confirme";
            case "ANNULE":    return "Annule";
            case "TERMINE":   return "Termine";
            case "URGENCE":   return "Urgence";
            default:          return statut;
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public int getMedecinId() { return medecinId; }
    public void setMedecinId(int medecinId) { this.medecinId = medecinId; }
    public String getDateRdv() { return dateRdv; }
    public void setDateRdv(String dateRdv) { this.dateRdv = dateRdv; }
    public String getHeureRdv() { return heureRdv; }
    public void setHeureRdv(String heureRdv) { this.heureRdv = heureRdv; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getNomPatient() { return nomPatient; }
    public void setNomPatient(String nomPatient) { this.nomPatient = nomPatient; }
    public String getNumeroDossierPatient() { return numeroDossierPatient; }
    public void setNumeroDossierPatient(String n) { this.numeroDossierPatient = n; }
    public String getCinPatient() { return cinPatient; }
    public void setCinPatient(String cinPatient) { this.cinPatient = cinPatient; }
    public int getAgePatient() { return agePatient; }
    public void setAgePatient(int agePatient) { this.agePatient = agePatient; }
    public String getNomMedecin() { return nomMedecin; }
    public void setNomMedecin(String nomMedecin) { this.nomMedecin = nomMedecin; }
    public String getSpecialiteMedecin() { return specialiteMedecin; }
    public void setSpecialiteMedecin(String specialiteMedecin) { this.specialiteMedecin = specialiteMedecin; }
}
