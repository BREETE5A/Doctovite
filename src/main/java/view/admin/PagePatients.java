package view.admin;

import controller.GestionPatient;
import model.Patient;
import model.Utilisateur;
import view.Composants;
import view.Couleurs;
import view.medecin.DialogueRendezVous;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class PagePatients extends JPanel {

    private Utilisateur utilisateur;
    private boolean lectureSeule;
    private Runnable onMaj;
    private GestionPatient gestion = new GestionPatient();
    private DefaultTableModel modele;
    private JTable tableau;
    private List<Patient> patients;
    private JTextField champRecherche;

    public PagePatients(Utilisateur utilisateur, boolean lectureSeule) {
        this(utilisateur, lectureSeule, null);
    }
    public PagePatients(Utilisateur utilisateur, boolean lectureSeule, Runnable onMaj) {
        this.onMaj = onMaj;
        this.utilisateur = utilisateur;
        this.lectureSeule = lectureSeule;
        setLayout(new BorderLayout());
        setBackground(Couleurs.FOND_PAGE);
        setBorder(new EmptyBorder(18, 18, 18, 18));
        construire();
        charger();
    }

    private void construire() {
        JPanel entete = new JPanel(new BorderLayout(10, 0));
        entete.setOpaque(false);
        entete.setBorder(new EmptyBorder(0, 0, 14, 0));
        entete.add(Composants.titre("Gestion des Patients"), BorderLayout.WEST);

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        droite.setOpaque(false);

        champRecherche = Composants.champTexte("Rechercher par CIN, nom, prenom, dossier...");
        champRecherche.setPreferredSize(new Dimension(280, 36));
        champRecherche.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { rechercher(); }
        });

        JButton btnRech = Composants.boutonPrincipal("Rechercher");
        btnRech.addActionListener(e -> rechercher());

        JButton btnActu = Composants.boutonGris("Actualiser");
        btnActu.addActionListener(e -> { champRecherche.setText(""); charger(); });

        droite.add(champRecherche);
        droite.add(btnRech);
        droite.add(btnActu);

        if (!lectureSeule) {
            JButton btnNouv = Composants.boutonSucces("+ Nouveau Patient");
            btnNouv.addActionListener(e -> ouvrirFormulaire(null));
            droite.add(btnNouv);
        }

        JButton btnPdf  = Composants.boutonPDF("PDF");
        JButton btnXlsx = Composants.boutonExcel("Excel");
        btnPdf.addActionListener(e -> controller.ServiceExport.exporterPDF(
            this, modele, "Patients", "patients"));
        btnXlsx.addActionListener(e -> controller.ServiceExport.exporterExcel(
            this, modele, "Patients", "patients"));
        droite.add(btnPdf);
        droite.add(btnXlsx);
        entete.add(droite, BorderLayout.EAST);

        String[] cols = {"ID", "Dossier", "CIN", "Nom", "Prenom", "Naissance", "Age", "Telephone", "Groupe"};
        modele = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableau = new JTable(modele);
        Composants.styliserTableau(tableau);
        tableau.getColumnModel().getColumn(0).setMaxWidth(50);
        tableau.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && !lectureSeule) {
                    int r = tableau.getSelectedRow();
                    if (r >= 0) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            ouvrirNouveauRdvPourPatient(patients.get(r));
                        } else {
                            ouvrirFormulaire(patients.get(r));
                        }
                    }
                }
            }
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        actions.setOpaque(false);
        if (!lectureSeule) {
            JButton btnMod = Composants.boutonPrincipal("Modifier");
            JButton btnRdv = Composants.boutonSucces("+ Nouveau RDV");
            JButton btnSup = Composants.boutonDanger("Supprimer");
            btnMod.addActionListener(e -> {
                int r = tableau.getSelectedRow();
                if (r < 0) { msg("Selectionnez un patient."); return; }
                ouvrirFormulaire(patients.get(r));
            });
            btnRdv.addActionListener(e -> {
                int r = tableau.getSelectedRow();
                if (r < 0) { msg("Selectionnez un patient."); return; }
                ouvrirNouveauRdvPourPatient(patients.get(r));
            });
            btnSup.addActionListener(e -> supprimer());
            actions.add(btnMod);
            actions.add(btnRdv);
            actions.add(btnSup);
            JLabel aide = new JLabel("  Double-clic gauche = Modifier  |  Double-clic droit = Nouveau RDV");
            aide.setFont(Couleurs.PETIT);
            aide.setForeground(Couleurs.TEXTE_PALE);
            actions.add(aide);
        }

        add(entete, BorderLayout.NORTH);
        add(Composants.enveloperTableau(tableau), BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
    }

    public void charger() {
        patients = gestion.listerTous();
        remplir();
    }

    // Ne recharge que si aucune recherche n'est en cours
    public void rafraichir() {
        if (champRecherche == null || champRecherche.getText().trim().isEmpty()) {
            charger();
        }
    }

    private void rechercher() {
        String t = champRecherche.getText().trim();
        patients = t.isEmpty() ? gestion.listerTous() : gestion.rechercher(t);
        remplir();
    }

    private void remplir() {
        modele.setRowCount(0);
        for (Patient p : patients) {
            modele.addRow(new Object[]{
                p.getId(),
                p.getNumeroDossier() != null ? p.getNumeroDossier() : "-",
                p.getCin(),
                p.getNom(),
                p.getPrenom(),
                p.getDateNaissance() != null ? p.getDateNaissance().toString() : "-",
                p.getAge() + " ans",
                p.getTelephone() != null ? p.getTelephone() : "-",
                p.getGroupeSanguin() != null ? p.getGroupeSanguin() : "-"
            });
        }
    }

    private void supprimer() {
        int r = tableau.getSelectedRow();
        if (r < 0) { msg("Selectionnez un patient."); return; }
        int rep = JOptionPane.showConfirmDialog(this,
            "Supprimer " + patients.get(r).getNomComplet() + " ?",
            "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (rep == JOptionPane.YES_OPTION) {
            gestion.supprimer(patients.get(r).getId());
            charger();
        }
    }

    private void ouvrirFormulaire(Patient p) {
        DialoguePatient dlg = new DialoguePatient((Frame) SwingUtilities.getWindowAncestor(this), p, gestion, utilisateur);
        dlg.setVisible(true);
        if (dlg.estModifie()) { charger(); if (onMaj != null) onMaj.run(); }
    }

    private void ouvrirNouveauRdvPourPatient(Patient p) {
        DialogueRendezVous dlg = new DialogueRendezVous(
            (Frame) SwingUtilities.getWindowAncestor(this), null, utilisateur
        );
        dlg.preSelectionnerPatient(p.getId());
        dlg.setVisible(true);
        if (dlg.estModifie()) { charger(); if (onMaj != null) onMaj.run(); }
    }

    private void msg(String m) { JOptionPane.showMessageDialog(this, m); }
}
