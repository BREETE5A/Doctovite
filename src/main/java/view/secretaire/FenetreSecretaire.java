package view.secretaire;

import controller.*;
import model.*;
import view.*;
import view.medecin.PageDocuments;
import view.medecin.PageRendezVous;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FenetreSecretaire extends JFrame {

    private Utilisateur utilisateur;
    private JPanel zoneContenu;
    private JLabel lblHorloge;
    private DashboardSecretaire dashboard;
    private view.Sidebar sidebarRef;

    public FenetreSecretaire(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        setTitle("Secretariat - " + utilisateur.getNomComplet() + " - DoctoVite");
        setSize(1300, 820);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        construire();
        demarrerHorloge();
    }

    private void construire() {
        JPanel racine = new JPanel(new BorderLayout());
        racine.setBackground(Couleurs.FOND_PAGE);

        zoneContenu = new JPanel(new BorderLayout());
        zoneContenu.setBackground(Couleurs.FOND_PAGE);

        dashboard = new DashboardSecretaire(utilisateur, this);

        Sidebar sidebar = new Sidebar(utilisateur, zoneContenu);
        sidebarRef = sidebar;
        Runnable rafraichir = sidebar::rafraichirTout;

        sidebar.ajouterPage("Tableau de bord", dashboard);
        sidebar.ajouterPage("Patients",        new view.secretaire.PagePatients(utilisateur, false, rafraichir));
        sidebar.ajouterPage("Rendez-vous",     new PageRendezVous(-1, false, utilisateur, rafraichir));
        sidebar.ajouterPage("Medecins",        new view.secretaire.PageMedecins(true, rafraichir));
        sidebar.ajouterPage("Ordonnances",     new PageDocuments(utilisateur, true));
        sidebar.ajouterPage("Factures",        new view.secretaire.PageFactures(utilisateur, rafraichir));
        sidebar.ajouterPage("Mon Profil",      new view.secretaire.PageProfilSimple(utilisateur));

        JPanel principal = new JPanel(new BorderLayout());
        principal.add(creerBarreHaute(), BorderLayout.NORTH);
        principal.add(zoneContenu, BorderLayout.CENTER);

        racine.add(sidebar, BorderLayout.WEST);
        racine.add(principal, BorderLayout.CENTER);
        setContentPane(racine);
    }

    private JPanel creerBarreHaute() {
        JPanel barre = new JPanel(new BorderLayout());
        barre.setBackground(Color.WHITE);
        barre.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Couleurs.BORDURE),
            new EmptyBorder(10, 22, 10, 22)
        ));

        JLabel lblNom = new JLabel("DoctoVite - Secretariat");
        lblNom.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNom.setForeground(Couleurs.TEXTE_NOIR);

        lblHorloge = new JLabel();
        lblHorloge.setFont(Couleurs.PETIT);
        lblHorloge.setForeground(Couleurs.TEXTE_GRIS);

        JPanel gauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        gauche.setOpaque(false);
        gauche.add(lblNom);
        gauche.add(lblHorloge);

        JLabel ul = new JLabel(utilisateur.getNomComplet());
        ul.setFont(Couleurs.GRAS);
        ul.setForeground(Couleurs.VERT_FONCE);

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        droite.setOpaque(false);
        droite.add(ul);

        barre.add(gauche, BorderLayout.WEST);
        barre.add(droite, BorderLayout.EAST);
        return barre;
    }

    private void demarrerHorloge() {
        new javax.swing.Timer(1000, e -> {
            lblHorloge.setText("  " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }).start();

        new javax.swing.Timer(3000, e -> {
            if (sidebarRef != null) sidebarRef.rafraichirTout();
        }).start();
    }

    // ── CLASSE INTERNE : Tableau de bord secretaire ──────────────────────────
    /**
     * Tableau de bord avec methode "rafraichir()" appelee par la Sidebar.
     */
    public static class DashboardSecretaire extends JPanel {

        private Utilisateur utilisateur;
        private JFrame parent;
        private JPanel zoneCentre;

        public DashboardSecretaire(Utilisateur utilisateur, JFrame parent) {
            this.utilisateur = utilisateur;
            this.parent = parent;
            setLayout(new BorderLayout(0, 14));
            setBackground(Couleurs.FOND_PAGE);
            setBorder(new EmptyBorder(20, 20, 20, 20));
            construire();
        }

        private void construire() {
            JPanel titreRow = new JPanel(new BorderLayout());
            titreRow.setOpaque(false);
            titreRow.setBorder(new EmptyBorder(0, 0, 4, 0));
            titreRow.add(Composants.titre("Tableau de Bord - Aujourd'hui"), BorderLayout.WEST);

            zoneCentre = new JPanel(new BorderLayout(0, 14));
            zoneCentre.setOpaque(false);

            // Boutons d'action rapide
            JPanel actionsRapides = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
            actionsRapides.setOpaque(false);
            JButton btnNouveauRdv     = Composants.boutonPrincipal("+ Nouveau RDV");
            JButton btnNouveauPatient = Composants.boutonGris("+ Nouveau Patient");
            JButton btnNouvelleFacture= Composants.boutonSucces("+ Nouvelle Facture");
            btnNouveauRdv.addActionListener(e -> {
                view.medecin.DialogueRendezVous dlg =
                    new view.medecin.DialogueRendezVous(parent, null, utilisateur);
                dlg.setVisible(true);
                if (dlg.estModifie()) rafraichir();
            });
            btnNouveauPatient.addActionListener(e -> {
                view.secretaire.DialoguePatient dlg = new view.secretaire.DialoguePatient(parent, null, new GestionPatient());
                dlg.setVisible(true);
            });
            btnNouvelleFacture.addActionListener(e -> ouvrirFormulaireFacture());
            actionsRapides.add(btnNouveauRdv);
            actionsRapides.add(btnNouveauPatient);
            actionsRapides.add(btnNouvelleFacture);

            add(titreRow, BorderLayout.NORTH);
            add(zoneCentre, BorderLayout.CENTER);
            add(actionsRapides, BorderLayout.SOUTH);

            rafraichir();
        }

        // METHODE PUBLIQUE APPELEE PAR LA SIDEBAR
        public void rafraichir() {
            zoneCentre.removeAll();

            String dateAujourd = LocalDate.now().toString();
            GestionRendezVous gestionRdv = new GestionRendezVous();

            // On affiche seulement les RDV actifs (pas TERMINE ni ANNULE)
            List<RendezVous> liste = gestionRdv.listerActifsParDate(dateAujourd);

            int nbPat = new GestionPatient().listerTous().size();
            int nConf = 0;
            int nUrg  = 0;
            for (RendezVous r : liste) {
                if ("CONFIRME".equals(r.getStatut())) nConf++;
                if ("URGENCE".equals(r.getStatut()))  nUrg++;
            }

            // Statistiques
            JPanel stats = new JPanel(new GridLayout(1, 4, 12, 0));
            stats.setOpaque(false);
            stats.setPreferredSize(new Dimension(0, 88));
            stats.add(Composants.carteStatistique("" + liste.size(), "RDV restants", Couleurs.VERT_MED, Couleurs.VERT_FONCE));
            stats.add(Composants.carteStatistique("" + nbPat, "Total patients", Couleurs.VIOLET, new Color(0x55, 0x2F, 0xA0)));
            stats.add(Composants.carteStatistique("" + nConf, "Confirmes", Couleurs.VERT_BTN, new Color(0x1E, 0x7E, 0x34)));
            stats.add(Composants.carteStatistique("" + nUrg, "Urgences", Couleurs.ROUGE, Couleurs.ROUGE_FONCE));

            // Tableau
            String[] cols = {"N°", "Heure", "Patient", "Medecin", "Specialite", "Motif", "Statut"};
            DefaultTableModel modele = new DefaultTableModel(cols, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };

            int pos = 1;
            for (RendezVous r : liste) {
                modele.addRow(new Object[]{
                    pos++, r.getHeureRdv(), r.getNomPatient(), r.getNomMedecin(),
                    r.getSpecialiteMedecin(),
                    r.getMotif() != null ? r.getMotif() : "-",
                    r.getStatutAffichage()
                });
            }

            JTable tableau = new JTable(modele) {
                public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int lig, int col) {
                    Component c = super.prepareRenderer(r, lig, col);
                    if (!isRowSelected(lig)) {
                        Object v = getValueAt(lig, 6);
                        if (v != null) {
                            String s = v.toString();
                            if ("Urgence".equals(s))  { c.setBackground(Couleurs.URGENCE_FOND); c.setForeground(Couleurs.URGENCE_TXT); }
                            else if ("Confirme".equals(s)) { c.setBackground(new Color(0xE8, 0xF8, 0xF5)); c.setForeground(Couleurs.VERT_FONCE); }
                            else { c.setBackground(Color.WHITE); c.setForeground(Color.BLACK); }
                        }
                    }
                    return c;
                }
            };
            Composants.styliserTableau(tableau);

            final List<RendezVous> listeFinale = liste;

            // Boutons d'action directement visibles
            JPanel actionsTableau = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            actionsTableau.setOpaque(false);

            JButton btnTerminer = Composants.boutonSucces("Terminer consultation");
            JButton btnUrgence  = Composants.boutonDanger("Urgence");

            btnTerminer.addActionListener(e -> {
                if (listeFinale.isEmpty()) {
                    JOptionPane.showMessageDialog(parent, "Aucun patient en attente.");
                    return;
                }
                // Toujours prendre le PREMIER de la liste (le prochain a consulter)
                RendezVous rdv = listeFinale.get(0);
                gestionRdv.changerStatut(rdv.getId(), "TERMINE");
                new controller.ServiceNotificationRdv().notifierModification(rdv, utilisateur);
                rafraichir(); // Il disparait immediatement
            });

            btnUrgence.addActionListener(e -> {
                int sel = tableau.getSelectedRow();
                if (sel < 0) { JOptionPane.showMessageDialog(parent, "Selectionnez un patient."); return; }
                RendezVous rdv = listeFinale.get(sel);
                gestionRdv.changerStatut(rdv.getId(), "URGENCE");
                new controller.ServiceNotificationRdv().notifierModification(rdv, utilisateur);
                rafraichir();
            });

            JLabel aide = new JLabel("  Selectionnez un patient puis cliquez sur l'action");
            aide.setFont(Couleurs.PETIT);
            aide.setForeground(Couleurs.TEXTE_PALE);

            actionsTableau.add(btnTerminer);
            actionsTableau.add(btnUrgence);
            actionsTableau.add(aide);

            // Carte principale
            JPanel carte = new JPanel(new BorderLayout(0, 8));
            carte.setBackground(Color.WHITE);
            carte.setBorder(BorderFactory.createCompoundBorder(
                new Composants.BordureArrondie(Couleurs.BORDURE, 1, 12),
                new EmptyBorder(14, 14, 14, 14)
            ));

            JLabel ctTitre = new JLabel("File d'Attente — Consultations du Jour");
            ctTitre.setFont(Couleurs.SOUS_TITRE);
            ctTitre.setForeground(Couleurs.TEXTE_NOIR);
            ctTitre.setBorder(new EmptyBorder(0, 0, 6, 0));

            carte.add(ctTitre, BorderLayout.NORTH);
            carte.add(Composants.enveloperTableau(tableau), BorderLayout.CENTER);
            carte.add(actionsTableau, BorderLayout.SOUTH);

            zoneCentre.add(stats, BorderLayout.NORTH);
            zoneCentre.add(carte, BorderLayout.CENTER);
            zoneCentre.revalidate();
            zoneCentre.repaint();
        }

        private void ouvrirFormulaireFacture() {
            controller.GestionFacture gestionFact = new controller.GestionFacture();

            JDialog dlg = new JDialog(parent, "Nouvelle Facture", true);
            dlg.setSize(520, 380);
            dlg.setLocationRelativeTo(parent);
            dlg.setLayout(new BorderLayout());

            JPanel hdr = Composants.panneauDegrade(Couleurs.VERT_FONCE, Couleurs.VERT_MED);
            hdr.setLayout(new BorderLayout());
            hdr.setBorder(new EmptyBorder(14, 18, 14, 18));
            JLabel t = new JLabel("Nouvelle Facture");
            t.setFont(Couleurs.SOUS_TITRE);
            t.setForeground(Color.WHITE);
            hdr.add(t);

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(Color.WHITE);
            form.setBorder(new EmptyBorder(20, 24, 14, 24));
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(8, 6, 8, 6);
            g.fill = GridBagConstraints.HORIZONTAL;

            // Liste des patients pour le combo
            java.util.List<model.Patient> patients = new GestionPatient().listerTous();
            JComboBox<model.Patient> cmbPat = new JComboBox<>(patients.toArray(new model.Patient[0]));
            cmbPat.setFont(Couleurs.CORPS);
            cmbPat.setPreferredSize(new Dimension(0, 40));

            JTextField txtMontant = Composants.champTexte("Ex: 350.00");
            txtMontant.setPreferredSize(new Dimension(0, 40));
            JTextField txtPaye    = Composants.champTexte("Montant paye (0 si non paye)");
            txtPaye.setPreferredSize(new Dimension(0, 40));
            JComboBox<String> cmbMode = new JComboBox<>(new String[]{"ESPECES", "CARTE", "VIREMENT", "ASSURANCE"});
            cmbMode.setFont(Couleurs.CORPS);
            cmbMode.setPreferredSize(new Dimension(0, 40));
            JTextField txtDesc = Composants.champTexte("Description (optionnel)");
            txtDesc.setPreferredSize(new Dimension(0, 40));

            g.gridy=0; g.gridx=0; g.weightx=0; form.add(Composants.etiquette("Patient *"), g);
            g.gridx=1; g.weightx=1; form.add(cmbPat, g);
            g.gridy=1; g.gridx=0; g.weightx=0; form.add(Composants.etiquette("Montant total (MAD) *"), g);
            g.gridx=1; g.weightx=1; form.add(txtMontant, g);
            g.gridy=2; g.gridx=0; g.weightx=0; form.add(Composants.etiquette("Montant paye (MAD)"), g);
            g.gridx=1; g.weightx=1; form.add(txtPaye, g);
            g.gridy=3; g.gridx=0; g.weightx=0; form.add(Composants.etiquette("Mode paiement"), g);
            g.gridx=1; g.weightx=1; form.add(cmbMode, g);
            g.gridy=4; g.gridx=0; g.weightx=0; form.add(Composants.etiquette("Description"), g);
            g.gridx=1; g.weightx=1; form.add(txtDesc, g);

            JPanel pied = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
            pied.setBackground(Couleurs.FOND_PAGE);
            JButton ann  = Composants.boutonGris("Annuler");
            JButton sauv = Composants.boutonSucces("Enregistrer");
            ann.addActionListener(e -> dlg.dispose());
            sauv.addActionListener(e -> {
                model.Patient pat = (model.Patient) cmbPat.getSelectedItem();
                String sMontant = txtMontant.getText().trim();
                if (pat == null || sMontant.isEmpty()) {
                    JOptionPane.showMessageDialog(dlg, "Patient et montant total sont obligatoires.", "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                try {
                    double total = Double.parseDouble(sMontant);
                    double paye  = txtPaye.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtPaye.getText().trim());
                    model.Facture f = new model.Facture();
                    f.setPatientId(pat.getId());
                    f.setNumeroFacture(gestionFact.genererNumero());
                    f.setMontantTotal(total);
                    f.setMontantPaye(paye);
                    f.setModePaiement((String) cmbMode.getSelectedItem());
                    // Calcul automatique du statut
                    if (paye >= total) {
                        f.setStatut("SOLDE");
                    } else if (paye > 0) {
                        f.setStatut("PARTIEL");
                    } else {
                        f.setStatut("EN_ATTENTE");
                    }
                    f.setDescription(txtDesc.getText().trim());
                    if (gestionFact.ajouter(f)) {
                        JOptionPane.showMessageDialog(dlg, "Facture creee avec succes !");
                        dlg.dispose();
                    } else {
                        JOptionPane.showMessageDialog(dlg, "Erreur lors de la creation.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dlg, "Montant invalide. Utilisez des chiffres (ex: 350.00)", "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            });
            pied.add(ann);
            pied.add(sauv);

            dlg.add(hdr, BorderLayout.NORTH);
            dlg.add(form, BorderLayout.CENTER);
            dlg.add(pied, BorderLayout.SOUTH);
            dlg.setVisible(true);
        }

        private void ouvrirDialogueAction(RendezVous rdv, int sel, DefaultTableModel modele) {
            GestionRendezVous gestion = new GestionRendezVous();

            JDialog dlg = new JDialog(parent, "Action - " + rdv.getNomPatient(), true);
            dlg.setSize(420, 260);
            dlg.setLocationRelativeTo(parent);
            dlg.setLayout(new BorderLayout());

            JPanel hdr = Composants.panneauDegrade(Couleurs.VERT_FONCE, Couleurs.VERT_MED);
            hdr.setLayout(new BorderLayout());
            hdr.setBorder(new EmptyBorder(14, 18, 14, 18));
            JLabel t = new JLabel("Patient : " + rdv.getNomPatient());
            t.setFont(Couleurs.SOUS_TITRE);
            t.setForeground(Color.WHITE);
            JLabel h = new JLabel(rdv.getHeureRdv() + " — " + rdv.getNomMedecin());
            h.setFont(Couleurs.PETIT);
            h.setForeground(new Color(255, 255, 255, 180));
            hdr.add(t, BorderLayout.CENTER);
            hdr.add(h, BorderLayout.SOUTH);

            JPanel corps = new JPanel(new GridLayout(2, 1, 0, 12));
            corps.setBackground(Color.WHITE);
            corps.setBorder(new EmptyBorder(22, 28, 22, 28));

            JButton btnTermine = Composants.boutonSucces("Marquer comme Termine");
            btnTermine.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnTermine.setPreferredSize(new Dimension(0, 50));
            btnTermine.addActionListener(e -> {
                gestion.changerStatut(rdv.getId(), "TERMINE");
                rdv.setStatut("TERMINE");
                // Notification automatique
                new ServiceNotificationRdv().notifierModification(rdv, utilisateur);
                if (sel < modele.getRowCount()) {
                    modele.setValueAt("Termine", sel, 6);
                    modele.setValueAt("Termine", sel, 7);
                }
                dlg.dispose();
            });

            JButton btnUrgence = Composants.boutonDanger("Declarer URGENCE");
            btnUrgence.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnUrgence.setPreferredSize(new Dimension(0, 50));
            btnUrgence.addActionListener(e -> {
                gestion.changerStatut(rdv.getId(), "URGENCE");
                rdv.setStatut("URGENCE");

                // Notification specifique urgence
                GestionNotification gestionNotif = new GestionNotification();
                Medecin med = new GestionMedecin().trouverParId(rdv.getMedecinId());
                if (med != null) {
                    Notification notif = new Notification(
                        med.getUtilisateurId(), utilisateur.getId(),
                        "URGENCE : " + rdv.getNomPatient() + " necessite une attention immediate !",
                        "URGENCE"
                    );
                    gestionNotif.envoyer(notif);
                }
                // Notification generale aussi (pour les admins)
                new ServiceNotificationRdv().notifierModification(rdv, utilisateur);

                if (sel < modele.getRowCount()) {
                    modele.setValueAt("Urgence", sel, 6);
                    modele.setValueAt("URGENT", sel, 7);
                }
                JOptionPane.showMessageDialog(parent,
                    "Urgence declaree et notif envoyee au medecin !",
                    "Urgence", JOptionPane.WARNING_MESSAGE);
                dlg.dispose();
            });

            corps.add(btnTermine);
            corps.add(btnUrgence);

            JPanel pied = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
            pied.setBackground(Couleurs.FOND_PAGE);
            JButton ann = Composants.boutonGris("Annuler");
            ann.addActionListener(e -> dlg.dispose());
            pied.add(ann);

            dlg.add(hdr, BorderLayout.NORTH);
            dlg.add(corps, BorderLayout.CENTER);
            dlg.add(pied, BorderLayout.SOUTH);
            dlg.setVisible(true);
        }
    }
}
