package view.medecin;

import controller.*;
import model.*;
import view.Composants;
import view.Couleurs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DialogueRendezVous extends JDialog {

    private boolean modifie = false;
    private RendezVous rdv;
    private Utilisateur utilisateurActif;   // utilisateur connecte qui fait l'action
    private JComboBox<Patient> cmbPatient;
    private JComboBox<Medecin> cmbMedecin;
    private JSpinner spinnerDate;
    private JSpinner spinnerHeure;
    private JTextField txtMotif;
    private JComboBox<String> cmbStatut;

    // Ancien constructeur conserve pour ne pas casser le code existant
    public DialogueRendezVous(Frame parent, RendezVous rdv) {
        this(parent, rdv, null);
    }

    // Nouveau constructeur qui accepte l'utilisateur connecte
    public DialogueRendezVous(Frame parent, RendezVous rdv, Utilisateur utilisateurActif) {
        super(parent, rdv == null ? "Nouveau Rendez-vous" : "Modifier Rendez-vous", true);
        this.rdv = rdv;
        this.utilisateurActif = utilisateurActif;
        setSize(560, 430);
        setLocationRelativeTo(parent);
        construire();
        if (rdv != null) remplir();
    }

    private void construire() {
        setLayout(new BorderLayout());

        JPanel hdr = Composants.panneauDegrade(Couleurs.VERT_FONCE, Couleurs.VERT_MED);
        hdr.setLayout(new BorderLayout());
        hdr.setBorder(new EmptyBorder(14, 18, 14, 18));
        JLabel t = new JLabel(rdv == null ? "Nouveau Rendez-vous" : "Modifier Rendez-vous");
        t.setFont(Couleurs.SOUS_TITRE);
        t.setForeground(Color.WHITE);
        hdr.add(t);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 26, 14, 26));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(9, 7, 9, 7);
        g.fill = GridBagConstraints.HORIZONTAL;

        List<Patient> patients = new GestionPatient().listerTous();
        List<Medecin> medecins = new GestionMedecin().listerTous();

        cmbPatient = new JComboBox<>(patients.toArray(new Patient[0]));
        cmbPatient.setFont(Couleurs.CORPS);
        cmbPatient.setPreferredSize(new Dimension(0, 40));

        cmbMedecin = new JComboBox<>(medecins.toArray(new Medecin[0]));
        cmbMedecin.setFont(Couleurs.CORPS);
        cmbMedecin.setPreferredSize(new Dimension(0, 40));

        SpinnerDateModel modelDate = new SpinnerDateModel();
        spinnerDate = new JSpinner(modelDate);
        JSpinner.DateEditor editorDate = new JSpinner.DateEditor(spinnerDate, "dd/MM/yyyy");
        spinnerDate.setEditor(editorDate);
        spinnerDate.setFont(Couleurs.CORPS);
        spinnerDate.setPreferredSize(new Dimension(0, 40));

        SpinnerDateModel modelHeure = new SpinnerDateModel();
        spinnerHeure = new JSpinner(modelHeure);
        JSpinner.DateEditor editorHeure = new JSpinner.DateEditor(spinnerHeure, "HH:mm");
        spinnerHeure.setEditor(editorHeure);
        spinnerHeure.setFont(Couleurs.CORPS);
        spinnerHeure.setPreferredSize(new Dimension(0, 40));

        txtMotif = Composants.champTexte("Motif de la consultation");
        txtMotif.setPreferredSize(new Dimension(0, 40));

        cmbStatut = new JComboBox<>(new String[]{"PLANIFIE", "CONFIRME", "ANNULE", "TERMINE", "URGENCE"});
        cmbStatut.setFont(Couleurs.CORPS);
        cmbStatut.setPreferredSize(new Dimension(0, 40));

        g.gridy=0; g.gridx=0; g.weightx=0; form.add(Composants.etiquette("Patient *"), g);
        g.gridx=1; g.weightx=1; form.add(cmbPatient, g);

        g.gridy=1; g.gridx=0; g.weightx=0; form.add(Composants.etiquette("Medecin *"), g);
        g.gridx=1; g.weightx=1; form.add(cmbMedecin, g);

        g.gridy=2; g.gridx=0; g.weightx=0; form.add(Composants.etiquette("Date *"), g);
        g.gridx=1; g.weightx=1; form.add(spinnerDate, g);

        g.gridy=3; g.gridx=0; g.weightx=0; form.add(Composants.etiquette("Heure *"), g);
        g.gridx=1; g.weightx=1; form.add(spinnerHeure, g);

        g.gridy=4; g.gridx=0; g.weightx=0; form.add(Composants.etiquette("Motif"), g);
        g.gridx=1; g.weightx=1; form.add(txtMotif, g);

        g.gridy=5; g.gridx=0; g.weightx=0; form.add(Composants.etiquette("Statut"), g);
        g.gridx=1; g.weightx=1; form.add(cmbStatut, g);

        JPanel pied = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pied.setBackground(Couleurs.FOND_PAGE);
        JButton ann  = Composants.boutonGris("Annuler");
        JButton sauv = Composants.boutonSucces(rdv == null ? "Enregistrer" : "Mettre a jour");
        ann.addActionListener(e -> dispose());
        sauv.addActionListener(e -> sauvegarder());
        pied.add(ann);
        pied.add(sauv);

        add(hdr, BorderLayout.NORTH);
        add(new JScrollPane(form) {{ setBorder(null); getViewport().setBackground(Color.WHITE); }}, BorderLayout.CENTER);
        add(pied, BorderLayout.SOUTH);
    }

    private void remplir() {
        try {
            SimpleDateFormat sdfDate  = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdfHeure = new SimpleDateFormat("HH:mm");
            if (rdv.getDateRdv() != null)  spinnerDate.setValue(sdfDate.parse(rdv.getDateRdv()));
            if (rdv.getHeureRdv() != null) spinnerHeure.setValue(sdfHeure.parse(rdv.getHeureRdv()));
        } catch (Exception e) {
            System.out.println("Erreur remplissage date : " + e.getMessage());
        }
        txtMotif.setText(rdv.getMotif() != null ? rdv.getMotif() : "");
        cmbStatut.setSelectedItem(rdv.getStatut());
    }

    private void sauvegarder() {
        boolean creation = (rdv == null);
        RendezVous r = creation ? new RendezVous() : rdv;

        r.setPatientId(((Patient) cmbPatient.getSelectedItem()).getId());
        r.setMedecinId(((Medecin) cmbMedecin.getSelectedItem()).getId());

        Date dateVal  = (Date) spinnerDate.getValue();
        Date heureVal = (Date) spinnerHeure.getValue();
        r.setDateRdv(new SimpleDateFormat("yyyy-MM-dd").format(dateVal));
        r.setHeureRdv(new SimpleDateFormat("HH:mm").format(heureVal));
        r.setMotif(txtMotif.getText().trim());
        r.setStatut((String) cmbStatut.getSelectedItem());

        // On complete les noms patient et medecin pour pouvoir les utiliser
        // dans le message de notification (ils ne sont pas remplis a la creation).
        Patient p = (Patient) cmbPatient.getSelectedItem();
        Medecin m = (Medecin) cmbMedecin.getSelectedItem();
        if (p != null) r.setNomPatient(p.getPrenom() + " " + p.getNom());
        if (m != null) r.setNomMedecin("Dr. " + m.getPrenom() + " " + m.getNom());

        GestionRendezVous gestion = new GestionRendezVous();
        boolean ok = creation ? gestion.ajouter(r) : gestion.modifier(r);
        if (ok) {
            // Envoi des notifications automatiques
            if (utilisateurActif != null) {
                ServiceNotificationRdv service = new ServiceNotificationRdv();
                if (creation) {
                    service.notifierCreation(r, utilisateurActif);
                } else {
                    service.notifierModification(r, utilisateurActif);
                }
            }
            JOptionPane.showMessageDialog(this, "Rendez-vous enregistre !");
            modifie = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean estModifie() { return modifie; }

    // Permet de pre-selectionner un patient dans le combo (appele depuis DialoguePatient)
    public void preSelectionnerPatient(int patientId) {
        if (cmbPatient == null) return;
        for (int i = 0; i < cmbPatient.getItemCount(); i++) {
            if (cmbPatient.getItemAt(i).getId() == patientId) {
                cmbPatient.setSelectedIndex(i);
                break;
            }
        }
    }
}
