package view.admin;

import controller.GestionMedecin;
import controller.GestionUtilisateur;
import model.Medecin;
import model.Utilisateur;
import view.Composants;
import view.Couleurs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class PageComptes extends JPanel {

    private Utilisateur utilisateur;
    private GestionUtilisateur gestion = new GestionUtilisateur();
    private DefaultTableModel modele;
    private JTable tableau;
    private List<Utilisateur> users;

    public PageComptes(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        setLayout(new BorderLayout(0, 14));
        setBackground(Couleurs.FOND_PAGE);
        setBorder(new EmptyBorder(18, 18, 18, 18));
        construire();
        charger();
    }

    public void rafraichir() {
        charger();
    }

    private void construire() {
        JPanel entete = new JPanel(new BorderLayout());
        entete.setOpaque(false);
        entete.setBorder(new EmptyBorder(0, 0, 12, 0));
        entete.add(Composants.titre("Gestion des Comptes Utilisateurs"), BorderLayout.WEST);

        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        boutons.setOpaque(false);

        JButton btnAdmin = Composants.boutonGris("+ Admin");
        JButton btnSec   = Composants.boutonPrincipal("+ Secretaire");
        JButton btnMed   = Composants.boutonSucces("+ Medecin");

        btnAdmin.addActionListener(e -> ouvrirNouveauCompte("ADMINISTRATEUR"));
        btnSec.addActionListener(e -> ouvrirNouveauCompte("SECRETAIRE"));
        btnMed.addActionListener(e -> ouvrirNouveauMedecin());

        boutons.add(btnAdmin);
        boutons.add(btnSec);
        boutons.add(btnMed);
        entete.add(boutons, BorderLayout.EAST);

        String[] cols = {"ID", "CIN", "Nom", "Prenom", "Role", "Email", "Telephone"};
        modele = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableau = new JTable(modele);
        Composants.styliserTableau(tableau);
        tableau.getColumnModel().getColumn(0).setMaxWidth(50);

        // Double-clic pour modifier
        tableau.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int sel = tableau.getSelectedRow();
                    if (sel >= 0) ouvrirModification(users.get(sel));
                }
            }
        });

        JPanel carte = new JPanel(new BorderLayout());
        carte.setBackground(Color.WHITE);
        carte.setBorder(new Composants.BordureArrondie(Couleurs.BORDURE, 1, 12));
        carte.add(Composants.enveloperTableau(tableau), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        actions.setOpaque(false);

        JButton btnModif = Composants.boutonPrincipal("Modifier");
        JButton btnDes   = Composants.boutonDanger("Desactiver");

        btnModif.addActionListener(e -> {
            int sel = tableau.getSelectedRow();
            if (sel < 0) { msg("Selectionnez un compte."); return; }
            ouvrirModification(users.get(sel));
        });

        btnDes.addActionListener(e -> {
            int sel = tableau.getSelectedRow();
            if (sel < 0) { msg("Selectionnez un compte."); return; }
            Utilisateur sel2 = users.get(sel);
            if (sel2.getId() == utilisateur.getId()) {
                msg("Impossible de desactiver votre propre compte.");
                return;
            }
            int rep = JOptionPane.showConfirmDialog(this,
                "Desactiver le compte de " + sel2.getNomComplet() + " ?",
                "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (rep == JOptionPane.YES_OPTION) {
                gestion.desactiver(sel2.getId());
                charger();
            }
        });

        JLabel aide = new JLabel("  Double-clic pour modifier");
        aide.setFont(Couleurs.PETIT);
        aide.setForeground(Couleurs.TEXTE_PALE);

        actions.add(btnModif);
        actions.add(btnDes);
        actions.add(aide);

        add(entete, BorderLayout.NORTH);
        add(carte, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
    }

    private void charger() {
        if (modele == null) return;
        modele.setRowCount(0);
        users = gestion.listerTous();
        for (Utilisateur u : users) {
            String roleLib;
            switch (u.getRole()) {
                case "ADMINISTRATEUR": roleLib = "Admin"; break;
                case "MEDECIN":        roleLib = "Medecin"; break;
                default:               roleLib = "Secretaire";
            }
            modele.addRow(new Object[]{
                u.getId(), u.getCin(), u.getNom(), u.getPrenom(), roleLib,
                u.getEmail() != null ? u.getEmail() : "-",
                u.getTelephone() != null ? u.getTelephone() : "-"
            });
        }
    }

    // ── OUVERTURE DU FORMULAIRE DE MODIFICATION ───────────────────────────────
    private void ouvrirModification(Utilisateur u) {
        switch (u.getRole()) {
            case "MEDECIN":
                // Ouvrir DialogueMedecin en mode modification
                Medecin med = new GestionMedecin().trouverParUtilisateurId(u.getId());
                if (med == null) {
                    msg("Profil medecin introuvable pour ce compte.");
                    return;
                }
                DialogueMedecin dlgMed = new DialogueMedecin(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    med, new GestionMedecin()
                );
                dlgMed.setVisible(true);
                if (dlgMed.estModifie()) charger();
                break;

            case "SECRETAIRE":
            case "ADMINISTRATEUR":
                // Ouvrir formulaire de modification inline
                ouvrirFormulaireModifCompte(u);
                break;
        }
    }

    // ── FORMULAIRE MODIFICATION SECRETAIRE / ADMIN ────────────────────────────
    private void ouvrirFormulaireModifCompte(Utilisateur u) {
        boolean estAdmin = "ADMINISTRATEUR".equals(u.getRole());
        String titre = estAdmin ? "Modifier Administrateur" : "Modifier Secretaire";
        Color couleur = estAdmin ? Couleurs.VIOLET : Couleurs.VERT_MED;
        Color couleurFonce = estAdmin ? new Color(0x55, 0x2F, 0xA0) : Couleurs.VERT_FONCE;

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), titre, true);
        dlg.setSize(560, 420);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel hdr = Composants.panneauDegrade(couleur, couleurFonce);
        hdr.setLayout(new BorderLayout());
        hdr.setBorder(new EmptyBorder(14, 18, 14, 18));
        JLabel t = new JLabel(titre);
        t.setFont(Couleurs.SOUS_TITRE);
        t.setForeground(Color.WHITE);
        hdr.add(t);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 26, 14, 26));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(9, 7, 9, 7);
        g.fill = GridBagConstraints.HORIZONTAL;

        // Champs pre-remplis avec les valeurs actuelles
        JTextField fCin = c(); fCin.setText(u.getCin()); fCin.setEditable(false);
        fCin.setBackground(new Color(0xF5, 0xF5, 0xF5));
        JTextField fNom = c(); fNom.setText(u.getNom());
        JTextField fPrn = c(); fPrn.setText(u.getPrenom());
        JTextField fEml = c(); if (u.getEmail() != null) fEml.setText(u.getEmail());
        JTextField fTel = c(); if (u.getTelephone() != null) fTel.setText(u.getTelephone());

        // Champ nouveau mot de passe (vide = pas de changement)
        JPasswordField fPwd = new JPasswordField();
        fPwd.setFont(Couleurs.CORPS);
        fPwd.setPreferredSize(new Dimension(180, 40));
        fPwd.setBorder(BorderFactory.createCompoundBorder(
            new Composants.BordureArrondie(Couleurs.BORDURE, 1, 8),
            new EmptyBorder(8, 12, 8, 12)));

        g.gridy=0; g.gridx=0; g.weightx=0; form.add(Composants.etiquette("CIN"), g);
        g.gridx=1; g.weightx=1; form.add(fCin, g);
        g.gridx=2; g.weightx=0; form.add(Composants.etiquette("Nom *"), g);
        g.gridx=3; g.weightx=1; form.add(fNom, g);

        g.gridy=1; g.gridx=0; g.weightx=0; form.add(Composants.etiquette("Prenom *"), g);
        g.gridx=1; g.weightx=1; form.add(fPrn, g);
        g.gridx=2; g.weightx=0; form.add(Composants.etiquette("Nouveau mot de passe"), g);
        g.gridx=3; g.weightx=1; form.add(fPwd, g);

        g.gridy=2; g.gridx=0; g.weightx=0; form.add(Composants.etiquette("Email"), g);
        g.gridx=1; g.weightx=1; form.add(fEml, g);
        g.gridx=2; g.weightx=0; form.add(Composants.etiquette("Telephone"), g);
        g.gridx=3; g.weightx=1; form.add(fTel, g);

        // Note sur le mot de passe
        JLabel noteMdp = new JLabel("Laisser vide pour ne pas modifier le mot de passe");
        noteMdp.setFont(Couleurs.PETIT);
        noteMdp.setForeground(Couleurs.TEXTE_PALE);
        g.gridy=3; g.gridx=0; g.gridwidth=4; g.weightx=1;
        form.add(noteMdp, g);
        g.gridwidth=1;

        JPanel pied = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pied.setBackground(Couleurs.FOND_PAGE);
        JButton ann  = Composants.boutonGris("Annuler");
        JButton sauv = Composants.boutonSucces("Enregistrer");
        ann.addActionListener(e -> dlg.dispose());
        sauv.addActionListener(e -> {
            String nom = fNom.getText().trim();
            if (nom.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Le nom est obligatoire.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Mettre a jour les infos de base
            u.setNom(nom);
            u.setPrenom(fPrn.getText().trim());
            u.setEmail(fEml.getText().trim());
            u.setTelephone(fTel.getText().trim());
            gestion.modifier(u);

            // Changer le mot de passe si fourni
            String nouMdp = new String(fPwd.getPassword()).trim();
            if (!nouMdp.isEmpty()) {
                if (nouMdp.length() < 4) {
                    JOptionPane.showMessageDialog(dlg,
                        "Le mot de passe doit avoir au moins 4 caracteres.",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                gestion.changerMotDePasse(u.getId(), nouMdp);
            }

            JOptionPane.showMessageDialog(dlg, "Compte mis a jour avec succes !");
            dlg.dispose();
            charger();
        });

        pied.add(ann);
        pied.add(sauv);

        dlg.add(hdr, BorderLayout.NORTH);
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(pied, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ── CREATION D'UN NOUVEAU COMPTE ──────────────────────────────────────────
    private void ouvrirNouveauCompte(String role) {
        String titreDialog = "ADMINISTRATEUR".equals(role) ? "Nouveau Administrateur" : "Nouvelle Secretaire";
        Color couleur = "ADMINISTRATEUR".equals(role) ? Couleurs.VIOLET : Couleurs.VERT_MED;
        Color couleurFonce = "ADMINISTRATEUR".equals(role) ? new Color(0x55, 0x2F, 0xA0) : Couleurs.VERT_FONCE;

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), titreDialog, true);
        dlg.setSize(560, 400);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel hdr = Composants.panneauDegrade(couleur, couleurFonce);
        hdr.setLayout(new BorderLayout());
        hdr.setBorder(new EmptyBorder(14, 18, 14, 18));
        JLabel t = new JLabel(titreDialog);
        t.setFont(Couleurs.SOUS_TITRE);
        t.setForeground(Color.WHITE);
        hdr.add(t);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 26, 14, 26));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(9, 7, 9, 7);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField fCin = c(), fNom = c(), fPrn = c(), fEml = c(), fTel = c();
        JPasswordField fPwd = new JPasswordField();
        fPwd.setFont(Couleurs.CORPS);
        fPwd.setPreferredSize(new Dimension(180, 40));
        fPwd.setBorder(BorderFactory.createCompoundBorder(
            new Composants.BordureArrondie(Couleurs.BORDURE, 1, 8),
            new EmptyBorder(8, 12, 8, 12)));

        g.gridy=0; g.gridx=0; g.weightx=0; form.add(Composants.etiquette("CIN *"), g);
        g.gridx=1; g.weightx=1; form.add(fCin, g);
        g.gridx=2; g.weightx=0; form.add(Composants.etiquette("Nom *"), g);
        g.gridx=3; g.weightx=1; form.add(fNom, g);

        g.gridy=1; g.gridx=0; g.weightx=0; form.add(Composants.etiquette("Prenom *"), g);
        g.gridx=1; g.weightx=1; form.add(fPrn, g);
        g.gridx=2; g.weightx=0; form.add(Composants.etiquette("Mot de passe *"), g);
        g.gridx=3; g.weightx=1; form.add(fPwd, g);

        g.gridy=2; g.gridx=0; g.weightx=0; form.add(Composants.etiquette("Email"), g);
        g.gridx=1; g.weightx=1; form.add(fEml, g);
        g.gridx=2; g.weightx=0; form.add(Composants.etiquette("Telephone"), g);
        g.gridx=3; g.weightx=1; form.add(fTel, g);

        JPanel pied = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pied.setBackground(Couleurs.FOND_PAGE);
        JButton ann  = Composants.boutonGris("Annuler");
        JButton sauv = Composants.boutonSucces("Creer le compte");
        ann.addActionListener(e -> dlg.dispose());
        sauv.addActionListener(e -> {
            String cin = fCin.getText().trim();
            String nom = fNom.getText().trim();
            String mdp = new String(fPwd.getPassword()).trim();
            if (cin.isEmpty() || nom.isEmpty() || mdp.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "CIN, Nom et Mot de passe sont obligatoires.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Utilisateur u = new Utilisateur(0, cin, nom, fPrn.getText().trim(), mdp, role,
                fEml.getText().trim(), fTel.getText().trim());

            int code = gestion.ajouterAvecCode(u);
            if (code == GestionUtilisateur.AJOUT_OK) {
                JOptionPane.showMessageDialog(dlg, "Compte cree avec succes !");
                dlg.dispose();
                charger();
            } else if (code == GestionUtilisateur.AJOUT_CIN_DUPLIQUE) {
                JOptionPane.showMessageDialog(dlg,
                    "Le CIN \"" + cin + "\" est deja utilise par un autre compte actif.",
                    "CIN deja utilise", JOptionPane.ERROR_MESSAGE);
            } else if (code == GestionUtilisateur.AJOUT_CIN_DESACTIVE) {
                int rep = JOptionPane.showConfirmDialog(dlg,
                    "Un ancien compte avec le CIN \"" + cin + "\" existe mais a ete desactive.\n" +
                    "Voulez-vous le reactiver avec ces nouvelles informations ?",
                    "Compte existant desactive", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (rep == JOptionPane.YES_OPTION) {
                    if (gestion.reactiverEtMettreAJour(u)) {
                        JOptionPane.showMessageDialog(dlg, "Compte reactive avec succes !");
                        dlg.dispose();
                        charger();
                    } else {
                        JOptionPane.showMessageDialog(dlg, "Erreur lors de la reactivation.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(dlg, "Erreur de base de donnees.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        pied.add(ann);
        pied.add(sauv);

        dlg.add(hdr, BorderLayout.NORTH);
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(pied, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void ouvrirNouveauMedecin() {
        DialogueMedecin dlg = new DialogueMedecin(
            (Frame) SwingUtilities.getWindowAncestor(this), null, new GestionMedecin());
        dlg.setVisible(true);
        if (dlg.estModifie()) charger();
    }

    private JTextField c() {
        JTextField tf = Composants.champTexte("");
        tf.setPreferredSize(new Dimension(180, 40));
        return tf;
    }

    private void msg(String m) { JOptionPane.showMessageDialog(this, m); }
}
