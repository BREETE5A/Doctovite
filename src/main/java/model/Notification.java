package model;

public class Notification {

    private int id;
    private int destinataireId;
    private int expediteurId;
    private String contenu;
    private String type;
    private boolean lu;
    private String dateEnvoi;

    public Notification() {}

    public Notification(int destinataireId, int expediteurId, String contenu, String type) {
        this.destinataireId = destinataireId;
        this.expediteurId = expediteurId;
        this.contenu = contenu;
        this.type = type;
        this.lu = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getDestinataireId() { return destinataireId; }
    public void setDestinataireId(int destinataireId) { this.destinataireId = destinataireId; }
    public int getExpediteurId() { return expediteurId; }
    public void setExpediteurId(int expediteurId) { this.expediteurId = expediteurId; }
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isLu() { return lu; }
    public void setLu(boolean lu) { this.lu = lu; }
    public String getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(String dateEnvoi) { this.dateEnvoi = dateEnvoi; }
}
