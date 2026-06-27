package view.admin;

import controller.GestionMedecin;
import controller.GestionPatient;
import controller.GestionRendezVous;
import model.Medecin;
import model.Patient;
import model.Utilisateur;
import view.Composants;
import view.Couleurs;
import view.medecin.DialogueRendezVous;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

public class DialoguePatient extends JDialog {

    private boolean modifie = false;
    private Patient patient;
    private GestionPatient gestion;
    private Utilisateur utilisateurActif;   // pour les notifications RDV

    private JTextField txtCin, txtNom, txtPrenom, txtTel, txtEmail, txtAdresse;
    private JSpinner spinnerNaissance;
    private JComboBox<String> cmbGroupe;

    // Constructeur original conserve
    public DialoguePatient(Frame parent, Patient patient, GestionPatient gestion) {
        this(parent, patient, gestion, null);
    }

    // Nouveau constructeur avec utilisateur actif
    public DialoguePatient(Frame parent, Patient patient, GestionPatient gestion, Utilisateur utilisateurActif) {
        super(parent, patient == null ? "Nouveau Patient" : "Modifier Patient", true);
        this.patient = patient;
        this.gestion = gestion;
        this.utilisateurActif = utilisateurActif;
        setSize(620, patient != null ? 480 : 440);
        setLocationRelativeTo(parent);
        construire();
        if (patient != null) remplir();
    }

    private void construire() {
        setLayout(new BorderLayout());

        JPanel hdr = Composants.panneauDegrade(Couleurs.VERT_FONCE, Couleurs.VERT_MED);
        hdr.setLayout(new BorderLayout());
        hdr.setBorder(new EmptyBorder(14, 18, 14, 18));
        JLabel t = new JLabel(patient == null ? "Nouveau Patient" : "Modifier Patient");
        t.setFont(Couleurs.SOUS_TITRE);
        t.setForeground(Color.WHITE);
        hdr.add(t);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 26, 14, 26));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(9, 7, 9, 7);
        g.fill = GridBagConstraints.HORIZONTAL;

        txtCin    = champ("Ex: AB1234");
        txtNom    = champ("Nom de famille");
        txtPrenom = champ("Prenom");
        txtTel    = champ("Ex: 0661001001");
        txtEmail  = champ("email@exemple.com");
        txtAdresse= champ("Adresse complete");
        cmbGroupe = new JComboBox<>(new String[]{"", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        cmbGroupe.setFont(Couleurs.CORPS);
        cmbGroupe.setPreferredSize(new Dimension(0, 40));

        SpinnerDateModel modelNaissance = new SpinnerDateModel();
        spinnerNaissance = new JSpinner(modelNaissance);
        JSpinner.DateEditor editorNaissance = new JSpinner.DateEditor(spinnerNaissance, "dd/MM/yyyy");
        spinnerNaissance.setEditor(editorNaissance);
        spinnerNaissance.setPreferredSize(new Dimension(0, 40));
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.YEAR, -30);
        spinnerNaissance.setValue(cal.getTime());

        if (patient != null) {
            txtCin.setEditable(false);
            txtCin.setBackground(new Color(0xF5, 0xF5, 0xF5));
        }

        int r = 0;
        ligne2(form, g, r++, "CIN *", txtCin, "Nom *", txtNom);
        ligne2(form, g, r++, "Prenom *", txtPrenom, "Date naissance", spinnerNaissance);
        ligne2(form, g, r++, "Telephone", txtTel, "Email", txtEmail);
        ligne2(form, g, r++, "Adresse", txtAdresse, "Groupe sanguin", cmbGroupe);

        JPanel pied = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pied.setBackground(Couleurs.FOND_PAGE);
        JButton ann  = Composants.boutonGris("Annuler");
        JButton sauv = Composants.boutonSucces(patient == null ? "Enregistrer" : "Mettre a jour");
        ann.addActionListener(e -> dispose());
        sauv.addActionListener(e -> sauvegarder());
        pied.add(ann);

        // Bouton "Nouveau RDV" seulement si on modifie un patient existant
        if (patient != null) {
            JButton btnRdv = Composants.boutonPrincipal("+ Nouveau RDV");
            btnRdv.addActionListener(e -> ouvrirNouveauRdv());
            pied.add(btnRdv);
        }
        pied.add(sauv);

        add(hdr, BorderLayout.NORTH);
        add(new JScrollPane(form) {{ setBorder(null); getViewport().setBackground(Color.WHITE); }}, BorderLayout.CENTER);
        add(pied, BorderLayout.SOUTH);
    }

    private void remplir() {
        txtCin.setText(patient.getCin());
        txtNom.setText(patient.getNom());
        txtPrenom.setText(patient.getPrenom());
        if (patient.getDateNaissance() != null) {
            try {
                Date d = new SimpleDateFormat("yyyy-MM-dd").parse(patient.getDateNaissance().toString());
                spinnerNaissance.setValue(d);
            } catch (Exception e) { System.out.println("Erreur date : " + e.getMessage()); }
        }
        if (patient.getTelephone() != null) txtTel.setText(patient.getTelephone());
        if (patient.getEmail() != null) txtEmail.setText(patient.getEmail());
        if (patient.getAdresse() != null) txtAdresse.setText(patient.getAdresse());
        if (patient.getGroupeSanguin() != null) cmbGroupe.setSelectedItem(patient.getGroupeSanguin());
    }

    private void sauvegarder() {
        if (txtCin.getText().trim().isEmpty() || txtNom.getText().trim().isEmpty() || txtPrenom.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "CIN, Nom et Prenom sont obligatoires.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Patient p = patient != null ? patient : new Patient();
        p.setCin(txtCin.getText().trim());
        p.setNom(txtNom.getText().trim());
        p.setPrenom(txtPrenom.getText().trim());
        Date dateVal = (Date) spinnerNaissance.getValue();
        p.setDateNaissance(LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(dateVal)));
        p.setTelephone(txtTel.getText().trim());
        p.setEmail(txtEmail.getText().trim());
        p.setAdresse(txtAdresse.getText().trim());
        p.setGroupeSanguin((String) cmbGroupe.getSelectedItem());

        if (patient == null) {
            // Creation : utiliser le code de retour precis
            int code = gestion.ajouterAvecCode(p);
            if (code == GestionPatient.AJOUT_OK) {
                JOptionPane.showMessageDialog(this, "Patient enregistre avec succes !");
                modifie = true;
                dispose();
            } else if (code == GestionPatient.AJOUT_CIN_DUPLIQUE) {
                JOptionPane.showMessageDialog(this,
                    "Le CIN \"" + p.getCin() + "\" est deja utilise par un autre patient.",
                    "CIN deja utilise", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Erreur BD : " + gestion.getDerniereErreur() + "\n\nSi le message est vide, verifiez que MySQL est demarre.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // Modification
            if (gestion.modifier(p)) {
                JOptionPane.showMessageDialog(this, "Patient mis a jour !");
                modifie = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Erreur lors de la mise a jour.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Ouvre le formulaire de nouveau rendez-vous pre-rempli avec ce patient
    private void ouvrirNouveauRdv() {
        dispose(); // fermer d'abord ce dialogue
        model.RendezVous rdvPreRempli = new model.RendezVous();
        rdvPreRempli.setPatientId(patient.getId());
        DialogueRendezVous dlgRdv = new DialogueRendezVous(
            (Frame) getOwner(), null, utilisateurActif
        );
        // Pre-selectionner le patient dans le combo
        dlgRdv.preSelectionnerPatient(patient.getId());
        dlgRdv.setVisible(true);
        modifie = dlgRdv.estModifie();
    }

    private JTextField champ(String hint) {
        JTextField tf = Composants.champTexte(hint);
        tf.setPreferredSize(new Dimension(180, 40));
        return tf;
    }

    private void ligne2(JPanel p, GridBagConstraints g, int row, String l1, Component c1, String l2, Component c2) {
        g.gridy=row; g.gridx=0; g.weightx=0; p.add(Composants.etiquette(l1), g);
        g.gridx=1; g.weightx=1; p.add(c1, g);
        g.gridx=2; g.weightx=0; p.add(Composants.etiquette(l2), g);
        g.gridx=3; g.weightx=1; p.add(c2, g);
    }

    public boolean estModifie() { return modifie; }
}
