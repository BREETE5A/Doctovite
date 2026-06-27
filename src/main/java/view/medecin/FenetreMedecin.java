package view.medecin;

import controller.*;
import model.*;
import view.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class FenetreMedecin extends JFrame {

    private Utilisateur utilisateur;
    private Medecin medecin;
    private JPanel zoneContenu;
    private JLabel lblHorloge;
    private view.Sidebar sidebarRef;

    public FenetreMedecin(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        this.medecin = new GestionMedecin().trouverParUtilisateurId(utilisateur.getId());
        setTitle("Dr. " + utilisateur.getNomComplet() + " - DoctoVite");
        setSize(1240, 780);
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

        int mid = (medecin != null) ? medecin.getId() : -1;

        Sidebar sidebar = new Sidebar(utilisateur, zoneContenu);
        sidebarRef = sidebar;
        Runnable rafraichir = sidebar::rafraichirTout;

        sidebar.ajouterPage("Tableau de bord",   new DashboardMedecin(medecin));
        sidebar.ajouterPage("Mes Consultations", new PageRendezVous(mid, true, utilisateur, rafraichir));
        sidebar.ajouterPage("Ordonnances",       creerDocuments());
        sidebar.ajouterPage("Mon Profil",        creerProfil());

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

        String spec = (medecin != null) ? " - " + medecin.getSpecialite() : "";
        JLabel lblNom = new JLabel("Dr. " + utilisateur.getNomComplet() + spec);
        lblNom.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNom.setForeground(Couleurs.TEXTE_NOIR);

        lblHorloge = new JLabel();
        lblHorloge.setFont(Couleurs.PETIT);
        lblHorloge.setForeground(Couleurs.TEXTE_GRIS);

        JPanel gauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        gauche.setOpaque(false);
        gauche.add(lblNom);
        gauche.add(lblHorloge);

        String ds = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE dd MMM yyyy", new Locale("fr", "FR")));
        JLabel dateLbl = new JLabel(ds);
        dateLbl.setFont(Couleurs.PETIT);
        dateLbl.setForeground(Couleurs.TEXTE_GRIS);

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        droite.setOpaque(false);
        droite.add(dateLbl);

        barre.add(gauche, BorderLayout.WEST);
        barre.add(droite, BorderLayout.EAST);
        return barre;
    }

    private JPanel creerDocuments() {
        return new PageDocuments(utilisateur, false);
    }

    private JPanel creerProfil() {
        return new PageProfilMedecin(utilisateur, medecin);
    }

    private void demarrerHorloge() {
        new javax.swing.Timer(1000, e -> {
            lblHorloge.setText("  " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }).start();

        new javax.swing.Timer(3000, e -> {
            if (sidebarRef != null) sidebarRef.rafraichirTout();
        }).start();
    }

    // ── CLASSE INTERNE : Tableau de bord du medecin ───────────────────────────
    /**
     * Cette classe a une methode publique "rafraichir()" qui est appelee
     * automatiquement par la Sidebar a chaque fois que l'utilisateur clique
     * sur "Tableau de bord". Cela garantit que les donnees sont a jour.
     */
    public static class DashboardMedecin extends JPanel {

        private Medecin medecin;
        private JPanel zoneCentre;

        public DashboardMedecin(Medecin medecin) {
            this.medecin = medecin;
            setLayout(new BorderLayout(0, 16));
            setBackground(Couleurs.FOND_PAGE);
            setBorder(new EmptyBorder(20, 20, 20, 20));
            construire();
        }

        private void construire() {
            String ds = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", new Locale("fr", "FR")));
            JPanel titreRow = new JPanel(new BorderLayout());
            titreRow.setOpaque(false);
            titreRow.setBorder(new EmptyBorder(0, 0, 4, 0));
            titreRow.add(Composants.titre("Consultations d'Aujourd'hui"), BorderLayout.WEST);
            titreRow.add(new JLabel(ds) {{ setFont(Couleurs.PETIT); setForeground(Couleurs.TEXTE_GRIS); }}, BorderLayout.EAST);

            zoneCentre = new JPanel(new BorderLayout(0, 14));
            zoneCentre.setOpaque(false);

            add(titreRow, BorderLayout.NORTH);
            add(zoneCentre, BorderLayout.CENTER);

            // Premier remplissage
            rafraichir();
        }

        // METHODE PUBLIQUE APPELEE PAR LA SIDEBAR POUR RECHARGER LES DONNEES
        public void rafraichir() {
            zoneCentre.removeAll();

            if (medecin == null) {
                JLabel msg = new JLabel("<html><center>Profil medecin non configure.<br>Contactez l'administrateur.</center></html>", SwingConstants.CENTER);
                msg.setFont(Couleurs.CORPS);
                msg.setForeground(Couleurs.TEXTE_GRIS);
                zoneCentre.add(msg, BorderLayout.CENTER);
                zoneCentre.revalidate();
                zoneCentre.repaint();
                return;
            }

            String dateAujourd = LocalDate.now().toString();
            GestionRendezVous gestionRdv = new GestionRendezVous();

            // Seulement les RDV actifs (pas TERMINE ni ANNULE)
            List<RendezVous> liste = gestionRdv.listerActifsParMedecinEtDate(medecin.getId(), dateAujourd);

            int nConf = 0;
            int nAtt  = 0;
            int nUrg  = 0;
            for (RendezVous r : liste) {
                if ("CONFIRME".equals(r.getStatut())) nConf++;
                else if ("PLANIFIE".equals(r.getStatut())) nAtt++;
                else if ("URGENCE".equals(r.getStatut())) nUrg++;
            }

            // Statistiques
            JPanel stats = new JPanel(new GridLayout(1, 4, 12, 0));
            stats.setOpaque(false);
            stats.setPreferredSize(new Dimension(0, 88));
            stats.add(Composants.carteStatistique("" + liste.size(), "Restants aujourd'hui", Couleurs.VERT_MED, Couleurs.VERT_FONCE));
            stats.add(Composants.carteStatistique("" + nConf, "Confirmes", Couleurs.VERT_BTN, new Color(0x1E, 0x7E, 0x34)));
            stats.add(Composants.carteStatistique("" + nAtt,  "En attente", Couleurs.ORANGE, new Color(0xD4, 0x7D, 0x10)));
            stats.add(Composants.carteStatistique("" + nUrg,  "Urgences", Couleurs.ROUGE, Couleurs.ROUGE_FONCE));

            // Tableau
            String[] colonnes = {"Heure", "Patient", "CIN", "Age", "Motif", "Statut"};
            DefaultTableModel modele = new DefaultTableModel(colonnes, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
            for (RendezVous r : liste) {
                modele.addRow(new Object[]{
                    r.getHeureRdv(), r.getNomPatient(), r.getCinPatient(),
                    r.getAgePatient() + " ans",
                    r.getMotif() != null ? r.getMotif() : "-",
                    r.getStatutAffichage()
                });
            }
            JTable tableau = new JTable(modele) {
                public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int lig, int col) {
                    Component c = super.prepareRenderer(r, lig, col);
                    if (!isRowSelected(lig)) {
                        Object v = getValueAt(lig, 5);
                        if (v != null && "Urgence".equals(v.toString())) {
                            c.setBackground(Couleurs.URGENCE_FOND);
                            c.setForeground(Couleurs.URGENCE_TXT);
                        }
                    }
                    return c;
                }
            };
            Composants.styliserTableau(tableau);

            final List<RendezVous> listeFinale = liste;

            // Boutons d'action directement visibles sous le tableau
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            actions.setOpaque(false);

            JButton btnTerminer = Composants.boutonSucces("Consultation terminee");
            JButton btnUrgence  = Composants.boutonDanger("Urgence");

            btnTerminer.addActionListener(e -> {
                if (listeFinale.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Aucun patient en attente.");
                    return;
                }
                // Toujours prendre le PREMIER de la liste (le prochain a consulter)
                RendezVous rdv = listeFinale.get(0);
                gestionRdv.changerStatut(rdv.getId(), "TERMINE");
                rafraichir(); // Il disparait immediatement
            });

            btnUrgence.addActionListener(e -> {
                int sel = tableau.getSelectedRow();
                if (sel < 0) { JOptionPane.showMessageDialog(null, "Selectionnez un patient."); return; }
                RendezVous rdv = listeFinale.get(sel);
                gestionRdv.changerStatut(rdv.getId(), "URGENCE");
                rafraichir();
            });

            JLabel aide = new JLabel("  Selectionnez un patient puis cliquez sur l'action");
            aide.setFont(Couleurs.PETIT);
            aide.setForeground(Couleurs.TEXTE_PALE);

            actions.add(btnTerminer);
            actions.add(btnUrgence);
            actions.add(aide);

            // Carte
            JPanel carte = new JPanel(new BorderLayout(0, 8));
            carte.setBackground(Color.WHITE);
            carte.setBorder(BorderFactory.createCompoundBorder(
                new Composants.BordureArrondie(Couleurs.BORDURE, 1, 12),
                new EmptyBorder(14, 14, 14, 14)
            ));
            JLabel ctTitre = new JLabel("Patients du Jour — File d'attente");
            ctTitre.setFont(Couleurs.SOUS_TITRE);
            ctTitre.setForeground(Couleurs.TEXTE_NOIR);
            ctTitre.setBorder(new EmptyBorder(0, 0, 6, 0));
            carte.add(ctTitre, BorderLayout.NORTH);
            carte.add(Composants.enveloperTableau(tableau), BorderLayout.CENTER);
            carte.add(actions, BorderLayout.SOUTH);

            zoneCentre.add(stats, BorderLayout.NORTH);
            zoneCentre.add(carte, BorderLayout.CENTER);
            zoneCentre.revalidate();
            zoneCentre.repaint();
        }
    }
}
