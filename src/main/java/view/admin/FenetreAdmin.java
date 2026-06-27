package view.admin;

import controller.*;
import model.*;
import view.*;
import view.medecin.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FenetreAdmin extends JFrame {

    private Utilisateur utilisateur;
    private JPanel zoneContenu;
    private JLabel lblHorloge;
    private view.Sidebar sidebarRef;

    public FenetreAdmin(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        setTitle("Administration - DoctoVite");
        setSize(1340, 820);
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

        Sidebar sidebar = new Sidebar(utilisateur, zoneContenu);
        sidebarRef = sidebar;
        Runnable rafraichir = sidebar::rafraichirTout;

        sidebar.ajouterPage("Tableau de bord",    new DashboardAdmin());
        sidebar.ajouterPage("Patients",           new PagePatients(utilisateur, false, rafraichir));
        sidebar.ajouterPage("Medecins",           new PageMedecins(false, rafraichir));
        sidebar.ajouterPage("Rendez-vous",        new PageRendezVous(-1, false, utilisateur, rafraichir));
        sidebar.ajouterPage("Ordonnances",        new PageDocuments(utilisateur, true));
        sidebar.ajouterPage("Factures",           new PageFactures(utilisateur, rafraichir));
        sidebar.ajouterPage("Comptes",            new PageComptes(utilisateur));
        sidebar.ajouterPage("Mon Profil",         new PageProfilSimple(utilisateur));

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

        JLabel lblNom = new JLabel("Administration - DoctoVite");
        lblNom.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNom.setForeground(Couleurs.TEXTE_NOIR);

        lblHorloge = new JLabel();
        lblHorloge.setFont(Couleurs.PETIT);
        lblHorloge.setForeground(Couleurs.TEXTE_GRIS);

        JPanel gauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        gauche.setOpaque(false);
        gauche.add(lblNom);
        gauche.add(lblHorloge);

        JLabel ul = new JLabel(utilisateur.getNomComplet() + " (Admin)");
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
        // Horloge : toutes les secondes
        new javax.swing.Timer(1000, e -> {
            lblHorloge.setText("  " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }).start();

        // Auto-refresh : rafraichit TOUTES les pages toutes les 3 secondes
        // pour que le tableau de bord et les tableaux soient toujours a jour
        new javax.swing.Timer(3000, e -> {
            if (sidebarRef != null) sidebarRef.rafraichirTout();
        }).start();
    }

    // ── CLASSE INTERNE : Tableau de bord administrateur ──────────────────────
    /**
     * Cette classe a une methode publique "rafraichir()" appelee par la Sidebar
     * a chaque clic sur "Tableau de bord" pour recharger les donnees.
     */
    public static class DashboardAdmin extends JPanel {

        private JPanel zoneCentre;

        public DashboardAdmin() {
            setLayout(new BorderLayout(0, 16));
            setBackground(Couleurs.FOND_PAGE);
            setBorder(new EmptyBorder(20, 20, 20, 20));
            construire();
        }

        private void construire() {
            JPanel titreRow = new JPanel(new BorderLayout());
            titreRow.setOpaque(false);
            titreRow.setBorder(new EmptyBorder(0, 0, 4, 0));
            titreRow.add(Composants.titre("Vue d'Ensemble - DoctoVite"), BorderLayout.WEST);

            zoneCentre = new JPanel(new BorderLayout(0, 14));
            zoneCentre.setOpaque(false);

            add(titreRow, BorderLayout.NORTH);
            add(zoneCentre, BorderLayout.CENTER);

            rafraichir();
        }

        // METHODE PUBLIQUE APPELEE PAR LA SIDEBAR
        public void rafraichir() {
            zoneCentre.removeAll();

            String dateAujourd = LocalDate.now().toString();
            GestionRendezVous gestionRdv = new GestionRendezVous();
            List<RendezVous> listeAuj = gestionRdv.listerActifsParDate(dateAujourd);

            int nbPatients = new GestionPatient().listerTous().size();
            int nbMedecins = new GestionMedecin().listerTous().size();
            int nbUrg = 0;
            for (RendezVous r : listeAuj) {
                if ("URGENCE".equals(r.getStatut())) nbUrg++;
            }

            JPanel stats = new JPanel(new GridLayout(1, 4, 12, 0));
            stats.setOpaque(false);
            stats.setPreferredSize(new Dimension(0, 88));
            stats.add(Composants.carteStatistique("" + nbPatients, "Patients enregistres", Couleurs.VERT_MED, Couleurs.VERT_FONCE));
            stats.add(Composants.carteStatistique("" + nbMedecins, "Medecins actifs", Couleurs.VIOLET, new Color(0x55, 0x2F, 0xA0)));
            stats.add(Composants.carteStatistique("" + listeAuj.size(), "RDV en attente", Couleurs.ORANGE, new Color(0xD4, 0x7D, 0x10)));
            stats.add(Composants.carteStatistique("" + nbUrg, "Urgences", Couleurs.ROUGE, Couleurs.ROUGE_FONCE));

            String[] cols = {"Heure", "Patient", "Medecin", "Specialite", "Motif", "Statut"};
            DefaultTableModel modele = new DefaultTableModel(cols, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
            for (RendezVous r : listeAuj) {
                modele.addRow(new Object[]{
                    r.getHeureRdv(), r.getNomPatient(), r.getNomMedecin(),
                    r.getSpecialiteMedecin(),
                    r.getMotif() != null ? r.getMotif() : "-",
                    r.getStatutAffichage()
                });
            }
            JTable tableau = new JTable(modele);
            Composants.styliserTableau(tableau);

            JPanel carte = new JPanel(new BorderLayout(0, 8));
            carte.setBackground(Color.WHITE);
            carte.setBorder(BorderFactory.createCompoundBorder(
                new Composants.BordureArrondie(Couleurs.BORDURE, 1, 12),
                new EmptyBorder(14, 14, 14, 14)
            ));
            JLabel ct = new JLabel("Rendez-vous du Jour");
            ct.setFont(Couleurs.SOUS_TITRE);
            ct.setForeground(Couleurs.TEXTE_NOIR);
            ct.setBorder(new EmptyBorder(0, 0, 6, 0));
            carte.add(ct, BorderLayout.NORTH);
            carte.add(Composants.enveloperTableau(tableau), BorderLayout.CENTER);

            zoneCentre.add(stats, BorderLayout.NORTH);
            zoneCentre.add(carte, BorderLayout.CENTER);

            zoneCentre.revalidate();
            zoneCentre.repaint();
        }
    }
}
