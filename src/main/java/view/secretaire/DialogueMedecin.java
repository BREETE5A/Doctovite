package view.secretaire;

import controller.GestionMedecin;
import model.Medecin;
import view.Composants;
import view.Couleurs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DialogueMedecin extends JDialog {

    private boolean modifie = false;
    private Medecin medecin;
    private GestionMedecin gestion;
    private JTextField txtCin, txtNom, txtPrenom, txtEmail, txtTel, txtDebut, txtFin;
    private JComboBox<String> cmbSpec;
    private JPasswordField txtNouMdp;

    private static final String[] SPECIALITES = {
        "Cardiologie","Dermatologie","Pediatrie","Neurologie","Ophtalmologie",
        "Gynecologie-Obstetrique","Orthopédie","Psychiatrie","Endocrinologie",
        "Gastro-enterologie","Pneumologie","Urologie","Rhumatologie","Nephrologie",
        "Oncologie","Chirurgie generale","Medecine interne","Radiologie","ORL",
        "Medecine d urgence","Stomatologie","Hematologie","Allergologie","Geriatrie"
    };

    public DialogueMedecin(Frame parent, Medecin medecin, GestionMedecin gestion) {
        super(parent, medecin == null ? "Nouveau Medecin" : "Modifier Medecin", true);
        this.medecin = medecin;
        this.gestion = gestion;
        setSize(660, medecin == null ? 480 : 580);
        setLocationRelativeTo(parent);
        construire();
        if (medecin != null) remplir();
    }

    private void construire() {
        setLayout(new BorderLayout());

        JPanel hdr = Composants.panneauDegrade(Couleurs.VERT_BTN, new Color(0x1E, 0x7E, 0x34));
        hdr.setLayout(new BorderLayout());
        hdr.setBorder(new EmptyBorder(14, 18, 14, 18));
        JLabel t = new JLabel(medecin == null ? "Nouveau Medecin" : "Modifier Medecin");
        t.setFont(Couleurs.SOUS_TITRE);
        t.setForeground(Color.WHITE);
        hdr.add(t);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 26, 14, 26));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(9, 7, 9, 7);
        g.fill = GridBagConstraints.HORIZONTAL;

        txtCin    = champ("Ex: MED021");
        txtNom    = champ("Nom");
        txtPrenom = champ("Prenom");
        txtEmail  = champ("email@clinique.ma");
        txtTel    = champ("0661001099");
        txtDebut  = champ("Ex: 08:00");
        txtFin    = champ("Ex: 17:00");
        cmbSpec   = new JComboBox<>(SPECIALITES);
        cmbSpec.setFont(Couleurs.CORPS);
        cmbSpec.setPreferredSize(new Dimension(0, 40));

        if (medecin != null) {
            txtCin.setEditable(false);
            txtCin.setBackground(new Color(0xF5, 0xF5, 0xF5));
        }

        int r = 0;
        ligne2(form, g, r++, "CIN *", txtCin, "Nom *", txtNom);
        ligne2(form, g, r++, "Prenom *", txtPrenom, "Email", txtEmail);
        ligne2(form, g, r++, "Telephone", txtTel, "Specialite *", cmbSpec);
        ligne2(form, g, r++, "Heure debut", txtDebut, "Heure fin", txtFin);

        if (medecin == null) {
            JPasswordField txtMdp = new JPasswordField();
            txtMdp.setFont(Couleurs.CORPS);
            txtstyler(txtMdp);
            g.gridy=r; g.gridx=0; g.weightx=0; form.add(Composants.etiquette("Mot de passe *"), g);
            g.gridx=1; g.weightx=1; form.add(txtMdp, g);
            JPanel pied = creerPied();
            JButton sauv = getSauvButton(pied);
            sauv.addActionListener(e -> sauvegarderCreation(new String(txtMdp.getPassword())));
            add(hdr, BorderLayout.NORTH);
            add(new JScrollPane(form) {{ setBorder(null); getViewport().setBackground(Color.WHITE); }}, BorderLayout.CENTER);
            add(pied, BorderLayout.SOUTH);
            return;
        }

        // Section mot de passe pour modification
        JSeparator sep = new JSeparator();
        sep.setForeground(Couleurs.BORDURE);
        g.gridy=r; g.gridx=0; g.gridwidth=4; g.weightx=1;
        form.add(sep, g);
        g.gridwidth=1;

        JLabel titreMdp = new JLabel("Modifier le mot de passe (laisser vide pour ne pas changer)");
        titreMdp.setFont(Couleurs.GRAS);
        titreMdp.setForeground(Couleurs.TEXTE_GRIS);
        g.gridy=++r; g.gridx=0; g.gridwidth=4;
        form.add(titreMdp, g);
        g.gridwidth=1;

        txtNouMdp = new JPasswordField(); txtstyler(txtNouMdp);
        g.gridy=++r; g.gridx=0; g.weightx=0; form.add(Composants.etiquette("Nouveau mot de passe :"), g);
        g.gridx=1; g.weightx=1; form.add(txtNouMdp, g);
        // Note explicative
        JLabel note = new JLabel("Laisser vide pour ne pas modifier le mot de passe");
        note.setFont(Couleurs.PETIT);
        note.setForeground(Couleurs.TEXTE_PALE);
        g.gridx=2; g.gridwidth=2; g.weightx=1; form.add(note, g);
        g.gridwidth=1;

        JPanel pied = creerPied();
        getSauvButton(pied).addActionListener(e -> sauvegarderModification());

        add(hdr, BorderLayout.NORTH);
        add(new JScrollPane(form) {{ setBorder(null); getViewport().setBackground(Color.WHITE); }}, BorderLayout.CENTER);
        add(pied, BorderLayout.SOUTH);
    }

    private JPanel creerPied() {
        JPanel pied = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pied.setBackground(Couleurs.FOND_PAGE);
        JButton ann = Composants.boutonGris("Annuler");
        ann.addActionListener(e -> dispose());
        JButton sauv = Composants.boutonSucces(medecin == null ? "Enregistrer" : "Mettre a jour");
        pied.add(ann);
        pied.add(sauv);
        return pied;
    }

    private JButton getSauvButton(JPanel pied) {
        return (JButton) pied.getComponent(pied.getComponentCount() - 1);
    }

    private void remplir() {
        txtCin.setText(medecin.getCin());
        txtNom.setText(medecin.getNom());
        txtPrenom.setText(medecin.getPrenom());
        if (medecin.getEmail() != null) txtEmail.setText(medecin.getEmail());
        if (medecin.getTelephone() != null) txtTel.setText(medecin.getTelephone());
        cmbSpec.setSelectedItem(medecin.getSpecialite());
        txtDebut.setText(medecin.getHeureDebut());
        txtFin.setText(medecin.getHeureFin());
    }

    private void sauvegarderCreation(String mdp) {
        if (txtCin.getText().trim().isEmpty() || txtNom.getText().trim().isEmpty() || mdp.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Champs obligatoires (*) manquants.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Medecin m = new Medecin();
        m.setCin(txtCin.getText().trim());
        m.setNom(txtNom.getText().trim());
        m.setPrenom(txtPrenom.getText().trim());
        m.setMotDePasse(mdp);
        m.setEmail(txtEmail.getText().trim());
        m.setTelephone(txtTel.getText().trim());
        m.setSpecialite((String) cmbSpec.getSelectedItem());
        m.setHeureDebut(txtDebut.getText().trim());
        m.setHeureFin(txtFin.getText().trim());

        // On utilise la nouvelle methode avec code de retour precis
        int code = gestion.ajouterAvecCode(m);

        if (code == GestionMedecin.AJOUT_OK) {
            JOptionPane.showMessageDialog(this, "Medecin enregistre avec succes !");
            modifie = true;
            dispose();
        } else if (code == GestionMedecin.AJOUT_CIN_DUPLIQUE) {
            JOptionPane.showMessageDialog(this,
                "Le CIN \"" + m.getCin() + "\" est deja utilise par un autre compte actif.",
                "CIN deja utilise", JOptionPane.ERROR_MESSAGE);
        } else if (code == GestionMedecin.AJOUT_CIN_DESACTIVE) {
            // Cas particulier : un compte avec ce CIN existe mais a ete desactive.
            // On propose de le reactiver au lieu de creer un doublon.
            int rep = JOptionPane.showConfirmDialog(this,
                "Un ancien compte avec le CIN \"" + m.getCin() + "\" existe mais a ete desactive.\n" +
                "Voulez-vous le reactiver avec ces nouvelles informations ?",
                "Compte existant desactive", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (rep == JOptionPane.YES_OPTION) {
                if (gestion.reactiverEtMettreAJour(m)) {
                    JOptionPane.showMessageDialog(this, "Compte reactive avec succes !");
                    modifie = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Erreur lors de la reactivation.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                "Erreur de base de donnees lors de l'enregistrement.\n" +
                "Verifiez les champs et reessayez.",
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sauvegarderModification() {
        if (txtNom.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Champs obligatoires manquants.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String ancMdp = "";
        String nouMdp = txtNouMdp != null ? new String(txtNouMdp.getPassword()).trim() : "";
        if (!nouMdp.isEmpty()) {
            if (nouMdp.length() < 4) {
                JOptionPane.showMessageDialog(this, "Le mot de passe doit avoir au moins 4 caracteres.", "Erreur", JOptionPane.WARNING_MESSAGE);
                return;
            }
            new controller.GestionUtilisateur().changerMotDePasse(medecin.getUtilisateurId(), nouMdp);
            medecin.setMotDePasse(nouMdp);
        }
        medecin.setNom(txtNom.getText().trim());
        medecin.setPrenom(txtPrenom.getText().trim());
        medecin.setEmail(txtEmail.getText().trim());
        medecin.setTelephone(txtTel.getText().trim());
        medecin.setSpecialite((String) cmbSpec.getSelectedItem());
        medecin.setHeureDebut(txtDebut.getText().trim());
        medecin.setHeureFin(txtFin.getText().trim());
        if (gestion.modifier(medecin)) {
            JOptionPane.showMessageDialog(this, "Medecin mis a jour !");
            modifie = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Erreur lors de la mise a jour.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JTextField champ(String h) {
        JTextField tf = Composants.champTexte(h);
        tf.setPreferredSize(new Dimension(180, 40));
        return tf;
    }

    private void txtstyler(JPasswordField pf) {
        pf.setFont(Couleurs.CORPS);
        pf.setForeground(Couleurs.TEXTE_NOIR);
        pf.setBackground(Color.WHITE);
        pf.setPreferredSize(new Dimension(180, 40));
        pf.setBorder(BorderFactory.createCompoundBorder(
            new Composants.BordureArrondie(Couleurs.BORDURE, 1, 8),
            new EmptyBorder(8, 12, 8, 12)));
    }

    private void ligne2(JPanel p, GridBagConstraints g, int row, String l1, Component c1, String l2, Component c2) {
        g.gridy=row; g.gridx=0; g.weightx=0; p.add(Composants.etiquette(l1), g);
        g.gridx=1; g.weightx=1; p.add(c1, g);
        g.gridx=2; g.weightx=0; p.add(Composants.etiquette(l2), g);
        g.gridx=3; g.weightx=1; p.add(c2, g);
    }

    public boolean estModifie() { return modifie; }
}
